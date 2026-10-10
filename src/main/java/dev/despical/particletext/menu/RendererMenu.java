/*
 * Particle Text - Persistent particle text for Minecraft.
 * Copyright (C) 2026  Berke Akçen
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package dev.despical.particletext.menu;

import dev.despical.inventoryframework.Gui;
import dev.despical.inventoryframework.GuiItem;
import dev.despical.inventoryframework.pane.PaginatedPane;
import dev.despical.inventoryframework.pane.StaticPane;
import dev.despical.particletext.ParticleTextPlugin;
import dev.despical.particletext.config.PluginSettings;
import dev.despical.particletext.message.MessageService;
import dev.despical.particletext.message.Var;
import dev.despical.particletext.model.RendererData;
import dev.despical.particletext.model.RendererLocation;
import dev.despical.particletext.render.RendererService;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Locale;
import java.util.logging.Level;

/**
 * Builds a paginated inventory for renderer navigation and visibility.
 * <p>
 * Materials, title, sounds, names, and lore come from the published configuration. Inventory Framework
 * panes keep entries inside a decorative border and provide page navigation.
 * <p>
 * Click handlers recheck edit or teleport permissions and refresh the renderer record before using its
 * location. Reopen the inventory after a presentation reload.
 *
 * @author Despical
 * <p>
 * Created at 10.10.2026
 */
public final class RendererMenu {

    private final ParticleTextPlugin plugin;
    private final RendererService rendererService;
    private final MessageService messages;

    public RendererMenu(ParticleTextPlugin plugin) {
        this.plugin = plugin;
        this.rendererService = plugin.getRendererService();
        this.messages = plugin.getMessages();
    }

    public void open(Player player) {
        open(player, true);
    }

    private void open(Player player, boolean playOpenSound) {
        PluginSettings.MenuSettings settings = plugin.getSettingsManager().current().menu();
        Gui gui = new Gui(plugin, settings.rows(), messages.parse(player, settings.title()));

        gui.setOnGlobalClick(event -> event.setCancelled(true));
        gui.setOnDrag(event -> event.setCancelled(true));

        StaticPane decoration = new StaticPane(0, 0, 9, settings.rows());

        for (int row = 0; row < settings.rows(); row++) {
            for (int column = 0; column < 9; column++) {
                if (row == 0 || row == settings.rows() - 1 || column == 0 || column == 8) {
                    decoration.addItem(decorationItem(player, settings), column, row);
                }
            }
        }

        gui.addPane(decoration);

        PaginatedPane pages = new PaginatedPane(1, 1, 7, settings.rows() - 2);
        List<GuiItem> items = rendererService.all().stream()
            .map(data -> rendererItem(player, data, settings))
            .toList();

        if (items.isEmpty()) {
            StaticPane empty = new StaticPane(1, 1, 7, settings.rows() - 2);
            empty.addItem(emptyItem(player, settings), 3, (settings.rows() - 3) / 2);
            gui.addPane(empty);
        } else {
            pages.populateWithGuiItems(items);
            pages.setPage(0);
            gui.addPane(pages);
        }

        StaticPane navigation = new StaticPane(0, settings.rows() - 1, 9, 1);

        if (pages.getPages() > 1) {
            navigation.addItem(navigationItem(player, settings.previousPageMaterial(), "menu.previous-page", () -> {
                if (pages.getPage() > 0) {
                    pages.setPage(pages.getPage() - 1);
                    gui.update();
                }
            }, settings.pageChangeSound()), 0, 0);
            navigation.addItem(navigationItem(player, settings.nextPageMaterial(), "menu.next-page", () -> {
                if (pages.getPage() + 1 < pages.getPages()) {
                    pages.setPage(pages.getPage() + 1);
                    gui.update();
                }
            }, settings.pageChangeSound()), 8, 0);
        }

        gui.addPane(navigation);
        gui.show(player);

        if (playOpenSound) {
            playSound(player, settings.openSound());
        }
    }

    private GuiItem rendererItem(Player viewer, RendererData data, PluginSettings.MenuSettings settings) {
        ItemStack item = new ItemStack(data.enabled() ? settings.enabledMaterial() : settings.disabledMaterial());
        ItemMeta meta = item.getItemMeta();
        Var[] variables = variables(data);

        meta.displayName(messages.parse(viewer, messages.raw("menu.renderer-name"), variables));
        meta.lore(messages.parseList(viewer, "menu.renderer-lore", variables));
        item.setItemMeta(meta);

        return GuiItem.of(item, event -> {
            Player player = (Player) event.getWhoClicked();

            if (!event.isLeftClick() && !event.isRightClick()) {
                return;
            }

            if (event.isRightClick()) {
                if (!player.hasPermission("particletext.command.edit")) {
                    messages.send(player, "no-permission");
                    return;
                }

                try {
                    var updated = rendererService.toggleEnabled(data.id());

                    if (updated.isEmpty()) {
                        messages.send(player, "renderer-not-found", Var.of("%id%", data.id()));
                        return;
                    }

                    messages.send(player, updated.get().enabled() ? "renderer-enabled" : "renderer-disabled",
                        Var.of("%id%", updated.get().id()));
                    playSound(player, updated.get().enabled() ? settings.enabledSound() : settings.disabledSound());
                    plugin.getServer().getScheduler().runTask(plugin, () -> open(player, false));
                } catch (RuntimeException error) {
                    plugin.getLogger().log(Level.SEVERE, "Could not toggle renderer " + data.id(), error);
                    messages.send(player, "operation-failed");
                }

                return;
            }

            if (!player.hasPermission("particletext.command.teleport")) {
                messages.send(player, "no-permission");
                return;
            }

            var current = rendererService.find(data.id());

            if (current.isEmpty()) {
                messages.send(player, "renderer-not-found", Var.of("%id%", data.id()));
                return;
            }

            Location location = current.get().location().toBukkitLocation();

            if (location == null) {
                messages.send(player, "world-unavailable", Var.of("%world%", current.get().location().world()));
                playSound(player, settings.disabledSound());
                return;
            }

            if (RendererLocation.from(player.getLocation()).equals(current.get().location())) {
                messages.send(player, "renderer-location-unchanged", Var.of("%id%", data.id()));
                return;
            }

            player.closeInventory();

            if (!player.teleport(location)) {
                messages.send(player, "teleport-failed", Var.of("%id%", data.id()));
                return;
            }

            playSound(player, settings.teleportSound());

            messages.send(player, "renderer-teleported", Var.of("%id%", data.id()));
        });
    }

    private GuiItem navigationItem(Player player, org.bukkit.Material material, String messagePath, Runnable action,
                                   Sound sound) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(messages.parse(player, messages.raw(messagePath)));
        meta.lore(messages.parseList(player, messagePath + "-lore"));
        item.setItemMeta(meta);

        return GuiItem.of(item, event -> {
            playSound(player, sound);
            action.run();
        });
    }

    private GuiItem emptyItem(Player player, PluginSettings.MenuSettings settings) {
        ItemStack item = new ItemStack(settings.emptyMaterial());
        ItemMeta meta = item.getItemMeta();

        meta.displayName(messages.parse(player, messages.raw("menu.empty-name")));
        meta.lore(messages.parseList(player, "menu.empty-lore"));
        item.setItemMeta(meta);

        return GuiItem.of(item, event -> playSound(player, settings.disabledSound()));
    }

    private GuiItem decorationItem(Player player, PluginSettings.MenuSettings settings) {
        ItemStack item = new ItemStack(settings.decorationMaterial());
        ItemMeta meta = item.getItemMeta();

        meta.displayName(messages.parse(player, messages.raw("menu.decoration-name")));
        item.setItemMeta(meta);

        return GuiItem.of(item);
    }

    private void playSound(Player player, Sound sound) {
        var settings = plugin.getSettingsManager().current().menu();
        player.playSound(player.getLocation(), sound, settings.soundVolume(), settings.soundPitch());
    }

    private Var[] variables(RendererData data) {
        return new Var[] {
            Var.of("%id%", data.id()), Var.of("%text%", data.text()),
            Var.of("%particle%", data.particle().name()),
            Var.of("%scale%", String.format(Locale.US, "%.2f", data.scale())),
            Var.of("%inverted%", data.inverted()),
            Var.of("%font%", data.font().name()), Var.of("%font_style%", data.font().style().name()),
            Var.of("%font_size%", data.font().size()),
            Var.of("%status%", messages.parse(messages.raw(data.enabled()
                ? "menu.status-enabled" : "menu.status-disabled")))
        };
    }
}

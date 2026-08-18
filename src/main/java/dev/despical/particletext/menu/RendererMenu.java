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
import dev.despical.particletext.render.RendererService;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Locale;

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
            }, settings.pageChangeSound(), 0.8f), 0, 0);
            navigation.addItem(navigationItem(player, settings.nextPageMaterial(), "menu.next-page", () -> {
                if (pages.getPage() + 1 < pages.getPages()) {
                    pages.setPage(pages.getPage() + 1);
                    gui.update();
                }
            }, settings.pageChangeSound(), 1.5f), 8, 0);
        }

        gui.addPane(navigation);
        gui.show(player);

        if (playOpenSound) {
            playSound(player, settings.openSound(), 1.5f);
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

            if (event.isRightClick()) {
                rendererService.toggleEnabled(data.id()).ifPresent(updated -> {
                    messages.send(player, updated.enabled() ? "renderer-enabled" : "renderer-disabled",
                        Var.of("%id%", updated.id()));
                    playSound(player, updated.enabled() ? settings.enabledSound() : settings.disabledSound(),
                        updated.enabled() ? 1.6f : 0.7f);
                    plugin.getServer().getScheduler().runTask(plugin, () -> open(player, false));
                });
                return;
            }

            Location location = data.location().toBukkitLocation();

            if (location == null) {
                messages.send(player, "world-unavailable", Var.of("%world%", data.location().world()));
                playSound(player, settings.disabledSound(), 0.7f);
                return;
            }

            player.closeInventory();
            player.teleport(location);
            playSound(player, settings.teleportSound(), 1f);

            messages.send(player, "renderer-teleported", Var.of("%id%", data.id()));
        });
    }

    private GuiItem navigationItem(Player player, org.bukkit.Material material, String messagePath, Runnable action,
                                   Sound sound, float pitch) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(messages.parse(player, messages.raw(messagePath)));
        meta.lore(messages.parseList(player, messagePath + "-lore"));
        item.setItemMeta(meta);

        return GuiItem.of(item, event -> {
            playSound(player, sound, pitch);
            action.run();
        });
    }

    private GuiItem emptyItem(Player player, PluginSettings.MenuSettings settings) {
        ItemStack item = new ItemStack(settings.emptyMaterial());
        ItemMeta meta = item.getItemMeta();

        meta.displayName(messages.parse(player, messages.raw("menu.empty-name")));
        meta.lore(messages.parseList(player, "menu.empty-lore"));
        item.setItemMeta(meta);

        return GuiItem.of(item, event -> playSound(player, settings.disabledSound(), 0.7f));
    }

    private GuiItem decorationItem(Player player, PluginSettings.MenuSettings settings) {
        ItemStack item = new ItemStack(settings.decorationMaterial());
        ItemMeta meta = item.getItemMeta();

        meta.displayName(messages.parse(player, messages.raw("menu.decoration-name")));
        item.setItemMeta(meta);

        return GuiItem.of(item);
    }

    private void playSound(Player player, Sound sound, float pitch) {
        player.playSound(player.getLocation(), sound, 1f, pitch);
    }

    private Var[] variables(RendererData data) {
        return List.of(
            Var.of("%id%", data.id()),
            Var.of("%text%", data.text()),
            Var.of("%particle%", data.particle().name()),
            Var.of("%scale%", String.format(Locale.US, "%.2f", data.scale())),
            Var.of("%inverted%", data.inverted()),
            Var.of("%font%", data.font().name()),
            Var.of("%font_size%", data.font().size()),
            Var.of("%status%", messages.raw(data.enabled() ? "menu.status-enabled" : "menu.status-disabled")))
        .toArray(Var[]::new);
    }
}

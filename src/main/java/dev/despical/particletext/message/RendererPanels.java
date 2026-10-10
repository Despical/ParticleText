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

package dev.despical.particletext.message;

import dev.despical.particletext.ParticleTextPlugin;
import dev.despical.particletext.command.CommandDefinition;
import dev.despical.particletext.model.RendererData;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Locale;

/**
 * Builds shared chat layouts for help, renderer lists, and details.
 * <p>
 * All headings, rows, hover descriptions, button labels, and navigation templates
 * come from messages.yml. Pagination uses the configured row limit and shows
 * only commands available to the requesting sender.
 * <p>
 * Player actions carry Adventure click and hover events with their own permission
 * checks. Console recipients receive the same readable cards without player-only
 * management buttons.
 *
 * @author Despical
 * <p>
 * Created at 10.10.2026
 */
public final class RendererPanels {

    private final ParticleTextPlugin plugin;

    public RendererPanels(ParticleTextPlugin plugin) {
        this.plugin = plugin;
    }

    public void help(CommandSender sender, int page) {
        var commands = Arrays.stream(CommandDefinition.values())
            .filter(command -> sender.hasPermission(command.permission())).toList();
        int size = plugin.getSettingsManager().current().chatPageSize();
        int pages = Math.max(1, (commands.size() + size - 1) / size);

        if (!checkPage(sender, page, pages)) {
            return;
        }

        sender.sendMessage(Component.empty());
        send(sender, "chat.help-title");

        for (var command : commands.subList((page - 1) * size, Math.min(page * size, commands.size()))) {
            String usage = plugin.getMessages().raw("usage." + command.action()).replace("%label%", "pt");
            String description = plugin.getMessages().raw("description." + command.action());
            Component link = format(sender, "chat.command", Var.of("%command%", usage));

            if (sender instanceof Player) {
                link = link.clickEvent(ClickEvent.suggestCommand("/pt " + command.action() + " "))
                    .hoverEvent(HoverEvent.showText(format(sender, "chat.command-hover",
                        Var.of("%command%", usage), Var.of("%description%", description))));
            }

            send(sender, "chat.help-row", Var.of("%command%", link), Var.of("%description%", description));
        }

        send(sender, "chat.help-footer");
        navigation(sender, "help", page, pages);
        sender.sendMessage(Component.empty());
    }

    public void list(CommandSender sender, int page) {
        var records = plugin.getRendererService().all();
        int size = plugin.getSettingsManager().current().chatPageSize();
        int pages = Math.max(1, (records.size() + size - 1) / size);

        if (!checkPage(sender, page, pages)) {
            return;
        }

        sender.sendMessage(Component.empty());
        send(sender, "chat.list-title", Var.of("%count%", records.size()));

        if (records.isEmpty()) {
            send(sender, "renderer-list-empty", Var.of("%label%", "pt"));
        }

        for (var data : records.subList((page - 1) * size, Math.min(page * size, records.size()))) {
            Component name = format(sender, "chat.name", Var.of("%id%", data.id()));

            if (sender instanceof Player && sender.hasPermission("particletext.command.info")) {
                name = name.clickEvent(ClickEvent.runCommand("/pt info " + data.id()))
                    .hoverEvent(HoverEvent.showText(format(sender, "chat.renderer-hover", variables(data))));
            }

            send(sender, "chat.list-row", Var.of("%name%", name), Var.of("%status%", status(sender, data)));
        }

        send(sender, "chat.list-footer");
        navigation(sender, "list", page, pages);
        sender.sendMessage(Component.empty());
    }

    public void info(CommandSender sender, String id) {
        var record = plugin.getRendererService().find(id);

        if (record.isEmpty()) {
            send(sender, "renderer-not-found", Var.of("%id%", id));
            return;
        }

        var data = record.get();
        sender.sendMessage(Component.empty());
        send(sender, "chat.info-title", Var.of("%id%", id));
        plugin.getMessages().sendList(sender, "chat.info", variables(data));

        Component actions = Component.empty();

        if (sender instanceof Player) {
            actions = actions.append(button(sender, "teleport", "particletext.command.teleport",
                "/pt teleport " + id, false));
            actions = actions.append(button(sender, "edit", "particletext.command.edit", "/pt text " + id + " ", true));
            actions = actions.append(button(sender, data.enabled() ? "disable" : "enable", "particletext.command.edit",
                "/pt enabled " + id + " " + !data.enabled(), false));
            actions = actions.append(button(sender, "delete", "particletext.command.delete", "/pt delete " + id, true));
        }

        send(sender, "chat.actions", Var.of("%actions%", actions));
        sender.sendMessage(Component.empty());
    }

    private Component button(CommandSender sender, String name, String permission, String command, boolean suggest) {
        if (!sender.hasPermission(permission)) {
            return Component.empty();
        }

        return format(sender, "chat.button-" + name).clickEvent(suggest ? ClickEvent.suggestCommand(command)
            : ClickEvent.runCommand(command)).hoverEvent(HoverEvent.showText(format(sender, "chat.hover-" + name)))
            .append(Component.space());
    }

    private void navigation(CommandSender sender, String action, int page, int pages) {
        Component previous = page > 1 ? format(sender, "chat.previous")
            .clickEvent(ClickEvent.runCommand("/pt " + action + " " + (page - 1))) : Component.empty();
        Component next = page < pages ? format(sender, "chat.next")
            .clickEvent(ClickEvent.runCommand("/pt " + action + " " + (page + 1))) : Component.empty();

        send(sender, "chat.navigation", Var.of("%previous%", previous), Var.of("%next%", next),
            Var.of("%page%", page), Var.of("%pages%", pages));
    }

    private boolean checkPage(CommandSender sender, int page, int pages) {
        if (page < 1 || page > pages) {
            send(sender, "invalid-page", Var.of("%pages%", pages));
            return false;
        }

        return true;
    }

    private Component status(CommandSender sender, RendererData data) {
        return format(sender, data.enabled() ? "menu.status-enabled" : "menu.status-disabled");
    }

    private Var[] variables(RendererData data) {
        var location = data.location();

        return new Var[] {
            Var.of("%id%", data.id()), Var.of("%text%", data.text()),
            Var.of("%world%", location.world()), Var.of("%x%", number(location.x())),
            Var.of("%y%", number(location.y())), Var.of("%z%", number(location.z())),
            Var.of("%scale%", number(data.scale())), Var.of("%particle%", data.particle().name()),
            Var.of("%font%", data.font().name()), Var.of("%font_style%", data.font().style().name()),
            Var.of("%font_size%", data.font().size()), Var.of("%inverted%", data.inverted()),
            Var.of("%points%", plugin.getRendererService().pointCount(data.id())),
            Var.of("%rotation%", number(data.rotation().x()) + ", " + number(data.rotation().y()) + ", "
                + number(data.rotation().z())),
            Var.of("%status%", plugin.getMessages().parse(plugin.getMessages().raw(data.enabled()
                ? "menu.status-enabled" : "menu.status-disabled")))
        };
    }

    private String number(double value) {
        return String.format(Locale.US, "%.2f", value);
    }

    private Component format(CommandSender sender, String key, Var... variables) {
        return plugin.getMessages().parse(sender instanceof Player player ? player : null,
            plugin.getMessages().raw(key), variables);
    }

    private void send(CommandSender sender, String key, Var... variables) {
        plugin.getMessages().send(sender, key, variables);
    }
}

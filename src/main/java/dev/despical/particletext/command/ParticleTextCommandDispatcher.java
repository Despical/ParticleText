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

package dev.despical.particletext.command;

import dev.despical.particletext.ParticleTextPlugin;
import dev.despical.particletext.menu.RendererMenu;
import dev.despical.particletext.message.Var;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Locale;
import java.util.logging.Level;

/**
 * Validates command invocations and dispatches renderer or plugin operations.
 * <p>
 * Aikar owns annotated command routing. Shared checks retain configurable usage, player-only,
 * and error responses, including invocations reaching the default handler.
 *
 * @author Despical
 * <p>
 * Created at 10.10.2026
 */
final class ParticleTextCommandDispatcher {

    private final ParticleTextPlugin plugin;
    private final RendererCommands renderers;

    ParticleTextCommandDispatcher(ParticleTextPlugin plugin) {
        this.plugin = plugin;

        this.renderers = new RendererCommands(plugin);
    }

    void execute(CommandSender sender, String[] input) {
        if (input.length == 0) {
            execute(sender, CommandDefinition.VERSION, input);
            return;
        }

        var definition = CommandDefinition.find(input[0]);
        if (definition.isEmpty()) {
            send(sender, "unrecognized-arguments", Var.of("%label%", "pt"));
            return;
        }

        execute(sender, definition.get(), Arrays.copyOfRange(input, 1, input.length));
    }

    void execute(CommandSender sender, CommandDefinition command, String[] args) {
        if (!sender.hasPermission(command.permission())) {
            send(sender, "no-permission");
            return;
        }

        if (command.playerOnly() && !(sender instanceof Player)) {
            send(sender, "player-only");
            return;
        }

        if (args.length < command.min() || args.length > command.max()) {
            send(sender, "correct-usage", Var.of("%usage%",
                plugin.getMessages().raw("usage." + command.action()).replace("%label%", "pt")));
            return;
        }

        try {
            switch (command) {
                case CREATE -> renderers.create(sender, args);
                case DELETE -> renderers.delete(sender, args);
                case TPHERE -> renderers.teleportHere(sender, args);
                case MOVE -> renderers.move(sender, args);
                case TEXT -> renderers.text(sender, args);
                case SETSIZE -> renderers.setSize(sender, args);
                case FONT -> renderers.font(sender, args);
                case PARTICLE -> renderers.particle(sender, args);
                case ENABLED -> renderers.enabled(sender, args);
                case INVERTED -> renderers.inverted(sender, args);
                case ROTATE -> renderers.rotate(sender, args);
                case RELOAD -> reload(sender);
                case LIST -> plugin.getPanels().list(sender, page(args));
                case MENU -> new RendererMenu(plugin).open((Player) sender);
                case TELEPORT -> renderers.teleport(sender, args);
                case HELP -> plugin.getPanels().help(sender, page(args));
                case VERSION -> version(sender);
                case INFO -> plugin.getPanels().info(sender, args[0].toLowerCase(Locale.ROOT));
                case FONTS -> fonts(sender);
            }
        } catch (NumberFormatException _) {
            int index = switch (command) {
                case SETSIZE -> 1;
                case FONT -> 3;
                case MOVE, ROTATE -> 2;
                default -> 0;
            };

            send(sender, "invalid-number", Var.of("%value%", args[index]));
        } catch (Exception error) {
            send(sender, "operation-failed", Var.of("%id%", args.length == 0 ? "" : args[0]));

            plugin.getLogger().log(Level.WARNING, "Command failed: " + command.action(), error);
        }
    }

    private int page(String[] args) {
        return args.length == 0 ? 1 : Integer.parseInt(args[0]);
    }

    private void reload(CommandSender sender) {
        try {
            plugin.reloadPlugin();
            send(sender, "reloaded");
        } catch (Exception error) {
            send(sender, "reload-failed", Var.of("%reason%", error.getMessage()));
            plugin.getLogger().warning("Reload rejected: " + error.getMessage());
        }
    }

    private void version(CommandSender sender) {
        send(sender, "plugin-info", Var.of("%version%", plugin.getPluginMeta().getVersion()));
    }

    private void fonts(CommandSender sender) {
        var fonts = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();

        send(sender, "font-list", Var.of("%fonts%", String.join(", ", fonts)));
    }

    private void send(CommandSender sender, String key, Var... variables) {
        plugin.getMessages().send(sender, key, variables);
    }
}

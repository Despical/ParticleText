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

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CatchUnknown;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Subcommand;

import dev.despical.particletext.ParticleTextPlugin;

import org.bukkit.command.CommandSender;

/**
 * Declares Aikar subcommands and permissions, following MazeEngine's command registration.
 * <p>
 * All annotated handlers share one dispatcher for configured validation and operation errors.
 * The private alias is executed by the native Paper command tree.
 *
 * @author Despical
 * <p>
 * Created at 10.10.2026
 */
@CommandAlias("__particletext")
public final class ParticleTextCommands extends BaseCommand {

    private final ParticleTextCommandDispatcher dispatcher;

    public ParticleTextCommands(ParticleTextPlugin plugin) {
        dispatcher = new ParticleTextCommandDispatcher(plugin);
    }

    @Default
    @CatchUnknown
    public void defaultCommand(CommandSender sender, String[] args) {
        dispatcher.execute(sender, args);
    }

    @Subcommand("create")
    @CommandPermission("particletext.command.create")
    public void createCommand(CommandSender sender, String[] args) {
        dispatcher.execute(sender, CommandDefinition.CREATE, args);
    }

    @Subcommand("delete")
    @CommandPermission("particletext.command.delete")
    public void deleteCommand(CommandSender sender, String[] args) {
        dispatcher.execute(sender, CommandDefinition.DELETE, args);
    }

    @Subcommand("tphere")
    @CommandPermission("particletext.command.teleport")
    public void tphereCommand(CommandSender sender, String[] args) {
        dispatcher.execute(sender, CommandDefinition.TPHERE, args);
    }

    @Subcommand("move")
    @CommandPermission("particletext.command.edit")
    public void moveCommand(CommandSender sender, String[] args) {
        dispatcher.execute(sender, CommandDefinition.MOVE, args);
    }

    @Subcommand("text")
    @CommandPermission("particletext.command.edit")
    public void textCommand(CommandSender sender, String[] args) {
        dispatcher.execute(sender, CommandDefinition.TEXT, args);
    }

    @Subcommand("setsize")
    @CommandPermission("particletext.command.edit")
    public void setsizeCommand(CommandSender sender, String[] args) {
        dispatcher.execute(sender, CommandDefinition.SETSIZE, args);
    }

    @Subcommand("font")
    @CommandPermission("particletext.command.edit")
    public void fontCommand(CommandSender sender, String[] args) {
        dispatcher.execute(sender, CommandDefinition.FONT, args);
    }

    @Subcommand("particle")
    @CommandPermission("particletext.command.edit")
    public void particleCommand(CommandSender sender, String[] args) {
        dispatcher.execute(sender, CommandDefinition.PARTICLE, args);
    }

    @Subcommand("enabled")
    @CommandPermission("particletext.command.edit")
    public void enabledCommand(CommandSender sender, String[] args) {
        dispatcher.execute(sender, CommandDefinition.ENABLED, args);
    }

    @Subcommand("inverted")
    @CommandPermission("particletext.command.edit")
    public void invertedCommand(CommandSender sender, String[] args) {
        dispatcher.execute(sender, CommandDefinition.INVERTED, args);
    }

    @Subcommand("rotate")
    @CommandPermission("particletext.command.edit")
    public void rotateCommand(CommandSender sender, String[] args) {
        dispatcher.execute(sender, CommandDefinition.ROTATE, args);
    }

    @Subcommand("reload")
    @CommandPermission("particletext.command.reload")
    public void reloadCommand(CommandSender sender, String[] args) {
        dispatcher.execute(sender, CommandDefinition.RELOAD, args);
    }

    @Subcommand("list")
    @CommandPermission("particletext.command.list")
    public void listCommand(CommandSender sender, String[] args) {
        dispatcher.execute(sender, CommandDefinition.LIST, args);
    }

    @Subcommand("menu")
    @CommandPermission("particletext.command.menu")
    public void menuCommand(CommandSender sender, String[] args) {
        dispatcher.execute(sender, CommandDefinition.MENU, args);
    }

    @Subcommand("teleport")
    @CommandPermission("particletext.command.teleport")
    public void teleportCommand(CommandSender sender, String[] args) {
        dispatcher.execute(sender, CommandDefinition.TELEPORT, args);
    }

    @Subcommand("help")
    @CommandPermission("particletext.command.help")
    public void helpCommand(CommandSender sender, String[] args) {
        dispatcher.execute(sender, CommandDefinition.HELP, args);
    }

    @Subcommand("version")
    @CommandPermission("particletext.command.version")
    public void versionCommand(CommandSender sender, String[] args) {
        dispatcher.execute(sender, CommandDefinition.VERSION, args);
    }

    @Subcommand("info")
    @CommandPermission("particletext.command.info")
    public void infoCommand(CommandSender sender, String[] args) {
        dispatcher.execute(sender, CommandDefinition.INFO, args);
    }

    @Subcommand("fonts")
    @CommandPermission("particletext.command.edit")
    public void fontsCommand(CommandSender sender, String[] args) {
        dispatcher.execute(sender, CommandDefinition.FONTS, args);
    }
}

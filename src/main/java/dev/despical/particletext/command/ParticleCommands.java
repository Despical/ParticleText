package dev.despical.particletext.command;

import dev.despical.commandframework.annotations.Command;
import dev.despical.particletext.menu.RendererMenu;
import dev.despical.particletext.message.Var;
import dev.despical.particletext.model.RendererData;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Optional;

public final class ParticleCommands extends CommandCategory {

    @Command(
        name = "pt",
        aliases = "particletext",
        fallbackPrefix = "particletext",
        usage = "/%label% help",
        desc = "Main ParticleText command."
    )
    public void root(Arguments arguments) {
        if (arguments.isArgumentsEmpty()) {
            arguments.sendConfigured("plugin-info", Var.of("%version%", plugin.getPluginMeta().getVersion()));
            return;
        }

        arguments.sendConfigured("unrecognized-arguments", Var.of("%label%", arguments.getLabel()));
    }

    @Command(
        name = "pt.list",
        aliases = "particletext.list",
        permission = "particletext.command.list",
        usage = "/%label% list",
        max = 0
    )
    public void list(Arguments arguments) {
        var all = renderers.all();

        if (all.isEmpty()) {
            arguments.sendConfigured("renderer-list-empty", Var.of("%label%", arguments.getLabel()));
            return;
        }

        arguments.sendConfigured("renderer-list",
            Var.of("%count%", all.size()),
            Var.of("%renderers%", String.join(", ", all.stream().map(RendererData::id).toList())));
    }

    @Command(
        name = "pt.menu",
        aliases = "particletext.menu",
        permission = "particletext.command.menu",
        usage = "/%label% menu",
        max = 0,
        senderType = Command.SenderType.PLAYER
    )
    public void menu(Arguments arguments) {
        Player player = arguments.getSender();

        new RendererMenu(plugin).open(player);
    }

    @Command(
        name = "pt.teleport",
        aliases = "particletext.teleport",
        permission = "particletext.command.teleport",
        usage = "/%label% teleport <id>",
        min = 1,
        max = 1,
        senderType = Command.SenderType.PLAYER
    )
    public void teleport(Arguments arguments) {
        String id = normalizeId(arguments.getFirst());
        Optional<RendererData> data = renderers.find(id);

        if (data.isEmpty()) {
            sendRendererNotFound(arguments, id);
            return;
        }

        Location location = data.get().location().toBukkitLocation();

        if (location == null) {
            arguments.sendConfigured("world-unavailable", Var.of("%world%", data.get().location().world()));
            return;
        }

        Player player = arguments.getSender();
        player.teleport(location);

        arguments.sendConfigured("renderer-teleported", Var.of("%id%", id));
    }

    @Command(
        name = "pt.help",
        aliases = "particletext.help",
        permission = "particletext.command.help",
        usage = "/%label% help",
        max = 0
    )
    public void help(Arguments arguments) {
        plugin.getMessages().sendList(arguments.getSender(), "help", Var.of("%label%", arguments.getLabel()));
    }

    @Command(
        name = "pt.version",
        aliases = "particletext.version",
        permission = "particletext.command.version",
        usage = "/%label% version",
        max = 0
    )
    public void version(Arguments arguments) {
        arguments.sendConfigured("plugin-info", Var.of("%version%", plugin.getPluginMeta().getVersion()));
    }
}

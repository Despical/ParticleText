package dev.despical.particletext.command;

import dev.despical.commandframework.CommandArguments;
import dev.despical.commandframework.CompleterHelper;
import dev.despical.commandframework.annotations.Completer;
import dev.despical.particletext.ParticleTextPlugin;
import dev.despical.particletext.model.FontStyle;
import dev.despical.particletext.model.ParticleSupport;
import dev.despical.particletext.model.RendererData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class TabCompleters {

    private final ParticleTextPlugin plugin = ParticleTextPlugin.getInstance();

    @Completer(
        name = "pt",
        aliases = "particletext",
        permission = "particletext.command.tabcomplete"
    )
    public List<String> complete(CommandArguments arguments, CompleterHelper helper) {
        int length = arguments.getLength();

        if (length <= 1) {
            return helper.copyMatches(0, commands(arguments));
        }

        String command = arguments.getArgument(0, "").toLowerCase();

        if (length == 2 && List.of("delete", "teleport", "tphere", "move", "text", "setsize", "font",
            "particle", "enabled", "inverted", "rotate").contains(command)) {
            return helper.copyMatches(1, plugin.getRendererService().all().stream()
                .map(RendererData::id)
                .toList());
        }

        if (length == 3 && command.equals("particle")) {
            return helper.copyMatches(2, ParticleSupport.names());
        }

        if (length == 3 && (command.equals("enabled") || command.equals("inverted"))) {
            return helper.copyMatches(2, List.of("true", "false"));
        }

        if (length == 3 && command.equals("rotate")) {
            return helper.copyMatches(2, List.of("x", "y", "z"));
        }

        if (length == 3 && command.equals("move")) {
            return helper.copyMatches(2, Arrays.stream(MoveDirection.values())
                .map(MoveDirection::displayName)
                .toList());
        }

        if (length == 4 && command.equals("move")) {
            return helper.copyMatches(3, List.of("0.1", "0.25", "0.5", "1"));
        }

        if (length == 4 && command.equals("font")) {
            return helper.copyMatches(3, Arrays.stream(FontStyle.values()).map(Enum::name).toList());
        }

        if (length == 5 && command.equals("font")) {
            return helper.copyMatches(4, List.of("12", "16", "20", "24", "32"));
        }

        return helper.empty();
    }

    private List<String> commands(CommandArguments arguments) {
        List<String> commands = new ArrayList<>();

        add(arguments, commands, "particletext.command.create", "create");
        add(arguments, commands, "particletext.command.delete", "delete");
        add(arguments, commands, "particletext.command.list", "list");
        add(arguments, commands, "particletext.command.menu", "menu");
        add(arguments, commands, "particletext.command.teleport", "teleport", "tphere");
        add(arguments, commands, "particletext.command.edit", "move", "text", "setsize", "font",
            "particle", "enabled", "inverted", "rotate");
        add(arguments, commands, "particletext.command.reload", "reload");
        add(arguments, commands, "particletext.command.help", "help");
        add(arguments, commands, "particletext.command.version", "version");

        return commands;
    }

    private void add(CommandArguments arguments, List<String> commands, String permission, String... values) {
        if (arguments.hasPermission(permission)) {
            commands.addAll(List.of(values));
        }
    }
}

package me.despical.particletext.commands;

import me.despical.commandframework.CommandArguments;
import me.despical.commandframework.Completer;
import me.despical.particletext.Main;
import org.bukkit.Particle;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TabCompleter extends AbstractCommand{

    public TabCompleter(Main plugin) {
        super(plugin);
    }

    @Completer(
            name = "pt"
    )
    public List<String> ptTabCompleter(CommandArguments arguments) {
        final List<String> completions = new ArrayList<>(), commands = plugin.getCommandFramework().getCommands().stream().map(cmd -> cmd.name().replace(arguments.getLabel() + '.', "")).collect(Collectors.toList());
        final String args[] = arguments.getArguments(), arg = args[0];

        commands.remove("pt");

        if (args.length == 1) {
            StringUtil.copyPartialMatches(arg, arguments.hasPermission("pt.admin") || arguments.getSender().isOp() ? commands : List.of("create", "delete", "tphere", "teleport", "enabled"), completions);
        }

        if (args.length == 2) {
            if (List.of("create", "list").contains(arg)) return completions;

            final var idList = new ArrayList<>(particleHandler.getRenderers().keySet().stream().toList());

            StringUtil.copyPartialMatches(args[1], idList, completions);
            idList.sort(null);
            return idList;
        }

        if (args.length == 3 && arg.equalsIgnoreCase("create")) {
            final var particleList = Stream.of(Particle.values()).map(Particle::name).sorted().toList();

            StringUtil.copyPartialMatches(args[2], particleList, completions);
            completions.sort(null);
            return completions;
        }

        if (args.length == 3 && arg.equalsIgnoreCase("enabled")) return List.of("true", "false");
        if (args.length == 4 && arg.equalsIgnoreCase("create")) return List.of("true", "false");

        completions.sort(null);
        return completions;
    }
}
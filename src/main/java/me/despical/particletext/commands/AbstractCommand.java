package me.despical.particletext.commands;

import me.despical.commons.string.StringMatcher;
import me.despical.particletext.Main;
import me.despical.particletext.handlers.ChatManager;
import me.despical.particletext.particles.ParticleHandler;

import java.util.List;
import java.util.stream.Collectors;

public abstract class AbstractCommand {

    protected final Main plugin;
    protected final ChatManager chatManager;
    protected final ParticleHandler particleHandler;

    public AbstractCommand(final Main plugin) {
        this.plugin = plugin;
        this.chatManager = plugin.getChatManager();
        this.particleHandler = plugin.getParticleHandler();
        this.plugin.getCommandFramework().registerCommands(this);
    }

    public static void registerCommands(final Main plugin) {
        final Class<?>[] commandClasses = new Class[] {ParticleCommands.class, TabCompleter.class};

        for (Class<?> clazz : commandClasses) {
            try {
                clazz.getConstructor(Main.class).newInstance(plugin);
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }

        final String didYouMeanMsg = plugin.getChatManager().message("admin-commands.did-you-mean");

        plugin.getCommandFramework().setAnyMatch(arguments -> {
            if (arguments.isArgumentsEmpty()) return;

            String label = arguments.getLabel(), arg = arguments.getArgument(0);
            List<StringMatcher.Match> matches = StringMatcher.match(arg, plugin.getCommandFramework().getCommands().stream().map(cmd -> cmd.name().replace(label + ".", "")).collect(Collectors.toList()));

            if (!matches.isEmpty()) {
                arguments.sendMessage(didYouMeanMsg.replace("%command%", label + " " + matches.get(0).getMatch()));
            }
        });
    }
}
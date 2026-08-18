package dev.despical.particletext.command;

import dev.despical.commandframework.CommandArguments;
import dev.despical.particletext.ParticleTextPlugin;
import dev.despical.particletext.message.Var;

public final class Arguments extends CommandArguments {

    private final ParticleTextPlugin plugin;

    public Arguments(CommandArguments arguments) {
        super(arguments);
        this.plugin = ParticleTextPlugin.getInstance();
    }

    public void sendConfigured(String path, Var... variables) {
        plugin.getMessages().send(getSender(), path, variables);
    }
}

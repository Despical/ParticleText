package dev.despical.particletext.command;

import dev.despical.particletext.ParticleTextPlugin;
import dev.despical.particletext.handler.ChatManager;
import dev.despical.particletext.particle.ParticleHandler;

public abstract class AbstractCommand {

    protected final ParticleTextPlugin plugin;
    protected final ChatManager chatManager;
    protected final ParticleHandler particleHandler;

    public AbstractCommand(ParticleTextPlugin plugin) {
        this.plugin = plugin;
        this.chatManager = plugin.getChatManager();
        this.particleHandler = plugin.getParticleHandler();
        this.plugin.getCommandFramework().registerCommands(this);
    }
}

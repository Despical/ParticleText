package me.despical.particletext.commands;

import me.despical.particletext.Main;
import me.despical.particletext.handlers.ChatManager;
import me.despical.particletext.particles.ParticleHandler;

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
        new ParticleCommands(plugin);
    }
}
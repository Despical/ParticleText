package me.despical.particletext;

import me.despical.commandframework.CommandFramework;
import me.despical.particletext.commands.AbstractCommand;
import me.despical.particletext.events.JoinQuitEvents;
import me.despical.particletext.handlers.ChatManager;
import me.despical.particletext.particles.ParticleHandler;
import me.despical.particletext.users.UserManager;
import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.io.File;
import java.util.stream.Stream;

/**
 * @author Despical
 * <p>
 * Created at 3.07.2023
 */
public class Main extends JavaPlugin {

	private ChatManager chatManager;
	private CommandFramework commandFramework;
	private ParticleHandler particleHandler;
	private UserManager userManager;

	@Override
	public void onEnable() {
		if (!supportsParticle()) return;

		this.initializeClasses();

		getLogger().info("Initialization finished. Join our Discord server: https://discord.gg/rVkaGmyszE");
	}

	@Override
	public void onDisable() {
		for (var entry : particleHandler.getRenderers().entrySet()) {
			var renderer = entry.getValue();

			if (renderer != null) renderer.stopRendering();
		}
	}

	private void initializeClasses() {
		this.setupConfigurationFiles();

		chatManager = new ChatManager(this);
		commandFramework = new CommandFramework(this);
		particleHandler = new ParticleHandler(this);
		userManager = new UserManager(this);

		AbstractCommand.registerCommands(this);

		new JoinQuitEvents(this);

		new Metrics(this, 18978);
	}

	private void setupConfigurationFiles() {
		Stream.of("messages", "renderers").filter(fileName -> !new File(getDataFolder(),fileName + ".yml").exists()).forEach(fileName -> this.saveResource(fileName + ".yml", false));
	}

	public @NotNull ChatManager getChatManager() {
		return chatManager;
	}

	public @NotNull CommandFramework getCommandFramework() {
		return commandFramework;
	}

	public @NotNull ParticleHandler getParticleHandler() {
		return particleHandler;
	}

	public @NotNull UserManager getUserManager() {
		return userManager;
	}

	public boolean supportsParticle() {
		try {
			Class.forName("org.bukkit.Particle");
			return true;
		} catch (ClassNotFoundException exception) {
			getLogger().severe("Your server does not support particles, we are disabling!..");
			setEnabled(false);
			return false;
		}
	}
}
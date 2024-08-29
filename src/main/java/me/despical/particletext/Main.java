package me.despical.particletext;

import me.despical.commandframework.CommandFramework;
import me.despical.commons.util.UpdateChecker;
import me.despical.particletext.commands.ParticleCommands;
import me.despical.particletext.events.JoinQuitEvents;
import me.despical.particletext.handlers.ChatManager;
import me.despical.particletext.particles.ParticleHandler;
import me.despical.particletext.users.UserManager;
import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

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
		this.initializeClasses();
		this.checkUpdate();

		getLogger().info("Initialization finished. Consider donating: https://buymeacoffee.com/despical");
	}

	@Override
	public void onDisable() {
		for (var entry : particleHandler.getRenderers().entrySet()) {
			var renderer = entry.getValue();

			if (renderer != null)
				renderer.stopRendering();
		}
	}

	private void initializeClasses() {
		this.setupConfigurationFiles();

		chatManager = new ChatManager(this);
		commandFramework = new CommandFramework(this);
		particleHandler = new ParticleHandler(this);
		userManager = new UserManager(this);

		new ParticleCommands(this);

		new JoinQuitEvents(this);
		new Metrics(this, 18978);
	}

	private void setupConfigurationFiles() {
		Stream.of("config", "messages", "renderers").filter(fileName -> !new File(getDataFolder(), fileName + ".yml").exists()).forEach(fileName -> this.saveResource(fileName + ".yml", false));
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

	private void checkUpdate() {
		if (!getConfig().getBoolean("Updates-Enabled", true)) return;

		UpdateChecker.init(this, 110996).requestUpdateCheck().whenComplete((result, exception) -> {
			if (result.requiresUpdate()) {
				final var logger = getLogger();

				logger.info("Found a new version available: v" + result.getNewestVersion());
			}
		});
	}
}
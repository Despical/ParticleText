package me.despical.particletext;

import me.despical.commandframework.CommandFramework;
import me.despical.particletext.command.MainCommands;
import me.despical.particletext.renderer.ParticleHandler;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

/**
 * @author Despical
 * <p>
 * Created at 7.08.2022
 */
public class Main extends JavaPlugin {

	private CommandFramework commandFramework;
	private ParticleHandler particleHandler;

	@Override
	public void onEnable() {
		if (!supportsParticle()) {
			log("Your server version does not support particles. Disabling plugin...");
			getServer().getPluginManager().disablePlugin(this);
			return;
		}

		this.commandFramework = new CommandFramework(this);
		this.particleHandler = new ParticleHandler(this);

		new MainCommands();
	}

	public void log(String message) {
		getLogger().log(Level.INFO, message);
	}

	public static boolean supportsParticle() {
		try {
			Class.forName("org.bukkit.Particle");
			return true;
		} catch (ClassNotFoundException exception) {
			return false;
		}
	}

	public CommandFramework getCommandFramework() {
		return commandFramework;
	}

	public ParticleHandler getParticleHandler() {
		return particleHandler;
	}
}
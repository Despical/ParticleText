package me.despical.particletext.command;

import me.despical.commons.util.Strings;
import me.despical.particletext.Main;
import me.despical.particletext.renderer.ParticleHandler;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public interface CommandImpl {

	Main plugin = JavaPlugin.getPlugin(Main.class);
	ParticleHandler particleHandler = plugin.getParticleHandler();

	default void sendMessage(Player player, String message) {
		player.sendMessage(Strings.format(message));
	}

	default void sendMessage(Player player, String message, Object... params) {
		this.sendMessage(player, String.format(message, params));
	}

	default void register(Object object) {
		plugin.getCommandFramework().registerCommands(object);
	}
}
package me.despical.particletext.users;

import me.despical.particletext.Main;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.text.MessageFormat;
import java.util.UUID;

/**
 * @author Despical
 * <p>
 * Created at 3.07.2023
 */
public record User(Player player) {

	private static final Main plugin = JavaPlugin.getPlugin(Main.class);

	public void sendMessage(final String path) {
		this.sendRawMessage(plugin.getChatManager().message(path));
	}

	public void sendMessage(final String path, final Object... args) {
		this.sendRawMessage(plugin.getChatManager().message(path), args);
	}

	public void sendRawMessage(final String message) {
		this.player.sendMessage(plugin.getChatManager().rawMessage(message));
	}

	public void sendRawMessage(final String message, final Object... args) {
		this.player.sendMessage(plugin.getChatManager().rawMessage(MessageFormat.format(message, args)));
	}

	public Location getLocation() {
		return this.player.getLocation();
	}

	public UUID getUniqueId() {
		return this.player.getUniqueId();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof User other)) return false;

		return other.getUniqueId().equals(this.getUniqueId());
	}

	@Override
	public String toString() {
		return "name=%s, uuid=%s".formatted(player.getName(), player.getUniqueId());
	}
}
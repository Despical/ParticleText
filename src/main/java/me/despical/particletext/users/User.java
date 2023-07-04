package me.despical.particletext.users;

import me.despical.particletext.Main;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

/**
 * @author Despical
 * <p>
 * Created at 3.07.2023
 */
public class User {

    private final static Main plugin = JavaPlugin.getPlugin(Main.class);

    private final Player player;

    public User(Player player) {
        this.player = player;
    }

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
        this.player.sendMessage(plugin.getChatManager().rawMessage(String.format(message, args)));
    }

    public Location getLocation() {
        return this.player.getLocation();
    }

    public Player getPlayer() {
        return this.player;
    }

    public String getName() {
        return this.player.getName();
    }

    public UUID getUniqueId() {
        return this.player.getUniqueId();
    }

    public boolean hasPermission(String permission) {
        return this.player.hasPermission(permission);
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
package dev.despical.particletext.event;

import dev.despical.particletext.ParticleTextPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class JoinQuitEvents implements Listener {

    private final ParticleTextPlugin plugin;

    public JoinQuitEvents(ParticleTextPlugin plugin) {
        this.plugin = plugin;

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        plugin.getUserManager().addUser(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        plugin.getUserManager().removeUser(event.getPlayer());
    }
}

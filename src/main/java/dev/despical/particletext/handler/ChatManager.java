package dev.despical.particletext.handler;

import dev.despical.commons.configuration.ConfigUtils;
import dev.despical.commons.util.Strings;
import dev.despical.particletext.ParticleTextPlugin;
import org.bukkit.configuration.file.FileConfiguration;

public class ChatManager {

    private final ParticleTextPlugin plugin;
    private FileConfiguration config;

    public ChatManager(ParticleTextPlugin plugin) {
        this.plugin = plugin;
        this.config = ConfigUtils.getConfig(plugin, "messages");
    }

    public String message(final String path) {
        return rawMessage(this.config.getString(path));
    }

    public String rawMessage(final String message) {
        return Strings.format(message);
    }

    public void reload() {
        this.config = ConfigUtils.getConfig(plugin, "messages");
    }
}

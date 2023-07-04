package me.despical.particletext.handlers;

import me.despical.commons.configuration.ConfigUtils;
import me.despical.commons.util.Strings;
import me.despical.particletext.Main;
import org.bukkit.configuration.file.FileConfiguration;

public class ChatManager {

    private final FileConfiguration config;

    public ChatManager(Main plugin) {
        this.config = ConfigUtils.getConfig(plugin, "messages");
    }

    public String message(final String path) {
        return rawMessage(this.config.getString(path));
    }

    public String rawMessage(final String message) {
        return Strings.format(message);
    }
}
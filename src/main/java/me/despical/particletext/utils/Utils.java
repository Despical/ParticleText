package me.despical.particletext.utils;

import me.despical.commons.configuration.ConfigUtils;
import me.despical.commons.number.NumberUtils;
import me.despical.particletext.Main;
import org.bukkit.plugin.java.JavaPlugin;

import java.awt.*;

public class Utils {

    public static final Main plugin = JavaPlugin.getPlugin(Main.class);

    public static Font getFont(String path) {
        final var config = ConfigUtils.getConfig(plugin, "renderers");
        final String[] fontAttributes = config.getString(path).split(":");

        if (fontAttributes.length != 3) return new Font("Tahoma", Font.PLAIN, 16);

        return new Font(fontAttributes[0], NumberUtils.getInt(fontAttributes[1], 0), NumberUtils.getInt(fontAttributes[2]));
    }
}
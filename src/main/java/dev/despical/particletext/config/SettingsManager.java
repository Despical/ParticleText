package dev.despical.particletext.config;

import dev.despical.particletext.ParticleTextPlugin;
import dev.despical.particletext.model.FontSpec;
import dev.despical.particletext.model.FontStyle;
import dev.despical.particletext.model.ParticleSupport;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.configuration.file.FileConfiguration;

import java.awt.Font;

public final class SettingsManager {

    @Getter
    @Accessors(fluent = true)
    private PluginSettings current;

    private final ParticleTextPlugin plugin;

    public SettingsManager(ParticleTextPlugin plugin) {
        this.plugin = plugin;
        this.reload();
    }

    public void reload() {
        plugin.reloadConfig();

        FileConfiguration config = plugin.getConfig();

        Particle defaultParticle = ParticleSupport.find(config.getString("defaults.particle"))
            .orElse(Particle.FLAME);
        FontStyle defaultStyle = FontStyle.find(config.getString("defaults.font.style"))
            .orElse(FontStyle.PLAIN);
        FontSpec defaultFont = new FontSpec(
            config.getString("defaults.font.name", Font.SANS_SERIF),
            defaultStyle,
            config.getInt("defaults.font.size", 16)
        );

        var defaults = new PluginSettings.RendererDefaults(
            defaultParticle,
            Math.clamp(config.getDouble("defaults.scale", 0.2), 0.01, 10.0),
            config.getBoolean("defaults.inverted", false),
            config.getBoolean("defaults.enabled", true),
            defaultFont
        );

        PluginSettings.MenuSettings menu = new PluginSettings.MenuSettings(
            config.getString("menu.title", "<#00E5FF><bold>ParticleText</bold>"),
            Math.clamp(config.getInt("menu.rows", 6), 2, 6),
            material(config, "menu.enabled-material", Material.LIME_DYE),
            material(config, "menu.disabled-material", Material.GRAY_DYE),
            material(config, "menu.previous-page-material", Material.ARROW),
            material(config, "menu.next-page-material", Material.ARROW),
            material(config, "menu.empty-material", Material.BARRIER)
        );

        current = new PluginSettings(
            config.getBoolean("updates-enabled", true),
            Math.clamp(config.getInt("performance.render-interval-ticks", 2), 1, 1200),
            Math.clamp(config.getInt("performance.initial-delay-ticks", 20), 0, 1200),
            Math.clamp(config.getDouble("performance.view-distance", 32.0), 1.0, 256.0),
            Math.clamp(config.getInt("performance.pixel-step", 1), 1, 16),
            Math.clamp(config.getInt("performance.max-points-per-renderer", 1500), 1, 10000),
            Math.clamp(config.getInt("performance.max-text-length", 128), 1, 512),
            config.getBoolean("performance.force-particles", false),
            Math.clamp(config.getInt("performance.placeholder-refresh-ticks", 100), 1, 72000),
            defaults,
            menu
        );
    }

    private Material material(FileConfiguration config, String path, Material fallback) {
        Material material = Material.matchMaterial(config.getString(path, fallback.name()));

        return material == null || material.isAir() ? fallback : material;
    }
}

package dev.despical.particletext.config;

import dev.despical.particletext.ParticleTextPlugin;
import dev.despical.particletext.model.FontSpec;
import dev.despical.particletext.model.FontStyle;
import dev.despical.particletext.model.ParticleSupport;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;

import java.awt.Font;
import java.util.Locale;

public final class SettingsManager {

    @Getter
    @Accessors(fluent = true)
    private PluginSettings current;

    private final ParticleTextPlugin plugin;

    public SettingsManager(ParticleTextPlugin plugin) {
        this.plugin = plugin;
        this.plugin.saveDefaultConfig();
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
            config.getString("menu.title", "<gradient:#00E5FF:#7C4DFF><bold>Particle Text</bold></gradient> <#37474F>»"),
            Math.clamp(config.getInt("menu.rows", 6), 3, 6),
            material(config, "menu.enabled-material", Material.LIME_DYE),
            material(config, "menu.disabled-material", Material.GRAY_DYE),
            material(config, "menu.previous-page-material", Material.ARROW),
            material(config, "menu.next-page-material", Material.ARROW),
            material(config, "menu.empty-material", Material.BARRIER),
            material(config, "menu.decoration-blocks.material", Material.GRAY_STAINED_GLASS_PANE),
            sound(config, "menu.sounds.open", Sound.UI_BUTTON_CLICK),
            sound(config, "menu.sounds.page-change", Sound.UI_BUTTON_CLICK),
            sound(config, "menu.sounds.teleport", Sound.ENTITY_ENDERMAN_TELEPORT),
            sound(config, "menu.sounds.enabled", Sound.BLOCK_NOTE_BLOCK_PLING),
            sound(config, "menu.sounds.disabled", Sound.BLOCK_NOTE_BLOCK_BASS)
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

    private Sound sound(FileConfiguration config, String path, Sound fallback) {
        String configured = config.getString(path, Registry.SOUNDS.getKeyOrThrow(fallback).asString());

        NamespacedKey key = NamespacedKey.fromString(configured.toLowerCase(Locale.ROOT));
        Sound sound = key == null ? null : Registry.SOUNDS.get(key);
        return sound == null ? fallback : sound;
    }
}

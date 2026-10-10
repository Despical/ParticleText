/*
 * Particle Text - Persistent particle text for Minecraft.
 * Copyright (C) 2026  Berke Akçen
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package dev.despical.particletext.config;

import dev.despical.particletext.ParticleTextPlugin;
import dev.despical.particletext.model.FontSpec;
import dev.despical.particletext.model.FontStyle;
import dev.despical.particletext.model.ParticleSupport;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.Locale;

/**
 * Validates global configuration and publishes immutable settings.
 * <p>
 * Numeric bounds, finite values, booleans, particle types, materials, sounds, and
 * font styles are checked while building a replacement. Loading never
 * changes the active settings value.
 * <p>
 * The plugin coordinates publication with messages and renderer records. Existing
 * renderers keep their saved defaults while global sampling, visibility, and
 * presentation settings take effect after a successful reload.
 *
 * @author Despical
 * <p>
 * Created at 10.10.2026
 */
public final class SettingsManager {

    private final ParticleTextPlugin plugin;
    private PluginSettings current;

    public SettingsManager(ParticleTextPlugin plugin) {
        this.plugin = plugin;
        apply(load());
    }

    public PluginSettings current() {
        return current;
    }

    public void apply(PluginSettings settings) {
        current = settings;
    }

    public PluginSettings load() {
        YamlConfiguration config = ConfigurationFiles.read(plugin, "config.yml");

        var particle = ParticleSupport.find(config.getString("defaults.particle"))
            .orElseThrow(() -> invalid("defaults.particle"));
        var style = FontStyle.find(config.getString("defaults.font.style"))
            .orElseThrow(() -> invalid("defaults.font.style"));
        var defaults = new PluginSettings.RendererDefaults(particle,
            decimal(config, "defaults.scale", 0.01, 10), bool(config, "defaults.inverted"),
            bool(config, "defaults.enabled"), new FontSpec(text(config, "defaults.font.name"), style,
            integer(config, "defaults.font.size", 4, 128))
        );

        var menu = new PluginSettings.MenuSettings(text(config, "menu.title"),
            integer(config, "menu.rows", 3, 6), material(config, "menu.enabled-material"),
            material(config, "menu.disabled-material"), material(config, "menu.previous-page-material"),
            material(config, "menu.next-page-material"), material(config, "menu.empty-material"),
            material(config, "menu.decoration-blocks.material"), sound(config, "menu.sounds.open"),
            sound(config, "menu.sounds.page-change"), sound(config, "menu.sounds.teleport"),
            sound(config, "menu.sounds.enabled"), sound(config, "menu.sounds.disabled"),
            (float) decimal(config, "menu.sound-volume", 0, 2),
            (float) decimal(config, "menu.sound-pitch", 0.5, 2)
        );

        return new PluginSettings(bool(config, "updates-enabled"),
            integer(config, "performance.render-interval-ticks", 1, 1200),
            integer(config, "performance.initial-delay-ticks", 0, 1200),
            decimal(config, "performance.view-distance", 1, 256),
            integer(config, "performance.pixel-step", 1, 16),
            integer(config, "performance.max-points-per-renderer", 1, 10000),
            integer(config, "performance.max-text-length", 1, 512), bool(config, "performance.force-particles"),
            integer(config, "performance.placeholder-refresh-ticks", 1, 72000), defaults, menu,
            integer(config, "performance.max-particles-per-tick", 1, 1000000),
            integer(config, "chat.page-size", 1, 30), decimal(config, "commands.default-move-amount", 0.01, 100)
        );
    }

    private static int integer(YamlConfiguration yaml, String path, int min, int max) {
        Object value = yaml.get(path);

        if (!(value instanceof Number number) || !Double.isFinite(number.doubleValue())
            || number.doubleValue() != number.intValue() || number.intValue() < min || number.intValue() > max
        ) {
            throw invalid(path + " (expected " + min + ".." + max + ")");
        }

        return number.intValue();
    }

    private static double decimal(YamlConfiguration yaml, String path, double min, double max) {
        Object value = yaml.get(path);

        if (!(value instanceof Number number) || !Double.isFinite(number.doubleValue())
            || number.doubleValue() < min || number.doubleValue() > max
        ) {
            throw invalid(path + " (expected " + min + ".." + max + ")");
        }

        return number.doubleValue();
    }

    private static boolean bool(YamlConfiguration yaml, String path) {
        if (!yaml.isBoolean(path)) {
            throw invalid(path + " (expected true or false)");
        }

        return yaml.getBoolean(path);
    }

    private static String text(YamlConfiguration yaml, String path) {
        if (!yaml.isString(path) || yaml.getString(path).isBlank()) {
            throw invalid(path);
        }

        return yaml.getString(path);
    }

    private static Material material(YamlConfiguration yaml, String path) {
        Material value = Material.matchMaterial(text(yaml, path));

        if (value == null || !value.isItem() || value.isAir()) {
            throw invalid(path);
        }

        return value;
    }

    private static Sound sound(YamlConfiguration yaml, String path) {
        NamespacedKey key = NamespacedKey.fromString(text(yaml, path).toLowerCase(Locale.ROOT));
        Sound value = key == null ? null : Registry.SOUNDS.get(key);

        if (value == null) {
            throw invalid(path);
        }

        return value;
    }

    private static IllegalArgumentException invalid(String path) {
        return new IllegalArgumentException("Invalid configuration: " + path);
    }
}

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

import org.bukkit.configuration.file.YamlConfiguration;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

/**
 * Installs missing resources and reads configuration with bundled defaults.
 * <p>
 * Existing files are never rewritten to add defaults. Known sections and value types are checked before
 * the bundled resource is overlaid in memory.
 * <p>
 * Malformed YAML or a mismatched known value type fails the staged load. Range and domain validation
 * belongs to SettingsManager or the renderer and message repositories.
 *
 * @author Despical
 * <p>
 * Created at 10.10.2026
 */
public final class ConfigurationFiles {

    private ConfigurationFiles() {
    }

    public static YamlConfiguration read(ParticleTextPlugin plugin, String name) {
        var path = plugin.getDataFolder().toPath().resolve(name);

        if (!Files.exists(path)) {
            plugin.saveResource(name, false);
        }

        var yaml = new YamlConfiguration();
        var defaults = new YamlConfiguration();

        try (var input = plugin.getResource(name)) {
            if (input == null) {
                throw new IllegalStateException("Missing bundled resource: " + name);
            }

            yaml.load(path.toFile());
            defaults.load(new InputStreamReader(input, StandardCharsets.UTF_8));

            for (String key : yaml.getKeys(true)) {
                Object expected = defaults.get(key);
                Object actual = yaml.get(key);

                if ((defaults.isConfigurationSection(key) && !yaml.isConfigurationSection(key))
                    || (expected instanceof Boolean && !(actual instanceof Boolean))
                    || (expected instanceof Number && !(actual instanceof Number))
                    || (expected instanceof String && !(actual instanceof String))
                    || (expected instanceof List<?> && !(actual instanceof List<?>))
                ) {
                    throw new IllegalArgumentException("Invalid value type: " + key);
                }
            }

            yaml.setDefaults(defaults);
            yaml.options().copyDefaults(true);

            return yaml;
        } catch (Exception error) {
            throw new IllegalArgumentException("Cannot read " + name + ": " + error.getMessage(), error);
        }
    }
}

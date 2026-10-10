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

package dev.despical.particletext.persistence;

import dev.despical.particletext.ParticleTextPlugin;
import dev.despical.particletext.config.ConfigurationFiles;
import dev.despical.particletext.config.PluginSettings;
import dev.despical.particletext.model.FontSpec;
import dev.despical.particletext.model.FontStyle;
import dev.despical.particletext.model.ParticleSupport;
import dev.despical.particletext.model.RendererData;
import dev.despical.particletext.model.RendererLocation;
import dev.despical.particletext.model.Rotation;
import lombok.RequiredArgsConstructor;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Loads and atomically replaces human-readable renderer records.
 * <p>
 * Missing legacy fields use the creation defaults. Invalid identifiers, value
 * types, particles, fonts, or transforms reject a snapshot before it becomes active.
 * <p>
 * Writes use a copied configuration and a temporary sibling file. The active
 * repository snapshot changes only after replacement succeeds, so a failed
 * save does not leave runtime and stored state describing different renderers.
 *
 * @author Despical
 * <p>
 * Created at 10.10.2026
 */
@RequiredArgsConstructor
public final class RendererRepository {

    private static final String ROOT = "renderers";
    private static final Pattern VALID_ID = Pattern.compile("[a-z0-9_-]{1,32}");

    private final ParticleTextPlugin plugin;
    private FileConfiguration configuration;

    public List<RendererData> loadAll(PluginSettings settings) {
        var snapshot = loadSnapshot(settings);
        apply(snapshot);

        return snapshot.renderers();
    }

    public Snapshot loadSnapshot(PluginSettings settings) {
        var configuration = ConfigurationFiles.read(plugin, "renderers.yml");
        ConfigurationSection root = configuration.getConfigurationSection(ROOT);

        if (root == null) {
            if (configuration.contains(ROOT)) {
                throw new IllegalArgumentException("renderers must be a YAML section");
            }

            return new Snapshot(configuration, List.of());
        }

        List<RendererData> renderers = new ArrayList<>();

        for (String id : root.getKeys(false)) {
            try {
                renderers.add(read(id, root.getConfigurationSection(id), settings));
            } catch (RuntimeException exception) {
                throw new IllegalArgumentException("Invalid renderer '" + id + "': " + exception.getMessage(),
                    exception);
            }
        }

        return new Snapshot(configuration, List.copyOf(renderers));
    }

    public void save(RendererData data) {
        var replacement = copy();
        String path = ROOT + "." + data.id();
        replacement.set(path, null);

        ConfigurationSection section = replacement.createSection(path);
        section.set("text", data.text());
        section.set("particle", data.particle().name());
        section.set("scale", data.scale());
        section.set("inverted", data.inverted());
        section.set("enabled", data.enabled());
        section.set("font.name", data.font().name());
        section.set("font.style", data.font().style().name());
        section.set("font.size", data.font().size());
        section.set("rotation.x", data.rotation().x());
        section.set("rotation.y", data.rotation().y());
        section.set("rotation.z", data.rotation().z());
        section.set("location.world", data.location().world());
        section.set("location.x", data.location().x());
        section.set("location.y", data.location().y());
        section.set("location.z", data.location().z());
        section.set("location.yaw", data.location().yaw());
        section.set("location.pitch", data.location().pitch());

        saveConfiguration(replacement);
    }

    public void delete(String id) {
        var replacement = copy();
        replacement.set(ROOT + "." + id, null);

        saveConfiguration(replacement);
    }

    public void apply(Snapshot snapshot) {
        configuration = snapshot.configuration();
    }

    private YamlConfiguration copy() {
        var copy = new YamlConfiguration();

        try {
            copy.loadFromString(configuration.saveToString());
        } catch (Exception error) {
            throw new IllegalStateException("Cannot copy renderer records", error);
        }

        return copy;
    }

    private void saveConfiguration(YamlConfiguration replacement) {
        try {
            write(plugin.getDataFolder().toPath().resolve("renderers.yml"),
                replacement.saveToString().getBytes(StandardCharsets.UTF_8)
            );
            configuration = replacement;
        } catch (IOException error) {
            throw new IllegalStateException("Cannot save renderer records", error);
        }
    }

    static RendererData read(String id, ConfigurationSection section, PluginSettings settings) {
        if (section == null) {
            throw new IllegalArgumentException("Renderer must be a YAML section");
        }

        if (!VALID_ID.matcher(id).matches()) {
            throw new IllegalArgumentException("Invalid renderer id");
        }

        validate(section);

        var defaults = settings.defaults();
        var particle = ParticleSupport.find(section.getString("particle", defaults.particle().name()))
            .orElseThrow(() -> new IllegalArgumentException("Unsupported particle"));
        var style = FontStyle.find(section.getString("font.style", defaults.font().style().name()))
            .orElseThrow(() -> new IllegalArgumentException("Invalid font style"));

        int fontSize = section.getInt("font.size", defaults.font().size());

        if (fontSize < 4 || fontSize > 128) {
            throw new IllegalArgumentException("Font size must be between 4 and 128");
        }

        var font = new FontSpec(section.getString("font.name", defaults.font().name()), style, fontSize);
        var location = new RendererLocation(section.getString("location.world", "world"),
            section.getDouble("location.x"), section.getDouble("location.y"), section.getDouble("location.z"),
            (float) section.getDouble("location.yaw"), (float) section.getDouble("location.pitch")
        );
        var rotation = new Rotation(section.getDouble("rotation.x"), section.getDouble("rotation.y"),
            section.getDouble("rotation.z")
        );

        return new RendererData(id, section.getString("text", id), particle,
            section.getDouble("scale", defaults.scale()), section.getBoolean("inverted", defaults.inverted()),
            section.getBoolean("enabled", defaults.enabled()), font, rotation, location
        );
    }

    private static void validate(ConfigurationSection section) {
        for (String path : List.of("font", "rotation", "location")) {
            if (section.contains(path) && !section.isConfigurationSection(path)) {
                throw new IllegalArgumentException("Expected renderer section: " + path);
            }
        }

        for (String path : List.of("text", "particle", "font.name", "font.style", "location.world")) {
            if (section.contains(path) && !section.isString(path)) {
                throw new IllegalArgumentException("Expected renderer text: " + path);
            }
        }

        for (String path : List.of("scale", "font.size", "rotation.x", "rotation.y", "rotation.z",
            "location.x", "location.y", "location.z", "location.yaw", "location.pitch")) {
            Object value = section.get(path);

            if (value != null && (!(value instanceof Number number) || !Double.isFinite(number.doubleValue()))) {
                throw new IllegalArgumentException("Expected finite renderer number: " + path);
            }
        }

        for (String path : List.of("inverted", "enabled")) {
            if (section.contains(path) && !section.isBoolean(path)) {
                throw new IllegalArgumentException("Expected renderer boolean: " + path);
            }
        }

        if (section.contains("font.size") && section.getDouble("font.size") != section.getInt("font.size")) {
            throw new IllegalArgumentException("Font size must be an integer");
        }
    }

    static void write(Path target, byte[] bytes) throws IOException {
        Path parent = target.toAbsolutePath().getParent();
        Files.createDirectories(parent);
        Path temporary = Files.createTempFile(parent, ".particletext-", ".tmp");

        try {
            Files.write(temporary, bytes);

            try {
                Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException error) {
                Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    public record Snapshot(YamlConfiguration configuration, List<RendererData> renderers) {
    }
}

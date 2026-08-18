package dev.despical.particletext.persistence;

import dev.despical.commons.configuration.ConfigUtils;
import dev.despical.particletext.ParticleTextPlugin;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

@RequiredArgsConstructor
public final class RendererRepository {

    private static final String ROOT = "renderers";
    private static final Pattern VALID_ID = Pattern.compile("[a-z0-9_-]{1,32}");

    private final ParticleTextPlugin plugin;
    private FileConfiguration configuration;

    public List<RendererData> loadAll(PluginSettings settings) {
        configuration = Objects.requireNonNull(ConfigUtils.getConfig(plugin, "renderers"));
        ConfigurationSection root = configuration.getConfigurationSection(ROOT);

        if (root == null) {
            configuration.createSection(ROOT);
            saveConfiguration();

            return List.of();
        }

        List<RendererData> renderers = new ArrayList<>();

        for (String id : root.getKeys(false)) {
            try {
                read(id, root.getConfigurationSection(id), settings).ifPresent(renderers::add);
            } catch (RuntimeException exception) {
                plugin.getLogger().warning("Skipping invalid renderer '" + id + "': " + exception.getMessage());
            }
        }

        return List.copyOf(renderers);
    }

    public void save(RendererData data) {
        String path = ROOT + "." + data.id();
        configuration.set(path, null);

        ConfigurationSection section = configuration.createSection(path);
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

        saveConfiguration();
    }

    public void delete(String id) {
        configuration.set(ROOT + "." + id, null);
        saveConfiguration();
    }

    private java.util.Optional<RendererData> read(String id, ConfigurationSection section, PluginSettings settings) {
        if (section == null) {
            return java.util.Optional.empty();
        }

        if (!VALID_ID.matcher(id).matches()) {
            plugin.getLogger().warning("Renderer id '" + id + "' is invalid and was skipped.");
            return java.util.Optional.empty();
        }

        PluginSettings.RendererDefaults defaults = settings.defaults();
        var particle = ParticleSupport.find(section.getString("particle", defaults.particle().name()));

        if (particle.isEmpty()) {
            plugin.getLogger().warning("Renderer '" + id + "' has an unsupported particle.");
            return java.util.Optional.empty();
        }

        FontStyle style = FontStyle.find(section.getString("font.style", defaults.font().style().name()))
            .orElse(defaults.font().style());

        FontSpec font = new FontSpec(
            section.getString("font.name", defaults.font().name()),
            style,
            section.getInt("font.size", defaults.font().size())
        );

        RendererLocation location = new RendererLocation(
            section.getString("location.world", "world"),
            section.getDouble("location.x"),
            section.getDouble("location.y"),
            section.getDouble("location.z"),
            (float) section.getDouble("location.yaw"),
            (float) section.getDouble("location.pitch")
        );

        Rotation rotation = new Rotation(
            section.getDouble("rotation.x"),
            section.getDouble("rotation.y"),
            section.getDouble("rotation.z")
        );

        return java.util.Optional.of(new RendererData(
            id,
            section.getString("text", id),
            particle.get(),
            Math.clamp(section.getDouble("scale", defaults.scale()), 0.01, 10.0),
            section.getBoolean("inverted", defaults.inverted()),
            section.getBoolean("enabled", defaults.enabled()),
            font,
            rotation,
            location
        ));
    }

    private void saveConfiguration() {
        ConfigUtils.saveConfig(plugin, configuration, "renderers");
    }
}

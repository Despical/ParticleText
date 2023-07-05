package me.despical.particletext.particles;

import me.despical.commons.configuration.ConfigUtils;
import me.despical.commons.serializer.LocationSerializer;
import me.despical.particletext.Main;
import org.bukkit.Location;
import org.bukkit.Particle;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Despical
 * <p>
 * Created at 3.07.2023
 */
public class ParticleHandler {

	private final Main plugin;
	private final Map<String, ParticleRenderer> rendererMap;

	public ParticleHandler(Main plugin) {
		this.plugin = plugin;
		this.rendererMap = new HashMap<>();

		var config =  ConfigUtils.getConfig(plugin, "renderers");
		var section = config.getConfigurationSection("renderer-instances");

		if (section == null) {
			plugin.getLogger().warning("'renderer-instances' section didn't find in renderers.yml");
			plugin.getLogger().warning("Creating a new one...");

			config.createSection("renderer-instances");
			return;
		}

		for (final String id : section.getKeys(false)) {
			if (id.equals("default")) continue;

			String path = "renderer-instances.%s.".formatted(id), text = config.getString(path + "text");
			Particle particle = Particle.valueOf(config.getString(path + "particle"));
			Location location = LocationSerializer.fromString(config.getString(path + "location"));
			float size = (float) config.getDouble(path + "size", .2F);
			boolean inverted = config.getBoolean(path + "inverted");

			ParticleRenderer renderer = new ParticleRenderer(location, particle, text, size, inverted);
			renderer.setEnabled(config.getBoolean(path + "enabled", true));
			this.rendererMap.put(id, renderer);
		}
	}

	public ParticleRenderer createRenderer(Location location, Particle particle, String id, String text, float size, boolean inverted) {
		ParticleRenderer renderer = new ParticleRenderer(location, particle, text, size, inverted);
		 String path = "renderer-instances.%s.".formatted(id);

		this.rendererMap.put(id, renderer);

		var config =  ConfigUtils.getConfig(plugin, "renderers");
		config.set(path + "id", id);
		config.set(path + "text", text);
		config.set(path + "size", size);
		config.set(path + "particle", particle.name());
		config.set(path + "inverted", inverted);
		config.set(path + "enabled", true);
		config.set(path + "location", LocationSerializer.toString(location));

		ConfigUtils.saveConfig(plugin, config, "renderers");

		return renderer;
	}

	public void removeRenderer(String id) {
		ParticleRenderer particleRenderer = rendererMap.get(id);

		if (particleRenderer != null) {
			this.rendererMap.remove(id);

			particleRenderer.stopRendering();
		}
	}

	public boolean containsRenderer(String id) {
		return this.rendererMap.containsKey(id);
	}

	public @Nullable ParticleRenderer getRenderer(String id) {
		return this.rendererMap.get(id);
	}

	public Map<String, ParticleRenderer> getRenderers() {
		return Map.copyOf(this.rendererMap);
	}
}
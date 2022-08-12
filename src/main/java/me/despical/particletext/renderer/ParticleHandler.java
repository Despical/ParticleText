package me.despical.particletext.renderer;

import me.despical.particletext.Main;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Despical
 * <p>
 * Created at 8.08.2022
 */
public class ParticleHandler {

	private final Map<String, ParticleRenderer> rendererMap;

	public ParticleHandler(Main plugin) {
		this.rendererMap = new HashMap<>();

		for (String name : plugin.getConfig().getConfigurationSection("renderers").getKeys(false)) {

		}
	}

	public void addRenderer(String name, ParticleRenderer renderer) {
		this.rendererMap.put(name, renderer);
	}

	public void removeRenderer(String name) {
		this.rendererMap.remove(name);
	}

	public boolean containsRenderer(String name) {
		return this.rendererMap.containsKey(name);
	}

	public Map<String, ParticleRenderer> getRenderers() {
		return rendererMap;
	}
}
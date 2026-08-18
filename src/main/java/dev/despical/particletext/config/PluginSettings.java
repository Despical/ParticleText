package dev.despical.particletext.config;

import dev.despical.particletext.model.FontSpec;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;

public record PluginSettings(
    boolean updatesEnabled,
    int renderIntervalTicks,
    int initialDelayTicks,
    double viewDistance,
    int pixelStep,
    int maxPointsPerRenderer,
    int maxTextLength,
    boolean forceParticles,
    int placeholderRefreshTicks,
    RendererDefaults defaults,
    MenuSettings menu
) {

    public double viewDistanceSquared() {
        return viewDistance * viewDistance;
    }

    public record RendererDefaults(Particle particle, double scale, boolean inverted, boolean enabled, FontSpec font) {
    }

    public record MenuSettings(
        String title,
        int rows,
        Material enabledMaterial,
        Material disabledMaterial,
        Material previousPageMaterial,
        Material nextPageMaterial,
        Material emptyMaterial,
        Material decorationMaterial,
        Sound openSound,
        Sound pageChangeSound,
        Sound teleportSound,
        Sound enabledSound,
        Sound disabledSound
    ) {
    }
}

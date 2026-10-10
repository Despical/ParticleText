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

import dev.despical.particletext.model.FontSpec;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;

/**
 * Holds the validated global settings for one plugin configuration snapshot.
 * <p>
 * Nested immutable records describe renderer defaults, inventory materials and sounds.
 * Services read a complete snapshot instead of traversing mutable YAML during rendering.
 * <p>
 * Creation defaults are copied into new renderer records. Global budgets, visibility, sampling, and
 * presentation remain separate so reloading does not silently replace saved renderer-specific values.
 *
 * @author Despical
 * <p>
 * Created at 10.10.2026
 */
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
    MenuSettings menu,
    int maxParticlesPerTick,
    int chatPageSize,
    double defaultMoveAmount
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
        Sound disabledSound,
        float soundVolume,
        float soundPitch
    ) {
    }
}

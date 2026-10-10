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

package dev.despical.particletext.model;

import org.bukkit.Particle;

/**
 * Stores immutable persistent content and presentation for one renderer.
 * <p>
 * Identifiers, scale, and required values are validated before a record becomes
 * active.
 * <p>
 * Copy methods preserve unrelated text, font, and transform settings.
 *
 * @author Despical
 * <p>
 * Created at 10.10.2026
 */
public record RendererData(String id, String text, Particle particle, double scale, boolean inverted,
    boolean enabled, FontSpec font, Rotation rotation, RendererLocation location) {

    public RendererData {
        if (id == null || !id.matches("[a-z0-9_-]{1,32}") || text == null || particle == null
            || !Double.isFinite(scale) || scale < 0.01 || scale > 10 || font == null || rotation == null
            || location == null) {
            throw new IllegalArgumentException("Invalid renderer data: " + id);
        }
    }

    public RendererData withText(String value) {
        return new RendererData(id, value, particle, scale, inverted, enabled, font, rotation, location);
    }

    public RendererData withParticle(Particle value) {
        return new RendererData(id, text, value, scale, inverted, enabled, font, rotation, location);
    }

    public RendererData withScale(double value) {
        return new RendererData(id, text, particle, value, inverted, enabled, font, rotation, location);
    }

    public RendererData withEnabled(boolean value) {
        return new RendererData(id, text, particle, scale, inverted, value, font, rotation, location);
    }

    public RendererData withInverted(boolean value) {
        return new RendererData(id, text, particle, scale, value, enabled, font, rotation, location);
    }

    public RendererData withFont(FontSpec value) {
        return new RendererData(id, text, particle, scale, inverted, enabled, value, rotation, location);
    }

    public RendererData withRotation(Rotation value) {
        return new RendererData(id, text, particle, scale, inverted, enabled, font, value, location);
    }

    public RendererData withLocation(RendererLocation value) {
        return new RendererData(id, text, particle, scale, inverted, enabled, font, rotation, value);
    }
}

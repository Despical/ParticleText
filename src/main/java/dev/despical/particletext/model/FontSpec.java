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

import java.awt.Font;

/**
 * Stores the portable AWT font settings used to rasterize text.
 * <p>
 * Blank families normalize to SansSerif, absent styles normalize to PLAIN, and programmatic sizes are
 * bounded to 4 through 128. Configuration and commands reject invalid sizes before construction.
 * <p>
 * Java substitutes a logical fallback when a requested family is not installed. The fonts command lists
 * available server families; these settings never require a client resource pack.
 *
 * @author Despical
 * <p>
 * Created at 10.10.2026
 */
public record FontSpec(String name, FontStyle style, int size) {

    public FontSpec {
        name = name == null || name.isBlank() ? Font.SANS_SERIF : name.trim();
        style = style == null ? FontStyle.PLAIN : style;
        size = Math.clamp(size, 4, 128);
    }

    public Font toFont() {
        return new Font(name, style.awtValue(), size);
    }
}

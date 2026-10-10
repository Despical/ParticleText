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

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import java.awt.Font;
import java.util.Locale;
import java.util.Optional;

/**
 * Maps supported text styles to their AWT font flags.
 * <p>
 * PLAIN, BOLD, ITALIC, and BOLD_ITALIC preserve the four supported rasterization styles. The combined
 * style uses the normal AWT bit flags.
 * <p>
 * Configuration and command lookups ignore case and return an empty result for unsupported styles,
 * allowing callers to send a configured validation response.
 *
 * @author Despical
 * <p>
 * Created at 10.10.2026
 */
@Getter
@RequiredArgsConstructor
public enum FontStyle {

    PLAIN(Font.PLAIN),
    BOLD(Font.BOLD),
    ITALIC(Font.ITALIC),
    BOLD_ITALIC(Font.BOLD | Font.ITALIC);

    @Accessors(fluent = true)
    private final int awtValue;

    public static Optional<FontStyle> find(String value) {
        if (value == null) {
            return Optional.empty();
        }

        try {
            return Optional.of(valueOf(value.toUpperCase(Locale.ENGLISH)));
        } catch (IllegalArgumentException _) {
            return Optional.empty();
        }
    }
}

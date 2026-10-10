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

import lombok.experimental.UtilityClass;

import org.bukkit.Particle;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Finds particles that can be emitted without additional Bukkit data.
 * <p>
 * The supported list is derived from the server API particle enum and contains only Void-data particles.
 * Names are sorted and immutable for command suggestions.
 * <p>
 * A particle requiring block, item, or other
 * data cannot accidentally enter the ordinary text render path.
 *
 * @author Despical
 * <p>
 * Created at 10.10.2026
 */
@UtilityClass
public class ParticleSupport {

    private final List<String> NAMES = Arrays.stream(Particle.values())
        .filter(particle -> particle.getDataType() == Void.class)
        .map(Particle::name)
        .sorted()
        .toList();

    public static Optional<Particle> find(String name) {
        if (name == null) {
            return Optional.empty();
        }

        try {
            Particle particle = Particle.valueOf(name.toUpperCase(Locale.ENGLISH));

            return particle.getDataType() == Void.class ? Optional.of(particle) : Optional.empty();
        } catch (IllegalArgumentException _) {
            return Optional.empty();
        }
    }

    public static List<String> names() {
        return NAMES;
    }
}

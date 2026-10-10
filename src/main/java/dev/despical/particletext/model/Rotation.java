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

/**
 * Stores finite rotation angles around the three local axes.
 * <p>
 * The record accepts finite degree values without wrapping them, preserving the angle entered by the
 * administrator. Point-cloud construction applies the saved axis order.
 * <p>
 * Replacing one axis returns a new record and retains the other two. An unknown axis is rejected rather
 * than silently changing an arbitrary component.
 *
 * @author Despical
 * <p>
 * Created at 10.10.2026
 */
public record Rotation(double x, double y, double z) {

    public Rotation {
        if (!Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(z)) {
            throw new IllegalArgumentException("Rotation must be finite");
        }
    }

    public static final Rotation NONE = new Rotation(0.0, 0.0, 0.0);

    public Rotation withAxis(char axis, double angle) {
        return switch (Character.toLowerCase(axis)) {
            case 'x' -> new Rotation(angle, y, z);
            case 'y' -> new Rotation(x, angle, z);
            case 'z' -> new Rotation(x, y, angle);
            default -> throw new IllegalArgumentException("Unknown rotation axis: " + axis);
        };
    }
}

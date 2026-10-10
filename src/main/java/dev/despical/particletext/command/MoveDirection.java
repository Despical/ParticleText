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

package dev.despical.particletext.command;

import java.util.Locale;
import java.util.Optional;

/**
 * Converts a movement direction and player yaw into a world offset.
 * <p>
 * Forward, backward, left, and right follow the player facing; up and down use the world vertical axis.
 * Distances are supplied by validated command input or the configurable default.
 * <p>
 * Lookups ignore case and return an empty result for unknown directions. Offset calculation is independent
 * of Bukkit locations, allowing deterministic movement checks.
 *
 * @author Despical
 * <p>
 * Created at 10.10.2026
 */
enum MoveDirection {

    FORWARD,
    BACKWARD,
    LEFT,
    RIGHT,
    UP,
    DOWN;

    static Optional<MoveDirection> find(String value) {
        try {
            return Optional.of(valueOf(value.toUpperCase(Locale.ROOT)));
        } catch (IllegalArgumentException _) {
            return Optional.empty();
        }
    }

    Offset offset(float yaw, double amount) {
        double radians = Math.toRadians(yaw);
        double forwardX = -Math.sin(radians) * amount;
        double forwardZ = Math.cos(radians) * amount;
        double rightX = -Math.cos(radians) * amount;
        double rightZ = -Math.sin(radians) * amount;

        return switch (this) {
            case FORWARD -> new Offset(forwardX, 0.0, forwardZ);
            case BACKWARD -> new Offset(-forwardX, 0.0, -forwardZ);
            case LEFT -> new Offset(-rightX, 0.0, -rightZ);
            case RIGHT -> new Offset(rightX, 0.0, rightZ);
            case UP -> new Offset(0.0, amount, 0.0);
            case DOWN -> new Offset(0.0, -amount, 0.0);
        };
    }

    String displayName() {
        return name().toLowerCase(Locale.ROOT);
    }

    record Offset(double x, double y, double z) {
    }
}

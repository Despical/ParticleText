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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MoveDirectionTest {

    @Test
    void resolvesDirectionsCaseInsensitively() {
        assertEquals(MoveDirection.LEFT, MoveDirection.find("LeFt").orElseThrow());
        assertTrue(MoveDirection.find("sideways").isEmpty());
    }

    @Test
    void usesPlayerYawForHorizontalDirections() {
        assertVector(MoveDirection.FORWARD.offset(0f, 1.0), 0.0, 0.0, 1.0);
        assertVector(MoveDirection.RIGHT.offset(0f, 1.0), -1.0, 0.0, 0.0);
        assertVector(MoveDirection.FORWARD.offset(90f, 1.0), -1.0, 0.0, 0.0);
        assertVector(MoveDirection.RIGHT.offset(90f, 1.0), 0.0, 0.0, -1.0);
    }

    @Test
    void keepsVerticalDirectionsIndependentFromYaw() {
        assertVector(MoveDirection.UP.offset(137f, 0.25), 0.0, 0.25, 0.0);
        assertVector(MoveDirection.DOWN.offset(-42f, 0.5), 0.0, -0.5, 0.0);
    }

    private void assertVector(MoveDirection.Offset actual, double x, double y, double z) {
        assertEquals(x, actual.x(), 1.0E-9);
        assertEquals(y, actual.y(), 1.0E-9);
        assertEquals(z, actual.z(), 1.0E-9);
    }
}

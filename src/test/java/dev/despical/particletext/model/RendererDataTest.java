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
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package dev.despical.particletext.model;

import org.bukkit.Particle;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RendererDataTest {

    @Test
    void preservesIdentifierBoundariesForCommandsAndModelCopies() {
        assertFalse(RendererData.isValidId(null));
        for (String id : List.of("a", "spawn_1-west", "a".repeat(32))) {
            assertTrue(RendererData.isValidId(id), id);
            assertEquals(id, data(id).withText("Changed").id());
        }

        for (String id : List.of("", "Uppercase", "a".repeat(33), "two words", "nested.path", "../escape", "é")) {
            assertFalse(RendererData.isValidId(id), id);
            assertThrows(IllegalArgumentException.class, () -> data(id), id);
        }
    }

    private RendererData data(String id) {
        return new RendererData(id, "Welcome", Particle.FLAME, 1, false, true,
            new FontSpec("SansSerif", FontStyle.PLAIN, 16), Rotation.NONE,
            new RendererLocation("world", 0, 0, 0, 0, 0));
    }
}

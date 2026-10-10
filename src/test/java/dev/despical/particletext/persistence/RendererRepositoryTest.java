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

package dev.despical.particletext.persistence;

import dev.despical.particletext.config.TestSettings;

import org.bukkit.configuration.file.YamlConfiguration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class RendererRepositoryTest {

    @TempDir
    Path directory;

    @Test
    void loadsLegacyTextRecordsWithMissingNewFields() throws Exception {
        var yaml = new YamlConfiguration();
        yaml.loadFromString("text: Welcome\nlocation:\n  world: example\n  x: 12.5\n  y: 70\n  z: -3\n");
        var data = RendererRepository.read("welcome", yaml, TestSettings.settings(100));
        assertEquals("Welcome", data.text());
        assertEquals("example", data.location().world());
        assertEquals(12.5, data.location().x());
        assertEquals(0.2, data.scale());
    }

    @Test
    void rejectsPresentWrongTypesInsteadOfSilentlyUsingDefaults() {
        var settings = TestSettings.settings(100);
        for (String path : new String[] {"scale", "enabled", "location.x", "font.size", "rotation", "font"}) {
            var yaml = new YamlConfiguration();
            yaml.set(path, "broken");
            assertThrows(IllegalArgumentException.class, () -> RendererRepository.read("test", yaml, settings), path);
        }
    }

    @Test
    void rejectsNonFiniteCoordinates() {
        var settings = TestSettings.settings(100);
        var yaml = new YamlConfiguration();
        yaml.set("location.x", Double.NaN);
        assertThrows(IllegalArgumentException.class, () -> RendererRepository.read("test", yaml, settings));
    }

    @Test
    void rejectsInvalidIdentifiersAndFractionalFontSizes() {
        var yaml = new YamlConfiguration();
        var settings = TestSettings.settings(100);
        assertThrows(IllegalArgumentException.class, () -> RendererRepository.read("../test", yaml, settings));
        yaml.set("font.size", 16.5);
        assertThrows(IllegalArgumentException.class, () -> RendererRepository.read("test", yaml, settings));
    }

    @Test
    void replacesCompleteFilesAndLeavesNoTemporarySiblings() throws Exception {
        Path file = directory.resolve("nested/renderers.yml");
        RendererRepository.write(file, "first".getBytes(StandardCharsets.UTF_8));
        RendererRepository.write(file, "second".getBytes(StandardCharsets.UTF_8));

        assertEquals("second", Files.readString(file));

        try (var siblings = Files.list(file.getParent())) {
            assertEquals(1, siblings.count());
        }
    }

    @Test
    void cleansTemporaryFilesWhenReplacementFails() throws Exception {
        Path target = directory.resolve("occupied");
        Files.createDirectory(target);
        Files.writeString(target.resolve("keep"), "keep");

        assertThrows(IOException.class, () -> RendererRepository.write(target, new byte[] {1}));
        assertEquals("keep", Files.readString(target.resolve("keep")));

        try (var siblings = Files.list(directory)) {
            assertEquals(1, siblings.count());
        }
    }
}

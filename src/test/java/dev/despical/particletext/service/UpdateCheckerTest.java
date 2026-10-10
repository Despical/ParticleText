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

package dev.despical.particletext.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UpdateCheckerTest {

    @Test
    void recognizesNewerVersions() {
        assertTrue(UpdateChecker.isNewer("2.0.1", "2.0.0"));
        assertTrue(UpdateChecker.isNewer("v2.1", "2.0.9"));
    }

    @Test
    void rejectsOlderAndEqualVersions() {
        assertFalse(UpdateChecker.isNewer("1.2.1", "2.0.0"));
        assertFalse(UpdateChecker.isNewer("2.0.0", "2.0"));
    }

    @Test
    void recognizesAStableReleaseAfterAPreRelease() {
        assertTrue(UpdateChecker.isNewer("2.0.1", "2.0.1-beta.1"));
        assertFalse(UpdateChecker.isNewer("2.0.1-beta.2", "2.0.1"));
    }

    @Test
    void ignoresInvalidUpdateResponses() {
        assertFalse(UpdateChecker.isNewer("404 Not Found", "2.0.2"));
        assertFalse(UpdateChecker.isNewer("999<html>", "2.0.2"));
        assertFalse(UpdateChecker.isNewer("", "2.0.2-beta.1"));
        assertFalse(UpdateChecker.isNewer(null, "2.0.2"));
        assertFalse(UpdateChecker.isNewer("2.0.3", null));
    }

    @Test
    void ignoresBuildMetadataWhenComparingVersions() {
        assertFalse(UpdateChecker.isNewer("2.0.2+build-1", "2.0.2+build-2"));
        assertTrue(UpdateChecker.isNewer("2.0.2+build-1", "2.0.2-beta.1+build-2"));
        assertTrue(UpdateChecker.isNewer("2.0.3+build-1", "2.0.2"));
    }
}

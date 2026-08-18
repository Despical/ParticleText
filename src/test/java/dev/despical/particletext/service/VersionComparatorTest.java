package dev.despical.particletext.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VersionComparatorTest {

    @Test
    void recognizesNewerVersions() {
        assertTrue(VersionComparator.isNewer("2.0.1", "2.0.0"));
        assertTrue(VersionComparator.isNewer("v2.1", "2.0.9"));
    }

    @Test
    void rejectsOlderAndEqualVersions() {
        assertFalse(VersionComparator.isNewer("1.2.1", "2.0.0"));
        assertFalse(VersionComparator.isNewer("2.0.0", "2.0"));
    }
}

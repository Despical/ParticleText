package dev.despical.particletext.render;

import dev.despical.particletext.model.FontSpec;
import dev.despical.particletext.model.FontStyle;
import dev.despical.particletext.model.Rotation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PointCloudFactoryTest {

    private final PointCloudFactory factory = new PointCloudFactory();
    private final FontSpec font = new FontSpec("SansSerif", FontStyle.PLAIN, 16);

    @Test
    void createsNoPointsForBlankText() {
        assertTrue(factory.create(" ", font, false, 0.2, Rotation.NONE, 0, 1, 100, 128).isEmpty());
    }

    @Test
    void respectsPointLimit() {
        var points = factory.create("ParticleText", font, true, 0.2, Rotation.NONE, 0, 1, 25, 128);
        assertFalse(points.isEmpty());
        assertTrue(points.size() <= 25);
    }

    @Test
    void rotatesPointsAroundZAxis() {
        var normal = factory.create("I", font, false, 1.0, Rotation.NONE, 0, 1, 1000, 128);
        var rotated = factory.create("I", font, false, 1.0, new Rotation(0, 0, 90), 0, 1, 1000, 128);

        assertEquals(normal.size(), rotated.size());
        assertEquals(-normal.getFirst().y(), rotated.getFirst().x(), 0.0001);
        assertEquals(normal.getFirst().x(), rotated.getFirst().y(), 0.0001);
    }
}

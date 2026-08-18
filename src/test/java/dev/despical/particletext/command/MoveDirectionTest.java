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

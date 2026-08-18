package dev.despical.particletext.command;

import java.util.Locale;
import java.util.Optional;

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
        } catch (IllegalArgumentException exception) {
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

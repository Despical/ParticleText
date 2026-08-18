package dev.despical.particletext.model;

public record Rotation(double x, double y, double z) {

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

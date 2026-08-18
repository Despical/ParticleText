package dev.despical.particletext.render;

import dev.despical.particletext.model.FontSpec;
import dev.despical.particletext.model.Rotation;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public final class PointCloudFactory {

    public List<Offset> create(
        String text,
        FontSpec font,
        boolean inverted,
        double scale,
        Rotation rotation,
        float yaw,
        int pixelStep,
        int maxPoints,
        int maxTextLength
    ) {
        if (text == null || text.isBlank()) {
            return List.of();
        }

        String safeText = text.length() > maxTextLength ? text.substring(0, maxTextLength) : text;
        BufferedImage image = rasterize(safeText, font);

        List<Offset> points = new ArrayList<>();
        int step = Math.max(1, pixelStep);

        for (int y = 0; y < image.getHeight(); y += step) {
            for (int x = 0; x < image.getWidth(); x += step) {
                boolean foreground = (image.getRGB(x, y) & 0xFFFFFF) != 0;

                if (foreground == inverted) {
                    continue;
                }

                double pointX = (image.getWidth() / 2.0 - x) * scale;
                double pointY = (image.getHeight() / 2.0 - y) * scale;
                points.add(rotate(new Offset(pointX, pointY, 0.0), rotation, yaw));
            }
        }

        if (points.size() <= maxPoints) {
            return List.copyOf(points);
        }

        double stride = points.size() / (double) maxPoints;
        List<Offset> limited = new ArrayList<>(maxPoints);

        for (int index = 0; index < maxPoints; index++) {
            limited.add(points.get((int) Math.floor(index * stride)));
        }

        return List.copyOf(limited);
    }

    private BufferedImage rasterize(String text, FontSpec fontSpec) {
        BufferedImage probe = new BufferedImage(1, 1, BufferedImage.TYPE_BYTE_BINARY);
        Graphics2D probeGraphics = probe.createGraphics();
        probeGraphics.setFont(fontSpec.toFont());

        FontMetrics metrics = probeGraphics.getFontMetrics();
        int width = Math.max(1, metrics.stringWidth(text) + 4);
        int height = Math.max(1, metrics.getHeight() + 4);

        probeGraphics.dispose();

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_BINARY);
        Graphics2D graphics = image.createGraphics();

        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        graphics.setColor(Color.BLACK);
        graphics.fillRect(0, 0, width, height);
        graphics.setColor(Color.WHITE);
        graphics.setFont(fontSpec.toFont());
        graphics.drawString(text, 2, 2 + metrics.getAscent());
        graphics.dispose();

        return image;
    }

    private Offset rotate(Offset point, Rotation rotation, float yaw) {
        Offset rotated = rotateY(point, Math.toRadians(yaw));
        rotated = rotateX(rotated, Math.toRadians(rotation.x()));
        rotated = rotateY(rotated, Math.toRadians(rotation.y()));

        return rotateZ(rotated, Math.toRadians(rotation.z()));
    }

    private Offset rotateX(Offset point, double angle) {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);

        return new Offset(point.x(), point.y() * cos - point.z() * sin, point.y() * sin + point.z() * cos);
    }

    private Offset rotateY(Offset point, double angle) {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);

        return new Offset(point.x() * cos + point.z() * sin, point.y(), point.z() * cos - point.x() * sin);
    }

    private Offset rotateZ(Offset point, double angle) {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);

        return new Offset(point.x() * cos - point.y() * sin, point.x() * sin + point.y() * cos, point.z());
    }
}

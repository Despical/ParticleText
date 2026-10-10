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

/**
 * Rasterizes text and builds bounded local particle geometry.
 * <p>
 * Portable AWT fonts produce a binary image without smoothing. Foreground or
 * inverted background pixels are sampled in two passes, limiting retained
 * offsets even for large source glyphs.
 * <p>
 * Output is immutable and evenly covers eligible pixels, preserving saved
 * facing and axis rotations.
 *
 * @author Despical
 * <p>
 * Created at 10.10.2026
 */
public final class PointCloudFactory {

    public List<Offset> create(String text, FontSpec font, boolean inverted, double scale, Rotation rotation,
        float yaw, int pixelStep, int maxPoints, int maxTextLength) {
        if (text == null || text.isBlank()) {
            return List.of();
        }

        String safeText = text.length() > maxTextLength ? text.substring(0, maxTextLength) : text;
        BufferedImage image = rasterize(safeText, font);

        int step = Math.max(1, pixelStep);

        int eligible = 0;
        for (int y = 0; y < image.getHeight(); y += step) {
            for (int x = 0; x < image.getWidth(); x += step) {
                if (((image.getRGB(x, y) & 0xFFFFFF) != 0) != inverted) {
                    eligible++;
                }
            }
        }
        int count = Math.min(eligible, maxPoints);
        List<Offset> points = new ArrayList<>(count);
        double stride = eligible / (double) Math.max(1, count);
        int seen = 0;

        for (int y = 0; y < image.getHeight(); y += step) {
            for (int x = 0; x < image.getWidth(); x += step) {
                boolean foreground = (image.getRGB(x, y) & 0xFFFFFF) != 0;

                if (foreground == inverted) {
                    continue;
                }

                if (points.size() >= count || seen++ != (int) Math.floor(points.size() * stride)) {
                    continue;
                }

                double pointX = (image.getWidth() / 2.0 - x) * scale;
                double pointY = (image.getHeight() / 2.0 - y) * scale;
                points.add(rotate(new Offset(pointX, pointY, 0.0), rotation, yaw));
            }
        }

        return List.copyOf(points);
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

    /**
     * Local coordinates before the renderer adds its world origin.
     */
    public record Offset(double x, double y, double z) {
    }
}

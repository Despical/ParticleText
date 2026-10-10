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

import dev.despical.particletext.config.PluginSettings;
import dev.despical.particletext.model.RendererData;
import dev.despical.particletext.render.PointCloudFactory.Offset;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Caches transformed particle points for one persistent renderer.
 * <p>
 * Text glyphs use data-free particles. Scale, yaw, and three-axis rotation are
 * applied when cached points change, rather than during every render pass.
 * <p>
 * Rendering filters viewers by world and distance, then respects the supplied
 * packet budget. Its cursor continues across partial passes so a low budget
 * does not repeatedly emit only the first pixels.
 *
 * @author Despical
 * <p>
 * Created at 10.10.2026
 */
public final class ParticleTextRenderer {

    private final PointCloudFactory factory = new PointCloudFactory();
    private final RendererData data;
    private String resolvedText;
    private List<Offset> points = List.of();
    private long cursor;

    public ParticleTextRenderer(RendererData data, PluginSettings settings, String resolvedText) {
        this.data = data;
        apply(settings, resolvedText);
    }

    public RendererData data() {
        return data;
    }

    public int pointCount() {
        return points.size();
    }

    private void apply(PluginSettings settings, String newResolvedText) {
        resolvedText = newResolvedText;
        points = factory.create(newResolvedText, data.font(), data.inverted(), data.scale(), data.rotation(),
            data.location().yaw(), settings.pixelStep(), settings.maxPointsPerRenderer(), settings.maxTextLength());
        cursor = 0;
    }

    public void refreshResolvedText(String text, PluginSettings settings) {
        if (!resolvedText.equals(text)) {
            apply(settings, text);
        }
    }

    public int render(PluginSettings settings, int budget) {
        if (!data.enabled() || points.isEmpty() || budget <= 0) {
            return 0;
        }
        Location origin = data.location().toBukkitLocation();
        if (origin == null) {
            return 0;
        }
        List<Player> viewers = new ArrayList<>();
        for (Player player : origin.getWorld().getPlayers()) {
            if (player.getLocation().distanceSquared(origin) <= settings.viewDistanceSquared()) {
                viewers.add(player);
            }
        }
        if (viewers.isEmpty()) {
            return 0;
        }
        return emit(settings, origin, viewers, budget);
    }

    int emit(PluginSettings settings, Location origin, List<Player> viewers, int budget) {
        if (points.isEmpty() || viewers.isEmpty() || budget <= 0) {
            return 0;
        }
        long total = (long) points.size() * viewers.size();
        int count = (int) Math.min(total, budget);
        for (int index = 0; index < count; index++) {
            long slot = (cursor + index) % total;
            Offset point = points.get((int) (slot / viewers.size()));
            Player viewer = viewers.get((int) (slot % viewers.size()));
            viewer.spawnParticle(data.particle(), origin.getX() + point.x(), origin.getY() + point.y(),
                origin.getZ() + point.z(), 1, 0, 0, 0, 0, null, settings.forceParticles());
        }
        cursor = (cursor + count) % total;
        return count;
    }
}

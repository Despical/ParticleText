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

package dev.despical.particletext.model;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.Nullable;

/**
 * Stores a renderer position by world name and finite coordinates.
 * <p>
 * Saving the world name keeps records usable across server restarts or delayed world loading. Conversion
 * returns no location while that world is unavailable instead of forcing it to load.
 * <p>
 * Player position capture and Bukkit-world conversion belong on the server thread. Yaw and pitch remain
 * part of the saved location and facing.
 *
 * @author Despical
 * <p>
 * Created at 10.10.2026
 */
public record RendererLocation(String world, double x, double y, double z, float yaw, float pitch) {

    public RendererLocation {
        if (world == null || world.isBlank() || !Double.isFinite(x) || !Double.isFinite(y)
            || !Double.isFinite(z) || !Float.isFinite(yaw) || !Float.isFinite(pitch)) {
            throw new IllegalArgumentException("Location must have a world and finite coordinates");
        }
    }

    public static RendererLocation from(Location location) {
        World world = location.getWorld();

        if (world == null) {
            throw new IllegalArgumentException("Renderer location must have a world");
        }

        return new RendererLocation(world.getName(), location.getX(), location.getY(), location.getZ(),
            location.getYaw(), location.getPitch());
    }

    public @Nullable Location toBukkitLocation() {
        World loadedWorld = Bukkit.getWorld(world);

        return loadedWorld == null ? null : new Location(loadedWorld, x, y, z, yaw, pitch);
    }
}

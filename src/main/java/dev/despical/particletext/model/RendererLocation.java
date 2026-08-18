package dev.despical.particletext.model;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.Nullable;

public record RendererLocation(String world, double x, double y, double z, float yaw, float pitch) {

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

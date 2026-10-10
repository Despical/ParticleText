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

package dev.despical.particletext.papi;

import dev.despical.particletext.ParticleTextPlugin;
import dev.despical.particletext.model.RendererData;
import lombok.RequiredArgsConstructor;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Exposes saved renderer counts and values to PlaceholderAPI.
 * <p>
 * Count and ID lookups read immutable renderer snapshots. Nearest-renderer placeholders use the requesting
 * player world and location; requests without a player have no nearest result.
 * <p>
 * The expansion persists across PlaceholderAPI reloads and is explicitly unregistered when Particle Text
 * disables. Unknown field requests do not mutate renderer state.
 *
 * @author Despical
 * <p>
 * Created at 10.10.2026
 */
@RequiredArgsConstructor
public final class ParticleTextExpansion extends PlaceholderExpansion {

    private final ParticleTextPlugin plugin;

    @Override
    public @NotNull String getIdentifier() {
        return "particletext";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Despical";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getPluginMeta().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String identifier) {
        String normalized = identifier.toLowerCase(Locale.ENGLISH);
        List<RendererData> renderers = plugin.getRendererService().all();

        return switch (normalized) {
            case "total" -> Integer.toString(renderers.size());
            case "enabled" -> Long.toString(renderers.stream().filter(RendererData::enabled).count());
            case "disabled" -> Long.toString(renderers.stream().filter(data -> !data.enabled()).count());
            case "nearest_id" -> nearest(player).map(RendererData::id).orElse("");
            case "nearest_text" -> nearest(player).map(RendererData::text).orElse("");
            case "nearest_distance" -> nearestDistance(player);
            default -> normalized.startsWith("renderer:") ? rendererValue(identifier) : null;
        };
    }

    private String rendererValue(String identifier) {
        String[] parts = identifier.split(":", 3);

        if (parts.length != 3) {
            return "";
        }

        Optional<RendererData> renderer = plugin.getRendererService().find(parts[1].toLowerCase(Locale.ENGLISH));

        if (renderer.isEmpty()) {
            return "";
        }

        RendererData data = renderer.get();

        return switch (parts[2].toLowerCase(Locale.ENGLISH)) {
            case "text" -> data.text();
            case "particle" -> data.particle().name();
            case "scale" -> String.format(Locale.US, "%.2f", data.scale());
            case "enabled" -> Boolean.toString(data.enabled());
            case "inverted" -> Boolean.toString(data.inverted());
            case "world" -> data.location().world();
            default -> "";
        };
    }

    private Optional<RendererData> nearest(Player player) {
        return player == null ? Optional.empty() : plugin.getRendererService().nearest(player);
    }

    private String nearestDistance(Player player) {
        Optional<RendererData> nearest = nearest(player);

        if (nearest.isEmpty()) {
            return "";
        }

        Location location = nearest.get().location().toBukkitLocation();

        if (location == null) {
            return "";
        }

        return String.format(Locale.US, "%.2f", Math.sqrt(location.distanceSquared(player.getLocation())));
    }
}

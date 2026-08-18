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

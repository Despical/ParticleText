package dev.despical.particletext.render;

import dev.despical.particletext.config.PluginSettings;
import dev.despical.particletext.model.RendererData;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public final class ParticleTextRenderer {

    private final PointCloudFactory pointCloudFactory;

    @Getter
    @Accessors(fluent = true)
    private RendererData data;
    private String resolvedText;
    private List<Offset> offsets;

    public ParticleTextRenderer(RendererData data, PluginSettings settings, String resolvedText) {
        this.pointCloudFactory = new PointCloudFactory();
        apply(data, settings, resolvedText);
    }

    public void apply(RendererData newData, PluginSettings settings, String newResolvedText) {
        this.data = newData;
        this.resolvedText = newResolvedText;
        this.offsets = pointCloudFactory.create(
            newResolvedText,
            newData.font(),
            newData.inverted(),
            newData.scale(),
            newData.rotation(),
            newData.location().yaw(),
            settings.pixelStep(),
            settings.maxPointsPerRenderer(),
            settings.maxTextLength()
        );
    }

    public void refreshResolvedText(String newResolvedText, PluginSettings settings) {
        if (!resolvedText.equals(newResolvedText)) {
            apply(data, settings, newResolvedText);
        }
    }

    public void render(PluginSettings settings) {
        if (!data.enabled() || offsets.isEmpty()) {
            return;
        }

        Location origin = data.location().toBukkitLocation();

        if (origin == null) {
            return;
        }

        World world = origin.getWorld();
        List<Player> viewers = new ArrayList<>();

        for (Player player : world.getPlayers()) {
            if (player.getLocation().distanceSquared(origin) <= settings.viewDistanceSquared()) {
                viewers.add(player);
            }
        }

        if (viewers.isEmpty()) {
            return;
        }

        for (Offset offset : offsets) {
            double x = origin.getX() + offset.x();
            double y = origin.getY() + offset.y();
            double z = origin.getZ() + offset.z();

            for (Player viewer : viewers) {
                viewer.spawnParticle(data.particle(), x, y, z, 1, 0.0, 0.0, 0.0, 0.0, null,
                    settings.forceParticles());
            }
        }
    }
}

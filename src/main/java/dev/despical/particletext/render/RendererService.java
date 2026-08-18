package dev.despical.particletext.render;

import dev.despical.particletext.ParticleTextPlugin;
import dev.despical.particletext.config.PluginSettings;
import dev.despical.particletext.config.SettingsManager;
import dev.despical.particletext.model.FontSpec;
import dev.despical.particletext.model.RendererData;
import dev.despical.particletext.model.RendererLocation;
import dev.despical.particletext.model.Rotation;
import dev.despical.particletext.papi.TextResolver;
import dev.despical.particletext.persistence.RendererRepository;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.UnaryOperator;

public final class RendererService {

    private BukkitTask renderTask;
    private long ticksSincePlaceholderRefresh;

    private final ParticleTextPlugin plugin;
    private final SettingsManager settingsManager;
    private final RendererRepository repository;
    private final TextResolver textResolver;
    private final Map<String, ParticleTextRenderer> renderers;

    public RendererService(
        ParticleTextPlugin plugin,
        SettingsManager settingsManager,
        RendererRepository repository,
        TextResolver textResolver
    ) {
        this.plugin = plugin;
        this.settingsManager = settingsManager;
        this.repository = repository;
        this.textResolver = textResolver;
        this.renderers = new LinkedHashMap<>();
        loadRenderers();
    }

    public void start() {
        stop();

        PluginSettings settings = settingsManager.current();
        renderTask = Bukkit.getScheduler()
            .runTaskTimer(plugin, this::renderAll, settings.initialDelayTicks(), settings.renderIntervalTicks());
    }

    public void stop() {
        if (renderTask != null) {
            renderTask.cancel();
            renderTask = null;
        }
    }

    public void reload() {
        stop();
        loadRenderers();
        start();
    }

    public Optional<RendererData> find(String id) {
        ParticleTextRenderer renderer = renderers.get(id);
        return renderer == null ? Optional.empty() : Optional.of(renderer.data());
    }

    public List<RendererData> all() {
        return renderers.values().stream()
            .map(ParticleTextRenderer::data)
            .sorted(Comparator.comparing(RendererData::id))
            .toList();
    }

    public RendererData create(String id, String text, Location location) {
        PluginSettings settings = settingsManager.current();
        PluginSettings.RendererDefaults defaults = settings.defaults();

        RendererData data = new RendererData(id, text, defaults.particle(), defaults.scale(), defaults.inverted(),
            defaults.enabled(), defaults.font(), Rotation.NONE, RendererLocation.from(location));

        put(data, true);
        return data;
    }

    public boolean delete(String id) {
        if (renderers.remove(id) == null) {
            return false;
        }

        repository.delete(id);

        return true;
    }

    public Optional<RendererData> updateText(String id, String text) {
        return update(id, data -> data.withText(text));
    }

    public Optional<RendererData> updateScale(String id, double scale) {
        return update(id, data -> data.withScale(scale));
    }

    public Optional<RendererData> updateFont(String id, FontSpec font) {
        return update(id, data -> data.withFont(font));
    }

    public Optional<RendererData> updateParticle(String id, Particle particle) {
        return update(id, data -> data.withParticle(particle));
    }

    public Optional<RendererData> updateEnabled(String id, boolean enabled) {
        return update(id, data -> data.withEnabled(enabled));
    }

    public Optional<RendererData> updateInverted(String id, boolean inverted) {
        return update(id, data -> data.withInverted(inverted));
    }

    public Optional<RendererData> toggleEnabled(String id) {
        return update(id, data -> data.withEnabled(!data.enabled()));
    }

    public Optional<RendererData> updateRotation(String id, char axis, double angle) {
        return update(id, data -> data.withRotation(data.rotation().withAxis(axis, angle)));
    }

    public Optional<RendererData> updateLocation(String id, Location location) {
        return update(id, data -> data.withLocation(RendererLocation.from(location)));
    }

    public Optional<RendererData> nearest(Player player) {
        Location playerLocation = player.getLocation();

        return all().stream()
            .filter(data -> data.location().world().equals(player.getWorld().getName()))
            .min(Comparator.comparingDouble(data -> {
                Location location = data.location().toBukkitLocation();

                return location == null ? Double.MAX_VALUE : location.distanceSquared(playerLocation);
            }));
    }

    private Optional<RendererData> update(String id, UnaryOperator<RendererData> updater) {
        ParticleTextRenderer renderer = renderers.get(id);

        if (renderer == null) {
            return Optional.empty();
        }

        RendererData updated = updater.apply(renderer.data());

        put(updated, true);

        return Optional.of(updated);
    }

    private void put(RendererData data, boolean save) {
        PluginSettings settings = settingsManager.current();
        String resolvedText = textResolver.resolve(null, data.text());
        ParticleTextRenderer renderer = renderers.get(data.id());

        if (renderer == null) {
            renderers.put(data.id(), new ParticleTextRenderer(data, settings, resolvedText));
        } else {
            renderer.apply(data, settings, resolvedText);
        }

        if (save) {
            repository.save(data);
        }
    }

    private void loadRenderers() {
        renderers.clear();
        repository.loadAll(settingsManager.current()).forEach(data -> put(data, false));
        ticksSincePlaceholderRefresh = 0L;
    }

    private void renderAll() {
        PluginSettings settings = settingsManager.current();
        ticksSincePlaceholderRefresh += settings.renderIntervalTicks();

        if (ticksSincePlaceholderRefresh >= settings.placeholderRefreshTicks()) {
            renderers.values().forEach(renderer -> renderer.refreshResolvedText(
                textResolver.resolve(null, renderer.data().text()), settings));
            ticksSincePlaceholderRefresh = 0L;
        }

        renderers.values().forEach(renderer -> renderer.render(settings));
    }
}

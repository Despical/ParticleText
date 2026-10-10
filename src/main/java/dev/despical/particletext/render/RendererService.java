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

/**
 * Owns saved renderer state and the shared particle scheduler.
 * <p>
 * Renderer creation and edits prepare replacement point clouds before persisting
 * their records. Runtime state is published only after a successful atomic write.
 * Read-only consumers receive immutable renderer snapshots.
 * <p>
 * All mutations and particle sends run on the server thread. Placeholder text is
 * refreshed at the configured interval; renderer and packet cursors keep the
 * shared budget bounded without permanently starving later renderers.
 *
 * @author Despical
 * <p>
 * Created at 10.10.2026
 */
public final class RendererService {

    private BukkitTask renderTask;
    private long ticksSincePlaceholderRefresh;
    private int renderIndex;

    private final ParticleTextPlugin plugin;
    private final SettingsManager settingsManager;
    private final RendererRepository repository;
    private final TextResolver textResolver;
    private final Map<String, ParticleTextRenderer> renderers = new LinkedHashMap<>();
    private volatile List<RendererData> records = List.of();
    private volatile Map<String, RendererData> byId = Map.of();

    public RendererService(ParticleTextPlugin plugin, SettingsManager settingsManager, RendererRepository repository,
        TextResolver textResolver) {
        this.plugin = plugin;
        this.settingsManager = settingsManager;
        this.repository = repository;
        this.textResolver = textResolver;
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

    public Optional<RendererData> find(String id) {
        return Optional.ofNullable(byId.get(id));
    }

    public List<RendererData> all() {
        return records;
    }

    public RendererData create(String id, String text, Location location) {
        PluginSettings settings = settingsManager.current();
        PluginSettings.RendererDefaults defaults = settings.defaults();

        RendererData data = new RendererData(id, text, defaults.particle(), defaults.scale(), defaults.inverted(),
            defaults.enabled(), defaults.font(), Rotation.NONE, RendererLocation.from(location));

        put(data);
        return data;
    }

    public boolean delete(String id) {
        if (!renderers.containsKey(id)) {
            return false;
        }

        repository.delete(id);
        renderers.remove(id);
        publish();

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
            .filter(data -> data.location().toBukkitLocation() != null)
            .min(Comparator.comparingDouble(data -> data.location().toBukkitLocation()
                .distanceSquared(playerLocation)));
    }

    private Optional<RendererData> update(String id, UnaryOperator<RendererData> updater) {
        ParticleTextRenderer renderer = renderers.get(id);

        if (renderer == null) {
            return Optional.empty();
        }

        RendererData updated = updater.apply(renderer.data());

        if (!updated.equals(renderer.data())) {
            put(updated);
        }

        return Optional.of(updated);
    }

    public int pointCount(String id) {
        var renderer = renderers.get(id);
        return renderer == null ? 0 : renderer.pointCount();
    }

    private void put(RendererData data) {
        var renderer = prepare(data, settingsManager.current());
        repository.save(data);
        renderers.put(data.id(), renderer);
        publish();
    }

    private ParticleTextRenderer prepare(RendererData data, PluginSettings settings) {
        return new ParticleTextRenderer(data, settings, textResolver.resolve(null, data.text()));
    }

    public Map<String, ParticleTextRenderer> prepareAll(List<RendererData> records, PluginSettings settings) {
        var replacement = new LinkedHashMap<String, ParticleTextRenderer>();
        for (var data : records) {
            replacement.put(data.id(), prepare(data, settings));
        }
        return replacement;
    }

    public void applyAll(Map<String, ParticleTextRenderer> replacement) {
        renderers.clear();
        renderers.putAll(replacement);
        ticksSincePlaceholderRefresh = 0;
        renderIndex = 0;
        publish();
    }

    private void publish() {
        records = renderers.values().stream().map(ParticleTextRenderer::data)
            .sorted(Comparator.comparing(RendererData::id)).toList();
        byId = records.stream().collect(java.util.stream.Collectors.toUnmodifiableMap(RendererData::id, data -> data));
    }

    private void loadRenderers() {
        applyAll(prepareAll(repository.loadAll(settingsManager.current()), settingsManager.current()));
    }

    private void renderAll() {
        PluginSettings settings = settingsManager.current();
        ticksSincePlaceholderRefresh += settings.renderIntervalTicks();

        if (ticksSincePlaceholderRefresh >= settings.placeholderRefreshTicks()) {
            renderers.values().forEach(renderer -> renderer.refreshResolvedText(
                textResolver.resolve(null, renderer.data().text()), settings));
            ticksSincePlaceholderRefresh = 0L;
        }

        var ordered = List.copyOf(renderers.values());
        int remaining = settings.maxParticlesPerTick();
        for (int offset = 0; offset < ordered.size() && remaining > 0; offset++) {
            int index = (renderIndex + offset) % ordered.size();
            remaining -= ordered.get(index).render(settings, remaining);
        }
        if (!ordered.isEmpty()) {
            renderIndex = (renderIndex + 1) % ordered.size();
        }
    }
}

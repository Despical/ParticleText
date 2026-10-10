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

package dev.despical.particletext;

import dev.despical.particletext.command.BrigadierCommands;
import dev.despical.particletext.config.SettingsManager;
import dev.despical.particletext.message.MessageService;
import dev.despical.particletext.message.RendererPanels;
import dev.despical.particletext.papi.ParticleTextExpansion;
import dev.despical.particletext.papi.PlaceholderApiTextResolver;
import dev.despical.particletext.papi.TextResolver;
import dev.despical.particletext.persistence.RendererRepository;
import dev.despical.particletext.render.RendererService;
import dev.despical.particletext.service.UpdateChecker;
import lombok.Getter;

import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

/**
 * Coordinates the Particle Text plugin lifecycle.
 * <p>
 * Startup installs resources, validates settings, loads renderer records, and registers
 * commands and integrations. Failures disable the plugin before partially initialized services become available.
 * <p>
 * Reload stages settings, message templates, and renderer records before publishing
 * replacements. Shutdown stops rendering and unregisters command and PlaceholderAPI integrations.
 *
 * @author Despical
 * <p>
 * Created at 10.10.2026
 */
@Getter
public final class ParticleTextPlugin extends JavaPlugin {

    private SettingsManager settingsManager;
    private MessageService messages;
    private RendererService rendererService;
    private RendererRepository repository;
    private BrigadierCommands commands;
    private RendererPanels panels;
    private Metrics metrics;
    private ParticleTextExpansion expansion;

    @Override
    public void onEnable() {
        try {
            settingsManager = new SettingsManager(this);
            TextResolver resolver = getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")
                ? new PlaceholderApiTextResolver() : TextResolver.passthrough();
            messages = new MessageService(this, resolver);
            repository = new RendererRepository(this);
            rendererService = new RendererService(this, settingsManager, repository, resolver);
            panels = new RendererPanels(this);
            commands = new BrigadierCommands(this);

            registerPlaceholderExpansion();

            rendererService.start();

            try {
                metrics = new Metrics(this, 18978);
            } catch (Exception error) {
                getLogger().warning("bStats could not start: " + error.getMessage());
            }

            if (settingsManager.current().updatesEnabled()) {
                new UpdateChecker(this).check();
            }

            getLogger().info("ParticleText enabled: " + rendererService.all().size() + " saved renderers.");
        } catch (Exception error) {
            getLogger().log(Level.SEVERE, "Cannot enable ParticleText", error);
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    public void reloadPlugin() {
        var settings = settingsManager.load();
        var templates = messages.load();
        var records = repository.loadSnapshot(settings);
        var prepared = rendererService.prepareAll(records.renderers(), settings);

        rendererService.stop();
        settingsManager.apply(settings);
        messages.apply(templates);
        repository.apply(records);
        rendererService.applyAll(prepared);
        rendererService.start();
    }

    private void registerPlaceholderExpansion() {
        if (getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            expansion = new ParticleTextExpansion(this);
            expansion.register();

            getLogger().info("PlaceholderAPI integration enabled.");
        }
    }

    @Override
    public void onDisable() {

        if (rendererService != null) {
            rendererService.stop();
        }

        if (commands != null) {
            commands.close();
        }

        if (expansion != null) {
            expansion.unregister();
        }

        if (metrics != null) {
            metrics.shutdown();
        }

    }

}

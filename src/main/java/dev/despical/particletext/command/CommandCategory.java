package dev.despical.particletext.command;

import dev.despical.particletext.ParticleTextPlugin;
import dev.despical.particletext.message.Var;
import dev.despical.particletext.render.RendererService;

import java.util.Locale;

abstract class CommandCategory {

    protected static final ParticleTextPlugin plugin = ParticleTextPlugin.getInstance();
    protected static final RendererService renderers = plugin.getRendererService();

    protected final void sendRendererNotFound(Arguments arguments, String id) {
        arguments.sendConfigured("renderer-not-found", Var.of("%id%", id));
    }

    protected final String normalizeId(String id) {
        return id.toLowerCase(Locale.ENGLISH);
    }

    protected final boolean validateTextLength(Arguments arguments, String text) {
        int maxLength = plugin.getSettingsManager().current().maxTextLength();

        if (text.length() <= maxLength) {
            return true;
        }

        arguments.sendConfigured("text-too-long", Var.of("%max%", maxLength));
        return false;
    }
}

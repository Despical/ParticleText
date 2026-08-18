package dev.despical.particletext.model;

import org.bukkit.Particle;

public record RendererData(
    String id,
    String text,
    Particle particle,
    double scale,
    boolean inverted,
    boolean enabled,
    FontSpec font,
    Rotation rotation,
    RendererLocation location
) {

    public RendererData withText(String value) {
        return new RendererData(id, value, particle, scale, inverted, enabled, font, rotation, location);
    }

    public RendererData withParticle(Particle value) {
        return new RendererData(id, text, value, scale, inverted, enabled, font, rotation, location);
    }

    public RendererData withScale(double value) {
        return new RendererData(id, text, particle, value, inverted, enabled, font, rotation, location);
    }

    public RendererData withEnabled(boolean value) {
        return new RendererData(id, text, particle, scale, inverted, value, font, rotation, location);
    }

    public RendererData withInverted(boolean value) {
        return new RendererData(id, text, particle, scale, value, enabled, font, rotation, location);
    }

    public RendererData withFont(FontSpec value) {
        return new RendererData(id, text, particle, scale, inverted, enabled, value, rotation, location);
    }

    public RendererData withRotation(Rotation value) {
        return new RendererData(id, text, particle, scale, inverted, enabled, font, value, location);
    }

    public RendererData withLocation(RendererLocation value) {
        return new RendererData(id, text, particle, scale, inverted, enabled, font, rotation, value);
    }
}

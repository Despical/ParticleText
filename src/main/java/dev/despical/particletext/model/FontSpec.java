package dev.despical.particletext.model;

import java.awt.Font;

public record FontSpec(String name, FontStyle style, int size) {

    public FontSpec {
        name = name == null || name.isBlank() ? Font.SANS_SERIF : name.trim();
        style = style == null ? FontStyle.PLAIN : style;
        size = Math.clamp(size, 4, 128);
    }

    public Font toFont() {
        return new Font(name, style.awtValue(), size);
    }
}

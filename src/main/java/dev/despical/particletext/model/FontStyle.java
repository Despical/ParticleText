package dev.despical.particletext.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import java.awt.Font;
import java.util.Locale;
import java.util.Optional;

@Getter
@RequiredArgsConstructor
public enum FontStyle {

    PLAIN(Font.PLAIN),
    BOLD(Font.BOLD),
    ITALIC(Font.ITALIC),
    BOLD_ITALIC(Font.BOLD | Font.ITALIC);

    @Accessors(fluent = true)
    private final int awtValue;

    public static Optional<FontStyle> find(String value) {
        if (value == null) {
            return Optional.empty();
        }

        try {
            return Optional.of(valueOf(value.toUpperCase(Locale.ENGLISH)));
        } catch (IllegalArgumentException _) {
            return Optional.empty();
        }
    }
}

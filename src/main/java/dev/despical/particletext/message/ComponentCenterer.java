package dev.despical.particletext.message;

import dev.despical.commons.miscellaneous.DefaultFontInfo;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextDecoration;

final class ComponentCenterer {

    private static final int CENTER_PX = 165;

    private ComponentCenterer() {
    }

    static Component center(Component component) {
        int messageWidth = measure(component);
        int spaceWidth = DefaultFontInfo.SPACE.getLength() + 1;
        int paddingSpaces = (CENTER_PX - messageWidth / 2) / spaceWidth;

        return Component.text(" ".repeat(Math.max(0, paddingSpaces))).append(component);
    }

    private static int measure(Component component) {
        int width = 0;

        if (component instanceof TextComponent textComponent) {
            boolean bold = component.style().hasDecoration(TextDecoration.BOLD);

            for (int index = 0; index < textComponent.content().length(); index++) {
                DefaultFontInfo fontInfo = DefaultFontInfo.getDefaultFontInfo(textComponent.content().charAt(index));
                width += bold ? fontInfo.getBoldLength() : fontInfo.getLength();
                width++;
            }
        }

        for (Component child : component.children()) {
            width += measure(child);
        }

        return width;
    }
}

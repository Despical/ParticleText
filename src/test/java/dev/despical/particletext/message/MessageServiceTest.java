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

package dev.despical.particletext.message;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MessageServiceTest {

    @Test
    void rendersPercentAndNativePlaceholdersAsLiteralUserText() {
        String input = "<click:run_command:'/op me'>hello</click>";
        var result = MessageService.format("%prefix%<green>%text%</green> <id>", "PT: ", token -> token,
            Var.of("%text%", input), Var.of("%id%", "test"));

        assertEquals("PT: " + input + " test", plain(result));
        assertFalse(hasClick(result));
    }

    @Test
    void escapesExternalPlaceholderOutput() {
        String input = "<click:run_command:'/op me'>hello</click>";
        var result = MessageService.format("%player_name%", "", token -> input);

        assertEquals(input, plain(result));
        assertFalse(hasClick(result));
    }

    @Test
    void keepsExplicitRichComponentsInteractive() {
        var link = Component.text("Teleport").clickEvent(ClickEvent.runCommand("/pt teleport test"));
        var result = MessageService.format("%action%", "", token -> token, Var.of("%action%", link));

        assertEquals("Teleport", plain(result));
        assertTrue(hasClick(result));
    }

    @Test
    void preservesReplacementCharactersInExternalOutput() {
        String input = "$10 \\ dollars <red>";
        var result = MessageService.format("<prefix>%external_value% %unknown%", "PT: ",
            token -> token.equals("%external_value%") ? input : token);

        assertEquals("PT: " + input + " %unknown%", plain(result));
        assertFalse(hasClick(result));
    }

    private String plain(Component component) {
        return PlainTextComponentSerializer.plainText().serialize(component);
    }

    private boolean hasClick(Component component) {
        return component.clickEvent() != null || component.children().stream().anyMatch(this::hasClick);
    }
}

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

package dev.despical.particletext.command;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BrigadierCommandsTest {

    @Test
    void preservesQuotedFontNamesAndText() throws Exception {
        assertArrayEquals(new String[] {"font", "test", "DejaVu Sans", "BOLD", "16"},
            BrigadierCommands.tokenize("font test \"DejaVu Sans\" BOLD 16"));
        assertArrayEquals(new String[] {"text", "test", "Welcome to the server"},
            BrigadierCommands.tokenize("text test \"Welcome to the server\""));
    }

    @Test
    void keepsUrlsAndMarkupTokensIntact() throws Exception {
        assertArrayEquals(new String[] {"text", "test", "<red>Welcome</red>", "https://example.com/?q=1&n=2"},
            BrigadierCommands.tokenize("text test <red>Welcome</red> https://example.com/?q=1&n=2"));
    }

    @Test
    void rejectsUnclosedQuotes() {
        assertThrows(Exception.class, () -> BrigadierCommands.tokenize("font test \"broken"));
    }
}

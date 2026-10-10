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

package dev.despical.particletext.papi;

import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.Nullable;

/**
 * Abstracts optional placeholder resolution from renderer and message services.
 * <p>
 * Implementations receive an optional offline-player context and return the resolved text. Shared renderer
 * content uses a null context; player messages may supply their recipient.
 * <p>
 * Callers decide the refresh frequency and formatting policy. The resolver does not publish renderer state
 * or interpret MiniMessage user input itself.
 *
 * @author Despical
 * <p>
 * Created at 10.10.2026
 */
@FunctionalInterface
public interface TextResolver {

    String resolve(@Nullable OfflinePlayer player, String text);

    static TextResolver passthrough() {
        return (_, text) -> text;
    }
}

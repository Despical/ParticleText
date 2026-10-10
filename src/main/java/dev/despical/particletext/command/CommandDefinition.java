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

import java.util.Arrays;
import java.util.Optional;

/**
 * Describes the permissions, arity, and sender rules for every public command.
 * <p>
 * The Aikar handlers, native Paper tree, and dispatcher share these action identities. Command usage and help
 * descriptions remain configurable message templates rather than presentation strings inside this enum.
 * <p>
 * The dispatcher checks permissions again when execution arrives through Aikar, so an alternative entry
 * path does not bypass rules enforced by the client command tree.
 *
 * @author Despical
 * <p>
 * Created at 10.10.2026
 */
public enum CommandDefinition {

    CREATE("create", "particletext.command.create", 2, 999, true),
    DELETE("delete", "particletext.command.delete", 1, 1, false),
    TPHERE("tphere", "particletext.command.teleport", 1, 1, true),
    MOVE("move", "particletext.command.edit", 2, 3, true),
    TEXT("text", "particletext.command.edit", 2, 999, false),
    SETSIZE("setsize", "particletext.command.edit", 2, 2, false),
    FONT("font", "particletext.command.edit", 4, 4, false),
    PARTICLE("particle", "particletext.command.edit", 2, 2, false),
    ENABLED("enabled", "particletext.command.edit", 2, 2, false),
    INVERTED("inverted", "particletext.command.edit", 2, 2, false),
    ROTATE("rotate", "particletext.command.edit", 3, 3, false),
    RELOAD("reload", "particletext.command.reload", 0, 0, false),
    LIST("list", "particletext.command.list", 0, 1, false),
    MENU("menu", "particletext.command.menu", 0, 0, true),
    TELEPORT("teleport", "particletext.command.teleport", 1, 1, true),
    HELP("help", "particletext.command.help", 0, 1, false),
    VERSION("version", "particletext.command.version", 0, 0, false),
    INFO("info", "particletext.command.info", 1, 1, false),
    FONTS("fonts", "particletext.command.edit", 0, 0, false);

    private final String action;
    private final String permission;
    private final int min;
    private final int max;
    private final boolean playerOnly;

    CommandDefinition(String action, String permission, int min, int max, boolean playerOnly) {
        this.action = action;
        this.permission = permission;
        this.min = min;
        this.max = max;
        this.playerOnly = playerOnly;
    }

    public String action() {
        return action;
    }

    public String permission() {
        return permission;
    }

    public int min() {
        return min;
    }

    public int max() {
        return max;
    }

    public boolean playerOnly() {
        return playerOnly;
    }

    public static Optional<CommandDefinition> find(String name) {
        return Arrays.stream(values()).filter(definition -> definition.action.equalsIgnoreCase(name)).findFirst();
    }
}

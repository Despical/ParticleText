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

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import dev.despical.particletext.model.FontStyle;
import dev.despical.particletext.model.ParticleSupport;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * Builds typed arguments and permission-aware suggestions for the public command.
 * <p>
 * Execution is supplied by the ACF adapter. Renderer and font suppliers are read
 * when completing, so reloads and renderer edits do not leave stale suggestions.
 * Font completions quote and escape names before inserting them into the command.
 *
 * @author Despical
 * <p>
 * Created at 17.10.2026
 */
final class ParticleTextCommandTree {

    private final Command<CommandSourceStack> executor;
    private final Supplier<List<String>> rendererIds;
    private final Supplier<List<String>> fontNames;

    ParticleTextCommandTree(Command<CommandSourceStack> executor, Supplier<List<String>> rendererIds,
        Supplier<List<String>> fontNames) {
        this.executor = executor;
        this.rendererIds = rendererIds;
        this.fontNames = fontNames;
    }

    LiteralArgumentBuilder<CommandSourceStack> build() {
        var root = Commands.literal("pt").executes(executor);

        for (var definition : CommandDefinition.values()) {
            var action = Commands.literal(definition.action())
                .requires(source -> source.getSender().hasPermission(definition.permission())).executes(executor);

            switch (definition) {
                case CREATE -> action.then(word("id").then(greedy("text")));
                case DELETE, TELEPORT, TPHERE, INFO -> action.then(names());
                case TEXT -> action.then(names().then(greedy("text")));
                case SETSIZE -> action.then(names().then(number("scale", 0.01, 10)));
                case FONT -> action.then(names().then(font()));
                case PARTICLE -> action.then(names().then(choices("particle", ParticleSupport::names)));
                case ENABLED, INVERTED -> action.then(names().then(Commands.argument("enabled", BoolArgumentType.bool())
                    .suggests((context, builder) -> suggest(context, builder, () -> List.of("true", "false"), false))
                    .executes(executor)));
                case ROTATE -> action.then(names().then(choices("axis", () -> List.of("x", "y", "z"))
                    .then(Commands.argument("angle", DoubleArgumentType.doubleArg()).executes(executor))));
                case MOVE -> action.then(names().then(choices("direction", () -> Arrays.stream(MoveDirection.values())
                    .map(MoveDirection::displayName).toList()).then(number("amount", 0.01, 100))));
                case LIST, HELP -> action.then(Commands.argument("page", IntegerArgumentType.integer(1))
                    .executes(executor));
                default -> {
                }
            }

            root.then(action);
        }

        root.then(Commands.argument("unknown", StringArgumentType.greedyString()).executes(executor));
        return root;
    }

    private RequiredArgumentBuilder<CommandSourceStack, String> names() {
        return choices("id", rendererIds);
    }

    private RequiredArgumentBuilder<CommandSourceStack, String> word(String name) {
        return Commands.argument(name, StringArgumentType.word()).executes(executor);
    }

    private RequiredArgumentBuilder<CommandSourceStack, String> greedy(String name) {
        return Commands.argument(name, StringArgumentType.greedyString()).executes(executor);
    }

    private RequiredArgumentBuilder<CommandSourceStack, Double> number(String name, double min, double max) {
        return Commands.argument(name, DoubleArgumentType.doubleArg(min, max)).executes(executor);
    }

    private RequiredArgumentBuilder<CommandSourceStack, String> font() {
        return Commands.argument("font", StringArgumentType.string())
            .suggests((context, builder) -> suggest(context, builder, fontNames, true))
            .executes(executor).then(choices("style", () -> Arrays.stream(FontStyle.values()).map(Enum::name).toList())
                .then(Commands.argument("size", IntegerArgumentType.integer(4, 128)).executes(executor)));
    }

    private RequiredArgumentBuilder<CommandSourceStack, String> choices(String name, Supplier<List<String>> values) {
        return word(name).suggests((context, builder) -> suggest(context, builder, values, false));
    }

    private CompletableFuture<Suggestions> suggest(CommandContext<CommandSourceStack> context,
        SuggestionsBuilder builder, Supplier<List<String>> values, boolean quoted) {
        if (context.getSource().getSender().hasPermission("particletext.command.tabcomplete")) {
            String remaining = builder.getRemainingLowerCase();
            if (quoted && remaining.startsWith("\"")) {
                remaining = remaining.substring(1);
            }

            String prefix = remaining;
            values.get().stream().filter(value -> value.toLowerCase(Locale.ROOT).startsWith(prefix))
                .map(value -> quoted ? "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\"" : value)
                .forEach(builder::suggest);
        }

        return builder.buildFuture();
    }
}

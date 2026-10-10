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

import co.aikar.commands.BukkitCommandManager;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import dev.despical.particletext.ParticleTextPlugin;
import dev.despical.particletext.model.FontStyle;
import dev.despical.particletext.model.ParticleSupport;
import dev.despical.particletext.model.RendererData;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Publishes native Paper command trees backed by Aikar execution.
 * <p>
 * The client receives typed arguments, dynamic renderer suggestions, and
 * permission-aware subcommands through the Paper lifecycle API. Quoted strings
 * survive forwarding to the framework-independent operation handlers.
 * <p>
 * The temporary ACF command registration is detached from the public command map.
 * Its executor remains the server-side adapter; closing unregisters the manager
 * when the owning plugin shuts down.
 *
 * @author Despical
 * <p>
 * Created at 10.10.2026
 */
public final class BrigadierCommands implements AutoCloseable {

    private final ParticleTextPlugin plugin;
    private final BukkitCommandManager manager;
    private final Command executor;

    public BrigadierCommands(ParticleTextPlugin plugin) {
        this.plugin = plugin;

        manager = new BukkitCommandManager(plugin);
        manager.registerCommand(new ParticleTextCommands(plugin));
        executor = Objects.requireNonNull(Bukkit.getCommandMap().getCommand("__particletext"));
        manager.unregisterCommands();

        plugin.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS,
            event -> event.registrar().register(tree().build(), plugin.getMessages().raw("command-description"),
                List.of("particletext")));
    }

    public LiteralArgumentBuilder<CommandSourceStack> tree() {
        var root = Commands.literal("pt").executes(this::run);

        for (var definition : CommandDefinition.values()) {
            var action = Commands.literal(definition.action())
                .requires(source -> source.getSender().hasPermission(definition.permission())).executes(this::run);

            switch (definition) {
                case CREATE -> action.then(word("id").then(greedy("text")));
                case DELETE, TELEPORT, TPHERE, INFO -> action.then(names());
                case TEXT -> action.then(names().then(greedy("text")));
                case SETSIZE -> action.then(names().then(number("scale", 0.01, 10)));
                case FONT -> action.then(names().then(Commands.argument("font", StringArgumentType.string())
                    .executes(this::run).then(choices("style",
                        () -> Arrays.stream(FontStyle.values()).map(Enum::name).toList())
                        .then(Commands.argument("size", IntegerArgumentType.integer(4, 128)).executes(this::run)))));
                case PARTICLE -> action.then(names().then(choices("particle", ParticleSupport::names)));
                case ENABLED, INVERTED -> action.then(names().then(Commands.argument("enabled", BoolArgumentType.bool())
                    .executes(this::run)));
                case ROTATE -> action.then(names().then(choices("axis", () -> List.of("x", "y", "z"))
                    .then(Commands.argument("angle", DoubleArgumentType.doubleArg()).executes(this::run))));
                case MOVE -> action.then(names().then(choices("direction", () -> Arrays.stream(MoveDirection.values())
                    .map(MoveDirection::displayName).toList()).then(number("amount", 0.01, 100))));
                case LIST, HELP -> action.then(Commands.argument("page", IntegerArgumentType.integer(1))
                    .executes(this::run));
                default -> {
                }
            }

            root.then(action);
        }

        root.then(Commands.argument("unknown", StringArgumentType.greedyString()).executes(this::run));
        return root;
    }

    private RequiredArgumentBuilder<CommandSourceStack, String> names() {
        return choices("id", () -> plugin.getRendererService().all().stream().map(RendererData::id).toList());
    }

    private RequiredArgumentBuilder<CommandSourceStack, String> word(String name) {
        return Commands.argument(name, StringArgumentType.word()).executes(this::run);
    }

    private RequiredArgumentBuilder<CommandSourceStack, String> greedy(String name) {
        return Commands.argument(name, StringArgumentType.greedyString()).executes(this::run);
    }

    private RequiredArgumentBuilder<CommandSourceStack, Double> number(String name, double min, double max) {
        return Commands.argument(name, DoubleArgumentType.doubleArg(min, max)).executes(this::run);
    }

    private RequiredArgumentBuilder<CommandSourceStack, String> choices(String name, Supplier<List<String>> values) {
        return word(name).suggests((context, builder) -> {
            if (context.getSource().getSender().hasPermission("particletext.command.tabcomplete")) {
                String remaining = builder.getRemainingLowerCase();
                values.get().stream().filter(value -> value.toLowerCase(Locale.ROOT).startsWith(remaining))
                    .forEach(builder::suggest);
            }

            return builder.buildFuture();
        });
    }

    private int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        String input = context.getInput();
        int separator = input.indexOf(' ');

        String[] arguments = separator < 0 ? new String[0] : tokenize(input.substring(separator + 1));
        executor.execute(context.getSource().getSender(), "__particletext", arguments);
        return 1;
    }

    static String[] tokenize(String input) throws CommandSyntaxException {
        var reader = new StringReader(input);
        var values = new ArrayList<String>();

        while (reader.canRead()) {
            reader.skipWhitespace();

            if (!reader.canRead()) {
                break;
            }

            if (StringReader.isQuotedStringStart(reader.peek())) {
                values.add(reader.readQuotedString());
            } else {
                int start = reader.getCursor();

                while (reader.canRead() && !Character.isWhitespace(reader.peek())) {
                    reader.skip();
                }

                values.add(input.substring(start, reader.getCursor()));
            }
        }

        return values.toArray(String[]::new);
    }

    @Override
    public void close() {
        manager.unregisterCommands();
    }
}

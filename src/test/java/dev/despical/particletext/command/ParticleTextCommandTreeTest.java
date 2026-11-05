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
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package dev.despical.particletext.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.tree.LiteralCommandNode;

import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Subcommand;

import io.papermc.paper.command.brigadier.CommandSourceStack;

import org.bukkit.command.CommandSender;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ParticleTextCommandTreeTest {

    @Test
    void keepsAcfPermissionsAlignedWithThePublicTree() {
        var actions = new ArrayList<String>();
        for (var method : ParticleTextCommands.class.getDeclaredMethods()) {
            var subcommand = method.getAnnotation(Subcommand.class);
            if (subcommand == null) {
                continue;
            }

            var definition = CommandDefinition.find(subcommand.value()).orElseThrow();
            assertEquals(definition.permission(), method.getAnnotation(CommandPermission.class).value());
            actions.add(subcommand.value());
        }

        assertEquals(Arrays.stream(CommandDefinition.values()).map(CommandDefinition::action).sorted().toList(),
            actions.stream().sorted().toList());
    }

    @Test
    void exposesOnlyPermittedSubcommandsInClientUsage() {
        var dispatcher = dispatcher(new ArrayList<>(), List.of());
        var root = dispatcher.getRoot().getChild("pt");
        assertEquals(List.of("version"), dispatcher.getSmartUsage(root, source("particletext.command.version"))
            .keySet().stream().filter(LiteralCommandNode.class::isInstance).map(node -> node.getName()).toList());
        assertTrue(dispatcher.getSmartUsage(root, source()).keySet().stream()
            .noneMatch(LiteralCommandNode.class::isInstance));
    }

    @Test
    void readsCurrentRendererIdsAndFiltersPrefixesWithoutCaseSensitivity() throws Exception {
        var ids = new ArrayList<>(List.of("spawn", "welcome"));
        var dispatcher = dispatcher(ids, List.of());
        var source = source("particletext.command.info", "particletext.command.tabcomplete");
        assertEquals(List.of("welcome"), suggestions(dispatcher, "pt info W", source));

        ids.clear();
        ids.add("new_spawn");
        assertEquals(List.of("new_spawn"), suggestions(dispatcher, "pt info ", source));
        assertTrue(suggestions(dispatcher, "pt info ", source("particletext.command.info")).isEmpty());
    }

    @Test
    void appliesCompletionPermissionToBooleansAndOtherArgumentChoices() throws Exception {
        var dispatcher = dispatcher(List.of("spawn"), List.of("DejaVu Sans"));
        var denied = source("particletext.command.edit");
        var allowed = source("particletext.command.edit", "particletext.command.tabcomplete");
        for (String input : List.of("pt enabled spawn ", "pt inverted spawn ", "pt move spawn ",
            "pt font spawn ", "pt font spawn SansSerif ", "pt particle spawn ", "pt rotate spawn ")) {
            assertTrue(suggestions(dispatcher, input, denied).isEmpty(), input);
            assertFalse(suggestions(dispatcher, input, allowed).isEmpty(), input);
        }

        assertEquals(List.of("false", "true"), suggestions(dispatcher, "pt enabled spawn ", allowed));
        assertEquals(List.of("true"), suggestions(dispatcher, "pt enabled spawn t", allowed));
    }

    @Test
    void insertsEscapedQuotedFontNamesThatCanBeExecuted() throws Exception {
        String font = "A \"Display\" \\ Font";
        var dispatcher = dispatcher(List.of("spawn"), List.of("DejaVu Sans", font));
        var source = source("particletext.command.edit", "particletext.command.tabcomplete");
        assertEquals(List.of("\"DejaVu Sans\""), suggestions(dispatcher, "pt font spawn Dej", source));
        assertEquals(List.of("\"DejaVu Sans\""), suggestions(dispatcher, "pt font spawn \"Dej", source));

        String input = "pt font spawn A";
        var completion = dispatcher.getCompletionSuggestions(dispatcher.parse(input, source)).get().getList().getFirst();
        String completed = completion.apply(input) + " BOLD 24";
        assertEquals(font, dispatcher.parse(completed, source).getContext().getArguments().get("font").getResult());
        assertEquals(1, dispatcher.execute(completed, source));
        assertArrayEquals(new String[] {"font", "spawn", font, "BOLD", "24"},
            BrigadierCommands.tokenize(completed.substring(3)));
    }

    @Test
    void preservesCommandInputsAndFallbackValidation() throws Exception {
        var inputs = new ArrayList<String>();
        var dispatcher = new CommandDispatcher<CommandSourceStack>();
        dispatcher.register(new ParticleTextCommandTree(context -> {
            inputs.add(context.getInput());
            return 1;
        }, () -> List.of("spawn"), () -> List.of("DejaVu Sans")).build());
        var source = source(Arrays.stream(CommandDefinition.values()).map(CommandDefinition::permission)
            .distinct().toArray(String[]::new));
        var commands = List.of("pt", "pt create welcome Hello world", "pt font spawn \"DejaVu Sans\" BOLD 24",
            "pt move spawn forward", "pt move spawn forward 1.5", "pt enabled spawn true", "pt list 2",
            "pt font spawn", "pt unrecognized");
        for (String input : commands) {
            assertEquals(1, dispatcher.execute(input, source), input);
        }

        assertEquals(commands, inputs);
        for (String input : List.of("pt setsize spawn NaN", "pt setsize spawn 0", "pt list 0")) {
            assertThrows(CommandSyntaxException.class, () -> dispatcher.execute(input, source), input);
        }
    }

    private CommandDispatcher<CommandSourceStack> dispatcher(List<String> ids, List<String> fonts) {
        var dispatcher = new CommandDispatcher<CommandSourceStack>();
        dispatcher.register(new ParticleTextCommandTree(_ -> 1, () -> ids, () -> fonts).build());
        return dispatcher;
    }

    private List<String> suggestions(CommandDispatcher<CommandSourceStack> dispatcher, String input,
        CommandSourceStack source) throws Exception {
        return dispatcher.getCompletionSuggestions(dispatcher.parse(input, source)).get().getList().stream()
            .map(Suggestion::getText).toList();
    }

    private CommandSourceStack source(String... permissions) {
        var granted = Set.of(permissions);
        var sender = (CommandSender) Proxy.newProxyInstance(getClass().getClassLoader(),
            new Class<?>[] {CommandSender.class}, (_, method, arguments) -> {
                if (method.getName().equals("hasPermission")) {
                    return granted.contains(arguments[0]);
                }

                throw new UnsupportedOperationException(method.getName());
            });
        return (CommandSourceStack) Proxy.newProxyInstance(getClass().getClassLoader(),
            new Class<?>[] {CommandSourceStack.class}, (_, method, _) -> {
                if (method.getName().equals("getSender")) {
                    return sender;
                }

                throw new UnsupportedOperationException(method.getName());
            });
    }
}

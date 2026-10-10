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

import dev.despical.particletext.ParticleTextPlugin;
import dev.despical.particletext.config.ConfigurationFiles;
import dev.despical.particletext.papi.TextResolver;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.map.MinecraftFont;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Loads immutable MiniMessage templates for chat and inventory presentation.
 * <p>
 * Missing keys fall back to bundled templates in memory. A replacement map is
 * published only after all message value types have been checked, allowing
 * invalid configuration to leave the active presentation intact.
 * <p>
 * User content and external placeholder output stay literal during formatting.
 * Explicit Adventure components carry trusted action buttons and hover details
 * without exposing user-controlled markup.
 *
 * @author Despical
 * <p>
 * Created at 10.10.2026
 */
public final class MessageService {

    private static final Pattern TOKEN = Pattern.compile("%([a-zA-Z0-9_:.-]+)%");

    private final ParticleTextPlugin plugin;
    private final TextResolver textResolver;
    private Map<String, Object> values;

    public MessageService(ParticleTextPlugin plugin, TextResolver textResolver) {
        this.plugin = plugin;
        this.textResolver = textResolver;

        apply(load());
    }

    public Map<String, Object> load() {
        var yaml = ConfigurationFiles.read(plugin, "messages.yml");
        var result = new HashMap<String, Object>();

        for (String key : yaml.getKeys(true)) {
            Object value = yaml.get(key);

            if (yaml.isConfigurationSection(key)) {
                continue;
            }

            if (value instanceof String text) {
                MiniMessage.miniMessage().deserialize(text);
                result.put(key, text);
            } else if (value instanceof List<?> list && list.stream().allMatch(String.class::isInstance)) {
                for (Object line : list) {
                    MiniMessage.miniMessage().deserialize(line.toString());
                }

                result.put(key, List.copyOf(list));
            } else {
                throw new IllegalArgumentException("Message must be text or a list of text: " + key);
            }
        }

        return Map.copyOf(result);
    }

    public void apply(Map<String, Object> replacement) {
        values = Map.copyOf(replacement);
    }

    public void send(CommandSender recipient, String path, Var... variables) {
        String configured = raw(path);

        if (!configured.isEmpty()) {
            recipient.sendMessage(parse(recipient instanceof Player player ? player : null, configured, variables));
        }
    }

    public void sendList(CommandSender recipient, String path, Var... variables) {
        for (String line : lines(path)) {
            boolean centered = line.startsWith("%center%");

            var component = parse(recipient instanceof Player player ? player : null,
                centered ? line.substring(8) : line, variables);
            recipient.sendMessage(centered ? center(component) : component);
        }
    }

    public Component parse(String raw, Var... variables) {
        return parse(null, raw, variables);
    }

    public Component parse(Player player, String raw, Var... variables) {
        return format(raw, raw("prefix"), token -> textResolver.resolve(player, token), variables);
    }

    public List<Component> parseList(Player player, String path, Var... variables) {
        return lines(path).stream().map(line -> parse(player, line, variables)).toList();
    }

    @SuppressWarnings("unchecked")
    public List<String> lines(String path) {
        Object value = values.get(path);
        return value instanceof List<?> ? (List<String>) value : List.of();
    }

    public String raw(String path) {
        Object value = values.get(path);
        return value instanceof String text ? text : "";
    }

    static Component format(String raw, String prefix, UnaryOperator<String> external, Var... variables) {
        var parser = MiniMessage.miniMessage();
        var builder = TagResolver.builder().resolver(Placeholder.parsed("prefix", prefix));
        var supplied = new HashSet<String>();

        for (Var variable : variables) {
            String name = variable.name().replace("%", "");
            supplied.add(name);

            builder.resolver(variable.value() instanceof Component component
                ? Placeholder.component(name, component)
                : Placeholder.unparsed(name, String.valueOf(variable.value())));
        }

        String template = TOKEN.matcher(raw.replace("%prefix%", prefix)).replaceAll(match -> {
            if (supplied.contains(match.group(1))) {
                return "<" + match.group(1) + ">";
            }

            String token = match.group();
            String resolved = external.apply(token);

            return Matcher.quoteReplacement(token.equals(resolved) ? token : parser.escapeTags(resolved));
        });

        return parser.deserialize(template, builder.build());
    }

    private static Component center(Component component) {
        String text = PlainTextComponentSerializer.plainText().serialize(component);
        int width = 0;

        for (char character : text.toCharArray()) {
            var glyph = MinecraftFont.Font.getChar(character);
            width += glyph == null ? 6 : glyph.getWidth() + 1;
        }

        return Component.text(" ".repeat(Math.max(0, (165 - width / 2) / 4))).append(component);
    }
}

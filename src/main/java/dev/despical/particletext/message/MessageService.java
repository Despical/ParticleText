package dev.despical.particletext.message;

import dev.despical.commons.configuration.ConfigUtils;
import dev.despical.particletext.ParticleTextPlugin;
import dev.despical.particletext.papi.TextResolver;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Objects;

public final class MessageService {

    private static final String CENTER_PREFIX = "%center%";

    private final ParticleTextPlugin plugin;
    private final TextResolver textResolver;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private FileConfiguration configuration;
    private String prefix;

    public MessageService(ParticleTextPlugin plugin, TextResolver textResolver) {
        this.plugin = plugin;
        this.textResolver = textResolver;
        reload();
    }

    public void reload() {
        configuration = Objects.requireNonNull(ConfigUtils.getConfig(plugin, "messages"));
        prefix = configuration.getString("prefix", "");
    }

    public void send(CommandSender recipient, String path, Var... variables) {
        String configured = configuration.getString(path, "");

        if (!configured.isEmpty()) {
            recipient.sendMessage(parse(recipient instanceof Player player ? player : null, configured, variables));
        }
    }

    public void sendList(CommandSender recipient, String path, Var... variables) {
        configuration.getStringList(path).stream()
            .map(line -> parseListLine(recipient instanceof Player player ? player : null, line, variables))
            .forEach(recipient::sendMessage);
    }

    public Component parse(String raw, Var... variables) {
        return parse(null, raw, variables);
    }

    public Component parse(Player player, String raw, Var... variables) {
        String resolved = replace(raw, variables);
        resolved = textResolver.resolve(player, resolved);

        return miniMessage.deserialize(resolved);
    }

    public List<Component> parseList(Player player, String path, Var... variables) {
        return configuration.getStringList(path).stream()
            .map(line -> parse(player, line, variables))
            .toList();
    }

    public String raw(String path) {
        return configuration.getString(path, "");
    }

    private Component parseListLine(Player player, String line, Var... variables) {
        boolean centered = line.startsWith(CENTER_PREFIX);
        Component component = parse(player, centered ? line.substring(CENTER_PREFIX.length()) : line, variables);

        return centered ? ComponentCenterer.center(component) : component;
    }

    private String replace(String raw, Var... variables) {
        String value = raw.replace("%prefix%", prefix);

        for (Var variable : variables) {
            value = value.replace(variable.name(), String.valueOf(variable.value()));
        }

        return value;
    }
}

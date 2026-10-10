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

import dev.despical.particletext.ParticleTextPlugin;
import dev.despical.particletext.message.Var;
import dev.despical.particletext.model.FontSpec;
import dev.despical.particletext.model.FontStyle;
import dev.despical.particletext.model.ParticleSupport;
import dev.despical.particletext.model.RendererData;
import dev.despical.particletext.model.RendererLocation;
import dev.despical.particletext.render.RendererService;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * Creates, edits, and moves text renderers through the persistent renderer service.
 * <p>
 * The dispatcher validates permissions, sender type, and arity before invoking these operations.
 * Content and transform validation remain beside the corresponding writes.
 *
 * @author Despical
 * <p>
 * Created at 10.10.2026
 */
final class RendererCommands {

    private static final Pattern VALID_ID = Pattern.compile("[a-z0-9_-]{1,32}");

    private final ParticleTextPlugin plugin;
    private final RendererService renderers;

    RendererCommands(ParticleTextPlugin plugin) {
        this.plugin = plugin;

        renderers = plugin.getRendererService();
    }

    public void create(CommandSender sender, String[] args) {
        String id = args[0];

        if (!VALID_ID.matcher(id).matches()) {
            send(sender, "invalid-id");
            return;
        }

        if (renderers.find(id).isPresent()) {
            send(sender, "renderer-exists", Var.of("%id%", id));
            return;
        }

        String text = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

        if (!validateTextLength(sender, text)) {
            return;
        }

        Player player = (Player) sender;
        renderers.create(id, text, player.getLocation());

        send(sender, "renderer-created", Var.of("%id%", id));
        plugin.getPanels().info(sender, id);
    }

    public void delete(CommandSender sender, String[] args) {
        String id = args[0].toLowerCase(Locale.ROOT);

        if (!renderers.delete(id)) {
            sendRendererNotFound(sender, id);
            return;
        }

        send(sender, "renderer-deleted", Var.of("%id%", id));
    }

    public void text(CommandSender sender, String[] args) {
        String id = args[0].toLowerCase(Locale.ROOT);
        String text = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

        if (!validateTextLength(sender, text)) {
            return;
        }

        if (unchanged(sender, id, data -> data.text().equals(text),
            "renderer-text-unchanged")) {
            return;
        }

        if (renderers.updateText(id, text).isEmpty()) {
            sendRendererNotFound(sender, id);
            return;
        }

        send(sender, "renderer-text", Var.of("%id%", id));
    }

    public void setSize(CommandSender sender, String[] args) {
        String rawScale = args[1];

        double scale = Double.parseDouble(args[1]);

        if (!Double.isFinite(scale)) {
            send(sender, "invalid-number", Var.of("%value%", rawScale));
            return;
        }

        if (scale < 0.01 || scale > 10.0) {
            send(sender, "invalid-size");
            return;
        }

        String id = args[0].toLowerCase(Locale.ROOT);

        if (unchanged(sender, id, data -> Double.compare(data.scale(), scale) == 0,
            "renderer-size-unchanged")) {
            return;
        }

        if (renderers.updateScale(id, scale).isEmpty()) {
            sendRendererNotFound(sender, id);
            return;
        }

        send(sender, "renderer-size", Var.of("%id%", id),
            Var.of("%size%", String.format(Locale.US, "%.2f", scale)));
    }

    public void font(CommandSender sender, String[] args) {
        Optional<FontStyle> style = FontStyle.find(args[2]);

        if (style.isEmpty()) {
            send(sender, "invalid-font");
            return;
        }

        int size = Integer.parseInt(args[3]);

        if (size < 4 || size > 128) {
            send(sender, "invalid-font");
            return;
        }

        String id = args[0].toLowerCase(Locale.ROOT);
        FontSpec font = new FontSpec(args[1], style.get(), size);

        if (unchanged(sender, id, data -> data.font().equals(font),
            "renderer-font-unchanged")) {
            return;
        }

        if (renderers.updateFont(id, font).isEmpty()) {
            sendRendererNotFound(sender, id);
            return;
        }

        send(sender, "renderer-font", Var.of("%id%", id), Var.of("%font%", font.name()),
            Var.of("%style%", font.style().name()), Var.of("%size%", font.size()));
    }

    public void particle(CommandSender sender, String[] args) {
        String requested = args[1];
        var particle = ParticleSupport.find(requested);

        if (particle.isEmpty()) {
            send(sender, "invalid-particle", Var.of("%particle%", requested));
            return;
        }

        String id = args[0].toLowerCase(Locale.ROOT);

        if (unchanged(sender, id, data -> data.particle() == particle.get(),
            "renderer-particle-unchanged")) {
            return;
        }

        if (renderers.updateParticle(id, particle.get()).isEmpty()) {
            sendRendererNotFound(sender, id);
            return;
        }

        send(sender, "renderer-particle", Var.of("%id%", id),
            Var.of("%particle%", particle.get().name()));
    }

    public void enabled(CommandSender sender, String[] args) {
        String rawValue = args[1];

        if (!("true".equalsIgnoreCase(rawValue) || "false".equalsIgnoreCase(rawValue))) {
            send(sender, "correct-usage",
                Var.of("%usage%", plugin.getMessages().raw("usage.enabled").replace("%label%", "pt")));
            return;
        }

        String id = args[0].toLowerCase(Locale.ROOT);
        boolean value = Boolean.parseBoolean(args[1]);

        if (unchanged(sender, id, data -> data.enabled() == value,
            value ? "renderer-already-enabled" : "renderer-already-disabled")) {
            return;
        }

        if (renderers.updateEnabled(id, value).isEmpty()) {
            sendRendererNotFound(sender, id);
            return;
        }

        send(sender, value ? "renderer-enabled" : "renderer-disabled", Var.of("%id%", id));
    }

    public void inverted(CommandSender sender, String[] args) {
        String rawValue = args[1];

        if (!("true".equalsIgnoreCase(rawValue) || "false".equalsIgnoreCase(rawValue))) {
            send(sender, "correct-usage",
                Var.of("%usage%", plugin.getMessages().raw("usage.inverted").replace("%label%", "pt")));
            return;
        }

        String id = args[0].toLowerCase(Locale.ROOT);
        boolean value = Boolean.parseBoolean(args[1]);

        if (unchanged(sender, id, data -> data.inverted() == value,
            "renderer-inverted-unchanged")) {
            return;
        }

        if (renderers.updateInverted(id, value).isEmpty()) {
            sendRendererNotFound(sender, id);
            return;
        }

        send(sender, "renderer-inverted", Var.of("%id%", id), Var.of("%inverted%", value));
    }

    public void teleportHere(CommandSender sender, String[] args) {
        String id = args[0].toLowerCase(Locale.ROOT);
        Player player = (Player) sender;

        if (unchanged(sender, id, data -> data.location().equals(RendererLocation.from(player.getLocation())),
            "renderer-location-unchanged")) {
            return;
        }

        if (renderers.updateLocation(id, player.getLocation()).isEmpty()) {
            sendRendererNotFound(sender, id);
            return;
        }

        send(sender, "renderer-moved", Var.of("%id%", id));
    }

    public void move(CommandSender sender, String[] args) {
        Optional<MoveDirection> direction = MoveDirection.find(args[1]);

        if (direction.isEmpty()) {
            send(sender, "invalid-direction");
            return;
        }

        double amount = plugin.getSettingsManager().current().defaultMoveAmount();

        if (args.length == 3) {
            String rawAmount = args[2];

            amount = Double.parseDouble(rawAmount);

            if (!Double.isFinite(amount)) {
                send(sender, "invalid-number", Var.of("%value%", rawAmount));
                return;
            }
        }

        if (amount < 0.01 || amount > 100.0) {
            send(sender, "invalid-move-amount");
            return;
        }

        String id = args[0].toLowerCase(Locale.ROOT);
        Optional<RendererData> renderer = renderers.find(id);

        if (renderer.isEmpty()) {
            sendRendererNotFound(sender, id);
            return;
        }

        var location = renderer.get().location().toBukkitLocation();

        if (location == null) {
            send(sender, "world-unavailable", Var.of("%world%", renderer.get().location().world()));
            return;
        }

        Player player = (Player) sender;
        MoveDirection moveDirection = direction.get();
        MoveDirection.Offset offset = moveDirection.offset(player.getLocation().getYaw(), amount);
        location.add(offset.x(), offset.y(), offset.z());
        renderers.updateLocation(id, location);

        send(sender, "renderer-shifted", Var.of("%id%", id),
            Var.of("%direction%", moveDirection.displayName()),
            Var.of("%amount%", String.format(Locale.US, "%.2f", amount)));
    }

    public void rotate(CommandSender sender, String[] args) {
        String rawAxis = args[1];

        if (rawAxis.length() != 1 || "xyz".indexOf(Character.toLowerCase(rawAxis.charAt(0))) < 0) {
            send(sender, "invalid-axis");
            return;
        }

        String rawAngle = args[2];

        double angle = Double.parseDouble(args[2]);

        if (!Double.isFinite(angle)) {
            send(sender, "invalid-number", Var.of("%value%", rawAngle));
            return;
        }

        String id = args[0].toLowerCase(Locale.ROOT);
        char axis = Character.toLowerCase(rawAxis.charAt(0));

        if (unchanged(sender, id, data -> data.rotation().equals(data.rotation().withAxis(axis, angle)),
            "renderer-rotation-unchanged", Var.of("%axis%", axis))) {
            return;
        }

        if (renderers.updateRotation(id, axis, angle).isEmpty()) {
            sendRendererNotFound(sender, id);
            return;
        }

        send(sender, "renderer-rotated", Var.of("%id%", id), Var.of("%axis%", axis),
            Var.of("%angle%", String.format(Locale.US, "%.2f", angle)));
    }

    public void teleport(CommandSender sender, String[] args) {
        String id = args[0].toLowerCase(Locale.ROOT);
        Optional<RendererData> data = renderers.find(id);

        if (data.isEmpty()) {
            sendRendererNotFound(sender, id);
            return;
        }

        Location location = data.get().location().toBukkitLocation();

        if (location == null) {
            send(sender, "world-unavailable", Var.of("%world%", data.get().location().world()));
            return;
        }

        Player player = (Player) sender;

        if (RendererLocation.from(player.getLocation()).equals(data.get().location())) {
            send(sender, "renderer-location-unchanged", Var.of("%id%", id));
            return;
        }

        if (!player.teleport(location)) {
            send(sender, "teleport-failed", Var.of("%id%", id));
            return;
        }

        send(sender, "renderer-teleported", Var.of("%id%", id));
    }

    private boolean unchanged(CommandSender sender, String id, Predicate<RendererData> matches, String key,
        Var... variables) {
        var renderer = renderers.find(id);

        if (renderer.isEmpty()) {
            sendRendererNotFound(sender, id);
            return true;
        }

        if (matches.test(renderer.get())) {
            Var[] context = Arrays.copyOf(variables, variables.length + 1);
            context[variables.length] = Var.of("%id%", id);
            send(sender, key, context);
            return true;
        }

        return false;
    }

    private void sendRendererNotFound(CommandSender sender, String id) {
        send(sender, "renderer-not-found", Var.of("%id%", id));
    }

    private boolean validateTextLength(CommandSender sender, String text) {
        int maxLength = plugin.getSettingsManager().current().maxTextLength();
        if (text.length() <= maxLength) {
            return true;
        }
        send(sender, "text-too-long", Var.of("%max%", maxLength));
        return false;
    }

    private void send(CommandSender sender, String key, Var... variables) {
        plugin.getMessages().send(sender, key, variables);
    }
}

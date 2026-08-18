package dev.despical.particletext.command;

import dev.despical.commandframework.annotations.Command;
import dev.despical.particletext.message.Var;
import dev.despical.particletext.model.FontSpec;
import dev.despical.particletext.model.FontStyle;
import dev.despical.particletext.model.ParticleSupport;
import dev.despical.particletext.model.RendererData;
import org.bukkit.entity.Player;

import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

public final class AdminCommands extends CommandCategory {

    private static final Pattern VALID_ID = Pattern.compile("[a-z0-9_-]{1,32}");

    @Command(
        name = "pt.create",
        aliases = "particletext.create",
        permission = "particletext.command.create",
        usage = "/%label% create <id> <text>",
        min = 2,
        senderType = Command.SenderType.PLAYER
    )
    public void create(Arguments arguments) {
        String id = arguments.getFirst();

        if (!VALID_ID.matcher(id).matches()) {
            arguments.sendConfigured("invalid-id");
            return;
        }

        if (renderers.find(id).isPresent()) {
            arguments.sendConfigured("renderer-exists", Var.of("%id%", id));
            return;
        }

        String text = arguments.concatRangeOf(1, arguments.getLength());

        if (!validateTextLength(arguments, text)) {
            return;
        }

        Player player = arguments.getSender();
        renderers.create(id, text, player.getLocation());

        arguments.sendConfigured("renderer-created", Var.of("%id%", id));
    }

    @Command(
        name = "pt.delete",
        aliases = "particletext.delete",
        permission = "particletext.command.delete",
        usage = "/%label% delete <id>",
        min = 1,
        max = 1
    )
    public void delete(Arguments arguments) {
        String id = normalizeId(arguments.getFirst());

        if (!renderers.delete(id)) {
            sendRendererNotFound(arguments, id);
            return;
        }

        arguments.sendConfigured("renderer-deleted", Var.of("%id%", id));
    }

    @Command(
        name = "pt.tphere",
        aliases = "particletext.tphere",
        permission = "particletext.command.teleport",
        usage = "/%label% tphere <id>",
        min = 1,
        max = 1,
        senderType = Command.SenderType.PLAYER
    )
    public void teleportHere(Arguments arguments) {
        String id = normalizeId(arguments.getFirst());
        Player player = arguments.getSender();

        if (renderers.updateLocation(id, player.getLocation()).isEmpty()) {
            sendRendererNotFound(arguments, id);
            return;
        }

        arguments.sendConfigured("renderer-moved", Var.of("%id%", id));
    }

    @Command(
        name = "pt.move",
        aliases = "particletext.move",
        permission = "particletext.command.edit",
        usage = "/%label% move <id> <forward|backward|left|right|up|down> [amount]",
        min = 2,
        max = 3,
        senderType = Command.SenderType.PLAYER
    )
    public void move(Arguments arguments) {
        Optional<MoveDirection> direction = MoveDirection.find(arguments.getArgument(1));

        if (direction.isEmpty()) {
            arguments.sendConfigured("invalid-direction");
            return;
        }

        double amount = 0.25;

        if (arguments.getLength() == 3) {
            String rawAmount = arguments.getArgument(2);

            if (!arguments.isFloatingDecimal(2)) {
                arguments.sendConfigured("invalid-number", Var.of("%value%", rawAmount));
                return;
            }

            amount = arguments.getArgumentAsDouble(2);

            if (!Double.isFinite(amount)) {
                arguments.sendConfigured("invalid-number", Var.of("%value%", rawAmount));
                return;
            }
        }

        if (amount < 0.01 || amount > 100.0) {
            arguments.sendConfigured("invalid-move-amount");
            return;
        }

        String id = normalizeId(arguments.getFirst());
        Optional<RendererData> renderer = renderers.find(id);

        if (renderer.isEmpty()) {
            sendRendererNotFound(arguments, id);
            return;
        }

        var location = renderer.get().location().toBukkitLocation();

        if (location == null) {
            arguments.sendConfigured("world-unavailable", Var.of("%world%", renderer.get().location().world()));
            return;
        }

        Player player = arguments.getSender();
        MoveDirection moveDirection = direction.get();
        MoveDirection.Offset offset = moveDirection.offset(player.getLocation().getYaw(), amount);
        location.add(offset.x(), offset.y(), offset.z());
        renderers.updateLocation(id, location);

        arguments.sendConfigured("renderer-shifted", Var.of("%id%", id),
            Var.of("%direction%", moveDirection.displayName()),
            Var.of("%amount%", String.format(Locale.US, "%.2f", amount)));
    }

    @Command(
        name = "pt.text",
        aliases = "particletext.text",
        permission = "particletext.command.edit",
        usage = "/%label% text <id> <text>",
        min = 2
    )
    public void text(Arguments arguments) {
        String id = normalizeId(arguments.getFirst());
        String text = arguments.concatRangeOf(1, arguments.getLength());

        if (!validateTextLength(arguments, text)) {
            return;
        }

        if (renderers.updateText(id, text).isEmpty()) {
            sendRendererNotFound(arguments, id);
            return;
        }

        arguments.sendConfigured("renderer-text", Var.of("%id%", id));
    }

    @Command(
        name = "pt.setsize",
        aliases = "particletext.setsize",
        permission = "particletext.command.edit",
        usage = "/%label% setsize <id> <scale>",
        min = 2,
        max = 2
    )
    public void setSize(Arguments arguments) {
        String rawScale = arguments.getArgument(1);

        if (!arguments.isFloatingDecimal(1)) {
            arguments.sendConfigured("invalid-number", Var.of("%value%", rawScale));
            return;
        }

        double scale = arguments.getArgumentAsDouble(1);

        if (!Double.isFinite(scale)) {
            arguments.sendConfigured("invalid-number", Var.of("%value%", rawScale));
            return;
        }

        if (scale <= 0.0 || scale > 10.0) {
            arguments.sendConfigured("invalid-size");
            return;
        }

        String id = normalizeId(arguments.getFirst());

        if (renderers.updateScale(id, scale).isEmpty()) {
            sendRendererNotFound(arguments, id);
            return;
        }

        arguments.sendConfigured("renderer-size", Var.of("%id%", id),
            Var.of("%size%", String.format(Locale.US, "%.2f", scale)));
    }

    @Command(
        name = "pt.font",
        aliases = "particletext.font",
        permission = "particletext.command.edit",
        usage = "/%label% font <id> <name> <style> <size>",
        min = 4,
        max = 4
    )
    public void font(Arguments arguments) {
        Optional<FontStyle> style = FontStyle.find(arguments.getArgument(2));

        if (style.isEmpty()) {
            arguments.sendConfigured("invalid-font");
            return;
        }

        String rawSize = arguments.getArgument(3);

        if (!arguments.isInteger(3)) {
            arguments.sendConfigured("invalid-number", Var.of("%value%", rawSize));
            return;
        }

        int size = arguments.getArgumentAsInt(3);

        if (size < 4 || size > 128) {
            arguments.sendConfigured("invalid-font");
            return;
        }

        String id = normalizeId(arguments.getFirst());
        FontSpec font = new FontSpec(arguments.getArgument(1), style.get(), size);

        if (renderers.updateFont(id, font).isEmpty()) {
            sendRendererNotFound(arguments, id);
            return;
        }

        arguments.sendConfigured("renderer-font", Var.of("%id%", id), Var.of("%font%", font.name()),
            Var.of("%style%", font.style().name()), Var.of("%size%", font.size()));
    }

    @Command(
        name = "pt.particle",
        aliases = "particletext.particle",
        permission = "particletext.command.edit",
        usage = "/%label% particle <id> <particle>",
        min = 2,
        max = 2
    )
    public void particle(Arguments arguments) {
        String requested = arguments.getArgument(1);
        var particle = ParticleSupport.find(requested);

        if (particle.isEmpty()) {
            arguments.sendConfigured("invalid-particle", Var.of("%particle%", requested));
            return;
        }

        String id = normalizeId(arguments.getFirst());

        if (renderers.updateParticle(id, particle.get()).isEmpty()) {
            sendRendererNotFound(arguments, id);
            return;
        }

        arguments.sendConfigured("renderer-particle", Var.of("%id%", id),
            Var.of("%particle%", particle.get().name()));
    }

    @Command(
        name = "pt.enabled",
        aliases = "particletext.enabled",
        permission = "particletext.command.edit",
        usage = "/%label% enabled <id> <true|false>",
        min = 2,
        max = 2
    )
    public void enabled(Arguments arguments) {
        String rawValue = arguments.getArgument(1);

        if (!("true".equalsIgnoreCase(rawValue) || "false".equalsIgnoreCase(rawValue))) {
            arguments.sendConfigured("correct-usage",
                Var.of("%usage%", "/" + arguments.getLabel() + " enabled <id> <true|false>"));
            return;
        }

        String id = normalizeId(arguments.getFirst());
        boolean value = arguments.getArgumentAsBoolean(1);

        if (renderers.updateEnabled(id, value).isEmpty()) {
            sendRendererNotFound(arguments, id);
            return;
        }

        arguments.sendConfigured(value ? "renderer-enabled" : "renderer-disabled", Var.of("%id%", id));
    }

    @Command(
        name = "pt.inverted",
        aliases = "particletext.inverted",
        permission = "particletext.command.edit",
        usage = "/%label% inverted <id> <true|false>",
        min = 2,
        max = 2
    )
    public void inverted(Arguments arguments) {
        String rawValue = arguments.getArgument(1);

        if (!("true".equalsIgnoreCase(rawValue) || "false".equalsIgnoreCase(rawValue))) {
            arguments.sendConfigured("correct-usage",
                Var.of("%usage%", "/" + arguments.getLabel() + " inverted <id> <true|false>"));
            return;
        }

        String id = normalizeId(arguments.getFirst());
        boolean value = arguments.getArgumentAsBoolean(1);

        if (renderers.updateInverted(id, value).isEmpty()) {
            sendRendererNotFound(arguments, id);
            return;
        }

        arguments.sendConfigured("renderer-inverted", Var.of("%id%", id), Var.of("%inverted%", value));
    }

    @Command(
        name = "pt.rotate",
        aliases = "particletext.rotate",
        permission = "particletext.command.edit",
        usage = "/%label% rotate <id> <x|y|z> <angle>",
        min = 3,
        max = 3
    )
    public void rotate(Arguments arguments) {
        String rawAxis = arguments.getArgument(1);

        if (rawAxis.length() != 1 || "xyz".indexOf(Character.toLowerCase(rawAxis.charAt(0))) < 0) {
            arguments.sendConfigured("invalid-axis");
            return;
        }

        String rawAngle = arguments.getArgument(2);

        if (!arguments.isFloatingDecimal(2)) {
            arguments.sendConfigured("invalid-number", Var.of("%value%", rawAngle));
            return;
        }

        double angle = arguments.getArgumentAsDouble(2);

        if (!Double.isFinite(angle)) {
            arguments.sendConfigured("invalid-number", Var.of("%value%", rawAngle));
            return;
        }

        String id = normalizeId(arguments.getFirst());
        char axis = Character.toLowerCase(rawAxis.charAt(0));

        if (renderers.updateRotation(id, axis, angle).isEmpty()) {
            sendRendererNotFound(arguments, id);
            return;
        }

        arguments.sendConfigured("renderer-rotated", Var.of("%id%", id), Var.of("%axis%", axis),
            Var.of("%angle%", String.format(Locale.US, "%.2f", angle)));
    }

    @Command(
        name = "pt.reload",
        aliases = "particletext.reload",
        permission = "particletext.command.reload",
        usage = "/%label% reload",
        max = 0
    )
    public void reload(Arguments arguments) {
        plugin.reloadPlugin();

        arguments.sendConfigured("reloaded");
    }
}

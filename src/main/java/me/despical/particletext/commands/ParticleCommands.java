package me.despical.particletext.commands;

import me.despical.commandframework.Command;
import me.despical.commandframework.CommandArguments;
import me.despical.commandframework.Completer;
import me.despical.commons.configuration.ConfigUtils;
import me.despical.commons.serializer.LocationSerializer;
import me.despical.particletext.Main;
import me.despical.particletext.particles.ParticleRenderer;
import org.bukkit.Particle;
import org.bukkit.util.StringUtil;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static me.despical.commandframework.Command.SenderType.PLAYER;

/**
 * @author Despical
 * <p>
 * Created at 3.07.2023
 */
public class ParticleCommands extends AbstractCommand {

	public ParticleCommands(Main plugin) {
		super(plugin);
	}

	@Command(
			name = "pt",
			desc = "Main command of Pixel Painter."
	)
	public void ptCommand(CommandArguments arguments) {
		final var user = plugin.getUserManager().getUser(arguments.getSender());

		if (arguments.isArgumentsEmpty()) {
			user.sendMessage("&3This server is running &bParticle Text " + plugin.getDescription().getVersion() + " &3by &bDespical&3!");

			if (user.hasPermission("pt.admin")) {
				user.sendRawMessage("&3Commands: &b/" + arguments.getLabel() + " help");
			}
		}
	}

	@Command(
			name = "pt.create",
			permission = "pt.create",
			usage = "/pt create <id> <particle type> <invert> <size> <text to show>",
			desc = "Shows a text message with specified particle effect.",
			senderType = PLAYER
	)
	public void ptCreateTextCommand(CommandArguments arguments) {
		final var user = plugin.getUserManager().getUser(arguments.getSender());
		final int length = arguments.getArgumentsLength();

		if (length < 5) {
			user.sendMessage("admin-commands.correct-usage");
			return;
		}

		final var id = arguments.getArgument(0);

		if (particleHandler.containsRenderer(id)) {
			user.sendMessage("admin-commands.duplicate-renderer");
			return;
		}

		final var particleName = arguments.getArgument(1);
		final Particle particle;

		try {
			particle = Particle.valueOf(particleName.toUpperCase());
		} catch (Exception exception) {
			user.sendMessage("admin-commands.no-particle-found", particleName);
			return;
		}

		final boolean invert = arguments.getArgumentAsBoolean(2);
		final float size = arguments.getArgumentAsFloat(3);

		if (size <= 0) {
			user.sendMessage("admin-commands.size-cannot-be-equal-0");
			return;
		}

		final var argumentList = Arrays.asList(arguments.getArguments());
		final var text = String.join(" ", argumentList.subList(4, argumentList.size()));
		final var renderer = particleHandler.createRenderer(user.getLocation(), particle, id, text, size, invert);
		renderer.render();

		user.sendMessage("admin-commands.created-renderer", id);
	}

	@Command(
			name = "pt.delete",
			permission = "pt.delete",
			usage = "/pt delete <id>",
			desc = "Delete the target particle renderer and stop rendering.",
			min = 1,
			senderType = PLAYER
	)
	public void ptDeleteCommand(CommandArguments arguments) {
		final var user = plugin.getUserManager().getUser(arguments.getSender());
		final var id = arguments.getArgument(0);

		if (!particleHandler.containsRenderer(id)) {
			user.sendMessage("admin-commands.no-particle-renderer-found");
			return;
		}

		particleHandler.removeRenderer(id);

		final var config = ConfigUtils.getConfig(plugin, "renderers");
		config.set("renderer-instances.%s".formatted(id), null);

		ConfigUtils.saveConfig(plugin, config, "renderers");

		user.sendMessage("admin-commands.deleted-renderer", id);
	}

	@Command(
			name = "pt.list",
			permission = "pt.list",
			usage = "/pt list",
			desc = "Shows a list of registered particle renderers.",
			senderType = PLAYER
	)
	public void ptListCommand(CommandArguments arguments) {
		final var user = plugin.getUserManager().getUser(arguments.getSender());
		final var list = String.join(", ", plugin.getParticleHandler().getRenderers().keySet());

		if (list.isEmpty()) {
			user.sendMessage("admin-commands.empty-list");
			return;
		}

		user.sendRawMessage(chatManager.message("admin-commands.renderer-list").replace("%list%", list));
	}

	@Command(
			name = "pt.teleport",
			permission = "pt.tp",
			usage = "/pt teleport <id>",
			desc = "Teleports you to renderer origin.",
			senderType = PLAYER
	)
	public void ptTeleportCommand(CommandArguments arguments) {
		final var user = plugin.getUserManager().getUser(arguments.getSender());
		final var id = arguments.getArgument(0);

		if (!particleHandler.containsRenderer(id)) {
			user.sendMessage("admin-commands.no-particle-renderer-found");
			return;
		}

		final var config = ConfigUtils.getConfig(plugin, "renderers");
		final var location = LocationSerializer.fromString(config.getString("renderer-instances.%s.location".formatted(id)));

		user.player().teleport(location);
	}

	@Command(
			name = "pt.tphere",
			permission = "pt.tphere",
			usage = "/pt tphere <id>",
			desc = "Teleports renderer to your location.",
			senderType = PLAYER
	)
	public void ptTpHereCommand(CommandArguments arguments) {
		final var user = plugin.getUserManager().getUser(arguments.getSender());
		final var id = arguments.getArgument(0);

		if (!particleHandler.containsRenderer(id)) {
			user.sendMessage("admin-commands.no-particle-renderer-found");
			return;
		}

		final var config = ConfigUtils.getConfig(plugin, "renderers");
		config.set("renderer-instances.%s.location".formatted(id), LocationSerializer.toString(user.getLocation()));
		ConfigUtils.saveConfig(plugin, config, "renderers");

		var renderer = particleHandler.getRenderer(id);

		if (renderer == null) return;

		renderer.updateLocation(user.getLocation());

		user.sendMessage("admin-commands.moved-here", id);
	}

	@Command(
			name = "pt.enabled",
			permission = "pt.enabled",
			usage = "/pt enabled <id> <true/false>",
			desc = "Enable or disable target particle renderer.",
			senderType = PLAYER
	)
	public void ptEnabledCommand(CommandArguments arguments) {
		final var user = plugin.getUserManager().getUser(arguments.getSender());
		final var id = arguments.getArgument(0);

		if (!particleHandler.containsRenderer(id)) {
			user.sendMessage("admin-commands.no-particle-renderer-found");
			return;
		}

		final var enabled = arguments.getArgumentAsBoolean(1);
		final var config = ConfigUtils.getConfig(plugin, "renderers");

		config.set("renderer-instances.%s.enabled".formatted(id), enabled);
		ConfigUtils.saveConfig(plugin, config, "renderers");

		ParticleRenderer renderer = particleHandler.getRenderer(id);

		if (renderer == null) return;

		renderer.setEnabled(enabled);

		user.sendMessage("admin-commands." + (enabled ? "enabled-renderer" : "disabled-renderer"), id);
	}

	@Command(
			name = "pt.font",
			permission = "pt.font",
			usage = "/pt font <id> <font name> <style> <size>",
			desc = "Enable or disable target particle renderer.",
			senderType = PLAYER
	)
	public void ptSetFontCommand(CommandArguments arguments) {
		final var user = plugin.getUserManager().getUser(arguments.getSender());
		final var id = arguments.getArgument(0);
		final var particleRenderer = particleHandler.getRenderer(id);

		if (particleRenderer == null) {
			user.sendMessage("admin-commands.no-particle-renderer-found");
			return;
		}

		if (arguments.getArgumentsLength() < 4) {
			user.sendRawMessage("&cCorrect usage: /pt font <id> <font name> <style> <size>");
			return;
		}

		try {
			final var font = new Font(arguments.getArgument(1), arguments.getArgumentAsInt(2), arguments.getArgumentAsInt(3));
			final var config = ConfigUtils.getConfig(plugin, "renderers");

			config.set("renderer-instances.%s.font".formatted(id), "%s:%d:%d".formatted(font.getFontName(), font.getStyle(), font.getSize()));
			ConfigUtils.saveConfig(plugin, config, "renderers");

			particleRenderer.setFont(font);

			user.sendMessage("admin-commands.font-changed");
		} catch (Exception exception) {
			user.sendRawMessage("&cThere isn't any font with given arguments!");
		}
	}

	@Completer(
			name = "pt"
	)
	public java.util.List<String> ptTabCompleter(CommandArguments arguments) {
		final java.util.List<String> completions = new ArrayList<>(), commands = plugin.getCommandFramework().getCommands().stream().map(cmd -> cmd.name().replace(arguments.getLabel() + '.', "")).collect(Collectors.toList());
		final String args[] = arguments.getArguments(), arg = args[0];

		commands.remove("pt");

		if (args.length == 1) {
			StringUtil.copyPartialMatches(arg, arguments.hasPermission("pt.admin") || arguments.getSender().isOp() ? commands : java.util.List.of("create", "delete", "tphere", "teleport", "enabled"), completions);
		}

		if (args.length == 2) {
			if (List.of("create", "list").contains(arg)) return completions;

			final var idList = new ArrayList<>(particleHandler.getRenderers().keySet().stream().toList());

			StringUtil.copyPartialMatches(args[1], idList, completions);
			idList.sort(null);
			return idList;
		}

		if (args.length == 3 && arg.equalsIgnoreCase("create")) {
			final var particleList = Stream.of(Particle.values()).map(Particle::name).sorted().toList();

			StringUtil.copyPartialMatches(args[2], particleList, completions);
			completions.sort(null);
			return completions;
		}

		if (args.length == 3 && arg.equalsIgnoreCase("enabled")) return java.util.List.of("true", "false");
		if (args.length == 4 && arg.equalsIgnoreCase("create")) return List.of("true", "false");

		completions.sort(null);
		return completions;
	}
}
package me.despical.particletext.commands;

import me.despical.commandframework.Command;
import me.despical.commandframework.CommandArguments;
import me.despical.commandframework.Completer;
import me.despical.commons.configuration.ConfigUtils;
import me.despical.commons.miscellaneous.MiscUtils;
import me.despical.commons.serializer.LocationSerializer;
import me.despical.commons.string.StringMatcher;
import me.despical.particletext.Main;
import me.despical.particletext.particles.ParticleRenderer;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.awt.*;
import java.util.*;
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

		plugin.getCommandFramework().setMatchFunction(arguments -> {
			if (arguments.isArgumentsEmpty()) return false;

			final String label = arguments.getLabel(), arg = arguments.getArgument(0);
			final var matches = StringMatcher.match(arg, plugin.getCommandFramework().getCommands().stream().map(cmd -> cmd.name().replace(label + ".", "")).collect(Collectors.toList()));

			if (!matches.isEmpty()) {
				final var didYouMeanMsg = plugin.getChatManager().message("admin-commands.did-you-mean");

				arguments.sendMessage(didYouMeanMsg.replace("%command%", label + " " + matches.get(0).getMatch()));
				return true;
			}

			return false;
		});
	}

	@Command(
			name = "pt",
			desc = "Main command of Pixel Painter."
	)
	public void ptCommand(CommandArguments arguments) {
		arguments.sendMessage(chatManager.rawMessage("&3This server is running &bParticle Text " + plugin.getDescription().getVersion() + " &3by &bDespical&3!"));

		if (arguments.hasPermission("pt.help")) {
			arguments.sendMessage(chatManager.rawMessage("&3Commands: &b/" + arguments.getLabel() + " help"));
		}
	}

	@Command(
			name = "pt.create",
			permission = "pt.create",
			usage = "/pt create <id> <particle type> <invert> <size> <text to show>",
			desc = "Shows a text message with specified particle effect.",
			allowInfiniteArgs = true,
			senderType = PLAYER
	)
	public void ptCreateTextCommand(CommandArguments arguments) {
		final var user = plugin.getUserManager().getUser(arguments.getSender());
		final int length = arguments.getLength();

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

		if (arguments.getLength() < 4) {
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

	@Command(
			name = "pt.reload",
			usage = "/pt reload",
			desc = "Reloads particle renderers and system files.",
			permission = "pt.reload",
			allowInfiniteArgs = true
	)
	public void reloadCommand(CommandArguments arguments) {
		plugin.getChatManager().reload();
		plugin.getParticleHandler().reload();

		arguments.sendMessage(chatManager.message("admin-commands.system-reloaded"));
	}

	@SuppressWarnings("deprecation")
	@Command(
			name = "pt.help",
			permission = "pt.help"
	)
	public void mmHelpCommand(CommandArguments arguments) {
		final var isPlayer = arguments.isSenderPlayer();
		final var sender = arguments.getSender();
		final var message = chatManager.rawMessage("&3&l---- Particle Text Admin Commands ----");

		arguments.sendMessage("");
		MiscUtils.sendCenteredMessage(sender, message);
		arguments.sendMessage("");

		for (final var command : plugin.getCommandFramework().getCommands().stream().sorted(Collections
				.reverseOrder(Comparator.comparingInt(cmd -> cmd.usage().length()))).toList()) {
			String usage = command.usage(), desc = command.desc();

			if (usage.isEmpty() || usage.contains("help")) continue;

			if (isPlayer) {
				((Player) sender).spigot().sendMessage(new ComponentBuilder()
						.color(ChatColor.DARK_GRAY)
						.append(" • ")
						.append(usage)
						.color(ChatColor.AQUA)
						.event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, usage))
						.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(desc)))
						.create());
			} else {
				sender.sendMessage(chatManager.rawMessage(" &8• &b" + usage + " &3- &b" + desc));
			}
		}

		if (isPlayer) {
			final var player = arguments.getSender();
			player.sendMessage("");
			player.spigot().sendMessage(new ComponentBuilder("TIP:").color(ChatColor.YELLOW).bold(true)
					.append(" Try to ", ComponentBuilder.FormatRetention.NONE).color(ChatColor.GRAY)
					.append("hover").color(ChatColor.WHITE).underlined(true)
					.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(ChatColor.LIGHT_PURPLE + "Hover on the commands to get info about them.")))
					.append(" or ", ComponentBuilder.FormatRetention.NONE).color(ChatColor.GRAY)
					.append("click").color(ChatColor.WHITE).underlined(true)
					.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(ChatColor.LIGHT_PURPLE + "Click on the commands to insert them in the chat.")))
					.append(" on the commands!", ComponentBuilder.FormatRetention.NONE).color(ChatColor.GRAY)
					.create());
		}
	}


	@Completer(
			name = "pt",
			permission = "pt.admin"
	)
	public List<String> ptTabCompleter(CommandArguments arguments) {
		final List<String> completions = new ArrayList<>(), commands = plugin.getCommandFramework().getCommands().stream().map(cmd -> cmd.name().replace(arguments.getLabel() + '.', "")).collect(Collectors.toList());
		final String args[] = arguments.getArguments(), arg = args[0];

		commands.remove("pt");

		if (args.length == 1) {
			StringUtil.copyPartialMatches(arg, commands, completions);
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

		if (args.length == 3 && arg.equalsIgnoreCase("enabled")) return List.of("true", "false");
		if (args.length == 4 && arg.equalsIgnoreCase("create")) return List.of("true", "false");

		completions.sort(null);
		return completions;
	}
}
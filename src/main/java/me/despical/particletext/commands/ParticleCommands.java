package me.despical.particletext.commands;

import me.despical.commandframework.CommandArguments;
import me.despical.commandframework.annotations.Command;
import me.despical.commandframework.annotations.Completer;
import me.despical.commons.configuration.ConfigUtils;
import me.despical.commons.miscellaneous.MiscUtils;
import me.despical.commons.serializer.LocationSerializer;
import me.despical.commons.string.StringMatcher;
import me.despical.particle.ParticleEffect;
import me.despical.particletext.Main;
import me.despical.particletext.particles.ParticleRenderer;
import me.despical.particletext.users.User;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Despical
 * <p>
 * Created at 3.07.2023
 */
public class ParticleCommands extends AbstractCommand {

	public ParticleCommands(Main plugin) {
		super(plugin);

		final var commandFramework = plugin.getCommandFramework();

		commandFramework.addCustomParameter("User", args -> plugin.getUserManager().getUser(args.getSender()));
		commandFramework.addCustomParameter("String", args -> args.getArgument(0));
	}

	@Command(
			name = "pt",
			usage = "/pt help",
			desc = "Main command of Pixel Painter."
	)
	public void mainCommand(CommandArguments arguments) {
		if (arguments.isArgumentsEmpty()) {
			arguments.sendMessage("&3This server is running &bParticle Text " + plugin.getDescription().getVersion() + " &3by &bDespical&3!");

			if (arguments.hasPermission("pt.help")) {
				arguments.sendMessage("&3Commands: &b/" + arguments.getLabel() + " help");
			}

			return;
		}

		var commandFramework = plugin.getCommandFramework();
		String label = arguments.getLabel(), arg = arguments.getArgument(0);
		List<String> commands = commandFramework.getSubCommands().stream().map(cmd -> cmd.name().replace(label + ".", "")).collect(Collectors.toList());
		List<StringMatcher.Match> matches = StringMatcher.match(arg, commands);

		if (!matches.isEmpty()) {
			Optional<Command> optionalMatch = commandFramework.getSubCommands().stream().filter(cmd -> cmd.name().equals(label + "." + matches.get(0).getMatch())).findFirst();

			if (optionalMatch.isPresent()) {
				String matchedName = getMatchingParts(optionalMatch.get().name(), label + "." + String.join(".", arguments.getArguments()));
				Optional<Command> matchedCommand = commandFramework.getSubCommands().stream().filter(cmd -> cmd.name().equals(matchedName)).findFirst();

				if (matchedCommand.isPresent()) {
					arguments.sendMessage(chatManager.message("admin-commands.correct-usage").replace("%usage%", matchedCommand.get().usage()));
					return;
				}

				arguments.sendMessage(chatManager.message("admin-commands.did-you-mean").replace("%command%", optionalMatch.get().usage()));
				return;
			}

			arguments.sendMessage(chatManager.message("admin-commands.did-you-mean").replace("%command%", label));
		}
	}

	@Command(
			name = "pt.create",
			permission = "pt.create",
			usage = "/pt create <id> <particle type> <invert> <size> <text to show>",
			desc = "Shows a text message with specified particle effect.",
			senderType = Command.SenderType.PLAYER
	)
	public void createCommand(CommandArguments arguments, User user, String id) {
		final int length = arguments.getLength();

		if (length < 5) {
			user.sendMessage("admin-commands.create-command-usage");
			return;
		}

		if (particleHandler.containsRenderer(id)) {
			user.sendMessage("admin-commands.duplicate-renderer");
			return;
		}

		final var particleName = arguments.getArgument(1);
		final ParticleEffect particle;

		try {
			particle = ParticleEffect.valueOf(particleName.toUpperCase());
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
		final var renderer = particleHandler.createRenderer(user.getLocation(), particle, id, text, size / 10F, invert);
		renderer.render();

		user.sendMessage("admin-commands.created-renderer", id);
	}

	@Command(
			name = "pt.delete",
			permission = "pt.delete",
			usage = "/pt delete <id>",
			desc = "Delete the target particle renderer and stop rendering.",
			min = 1,
			senderType = Command.SenderType.PLAYER
	)
	public void deleteCommand(CommandArguments arguments, User user, String id) {
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
			senderType = Command.SenderType.PLAYER
	)
	public void listCommand(CommandArguments arguments, User user) {
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
			min = 1,
			senderType = Command.SenderType.PLAYER
	)
	public void teleportCommand(CommandArguments arguments, User user, String id) {
		if (!particleHandler.containsRenderer(id)) {
			user.sendMessage("admin-commands.no-particle-renderer-found");
			return;
		}

		final var config = ConfigUtils.getConfig(plugin, "renderers");
		final var location = LocationSerializer.fromString(config.getString("renderer-instances.%s.location".formatted(id)));

		user.player().teleport(location);
	}

	@Command(
			name = "pt.setsize",
			permission = "pt.setsize",
			usage = "/pt setsize <id> <size>",
			desc = "Sets the particle size of the renderer.",
			min = 2,
			senderType = Command.SenderType.PLAYER
	)
	public void setSizeMethod(CommandArguments arguments, User user, String id) {
		if (!particleHandler.containsRenderer(id)) {
			user.sendMessage("admin-commands.no-particle-renderer-found");
			return;
		}

		final var config = ConfigUtils.getConfig(plugin, "renderers");
		final var newSize = arguments.getArgumentAsFloat(1);
		final var renderer = particleHandler.getRenderer(id);

		renderer.setSize(newSize / 10F);

		config.set("renderer-instances.%s.size".formatted(id), newSize / 10F);
		ConfigUtils.saveConfig(plugin, config, "renderers");

		user.sendMessage("admin-commands.set-size", id, newSize);
	}

	@Command(
			name = "pt.tphere",
			permission = "pt.tphere",
			usage = "/pt tphere <id>",
			desc = "Teleports renderer to your location.",
			min = 1,
			senderType = Command.SenderType.PLAYER
	)
	public void tpHereCommand(CommandArguments arguments, User user, String id) {
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
			min = 1,
			senderType = Command.SenderType.PLAYER
	)
	public void enabledCommand(CommandArguments arguments, User user, String id) {
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
			min = 1,
			max = 4,
			senderType = Command.SenderType.PLAYER
	)
	public void setFontCommand(CommandArguments arguments, User user, String id) {
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
			name = "pt.particle",
			permission = "pt.particle",
			usage = "/pt font <id> <particle name>",
			desc = "Change particle effect of the renderer.",
			min = 1,
			max = 2,
			senderType = Command.SenderType.PLAYER
	)
	public void setParticleCommand(CommandArguments arguments, User user, String id) {
		final var particleRenderer = particleHandler.getRenderer(id);

		if (particleRenderer == null) {
			user.sendMessage("admin-commands.no-particle-renderer-found");
			return;
		}

		if (arguments.getLength() < 2) {
			user.sendRawMessage("&cCorrect usage: /pt font <id> <particle name>");
			return;
		}

		String particleName = arguments.getArgument(1);
		ParticleEffect particle;

		try {
			particle = ParticleEffect.valueOf(particleName.toUpperCase());
		} catch (Exception exception) {
			user.sendMessage("admin-commands.no-particle-found", particleName);
			return;
		}

		final var config = ConfigUtils.getConfig(plugin, "renderers");

		config.set("renderer-instances.%s.particle".formatted(id), particleName);
		ConfigUtils.saveConfig(plugin, config, "renderers");

		particleRenderer.setParticle(particle);

		user.sendMessage("admin-commands.particle-changed");
	}

	@Command(
			name = "pt.reload",
			usage = "/pt reload",
			desc = "Reloads particle renderers and system files.",
			permission = "pt.reload"
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
	public void helpCommand(CommandArguments arguments) {
		arguments.sendMessage("");
		MiscUtils.sendCenteredMessage(arguments.getSender(), "&3&l---- Particle Text ----");
		arguments.sendMessage("");

		final CommandSender sender = arguments.getSender();
		final boolean isPlayer = arguments.isSenderPlayer();

		for (final var command : plugin.getCommandFramework().getSubCommands()) {
			final String usage = command.usage(), desc = command.desc();

			if (usage.isEmpty() || desc.isEmpty() || usage.contains("help")) continue;

			if (isPlayer) {
				((Player) sender).spigot().sendMessage(new ComponentBuilder(ChatColor.DARK_GRAY + " • ")
						.append(usage)
						.color(ChatColor.AQUA)
						.event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, usage))
						.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(desc)))
						.create());
			} else {
				sender.sendMessage(" &8• &b" + usage + " &3- &b" + desc);
			}
		}

		if (isPlayer) {
			final Player player = arguments.getSender();

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
			name = "pt.particle",
			permission = "pt.admin"
	)
	public List<String> particleCompleter(CommandArguments arguments) {
		List<String> completions = new ArrayList<>();

		if (arguments.getLength() == 1) {
			final var idList = new ArrayList<>(particleHandler.getRenderers().keySet().stream().toList());

			return StringUtil.copyPartialMatches(arguments.getArgument(0), idList, completions);
		}

		if (arguments.getLength() == 2) {
			final var particleList = Stream.of(ParticleEffect.values()).map(ParticleEffect::name).sorted().toList();

			return StringUtil.copyPartialMatches(arguments.getArgument(1), particleList, completions);
		}

		return completions;
	}

	@Completer(
			name = "pt",
			permission = "pt.admin"
	)
	public List<String> ptTabCompleter(CommandArguments arguments) {
		final List<String> completions = new ArrayList<>(), commands = plugin.getCommandFramework().getSubCommands().stream().map(cmd -> cmd.name().replace(arguments.getLabel() + '.', "")).collect(Collectors.toList());
		final String args[] = arguments.getArguments(), arg = args[0];

		if (args.length == 1) {
			return StringUtil.copyPartialMatches(arg, commands, completions);
		}

		if (args.length == 2) {
			if (List.of("create", "list", "help", "reload").contains(args[0])) return completions;

			final var idList = new ArrayList<>(particleHandler.getRenderers().keySet().stream().toList());

			return StringUtil.copyPartialMatches(args[1], idList, completions);
		}

		if (args.length == 3 && arg.equalsIgnoreCase("create")) {
			final var particleList = Stream.of(ParticleEffect.values()).map(ParticleEffect::name).sorted().toList();

			return StringUtil.copyPartialMatches(args[2], particleList, completions);
		}

		if (args.length == 3 && arg.equalsIgnoreCase("enabled")) return List.of("true", "false");
		if (args.length == 4 && arg.equalsIgnoreCase("create")) return List.of("true", "false");

		return completions;
	}

	private String getMatchingParts(String matched, String current) {
		String[] matchedArray = matched.split("\\."), currentArray = current.split("\\.");
		int max = Math.min(matchedArray.length, currentArray.length);
		List<String> matchingParts = new ArrayList<>();

		for (int i = 0; i < max; i++) {
			if (matchedArray[i].equals(currentArray[i])) {
				matchingParts.add(matchedArray[i]);
			}
		}

		return String.join(".", matchingParts);
	}
}
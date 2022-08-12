package me.despical.particletext.command;

import me.despical.commandframework.Command;
import me.despical.commandframework.CommandArguments;
import me.despical.particletext.renderer.ParticleRenderer;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

import java.util.Arrays;

import static me.despical.commandframework.Command.SenderType.PLAYER;

/**
 * @author Despical
 * <p>
 * Created at 7.08.2022
 */
public class MainCommands implements CommandImpl {

	@Command(
			name = "pt"
	)
	public void particleCommand(CommandArguments arguments) {
		arguments.sendMessage("Welcome to Particle Text plugin!");
		arguments.sendMessage("Use /pt help command to see commands.");
	}

	@Command(
			name = "pt.create",
			permission = "pt.create",
			usage = "/pt create <name> <particle> <invert> <size> <text to show>",
			desc = "Shows a text message with specified particle effect.",
			senderType = PLAYER
	)
	public void createTextCommand(CommandArguments arguments) {
		final Player player = arguments.getSender();
		final int length = arguments.getArgumentsLength();

		if (length == 0) {
			sendMessage(player, "Please provide a name to save the renderer!");
			return;
		}

		String name = arguments.getArgument(0);

		if (particleHandler.containsRenderer(name)) {
			sendMessage(player, "There is already an particle renderer with this name (%s)!", name);
			return;
		}

		if (length == 1) {
			sendMessage(player, "Please provide a particle name to display!");
			return;
		}

		String particleName = arguments.getArgument(1);
		Particle particle;

		try {
			particle = Particle.valueOf(particleName);
		} catch (Exception exception) {
			sendMessage(player, "There is no particle with the name given (%s)!", particleName);
			return;
		}

		if (length == 2) {
			sendMessage(player, "Please provide a inverted value. (true or false)");
			return;
		}

		String invertedString = arguments.getArgument(2), text = String.join(" ", Arrays.copyOfRange(arguments.getArguments(), 2, length));
		boolean inverted = Boolean.parseBoolean(invertedString);
		float size = arguments.getArgumentAsFloat(arguments.getArgumentAsInt(3));

		ParticleRenderer renderer = new ParticleRenderer(particle, text, inverted);
		renderer.render(player.getLocation().clone());

		particleHandler.addRenderer(name, renderer);

		sendMessage(player, "Playing text effect. Particle: %s, Text: %s, inverted: %b, size: %d", particleName, text, inverted, size);
	}

	{
		register(this);
	}
}
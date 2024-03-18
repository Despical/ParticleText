package me.despical.particletext.particles;

import me.despical.particletext.Main;
import me.despical.particletext.utils.ParticleUtils;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import xyz.xenondevs.particle.ParticleBuilder;
import xyz.xenondevs.particle.ParticleEffect;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * @author Despical
 * <p>
 * Created at 3.07.2023
 */
public class ParticleRenderer {

	private static final Main plugin = JavaPlugin.getPlugin(Main.class);
	private static final float degreesToRadians = 3.1415927f / 180;

	private final ParticleEffect particle;
	private final String text;
	private final boolean invert;
	private final int stepX = 1, stepY = 1;

	private float size;
	private boolean enabled = true;
	private Location location;
	private BukkitTask renderTask;
	private BufferedImage image;

	public ParticleRenderer(Location location, ParticleEffect particle, String text, float size, boolean invert) {
		this(location, particle, text, size, invert, new Font("Tahoma", Font.PLAIN, 16));
	}

	public ParticleRenderer(Location location, ParticleEffect particle, String text, float size, boolean invert, Font font) {
		this.location = location;
		this.particle = particle;
		this.text = text;
		this.invert = invert;
		this.size = size;
		this.image = ParticleUtils.stringToBufferedImage(font, text);
	}

	public void render() {
		renderTask = new BukkitRunnable() {

			private final ParticleBuilder particleBuilder = new ParticleBuilder(particle);

			@Override
			public void run() {
				if (!enabled) {
					cancel();
					return;
				}

				int color;

				try {
					for (int y = 0; y < image.getHeight(); y += stepY) {
						for (int x = 0; x < image.getWidth(); x += stepX) {
							color = image.getRGB(x, y);

							if (!invert && Color.black.getRGB() != color) {
								continue;
							} else if (invert && Color.black.getRGB() == color) {
								continue;
							}

							Vector vector = new Vector((float) image.getWidth() / 2 - x, (float) image.getHeight() / 2 - y, 0).multiply(size);
							ParticleUtils.rotateAroundAxisY(vector, -location.getYaw() * degreesToRadians);

							particleBuilder.setLocation(location.add(vector)).display();

							location.subtract(vector);
						}
					}
				} catch (Exception exception) {
					cancel();
				}
			}
		}.runTaskTimer(plugin, 20, 1);
	}

	public void stopRendering() {
		if (renderTask != null) {
			renderTask.cancel();
			renderTask = null;
		}
	}

	public void updateLocation(Location location) {
		this.location = location;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;

		if (enabled) {
			this.render();
		} else {
			this.stopRendering();
		}
	}

	public void setSize(float size) {
		this.size = size;
	}

	public void setFont(Font font) {
		this.image = ParticleUtils.stringToBufferedImage(font, text);
	}
}
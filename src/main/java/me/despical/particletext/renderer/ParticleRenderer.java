package me.despical.particletext.renderer;

import me.despical.particletext.Main;
import me.despical.particletext.util.ParticleUtils;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * @author Despical
 * <p>
 * Created at 7.08.2022
 */
public class ParticleRenderer {

	private static final Main plugin = JavaPlugin.getPlugin(Main.class);
	private static final float degreesToRadians = 3.1415927f / 180;

	private final Particle particle;
	private final String text;
	private final Font font;
	private final BufferedImage image;
	private final boolean invert;
	private final int stepX = 1, stepY = 1;
	private final float size = (float) 1 / 5;

	private BukkitTask renderTask;

	public ParticleRenderer(Particle particle, String text, boolean invert) {
		this.particle = particle;
		this.text = text;
		this.invert = invert;
		this.font = new Font("Tahoma", Font.PLAIN, 16);
		this.image = ParticleUtils.stringToBufferedImage(font, text);
	}

	public void render(Location location) {
		renderTask = new BukkitRunnable() {

			@Override
			public void run() {
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

							location.getWorld().spawnParticle(particle, location.add(vector), 0);
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
		if (renderTask != null) renderTask.cancel();
	}
}
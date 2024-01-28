package me.despical.particletext.utils;

import me.despical.commons.configuration.ConfigUtils;
import me.despical.commons.number.NumberUtils;
import me.despical.particletext.Main;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * @author Despical
 * <p>
 * Created at 3.07.2023
 */
public class ParticleUtils {

	private ParticleUtils() {
	}

	private static final Main plugin = JavaPlugin.getPlugin(Main.class);

	public static BufferedImage stringToBufferedImage(Font font, String s) {
		var img = new BufferedImage(1, 1, BufferedImage.TYPE_4BYTE_ABGR);
		var graphics = img.getGraphics();
		graphics.setFont(font);

		var frc = graphics.getFontMetrics().getFontRenderContext();
		var rect = font.getStringBounds(s, frc);
		graphics.dispose();

		img = new BufferedImage((int) Math.ceil(rect.getWidth()), (int) Math.ceil(rect.getHeight()), BufferedImage.TYPE_4BYTE_ABGR);
		graphics = img.getGraphics();
		graphics.setColor(Color.black);
		graphics.setFont(font);
		graphics.drawString(s, 0, graphics.getFontMetrics().getAscent());
		graphics.dispose();
		return img;
	}

	public static void rotateAroundAxisY(Vector v, double angle) {
		double x, z, cos, sin;
		cos = Math.cos(angle);
		sin = Math.sin(angle);
		x = v.getX() * cos + v.getZ() * sin;
		z = v.getX() * -sin + v.getZ() * cos;
		v.setX(x).setZ(z);
	}

	public static Font getFont(String path) {
		final var config = ConfigUtils.getConfig(plugin, "renderers");
		final var fontAttributes = config.getString(path).split(":");

		if (fontAttributes.length != 3)
			return new Font("Tahoma", Font.PLAIN, 16);

		return new Font(fontAttributes[0], NumberUtils.getInt(fontAttributes[1], 0), NumberUtils.getInt(fontAttributes[2]));
	}
}
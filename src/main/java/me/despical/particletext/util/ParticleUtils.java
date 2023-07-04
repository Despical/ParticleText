package me.despical.particletext.util;

import org.bukkit.util.Vector;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

/**
 * @author Despical
 * <p>
 * Created at 3.07.2023
 */
public class ParticleUtils {

	public static BufferedImage stringToBufferedImage(Font font, String s) {
		BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_4BYTE_ABGR);
		Graphics graphics = img.getGraphics();
		graphics.setFont(font);

		FontRenderContext frc = graphics.getFontMetrics().getFontRenderContext();
		Rectangle2D rect = font.getStringBounds(s, frc);
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
}
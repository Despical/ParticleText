package me.despical.particletext.util;

import org.bukkit.util.Vector;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

/**
 * @author Despical
 * <p>
 * Created at 7.08.2022
 */
public class ParticleUtils {

	public static BufferedImage stringToBufferedImage(Font font, String s) {
		BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_4BYTE_ABGR);
		Graphics g = img.getGraphics();
		g.setFont(font);

		FontRenderContext frc = g.getFontMetrics().getFontRenderContext();
		Rectangle2D rect = font.getStringBounds(s, frc);
		g.dispose();

		img = new BufferedImage((int) Math.ceil(rect.getWidth()), (int) Math.ceil(rect.getHeight()), BufferedImage.TYPE_4BYTE_ABGR);
		g = img.getGraphics();
		g.setColor(Color.black);
		g.setFont(font);

		FontMetrics fm = g.getFontMetrics();
		int x = 0;
		int y = fm.getAscent();

		g.drawString(s, x, y);
		g.dispose();

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
package me.despical.particletext.utils;

import org.bukkit.util.Vector;

/**
 * @author Despical
 * <p>
 * Created at 22.03.2024
 */
public final class VectorUtils {

	private VectorUtils() {
	}

	public static void rotateAroundAxisX(Vector v, double angle) {
		if (angle == 0) return;

		double cos = Math.cos(angle);
		double sin = Math.sin(angle);
		double y = cos * v.getY() - sin * v.getZ();
		double z = sin * v.getY() + cos * v.getZ();
		v.setY(y).setZ(z);
	}

	public static void rotateAroundAxisY(Vector v, double angle) {
		double cos = Math.cos(angle);
		double sin = Math.sin(angle);
		double x = v.getX() * cos + v.getZ() * sin;
		double z = v.getX() * -sin + v.getZ() * cos;
		v.setX(x).setZ(z);
	}

	public static void rotateAroundAxisZ(Vector v, double angle) {
		if (angle == 0) return;

		double cos = Math.cos(angle);
		double sin = Math.sin(angle);
		double x = cos * v.getX() - sin * v.getY();
		double y = sin * v.getX() + cos * v.getY();
		v.setX(x).setY(y);
	}
}
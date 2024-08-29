package me.despical.particletext.particles;

/**
 * @author Despical
 * <p>
 * Created at 29.08.2024
 */
public class Rotation {

	private double angleX, angleY, angleZ;

	public Rotation() {
		this(0D, 0D, 0D);
	}

	public Rotation(double angleX, double angleY, double angleZ) {
		this.angleX = angleX;
		this.angleY = angleY;
		this.angleZ = angleZ;
	}

	public double getAngleX() {
		return angleX;
	}

	public void setAngleX(double angle) {
		this.angleX = angle;
	}

	public double getAngleY() {
		return angleY;
	}

	public void setAngleY(double angle) {
		this.angleY = angle;
	}

	public double getAngleZ() {
		return angleZ;
	}

	public void setAngleZ(double angle) {
		this.angleZ = angle;
	}
}
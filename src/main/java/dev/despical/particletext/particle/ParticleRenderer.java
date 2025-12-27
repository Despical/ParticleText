package dev.despical.particletext.particle;

import dev.despical.particletext.ParticleTextPlugin;
import dev.despical.particletext.util.ParticleUtils;
import dev.despical.particletext.util.VectorUtils;
import me.despical.particle.ParticleBuilder;
import me.despical.particle.ParticleEffect;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * @author Despical
 * <p>
 * Created at 3.07.2023
 */
public class ParticleRenderer {

    private static final ParticleTextPlugin plugin = JavaPlugin.getPlugin(ParticleTextPlugin.class);
    private static final float degreesToRadians = 3.1415927f / 180;

    private final String text;
    private final boolean invert;
    private final int stepX = 1, stepY = 1;
    private final Rotation rotation;

    private float size;
    private boolean enabled = true;
    private Location location;
    private BukkitTask renderTask;
    private BufferedImage image;
    private ParticleBuilder particleBuilder;

    public ParticleRenderer(Location location, ParticleEffect particle, String text, float size, boolean invert) {
        this(location, particle, text, size, invert, new Font("Tahoma", Font.PLAIN, 16), new Rotation());
    }

    public ParticleRenderer(Location location, ParticleEffect particle, String text, float size, boolean invert, Font font, Rotation rotation) {
        this.location = location;
        this.text = text;
        this.invert = invert;
        this.size = size;
        this.image = ParticleUtils.stringToBufferedImage(font, text);
        this.particleBuilder = new ParticleBuilder(particle);
        this.rotation = rotation;
    }

    public void render() {
        renderTask = new BukkitRunnable() {

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

                            Vector vector = new Vector((float) image.getWidth() / 2 - x, (float) image.getHeight() / 2 - y, 0);
                            VectorUtils.rotateAroundAxisY(vector, location.getYaw() * degreesToRadians);
                            VectorUtils.rotateAroundAxisX(vector, Math.toRadians(rotation.getAngleX()));
                            VectorUtils.rotateAroundAxisY(vector, Math.toRadians(rotation.getAngleY()));
                            VectorUtils.rotateAroundAxisZ(vector, Math.toRadians(rotation.getAngleZ()));

                            particleBuilder.setLocation(location.add(vector.multiply(size))).display();

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

    public void setParticle(ParticleEffect particle) {
        this.particleBuilder = new ParticleBuilder(particle);
    }

    public void setFont(Font font) {
        this.image = ParticleUtils.stringToBufferedImage(font, text);
    }

    public Rotation getRotation() {
        return rotation;
    }
}

package dev.despical.particletext.util;

import dev.despical.commons.configuration.ConfigUtils;
import dev.despical.commons.number.NumberUtils;
import dev.despical.particletext.ParticleTextPlugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * @author Despical
 * <p>
 * Created at 3.07.2023
 */
public class ParticleUtils {

    private static final ParticleTextPlugin plugin = JavaPlugin.getPlugin(ParticleTextPlugin.class);

    private ParticleUtils() {
    }

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

    public static Font getFont(String path) {
        final var config = ConfigUtils.getConfig(plugin, "renderers");
        final var fontAttributes = config.getString(path).split(":");

        if (fontAttributes.length != 3)
            return new Font("Tahoma", Font.PLAIN, 16);

        return new Font(fontAttributes[0], NumberUtils.getInt(fontAttributes[1], 0), NumberUtils.getInt(fontAttributes[2]));
    }
}

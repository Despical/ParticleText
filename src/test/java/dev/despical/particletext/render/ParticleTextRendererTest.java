/*
 * Particle Text - Persistent particle text for Minecraft.
 * Copyright (C) 2026  Berke Akçen
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package dev.despical.particletext.render;

import dev.despical.particletext.config.TestSettings;
import dev.despical.particletext.model.FontSpec;
import dev.despical.particletext.model.FontStyle;
import dev.despical.particletext.model.RendererData;
import dev.despical.particletext.model.RendererLocation;
import dev.despical.particletext.model.Rotation;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ParticleTextRendererTest {

    @Test
    void countsEveryViewerPacketAndContinuesAcrossLowBudgetPasses() {
        var settings = TestSettings.settings(3);
        var renderer = new ParticleTextRenderer(data(), settings, "I");
        assertEquals(3, renderer.pointCount());
        var packets = new ArrayList<String>();
        var viewers = List.of(viewer("first", packets), viewer("second", packets));
        var origin = new Location(null, 0, 0, 0);
        for (int pass = 0; pass < 6; pass++) {
            assertEquals(1, renderer.emit(settings, origin, viewers, 1));
        }
        assertEquals(6, packets.size());
        assertEquals(3, packets.stream().filter(packet -> packet.startsWith("first")).count());
        assertEquals(3, packets.stream().filter(packet -> packet.startsWith("second")).count());
        assertEquals(6, packets.stream().distinct().count());
        assertEquals(0, renderer.emit(settings, origin, viewers, 0));
        assertEquals(6, packets.size());
    }

    @Test
    void refreshesPlaceholderTextAndSkipsBlankText() {
        var settings = TestSettings.settings(3);
        var renderer = new ParticleTextRenderer(data(), settings, "I");
        var packets = new ArrayList<String>();
        var viewers = List.of(viewer("viewer", packets));
        var origin = new Location(null, 0, 0, 0);

        renderer.refreshResolvedText("", settings);
        assertEquals(0, renderer.pointCount());
        assertEquals(0, renderer.emit(settings, origin, viewers, 3));

        renderer.refreshResolvedText("I", settings);
        assertEquals(3, renderer.pointCount());
        assertEquals(3, renderer.emit(settings, origin, viewers, 3));
        assertEquals(3, packets.size());
    }

    private RendererData data() {
        return new RendererData("test", "I", Particle.FLAME, 1, false, true,
            new FontSpec("SansSerif", FontStyle.PLAIN, 16), Rotation.NONE,
            new RendererLocation("world", 0, 0, 0, 0, 0));
    }

    private Player viewer(String name, List<String> packets) {
        return (Player) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] {Player.class},
            (proxy, method, arguments) -> {
                if (method.getName().equals("spawnParticle")) {
                    packets.add(name + ":" + arguments[1] + ":" + arguments[2] + ":" + arguments[3]);
                }
                return null;
            });
    }
}

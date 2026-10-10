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

package dev.despical.particletext.service;

import dev.despical.particletext.ParticleTextPlugin;
import lombok.RequiredArgsConstructor;

import java.math.BigInteger;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.regex.Pattern;

/**
 * Checks the public plugin version asynchronously at startup.
 * <p>
 * The request uses a bounded connect and response timeout and compares the result with the installed
 * version. A newer version produces a server log notice.
 * <p>
 * A failed update request does not disable rendering or modify files. The feature is optional through
 * updates-enabled and makes no player-facing chat announcements.
 *
 * @author Despical
 * <p>
 * Created at 10.10.2026
 */
@RequiredArgsConstructor
public final class UpdateChecker {

    private static final URI VERSION_URI = URI.create("https://api.spigotmc.org/legacy/update.php?resource=110996");
    private static final Pattern VERSION = Pattern.compile(
        "[vV]?\\d+(?:\\.\\d+)*(?:-[0-9A-Za-z]+(?:[.-][0-9A-Za-z]+)*)?"
        + "(?:\\+[0-9A-Za-z]+(?:[.-][0-9A-Za-z]+)*)?");

    private final ParticleTextPlugin plugin;

    public void check() {
        HttpRequest request = HttpRequest.newBuilder(VERSION_URI)
            .timeout(Duration.ofSeconds(5))
            .header("User-Agent", "ParticleText/" + plugin.getPluginMeta().getVersion())
            .GET()
            .build();

        HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build()
            .sendAsync(request, HttpResponse.BodyHandlers.ofString())
            .thenAccept(response -> {
                String latest = response.body().trim();

                if (response.statusCode() == 200 && isNewer(latest, plugin.getPluginMeta().getVersion())) {
                    plugin.getLogger().info("A newer ParticleText version is available: v" + latest);
                }
            })
            .exceptionally(_ -> null);
    }

    static boolean isNewer(String candidate, String current) {
        if (candidate == null || current == null) {
            return false;
        }

        candidate = candidate.strip();
        current = current.strip();

        if (!VERSION.matcher(candidate).matches() || !VERSION.matcher(current).matches()) {
            return false;
        }

        String[] candidateParts = parts(candidate);
        String[] currentParts = parts(current);
        int length = Math.max(candidateParts.length, currentParts.length);

        for (int index = 0; index < length; index++) {
            BigInteger candidatePart = index < candidateParts.length
                ? new BigInteger(candidateParts[index]) : BigInteger.ZERO;
            BigInteger currentPart = index < currentParts.length
                ? new BigInteger(currentParts[index]) : BigInteger.ZERO;
            int order = candidatePart.compareTo(currentPart);

            if (order != 0) {
                return order > 0;
            }
        }

        return !isPreRelease(candidate) && isPreRelease(current);
    }

    private static String[] parts(String version) {
        return version.replaceFirst("^[vV]", "").split("[-+]", 2)[0].split("\\.");
    }

    private static boolean isPreRelease(String version) {
        return version.split("\\+", 2)[0].contains("-");
    }
}

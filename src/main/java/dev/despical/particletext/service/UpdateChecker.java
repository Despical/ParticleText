package dev.despical.particletext.service;

import dev.despical.particletext.ParticleTextPlugin;
import lombok.RequiredArgsConstructor;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@RequiredArgsConstructor
public final class UpdateChecker {

    private static final URI VERSION_URI = URI.create("https://api.spigotmc.org/legacy/update.php?resource=110996");

    private final ParticleTextPlugin plugin;

    public void check() {
        HttpRequest request = HttpRequest.newBuilder(VERSION_URI)
            .timeout(Duration.ofSeconds(5))
            .header("User-Agent", "ParticleText/" + plugin.getPluginMeta().getVersion())
            .GET()
            .build();

        HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build()
            .sendAsync(request, HttpResponse.BodyHandlers.ofString())
            .thenApply(HttpResponse::body)
            .thenApply(String::trim)
            .thenAccept(latest -> {
                if (VersionComparator.isNewer(latest, plugin.getPluginMeta().getVersion())) {
                    plugin.getLogger().info("A newer ParticleText version is available: v" + latest);
                }
            })
            .exceptionally(exception -> null);
    }
}

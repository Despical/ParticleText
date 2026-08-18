package dev.despical.particletext.model;

import lombok.experimental.UtilityClass;
import org.bukkit.Particle;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@UtilityClass
public class ParticleSupport {

    private final List<String> NAMES = Arrays.stream(Particle.values())
        .filter(particle -> particle.getDataType() == Void.class)
        .map(Particle::name)
        .sorted()
        .toList();

    public static Optional<Particle> find(String name) {
        if (name == null) {
            return Optional.empty();
        }

        try {
            Particle particle = Particle.valueOf(name.toUpperCase(Locale.ENGLISH));

            return particle.getDataType() == Void.class ? Optional.of(particle) : Optional.empty();
        } catch (IllegalArgumentException _) {
            return Optional.empty();
        }
    }

    public static List<String> names() {
        return NAMES;
    }
}

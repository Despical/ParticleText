package dev.despical.particletext.message;

public record Var(String name, Object value) {

    public static Var of(String name, Object value) {
        return new Var(name, value);
    }
}

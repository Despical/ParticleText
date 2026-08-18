package dev.despical.particletext.papi;

import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.Nullable;

public final class PlainTextResolver implements TextResolver {

    @Override
    public String resolve(@Nullable OfflinePlayer player, String text) {
        return text;
    }
}

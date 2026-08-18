package dev.despical.particletext.papi;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.Nullable;

public final class PlaceholderApiTextResolver implements TextResolver {

    @Override
    public String resolve(@Nullable OfflinePlayer player, String text) {
        return PlaceholderAPI.setPlaceholders(player, text);
    }
}

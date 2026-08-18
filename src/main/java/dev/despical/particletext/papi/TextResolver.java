package dev.despical.particletext.papi;

import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.Nullable;

public interface TextResolver {

    String resolve(@Nullable OfflinePlayer player, String text);
}

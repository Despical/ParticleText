package dev.despical.particletext.user;

import dev.despical.particletext.ParticleTextPlugin;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author Despical
 * <p>
 * Created at 3.07.2023
 */
public class UserManager {

    private final Map<UUID, User> users;

    public UserManager(ParticleTextPlugin plugin) {
        this.users = new HashMap<>();

        plugin.getServer().getOnlinePlayers().forEach(this::addUser);
    }

    @NotNull
    public User addUser(Player player) {
        User user = new User(player);

        users.put(player.getUniqueId(), user);
        return user;
    }

    public void removeUser(Player player) {
        users.remove(player.getUniqueId());
    }

    @NotNull
    public User getUser(Player player) {
        User user = users.get(player.getUniqueId());

        if (user != null) {
            return user;
        }

        return this.addUser(player);
    }
}

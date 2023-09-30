package me.despical.particletext.users;

import me.despical.particletext.Main;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Despical
 * <p>
 * Created at 3.07.2023
 */
public class UserManager {

    @NotNull
    private final Set<User> users;

    public UserManager(Main plugin) {
        this.users = new HashSet<>();

        plugin.getServer().getOnlinePlayers().forEach(this::getUser);
    }

    @NotNull
    public User addUser(final Player player) {
        final var user = new User(player);

        this.users.add(user);
        return user;
    }

    public void removeUser(final Player player) {
        this.users.remove(this.getUser(player));
    }

    @NotNull
    public User getUser(final Player player) {
        final var uuid = player.getUniqueId();

        for (var user : this.users) {
            if (uuid.equals(user.getUniqueId())) {
                return user;
            }
        }

        return this.addUser(player);
    }
}
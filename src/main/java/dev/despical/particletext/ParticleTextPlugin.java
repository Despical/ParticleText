package dev.despical.particletext;

import dev.despical.commandframework.CommandFramework;
import dev.despical.commons.util.UpdateChecker;
import dev.despical.particletext.command.ParticleCommands;
import dev.despical.particletext.event.JoinQuitEvents;
import dev.despical.particletext.handler.ChatManager;
import dev.despical.particletext.particle.ParticleHandler;
import dev.despical.particletext.particle.ParticleRenderer;
import dev.despical.particletext.user.UserManager;
import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * @author Despical
 * <p>
 * Created at 3.07.2023
 */
public class ParticleTextPlugin extends JavaPlugin {

    private ChatManager chatManager;
    private CommandFramework commandFramework;
    private ParticleHandler particleHandler;
    private UserManager userManager;

    @Override
    public void onEnable() {
        initializeClasses();
        checkUpdate();

        getLogger().info("Initialization finished.");
        getLogger().info("Join our Discord server: https://discord.gg/uXVU8jmtpU");
    }

    @Override
    public void onDisable() {
        particleHandler.getRenderers()
            .values()
            .stream()
            .filter(Objects::nonNull)
            .forEach(ParticleRenderer::stopRendering);
    }

    private void initializeClasses() {
        createFiles();

        chatManager = new ChatManager(this);
        commandFramework = new CommandFramework(this);
        particleHandler = new ParticleHandler(this);
        userManager = new UserManager(this);

        new ParticleCommands(this);

        new JoinQuitEvents(this);
        new Metrics(this, 18978);
    }

    private void createFiles() {
        saveDefaultConfig();

        Stream.of("messages", "renderers")
            .map(fileName -> new File(getDataFolder(), fileName + ".yml"))
            .filter(Predicate.not(File::exists))
            .forEach(file -> saveResource(file.getName(), false));
    }

    @NotNull
    public ChatManager getChatManager() {
        return chatManager;
    }

    @NotNull
    public CommandFramework getCommandFramework() {
        return commandFramework;
    }

    @NotNull
    public ParticleHandler getParticleHandler() {
        return particleHandler;
    }

    @NotNull
    public UserManager getUserManager() {
        return userManager;
    }

    private void checkUpdate() {
        if (!getConfig().getBoolean("Updates-Enabled", true)) return;

        UpdateChecker.init(this, 110996).onNewUpdate(result -> getLogger().info("Found a new version available: v" + result.getNewestVersion()));
    }
}

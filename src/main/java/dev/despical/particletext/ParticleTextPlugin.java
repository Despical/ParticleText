package dev.despical.particletext;

import dev.despical.commandframework.CommandErrorMessage;
import dev.despical.commandframework.CommandFramework;
import dev.despical.particletext.command.Arguments;
import dev.despical.particletext.config.SettingsManager;
import dev.despical.particletext.message.MessageService;
import dev.despical.particletext.message.Var;
import dev.despical.particletext.papi.ParticleTextExpansion;
import dev.despical.particletext.papi.PlaceholderApiTextResolver;
import dev.despical.particletext.papi.PlainTextResolver;
import dev.despical.particletext.papi.TextResolver;
import dev.despical.particletext.persistence.RendererRepository;
import dev.despical.particletext.render.RendererService;
import dev.despical.particletext.service.UpdateChecker;
import lombok.Getter;
import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;
import java.util.stream.Stream;

@Getter
public final class ParticleTextPlugin extends JavaPlugin {

    private static ParticleTextPlugin instance;

    private SettingsManager settingsManager;
    private MessageService messages;
    private RendererService rendererService;
    private CommandFramework commandFramework;

    @Override
    public void onEnable() {
        instance = this;
        settingsManager = new SettingsManager(this);

        TextResolver textResolver = createTextResolver();
        messages = new MessageService(this, textResolver);

        RendererRepository repository = new RendererRepository(this);
        rendererService = new RendererService(this, settingsManager, repository, textResolver);

        registerCommands();
        registerPlaceholderExpansion();

        rendererService.start();

        new Metrics(this, 18978);

        if (settingsManager.current().updatesEnabled()) {
            new UpdateChecker(this).check();
        }

        int rendererAmount = rendererService.all().size();
        getLogger().log(Level.INFO, "ParticleText v{0} initialized with {1} renderer{2}.",
            new Object[] { getPluginMeta().getVersion(), rendererAmount, rendererAmount > 1 ? "s" : ""});
    }

    @Override
    public void onDisable() {
        if (rendererService != null) {
            rendererService.stop();
        }

        instance = null;
    }

    public void reloadPlugin() {
        settingsManager.reload();
        messages.reload();
        rendererService.reload();
    }

    private TextResolver createTextResolver() {
        if (getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            return new PlaceholderApiTextResolver();
        }

        return new PlainTextResolver();
    }

    private void registerCommands() {
        commandFramework = new CommandFramework(this);
        commandFramework.setDefaultArguments(Arguments::new);
        commandFramework.registerAllInPackage("dev.despical.particletext.command");

        Stream.of(CommandErrorMessage.SHORT_ARG_SIZE, CommandErrorMessage.LONG_ARG_SIZE).forEach(error ->
            error.setHandler((command, arguments) -> {
                messages.send(arguments.getSender(), "correct-usage",
                    Var.of("%usage%", command.usage().replace("%label%", arguments.getLabel())));
                return true;
            }));

        CommandErrorMessage.ONLY_BY_PLAYERS.setHandler((_, arguments) -> {
            messages.send(arguments.getSender(), "player-only");
            return true;
        });
    }

    private void registerPlaceholderExpansion() {
        if (getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new ParticleTextExpansion(this).register();

            getLogger().info("PlaceholderAPI integration enabled.");
        }
    }

    @NotNull
    public static ParticleTextPlugin getInstance() {
        if (instance == null) {
            throw new IllegalStateException("ParticleText is not enabled");
        }

        return instance;
    }
}

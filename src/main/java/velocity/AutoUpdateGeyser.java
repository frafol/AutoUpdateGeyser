package velocity;

import com.moandjiezana.toml.Toml;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Dependency;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import common.Floodgate;
import common.Geyser;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static common.BuildYml.createYamlFile;
import static common.BuildYml.updateBuildNumber;

@Plugin(id = "autoupdategeyser",name = "AutoUpdateGeyser",version = "1.0", url = "https://www.spigotmc.org/resources/autoupdategeyser.109632/", authors = "NewAmazingPVP & frafol", dependencies = {
        @Dependency(id = "geyser", optional = true), @Dependency(id = "floodgate", optional = true)})
public final class AutoUpdateGeyser {

    private Geyser m_geyser;
    private Floodgate m_floodgate;
    private final Toml config;
    private final ProxyServer proxy;
    private final Metrics.Factory metricsFactory;
    private final Path dataDirectory;
    private PluginContainer ifGeyser;
    private PluginContainer ifFloodgate;
    private boolean configGeyser;
    private boolean configFloodgate;

    @Inject
    public AutoUpdateGeyser(ProxyServer proxy, @DataDirectory Path dataDirectory, Metrics.Factory metricsFactory) {
        this.proxy = proxy;
        this.metricsFactory = metricsFactory;
        this.dataDirectory = dataDirectory;
        config = loadConfig(dataDirectory);
    }

    @Subscribe
    public void onProxyInitialize(ProxyInitializeEvent event) {
        metricsFactory.make(this, 18448);
        m_geyser = new Geyser();
        m_floodgate = new Floodgate();
        createYamlFile(dataDirectory.toAbsolutePath().toString());
        loadConfiguration();
        proxy.getConsoleCommandSource().sendMessage(Component.text("AutoUpdateGeyser started correctly.", NamedTextColor.GREEN));
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        proxy.getConsoleCommandSource().sendMessage(Component.text("Starting AutoUpdateGeyser process...", NamedTextColor.GREEN));
        updatePlugin("Geyser", ifGeyser, configGeyser);
        updatePlugin("Floodgate", ifFloodgate, configFloodgate);
    }

    public void loadConfiguration() {
        ifGeyser = proxy.getPluginManager().getPlugin("geyser").orElse(null);
        ifFloodgate = proxy.getPluginManager().getPlugin("floodgate").orElse(null);
        configGeyser = config.getBoolean("updates.geyser");
        configFloodgate = config.getBoolean("updates.floodgate");
    }

    private void updatePlugin(String pluginName, PluginContainer pluginInstance, boolean configCheck) {
        if (pluginInstance == null && configCheck) {
            updateBuildNumber(pluginName, -1);
            if (updatePluginInstallation(pluginName)) {
                proxy.getConsoleCommandSource().sendMessage(Component.text(pluginName + " has been installed for the first time.", NamedTextColor.GREEN));
            }
        } else if (configCheck) {
            if (updatePluginInstallation(pluginName)) {
                proxy.getConsoleCommandSource().sendMessage(Component.text("A new update of " + pluginName + " was downloaded.", NamedTextColor.GREEN));
            }
        }
    }

    private boolean updatePluginInstallation(String pluginName) {
        return switch (pluginName) {
            case "Geyser" -> m_geyser.updateGeyser("Velocity");
            case "Floodgate" -> m_floodgate.updateFloodgate("Velocity");
            default -> false;
        };
    }

    private Toml loadConfig(Path path) {
        File folder = path.toFile();
        File file = new File(folder, "config.toml");
        if (!file.exists()) {
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            try (InputStream input = getClass().getResourceAsStream("/" + file.getName())) {
                if (input != null) {
                    Files.copy(input, file.toPath());
                } else {
                    file.createNewFile();
                }
            } catch (IOException exception) {
                return null;
            }
        }
        return new Toml().read(file);
    }
}

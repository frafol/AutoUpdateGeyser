package bungeecord;

import common.Floodgate;
import common.Geyser;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import net.md_5.bungee.api.ChatColor;

import java.io.*;
import java.nio.file.Files;

import static common.BuildYml.createYamlFile;
import static common.BuildYml.updateBuildNumber;

public final class AutoUpdateGeyser extends Plugin {

    private Geyser m_geyser;
    private Floodgate m_floodgate;
    private Configuration config;
    private Plugin ifGeyser;
    private Plugin ifFloodgate;
    private boolean configGeyser;
    private boolean configFloodgate;

    @Override
    public void onEnable() {
        new Metrics(this, 18449);
        m_geyser = new Geyser();
        m_floodgate = new Floodgate();
        saveDefaultConfig();
        createYamlFile(getDataFolder().getAbsolutePath());
        loadConfiguration();
        getLogger().info(org.bukkit.ChatColor.GREEN + "AutoGeyserUpdate started correctly.");
    }

    @Override
    public void onDisable() {
        updatePlugin("Geyser", ifGeyser, configGeyser);
        updatePlugin("Floodgate", ifFloodgate, configFloodgate);
    }

    private void updatePlugin(String pluginName, Object pluginInstance, boolean configCheck) {
        if (pluginInstance == null && configCheck) {
            updateBuildNumber(pluginName, -1);
            if (updatePluginInstallation(pluginName)) {
                getLogger().info(ChatColor.GREEN + pluginName + " has been installed for the first time.");
            }
        } else if (configCheck) {
            if (updatePluginInstallation(pluginName)) {
                getLogger().info(ChatColor.GREEN + "A new update of " + pluginName + " was downloaded.");
            }
        }
    }

    private boolean updatePluginInstallation(String pluginName) {
        return switch (pluginName) {
            case "Geyser" -> m_geyser.updateGeyser("BungeeCord");
            case "Floodgate" -> m_floodgate.updateFloodgate("BungeeCord");
            default -> false;
        };
    }

    private void saveDefaultConfig() {
        File file = new File(getDataFolder(), "config.yml");
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            try (InputStream in = getClass().getResourceAsStream("/config.yml")) {
                assert in != null;
                Files.copy(in, file.toPath());
            } catch (IOException ignored) {}
        }
    }

    private void loadConfiguration() {
        File file = new File(getDataFolder(), "config.yml");
        try {
            config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        ifGeyser = getProxy().getPluginManager().getPlugin("Geyser-BungeeCord");
        ifFloodgate = getProxy().getPluginManager().getPlugin("floodgate");
        configGeyser = config.getBoolean("updates.geyser");
        configFloodgate = config.getBoolean("updates.floodgate");
    }
}

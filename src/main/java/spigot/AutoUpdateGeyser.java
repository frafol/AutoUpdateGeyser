package spigot;

import common.Floodgate;
import common.Geyser;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Bukkit;

import static common.BuildYml.createYamlFile;
import static common.BuildYml.updateBuildNumber;

public final class AutoUpdateGeyser extends JavaPlugin {

    private Geyser m_geyser;
    private Floodgate m_floodgate;
    private FileConfiguration config;
    private Plugin ifGeyser;
    private Plugin ifFloodgate;
    private boolean configGeyser;
    private boolean configFloodgate;

    @Override
    public void onEnable() {
        new Metrics(this, 18445);
        m_geyser = new Geyser();
        m_floodgate = new Floodgate();
        saveDefaultConfig();
        createYamlFile(getDataFolder().getAbsolutePath());
        loadConfiguration();
        getLogger().info(ChatColor.GREEN + "AutoGeyserUpdate started correctly.");
    }

    @Override
    public void onDisable() {
        getLogger().info(ChatColor.GREEN + "Starting AutoGeyserUpdate process...");
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
            case "Geyser" -> m_geyser.updateGeyser("Spigot");
            case "Floodgate" -> m_floodgate.updateFloodgate("Spigot");
            default -> false;
        };
    }

    public void loadConfiguration(){
        config = getConfig();
        config.addDefault("updates.geyser", true);
        config.addDefault("updates.floodgate", false);
        config.options().copyDefaults(true);
        saveConfig();
        ifGeyser = Bukkit.getPluginManager().getPlugin("Geyser-Spigot");
        ifFloodgate = Bukkit.getPluginManager().getPlugin("floodgate");
        configGeyser = config.getBoolean("updates.geyser");
        configFloodgate = config.getBoolean("updates.floodgate");
    }
}

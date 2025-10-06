package me.wilcodwg.borderguardian.managers;

import me.wilcodwg.borderguardian.BorderGuardian;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.List;

public class ConfigManager {

    private final BorderGuardian plugin;
    private FileConfiguration config;
    private FileConfiguration messages;

    public ConfigManager(BorderGuardian plugin) {
        this.plugin = plugin;
        loadConfigs();
    }

    private void loadConfigs() {
        // Save default configs
        plugin.saveDefaultConfig();
        saveDefaultMessages();
        
        // Load configs
        config = plugin.getConfig();
        messages = loadMessagesConfig();
    }

    private void saveDefaultMessages() {
        if (!new File(plugin.getDataFolder(), "messages.yml").exists()) {
            plugin.saveResource("messages.yml", false);
        }
    }

    private FileConfiguration loadMessagesConfig() {
        File messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        return YamlConfiguration.loadConfiguration(messagesFile);
    }

    public void reloadConfigs() {
        plugin.reloadConfig();
        config = plugin.getConfig();
        messages = loadMessagesConfig();
    }

    // Config getters
    public double getDefaultRadius() {
        return config.getDouble("border.default-radius", 1000);
    }

    public String getBorderWorld() {
        return config.getString("border.world", "");
    }

    public double getCenterX() {
        return config.getDouble("border.center.x", 0);
    }

    public double getCenterZ() {
        return config.getDouble("border.center.z", 0);
    }

    public double getShrinkRate() {
        return config.getDouble("animation.shrink-rate", 1.0);
    }

    public double getExpandRate() {
        return config.getDouble("animation.expand-rate", 1.0);
    }

    public boolean showActionBar() {
        return config.getBoolean("animation.show-action-bar", true);
    }

    public int getUpdateInterval() {
        return config.getInt("animation.update-interval", 20);
    }

    public double getDamageAmount() {
        return config.getDouble("damage.damage-amount", 2.0);
    }

    public int getDamageInterval() {
        return config.getInt("damage.damage-interval", 20);
    }

    public double getWarningDistance() {
        return config.getDouble("damage.warning-distance", 5);
    }

    public String getDamageType() {
        return config.getString("damage.damage-type", "GENERIC");
    }

    public boolean showParticles() {
        return config.getBoolean("effects.show-particles", true);
    }

    public String getParticleType() {
        return config.getString("effects.particle-type", "FLAME");
    }

    public int getParticleDensity() {
        return config.getInt("effects.particle-density", 3);
    }

    public boolean showWarnings() {
        return config.getBoolean("effects.show-warnings", true);
    }

    public double getWarningTitleDistance() {
        return config.getDouble("effects.warning-title-distance", 10);
    }

    public int getCheckInterval() {
        return config.getInt("performance.check-interval", 10);
    }

    public int getMaxPlayersPerCycle() {
        return config.getInt("performance.max-players-per-cycle", 20);
    }

    // Message getters
    public String getMessage(String path) {
        String message = messages.getString(path, "&cMessage not found: " + path);
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public String getMessage(String path, String... placeholders) {
        String message = getMessage(path);
        for (int i = 0; i < placeholders.length; i += 2) {
            if (i + 1 < placeholders.length) {
                message = message.replace(placeholders[i], placeholders[i + 1]);
            }
        }
        return message;
    }

    public List<String> getMessageList(String path) {
        List<String> messages = this.messages.getStringList(path);
        for (int i = 0; i < messages.size(); i++) {
            messages.set(i, ChatColor.translateAlternateColorCodes('&', messages.get(i)));
        }
        return messages;
    }

    public String getPrefix() {
        return getMessage("prefix");
    }
}
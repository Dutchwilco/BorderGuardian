package me.wilcodwg.borderguardian;

import me.wilcodwg.borderguardian.commands.BorderCommand;
import me.wilcodwg.borderguardian.listeners.PlayerListener;
import me.wilcodwg.borderguardian.managers.BorderManager;
import me.wilcodwg.borderguardian.managers.ConfigManager;
import me.wilcodwg.borderguardian.managers.DamageManager;
import me.wilcodwg.borderguardian.managers.EffectsManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class BorderGuardian extends JavaPlugin {

    private static BorderGuardian instance;
    private ConfigManager configManager;
    private BorderManager borderManager;
    private DamageManager damageManager;
    private EffectsManager effectsManager;

    @Override
    public void onEnable() {
        instance = this;
        
        // Initialize managers
        configManager = new ConfigManager(this);
        borderManager = new BorderManager(this);
        damageManager = new DamageManager(this);
        effectsManager = new EffectsManager(this);
        
        // Register commands and listeners
        getCommand("border").setExecutor(new BorderCommand(this));
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        
        // Start managers
        borderManager.initialize();
        damageManager.startDamageTask();
        effectsManager.startEffectsTask();
        
        getLogger().info("BorderGuardian has been enabled!");
    }

    @Override
    public void onDisable() {
        if (borderManager != null) {
            borderManager.shutdown();
        }
        if (damageManager != null) {
            damageManager.stopDamageTask();
        }
        if (effectsManager != null) {
            effectsManager.stopEffectsTask();
        }
        
        getLogger().info("BorderGuardian has been disabled!");
    }

    public static BorderGuardian getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public BorderManager getBorderManager() {
        return borderManager;
    }

    public DamageManager getDamageManager() {
        return damageManager;
    }

    public EffectsManager getEffectsManager() {
        return effectsManager;
    }
}
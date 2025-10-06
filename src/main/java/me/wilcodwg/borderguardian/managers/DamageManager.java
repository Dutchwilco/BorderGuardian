package me.wilcodwg.borderguardian.managers;

import me.wilcodwg.borderguardian.BorderGuardian;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class DamageManager {

    private final BorderGuardian plugin;
    private final ConfigManager configManager;
    private final BorderManager borderManager;
    private BukkitTask damageTask;

    public DamageManager(BorderGuardian plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
        this.borderManager = plugin.getBorderManager();
    }

    public void startDamageTask() {
        int damageInterval = configManager.getDamageInterval();
        
        damageTask = new BukkitRunnable() {
            @Override
            public void run() {
                processDamage();
            }
        }.runTaskTimer(plugin, 0L, damageInterval);
    }

    private void processDamage() {
        double damageAmount = configManager.getDamageAmount();
        
        for (Player player : borderManager.getBorderWorld().getPlayers()) {
            // Skip players with bypass permission
            if (player.hasPermission("borderguardian.bypass")) {
                continue;
            }
            
            // Check if player is outside border
            if (borderManager.isOutsideBorder(player.getLocation())) {
                // Create damage event
                EntityDamageEvent.DamageCause cause;
                try {
                    cause = EntityDamageEvent.DamageCause.valueOf(configManager.getDamageType());
                } catch (IllegalArgumentException e) {
                    cause = EntityDamageEvent.DamageCause.CUSTOM;
                }

                // Apply damage
                player.damage(damageAmount);
                
                // Optional: Add custom damage event
                EntityDamageEvent damageEvent = new EntityDamageEvent(player, cause, damageAmount);
                plugin.getServer().getPluginManager().callEvent(damageEvent);
            }
        }
    }

    public void stopDamageTask() {
        if (damageTask != null && !damageTask.isCancelled()) {
            damageTask.cancel();
        }
    }
}
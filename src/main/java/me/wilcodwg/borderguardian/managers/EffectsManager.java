package me.wilcodwg.borderguardian.managers;

import me.wilcodwg.borderguardian.BorderGuardian;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EffectsManager {

    private final BorderGuardian plugin;
    private final ConfigManager configManager;
    private final BorderManager borderManager;
    private BukkitTask effectsTask;
    private final Map<UUID, Long> lastWarningTime = new HashMap<>();

    public EffectsManager(BorderGuardian plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
        this.borderManager = plugin.getBorderManager();
    }

    public void startEffectsTask() {
        int checkInterval = configManager.getCheckInterval();
        
        effectsTask = new BukkitRunnable() {
            @Override
            public void run() {
                processEffects();
            }
        }.runTaskTimer(plugin, 0L, checkInterval);
    }

    private void processEffects() {
        for (Player player : borderManager.getBorderWorld().getPlayers()) {
            processPlayerEffects(player);
        }
        
        if (configManager.showParticles()) {
            spawnBorderParticles();
        }
    }

    private void processPlayerEffects(Player player) {
        // While the border is animating and action bar is enabled, don't override the animation action bar
        if (configManager.showActionBar() && borderManager.isAnimating()) {
            return;
        }

        double distanceToBorder = borderManager.getDistanceToBorder(player.getLocation());
        double warningDistance = configManager.getWarningDistance();
        double warningTitleDistance = configManager.getWarningTitleDistance();
        
        // Clear action bar first
        boolean shouldShowActionBar = false;
        String actionBarMessage = "";
        
        // Check if player is outside border
        if (distanceToBorder < 0) {
            shouldShowActionBar = true;
            actionBarMessage = configManager.getMessage("actionbar.danger");
            
            // Show danger title
            if (configManager.showWarnings()) {
                showWarningTitle(player, "titles.danger.title", "titles.danger.subtitle");
            }
        }
        // Check if player is approaching border
        else if (distanceToBorder < warningTitleDistance && distanceToBorder > 0) {
            shouldShowActionBar = true;
            actionBarMessage = configManager.getMessage("actionbar.warning");
            
            // Show warning title
            if (configManager.showWarnings()) {
                showWarningTitle(player, "titles.warning.title", "titles.warning.subtitle");
            }
        }
        
        // Send action bar
        if (shouldShowActionBar) {
            player.sendActionBar(Component.text(actionBarMessage));
        } else {
            // Clear action bar if player is safe
            player.sendActionBar(Component.empty());
        }
    }

    private void showWarningTitle(Player player, String titlePath, String subtitlePath) {
        UUID playerId = player.getUniqueId();
        long currentTime = System.currentTimeMillis();
        long lastWarning = lastWarningTime.getOrDefault(playerId, 0L);
        
        // Only show title every 3 seconds to avoid spam
        if (currentTime - lastWarning > 3000) {
            String titleText = configManager.getMessage(titlePath);
            String subtitleText = configManager.getMessage(subtitlePath);
            
            Title title = Title.title(
                Component.text(titleText),
                Component.text(subtitleText),
                Title.Times.times(
                    Duration.ofMillis(500),  // Fade in
                    Duration.ofMillis(2000), // Stay
                    Duration.ofMillis(500)   // Fade out
                )
            );
            
            player.showTitle(title);
            lastWarningTime.put(playerId, currentTime);
        }
    }

    private void spawnBorderParticles() {
        // This is a simplified particle system - you might want to make it more sophisticated
        double radius = borderManager.getCurrentRadius();
        double centerX = borderManager.getWorldBorder().getCenter().getX();
        double centerZ = borderManager.getWorldBorder().getCenter().getZ();
        
        Particle particleType;
        try {
            particleType = Particle.valueOf(configManager.getParticleType());
        } catch (IllegalArgumentException e) {
            particleType = Particle.FLAME;
        }
        
        // Spawn particles at regular intervals around the border
        int particleDensity = configManager.getParticleDensity();
        double angleStep = (2 * Math.PI) / (radius * particleDensity / 10); // Adjust density
        
        for (double angle = 0; angle < 2 * Math.PI; angle += angleStep) {
            double x = centerX + radius * Math.cos(angle);
            double z = centerZ + radius * Math.sin(angle);
            
            // Find a suitable Y coordinate (simplified - just use world surface)
            Location particleLocation = new Location(borderManager.getBorderWorld(), x, 
                borderManager.getBorderWorld().getHighestBlockYAt((int) x, (int) z) + 1, z);
            
            // Only spawn particles if players are nearby to see them
            boolean hasNearbyPlayers = borderManager.getBorderWorld().getPlayers().stream()
                .anyMatch(p -> p.getLocation().distance(particleLocation) < 50);
                
            if (hasNearbyPlayers) {
                borderManager.getBorderWorld().spawnParticle(particleType, particleLocation, 1, 0, 0, 0, 0);
            }
        }
    }

    public void stopEffectsTask() {
        if (effectsTask != null && !effectsTask.isCancelled()) {
            effectsTask.cancel();
        }
        lastWarningTime.clear();
    }
}
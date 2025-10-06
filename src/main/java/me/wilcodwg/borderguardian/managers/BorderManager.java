package me.wilcodwg.borderguardian.managers;

import me.wilcodwg.borderguardian.BorderGuardian;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class BorderManager {

    private final BorderGuardian plugin;
    private final ConfigManager configManager;
    private World borderWorld;
    private WorldBorder worldBorder;
    private double currentRadius;
    private boolean isAnimating = false;
    private BukkitTask animationTask;
    private BukkitTask finishTask;

    public BorderManager(BorderGuardian plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
    }

    public void initialize() {
        // Get the world for the border
        String worldName = configManager.getBorderWorld();
        if (worldName.isEmpty()) {
            borderWorld = Bukkit.getWorlds().get(0); // Default to first world
        } else {
            borderWorld = Bukkit.getWorld(worldName);
            if (borderWorld == null) {
                plugin.getLogger().warning("World '" + worldName + "' not found! Using default world.");
                borderWorld = Bukkit.getWorlds().get(0);
            }
        }

        // Get world border
        worldBorder = borderWorld.getWorldBorder();
        
        // Disable vanilla border damage and effects to use plugin's system
        worldBorder.setDamageAmount(0.0);
        worldBorder.setDamageBuffer(0.0);
        worldBorder.setWarningDistance(0);
        worldBorder.setWarningTime(0);
        
        // Set initial border settings
        double centerX = configManager.getCenterX();
        double centerZ = configManager.getCenterZ();
        worldBorder.setCenter(centerX, centerZ);
        
        currentRadius = configManager.getDefaultRadius();
        worldBorder.setSize(currentRadius * 2); // WorldBorder uses diameter, not radius
        
        plugin.getLogger().info("Border initialized in world '" + borderWorld.getName() + 
                               "' with radius " + currentRadius + " at center (" + centerX + ", " + centerZ + ")");
        plugin.getLogger().info("Vanilla border damage and warnings disabled - using plugin system");
    }

    public void setBorderRadius(double radius) {
        if (isAnimating) return;
        
        currentRadius = radius;
        worldBorder.setSize(radius * 2); // WorldBorder uses diameter
        saveBorderRadius(radius);
    }
    
    private void saveBorderRadius(double radius) {
        plugin.getConfig().set("border.default-radius", radius);
        plugin.saveConfig();
    }

    public void shrinkBorder(double amount, Player sender) {
        if (isAnimating) {
            sender.sendMessage(configManager.getPrefix() + configManager.getMessage("commands.already-animating"));
            return;
        }

        double newRadius = Math.max(1, getCurrentRadius() - amount);
        double shrinkRate = getAnimationSpeed();
        
        sender.sendMessage(configManager.getPrefix() + configManager.getMessage("commands.border-shrinking",
                "%amount%", String.valueOf(amount),
                "%rate%", String.valueOf(shrinkRate)));

        animateBorder(newRadius, shrinkRate, true);
    }

    public void expandBorder(double amount, Player sender) {
        if (isAnimating) {
            sender.sendMessage(configManager.getPrefix() + configManager.getMessage("commands.already-animating"));
            return;
        }

        double newRadius = Math.min(30000000, getCurrentRadius() + amount);
        double expandRate = getAnimationSpeed();
        
        sender.sendMessage(configManager.getPrefix() + configManager.getMessage("commands.border-expanding",
                "%amount%", String.valueOf(amount),
                "%rate%", String.valueOf(expandRate)));

        animateBorder(newRadius, expandRate, false);
    }

    private void animateBorder(double targetRadius, double rate, boolean shrinking) {
        isAnimating = true;

        double startRadius = getCurrentRadius();
        double distance = Math.abs(startRadius - targetRadius);
        long seconds = Math.max(1L, Math.round(distance / Math.max(0.001, rate)));

        // Use built-in smooth transition (diameter)
        worldBorder.setSize(targetRadius * 2, seconds);

        // Action bar updates during animation
        if (configManager.showActionBar()) {
            String actionBarMessage = shrinking ?
                    configManager.getMessage("actionbar.shrinking") :
                    configManager.getMessage("actionbar.expanding");

            if (animationTask != null && !animationTask.isCancelled()) {
                animationTask.cancel();
            }
            animationTask = new BukkitRunnable() {
                @Override
                public void run() {
                    for (Player player : borderWorld.getPlayers()) {
                        player.sendActionBar(Component.text(actionBarMessage));
                    }
                }
            }.runTaskTimer(plugin, 0L, 20L); // every second
        }

        // Clean up when animation completes
        if (finishTask != null && !finishTask.isCancelled()) {
            finishTask.cancel();
        }
        finishTask = new BukkitRunnable() {
            @Override
            public void run() {
                // Snap to exact final size and update state
                worldBorder.setSize(targetRadius * 2);
                currentRadius = targetRadius;
                isAnimating = false;

                // Stop action bar and clear it
                if (animationTask != null && !animationTask.isCancelled()) {
                    animationTask.cancel();
                }
                for (Player player : borderWorld.getPlayers()) {
                    player.sendActionBar(Component.empty());
                }

                String completeMessage = shrinking ?
                        configManager.getMessage("commands.shrink-complete") :
                        configManager.getMessage("commands.expand-complete");
                plugin.getLogger().info(completeMessage);
                
                // Save the new radius to config
                saveBorderRadius(targetRadius);
            }
        }.runTaskLater(plugin, seconds * 20L);
    }

    public void setBorderCenter(double x, double z) {
        worldBorder.setCenter(x, z);
    }

    public double getDistanceToBorder(Location location) {
        if (!location.getWorld().equals(borderWorld)) {
            return Double.MAX_VALUE;
        }

        double centerX = worldBorder.getCenter().getX();
        double centerZ = worldBorder.getCenter().getZ();
        
        // Square border calculation - distance to nearest edge
        double x = Math.abs(location.getX() - centerX);
        double z = Math.abs(location.getZ() - centerZ);
        double radius = worldBorder.getSize() / 2.0;
        
        // Return the minimum distance to any border edge
        double xDistance = radius - x;
        double zDistance = radius - z;
        return Math.min(xDistance, zDistance);
    }

    public boolean isOutsideBorder(Location location) {
        if (!location.getWorld().equals(borderWorld)) {
            return false;
        }
        
        double centerX = worldBorder.getCenter().getX();
        double centerZ = worldBorder.getCenter().getZ();
        double radius = worldBorder.getSize() / 2.0;
        
        // Square border - check if player is outside the square boundary
        double x = Math.abs(location.getX() - centerX);
        double z = Math.abs(location.getZ() - centerZ);
        return x > radius || z > radius;
    }

    public boolean isNearBorder(Location location, double warningDistance) {
        double distance = getDistanceToBorder(location);
        return distance < warningDistance && distance > 0;
    }

    public void shutdown() {
        if (animationTask != null && !animationTask.isCancelled()) {
            animationTask.cancel();
        }
        if (finishTask != null && !finishTask.isCancelled()) {
            finishTask.cancel();
        }
        isAnimating = false;
    }

    // Getters
    public World getBorderWorld() {
        return borderWorld;
    }

    public WorldBorder getWorldBorder() {
        return worldBorder;
    }

    public double getCurrentRadius() {
        return worldBorder.getSize() / 2.0;
    }

    public boolean isAnimating() {
        return isAnimating;
    }
    
    public void setAnimationSpeed(double speed) {
        plugin.getConfig().set("animation.shrink-rate", speed);
        plugin.getConfig().set("animation.expand-rate", speed);
        plugin.saveConfig();
    }
    
    public double getAnimationSpeed() {
        // Use shrink-rate as the current speed (both should be same after speed command)
        return configManager.getShrinkRate();
    }
}
package me.wilcodwg.borderguardian.listeners;

import me.wilcodwg.borderguardian.BorderGuardian;
import me.wilcodwg.borderguardian.managers.BorderManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {

    private final BorderGuardian plugin;
    private final BorderManager borderManager;

    public PlayerListener(BorderGuardian plugin) {
        this.plugin = plugin;
        this.borderManager = plugin.getBorderManager();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Optional: Show border info to new players
        // You can uncomment this if you want players to be informed about the border when they join
        /*
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (event.getPlayer().isOnline()) {
                event.getPlayer().sendMessage(plugin.getConfigManager().getPrefix() + 
                    "§7World border is set to §e" + (int) borderManager.getCurrentRadius() + " §7blocks radius.");
            }
        }, 20L); // Send after 1 second delay
        */
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Clean up any player-specific data if needed
        // Currently handled by EffectsManager automatically
    }
}
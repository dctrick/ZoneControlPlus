/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.Location
 *  org.bukkit.command.CommandSender
 *  org.bukkit.entity.Player
 *  org.bukkit.plugin.java.JavaPlugin
 *  org.bukkit.scheduler.BukkitRunnable
 *  org.bukkit.util.Vector
 */
package dev.trick.zonecontrolplus.managers;

import dev.trick.zonecontrolplus.config.ConfigManager;
import dev.trick.zonecontrolplus.config.RegionConfig;
import dev.trick.zonecontrolplus.managers.RegionManager;
import dev.trick.zonecontrolplus.utils.MessageManager;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class BorderDamageManager
extends BukkitRunnable {
    private final JavaPlugin plugin;
    private final ConfigManager configManager;
    private final RegionManager regionManager;
    private final MessageManager messageManager;
    private final Map<UUID, String> playersInViolation = new HashMap<>();
    private final Map<UUID, Integer> damageWarningCounts = new HashMap<>();

    private final Map<UUID, Set<String>> previousPlayerRegions = new HashMap<>();

    public BorderDamageManager(JavaPlugin plugin, ConfigManager configManager, RegionManager regionManager, MessageManager messageManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.regionManager = regionManager;
        this.messageManager = messageManager;
    }

    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            this.checkPlayerBorderViolation(player);
        }
    }

    private void checkPlayerBorderViolation(Player player) {
        UUID playerId = player.getUniqueId();
        Set<String> currentRegions = this.regionManager.getPlayerRegions(player);
        Set<String> previousRegions = this.previousPlayerRegions.getOrDefault(playerId, new java.util.HashSet<>());

        // Check if player is currently violating a region
        if (this.playersInViolation.containsKey(playerId)) {
            String regionName = this.playersInViolation.get(playerId);
            // If player returned to the region, stop violation
            if (currentRegions.contains(regionName)) {
                this.cleanupPlayer(playerId);
            } else {
                // Still violating, continue damage
                RegionConfig config = this.regionManager.configManager.getRegionConfig(regionName);
                if (config != null && config.isDamageOnExitEnabled()) {
                     this.handleBorderViolation(player, regionName, config);
                } else {
                    // Config changed or invalid, stop
                    this.cleanupPlayer(playerId);
                }
            }
        } else {
            // Check for new violations (Leaving a region)
            for (String regionName : previousRegions) {
                if (!currentRegions.contains(regionName)) {
                     // Player left this region
                     RegionConfig config = this.regionManager.configManager.getRegionConfig(regionName);
                     if (config.isDamageOnExitEnabled()) {
                         this.playersInViolation.put(playerId, regionName);
                         this.handleBorderViolation(player, regionName, config);
                         break; // Handle one violation at a time
                     }
                }
            }
        }
        
        this.previousPlayerRegions.put(playerId, currentRegions);
    }

    private void handleBorderViolation(Player player, String regionName, RegionConfig config) {
        UUID playerId = player.getUniqueId();
        this.playersInViolation.put(playerId, regionName);
        int warningCount = this.damageWarningCounts.getOrDefault(playerId, 0);
        if (warningCount < 3 || warningCount % 10 == 0) {
            this.messageManager.sendMessage((CommandSender)player, "border.damage-warning");
            this.damageWarningCounts.put(playerId, warningCount + 1);
        }
        double damage = config.getDamagePerSecond() * ((double)this.configManager.getBorderDamageInterval() / 20.0);
        player.damage(damage);
        HashMap<String, String> placeholders = new HashMap<>();
        placeholders.put("damage", String.format("%.1f", damage));
        this.messageManager.sendMessage((CommandSender)player, "border.damage-tick", placeholders);
        if (config.isPushBack()) {
            this.pushPlayerBackToRegion(player, regionName);
        }
        if (this.configManager.isDebugMode()) {
            this.plugin.getLogger().info("Applied border damage to " + player.getName() + " for leaving region: " + regionName + " (" + damage + " damage)");
        }
    }

    private void pushPlayerBackToRegion(Player player, String regionName) {
        Location currentLocation = player.getLocation();
        Location lastKnownLocation = this.regionManager.getLastKnownLocation(player);
        if (lastKnownLocation != null && lastKnownLocation.getWorld().equals(currentLocation.getWorld())) {
            Vector pushVector = lastKnownLocation.toVector().subtract(currentLocation.toVector());
            pushVector.normalize().multiply(0.5);
            player.setVelocity(pushVector);
            this.messageManager.sendMessage((CommandSender)player, "border.pushed-back");
            if (this.configManager.isDebugMode()) {
                this.plugin.getLogger().info("Pushed " + player.getName() + " back towards region: " + regionName);
            }
        }
    }

    private void cleanupPlayer(UUID playerId) {
        this.playersInViolation.remove(playerId);
        this.damageWarningCounts.remove(playerId);
        // Do not remove previousPlayerRegions here, as it's needed for state tracking while online.
        // It is removed in onPlayerQuit.
    }

    public void onPlayerQuit(Player player) {
        UUID playerId = player.getUniqueId();
        this.cleanupPlayer(playerId);
        this.previousPlayerRegions.remove(playerId);
    }

    public boolean isPlayerTakingBorderDamage(UUID playerId) {
        return this.playersInViolation.containsKey(playerId);
    }

    public String getViolatedRegion(UUID playerId) {
        return this.playersInViolation.get(playerId);
    }
}


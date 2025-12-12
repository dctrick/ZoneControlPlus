/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 * 
 * Could not load the following classes:
 *  com.sk89q.worldedit.bukkit.BukkitAdapter
 *  com.sk89q.worldedit.util.Location
 *  com.sk89q.worldguard.LocalPlayer
 *  com.sk89q.worldguard.WorldGuard
 *  com.sk89q.worldguard.bukkit.WorldGuardPlugin
 *  com.sk89q.worldguard.protection.ApplicableRegionSet
 *  com.sk89q.worldguard.protection.regions.ProtectedRegion
 *  com.sk89q.worldguard.protection.regions.RegionContainer
 *  com.sk89q.worldguard.protection.regions.RegionQuery
 *  org.bukkit.Location
 *  org.bukkit.entity.Player
 *  org.bukkit.plugin.java.JavaPlugin
 *  org.bukkit.potion.PotionEffect
 *  org.bukkit.potion.PotionEffectType
 */
package dev.trick.zonecontrolplus.managers;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import dev.trick.zonecontrolplus.config.ConfigManager;
import dev.trick.zonecontrolplus.config.RegionConfig;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class RegionManager {
    private final JavaPlugin plugin;
    public final ConfigManager configManager;
    private final RegionContainer regionContainer;
    private final RegionQuery query;
    private final Map<UUID, Set<String>> playerRegions;
    private final Map<UUID, Location> lastKnownLocations;

    public RegionManager(JavaPlugin plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.playerRegions = new ConcurrentHashMap<>();
        this.lastKnownLocations = new ConcurrentHashMap<>();
        this.regionContainer = WorldGuard.getInstance().getPlatform().getRegionContainer();
        this.query = this.regionContainer.createQuery();
        plugin.getLogger().info("RegionManager initialized with WorldGuard integration");
    }

    public Set<String> getPlayerRegions(Player player) {
        Location location = player.getLocation();
        com.sk89q.worldedit.util.Location weLocation = BukkitAdapter.adapt(location);
        ApplicableRegionSet regions = this.query.getApplicableRegions(weLocation);
        HashSet<String> regionNames = new HashSet<>();
        for (ProtectedRegion region : regions) {
            regionNames.add(region.getId());
        }
        return regionNames;
    }

    public boolean isPlayerInRegion(Player player, String regionName) {
        return this.getPlayerRegions(player).contains(regionName.toLowerCase());
    }

    public String getPrimaryRegion(Player player) {
        Location location = player.getLocation();
        com.sk89q.worldedit.util.Location weLocation = BukkitAdapter.adapt(location);
        ApplicableRegionSet regions = this.query.getApplicableRegions(weLocation);
        ProtectedRegion primary = null;
        int highestPriority = Integer.MIN_VALUE;
        for (ProtectedRegion region : regions) {
            if (region.getPriority() <= highestPriority) continue;
            primary = region;
            highestPriority = region.getPriority();
        }
        return primary != null ? primary.getId() : null;
    }

    public Set<String> getRegionsAtLocation(Location location) {
        com.sk89q.worldedit.util.Location weLocation = BukkitAdapter.adapt(location);
        ApplicableRegionSet regions = this.query.getApplicableRegions(weLocation);
        HashSet<String> regionNames = new HashSet<>();
        for (ProtectedRegion region : regions) {
            regionNames.add(region.getId());
        }
        return regionNames;
    }

    public void updatePlayerRegions(Player player) {
        UUID playerId = player.getUniqueId();
        Set<String> currentRegions = this.getPlayerRegions(player);
        Set<String> previousRegions = this.playerRegions.get(playerId);
        this.playerRegions.put(playerId, new HashSet<>(currentRegions));
        this.lastKnownLocations.put(playerId, player.getLocation().clone());
        if (previousRegions == null) {
            previousRegions = new HashSet<>();
        }
        HashSet<String> enteredRegions = new HashSet<>(currentRegions);
        enteredRegions.removeAll(previousRegions);
        HashSet<String> exitedRegions = new HashSet<>(previousRegions);
        exitedRegions.removeAll(currentRegions);
        for (String regionName : enteredRegions) {
            this.handleRegionEnter(player, regionName);
        }
        for (String regionName : exitedRegions) {
            this.handleRegionExit(player, regionName);
        }
    }

    private void handleRegionEnter(Player player, String regionName) {
        RegionConfig config = this.configManager.getRegionConfig(regionName);
        if (this.configManager.isDebugMode()) {
            this.plugin.getLogger().info("Player " + player.getName() + " entered region: " + regionName);
        }
        this.handlePotionEffectsOnEnter(player, config);
    }

    private void handleRegionExit(Player player, String regionName) {
        RegionConfig config = this.configManager.getRegionConfig(regionName);
        if (this.configManager.isDebugMode()) {
            this.plugin.getLogger().info("Player " + player.getName() + " exited region: " + regionName);
        }
        this.handleEffectCleanupOnExit(player, config);
    }

    private void handlePotionEffectsOnEnter(Player player, RegionConfig config) {
        if (config.isElytraDisabled() && player.isGliding()) {
            player.setGliding(false);
        }
        config.getClearOnEnterEffects().forEach(effect -> {
            if (player.hasPotionEffect(effect)) {
                player.removePotionEffect(effect);
            }
        });
        config.getGiveOnEnterEffects().forEach((effect, potionConfig) -> {
            if (potionConfig.isPermanent()) {
                player.addPotionEffect(new PotionEffect(effect, Integer.MAX_VALUE, potionConfig.getAmplifier(), false, false));
            } else {
                player.addPotionEffect(new PotionEffect(effect, potionConfig.getDuration(), potionConfig.getAmplifier(), false, false));
            }
        });
    }

    private void handleEffectCleanupOnExit(Player player, RegionConfig config) {
        config.getGiveOnEnterEffects().forEach((effect, potionConfig) -> {
            boolean stillInEffectRegion = false;
            if (potionConfig.isPermanent() && player.hasPotionEffect(effect)) {
                stillInEffectRegion = this.getPlayerRegions(player).stream().anyMatch(regionName -> {
                    RegionConfig otherConfig = this.configManager.getRegionConfig(regionName);
                    return otherConfig.getGiveOnEnterEffects().containsKey(effect) && otherConfig.getGiveOnEnter(effect).isPermanent();
                });
            }
            if (!stillInEffectRegion && potionConfig.isPermanent()) { // Only remove if it was permanent and we are no longer in a region granting it? Or generally.
                 // The logic was: if (permanent && hasEffect && !stillInEffectRegion) remove.
                 // The decompiled logic: if (perm && has && !(stillIn = check)) remove
                 // Correct logic: if it's a permanent effect given by this region, and the player is not in ANY OTHER region that also gives it permanently, remove it.
                 // Wait, check the original again:
                 /*
                 if (potionConfig.isPermanent() && player.hasPotionEffect(effect) && !(stillInEffectRegion = check())) {
                    player.removePotionEffect(effect);
                 }
                 */
                 // So if it IS strictly permanent, it checks if it should be kept.
                 // My rewrite:
            }
             
            if (potionConfig.isPermanent() && player.hasPotionEffect(effect)) {
                 boolean keptByOtherRegion = this.getPlayerRegions(player).stream().anyMatch(regionName -> {
                    RegionConfig otherConfig = this.configManager.getRegionConfig(regionName);
                    return otherConfig.getGiveOnEnterEffects().containsKey(effect) && otherConfig.getGiveOnEnter(effect).isPermanent();
                });
                if (!keptByOtherRegion) {
                    player.removePotionEffect(effect);
                }
            }
        });
    }

    public Location getLastKnownLocation(Player player) {
        return this.lastKnownLocations.get(player.getUniqueId());
    }

    public boolean isPlayerOutsideRequiredRegion(Player player, String regionName) {
        return !this.isPlayerInRegion(player, regionName);
    }

    public double getDistanceToRegion(Player player, String regionName) {
        Location location = player.getLocation();
        com.sk89q.worldedit.util.Location weLocation = BukkitAdapter.adapt(location);
        ApplicableRegionSet regions = this.query.getApplicableRegions(weLocation);
        for (ProtectedRegion region : regions) {
            if (!region.getId().equalsIgnoreCase(regionName)) continue;
            return 0.0;
        }
        return 10.0;
    }

    public void cleanupPlayer(Player player) {
        UUID playerId = player.getUniqueId();
        this.playerRegions.remove(playerId);
        this.lastKnownLocations.remove(playerId);
    }

    public Set<UUID> getTrackedPlayers() {
        return new HashSet<>(this.playerRegions.keySet());
    }

    public LocalPlayer wrapPlayer(Player player) {
        return WorldGuardPlugin.inst().wrapPlayer(player);
    }
}


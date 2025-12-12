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
import org.bukkit.attribute.Attribute;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class AutoHealManager
extends BukkitRunnable {
    private final JavaPlugin plugin;
    private final ConfigManager configManager;
    private final RegionManager regionManager;
    private final MessageManager messageManager;
    private final Map<UUID, String> playersInHealingRegions = new HashMap<>();
    private final Map<UUID, Long> lastHealTimes = new HashMap<>();
    private final Map<UUID, Boolean> sentHealMessage = new HashMap<>();
    private static final long HEAL_INTERVAL_TICKS = 100L;

    public AutoHealManager(JavaPlugin plugin, ConfigManager configManager, RegionManager regionManager, MessageManager messageManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.regionManager = regionManager;
        this.messageManager = messageManager;
    }

    public void run() {
        long currentTick = Bukkit.getServer().getCurrentTick();
        for (Player player : Bukkit.getOnlinePlayers()) {
            this.checkPlayerHealing(player, currentTick);
        }
    }

    private void checkPlayerHealing(Player player, long currentTick) {
        Set<String> currentRegions = this.regionManager.getPlayerRegions(player);
        RegionConfig bestHealingConfig = null;
        String bestHealingRegion = null;
        for (String regionName : currentRegions) {
            RegionConfig config = this.regionManager.configManager.getRegionConfig(regionName);
            if (!config.isAutoHealEnabled()) {
                continue;
            }
            if (bestHealingConfig != null && config.getHeartsPerFiveSeconds() <= bestHealingConfig.getHeartsPerFiveSeconds()) {
                continue;
            }
            bestHealingConfig = config;
            bestHealingRegion = regionName;
        }
        if (bestHealingConfig != null && bestHealingRegion != null) {
            this.handlePlayerHealing(player, bestHealingRegion, bestHealingConfig, currentTick);
        } else {
            this.handlePlayerLeavingHealingRegion(player);
        }
    }

    private void handlePlayerHealing(Player player, String regionName, RegionConfig config, long currentTick) {
        UUID playerId = player.getUniqueId();
        if (!this.playersInHealingRegions.containsKey(playerId) || !this.playersInHealingRegions.get(playerId).equals(regionName)) {
            this.playersInHealingRegions.put(playerId, regionName);
            this.sentHealMessage.put(playerId, false);
            if (!this.sentHealMessage.getOrDefault(playerId, false)) {
                this.messageManager.sendMessage((CommandSender)player, "healing.heal-zone-enter");
                this.sentHealMessage.put(playerId, true);
            }
        }
        Long lastHealTime = this.lastHealTimes.get(playerId);
        if (lastHealTime == null || currentTick - lastHealTime >= HEAL_INTERVAL_TICKS) {
            this.applyHealing(player, config);
            this.lastHealTimes.put(playerId, currentTick);
        }
    }

    private void applyHealing(Player player, RegionConfig config) {
        boolean healed = false;
        double maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        double currentHealth = player.getHealth();
        double targetHealth = config.getMaxHealth() > 0 ? Math.min((double)config.getMaxHealth(), maxHealth) : maxHealth;

        double healAmount = Math.min(config.getHeartsPerFiveSeconds() * 2.0, targetHealth - currentHealth);
        if (currentHealth < targetHealth && healAmount > 0.0) {
            player.setHealth(Math.min(currentHealth + healAmount, targetHealth));
            HashMap<String, String> placeholders = new HashMap<>();
            placeholders.put("hearts", String.format("%.1f", healAmount / 2.0));
            this.messageManager.sendMessage((CommandSender)player, "healing.heal-tick", placeholders);
            healed = true;
        }
        if (config.isRestoreFood() && player.getFoodLevel() < config.getFoodLevel()) {
            player.setFoodLevel(config.getFoodLevel());
            player.setSaturation(20.0f);
            if (!healed) {
                this.messageManager.sendMessage((CommandSender)player, "healing.food-restored");
            }
            healed = true;
        }
        if (healed && this.configManager.isDebugMode()) {
            this.plugin.getLogger().info("Applied healing to " + player.getName() + " in region: " + this.playersInHealingRegions.get(player.getUniqueId()));
        }
    }

    private void handlePlayerLeavingHealingRegion(Player player) {
        UUID playerId = player.getUniqueId();
        if (this.playersInHealingRegions.containsKey(playerId)) {
            this.messageManager.sendMessage((CommandSender)player, "healing.heal-zone-exit");
            this.playersInHealingRegions.remove(playerId);
            this.lastHealTimes.remove(playerId);
            this.sentHealMessage.remove(playerId);
        }
    }

    public void onPlayerQuit(Player player) {
        UUID playerId = player.getUniqueId();
        this.playersInHealingRegions.remove(playerId);
        this.lastHealTimes.remove(playerId);
        this.sentHealMessage.remove(playerId);
    }

    public boolean isPlayerInHealingRegion(UUID playerId) {
        return this.playersInHealingRegions.containsKey(playerId);
    }

    public String getHealingRegion(UUID playerId) {
        return this.playersInHealingRegions.get(playerId);
    }
}


/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.entity.Player
 *  org.bukkit.event.Listener
 *  org.bukkit.plugin.Plugin
 *  org.bukkit.plugin.java.JavaPlugin
 *  org.bukkit.scheduler.BukkitRunnable
 */
package dev.trick;

import dev.trick.zonecontrolplus.config.ConfigManager;
import dev.trick.zonecontrolplus.listeners.BlockProtectionListener;
import dev.trick.zonecontrolplus.listeners.EnderPearlListener;
import dev.trick.zonecontrolplus.listeners.PotionControlListener;
import dev.trick.zonecontrolplus.managers.AutoHealManager;
import dev.trick.zonecontrolplus.managers.BorderDamageManager;
import dev.trick.zonecontrolplus.managers.RegionManager;
import dev.trick.zonecontrolplus.utils.MessageManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public final class ZoneControlPlus
extends JavaPlugin {
    private ConfigManager configManager;
    private MessageManager messageManager;
    private RegionManager regionManager;
    private AutoHealManager autoHealManager;
    private BorderDamageManager borderDamageManager;
    private BlockProtectionListener blockProtectionListener;
    private EnderPearlListener enderPearlListener;
    private PotionControlListener potionControlListener;
    private dev.trick.zonecontrolplus.listeners.ElytraListener elytraListener;

    public void onEnable() {
        if (!this.checkDependencies()) {
            this.getLogger().severe("Required dependencies not found! Disabling plugin.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        this.initializeManagers();
        this.registerListeners();
        this.registerCommands();
        this.startTasks();
        this.getLogger().info("\u2694 ZoneControlPlus enabled! PvP regions are now active. \u2694");
    }

    public void onDisable() {
        this.getLogger().info("\u2694 ZoneControlPlus disabled. \u2694");
    }

    private boolean checkDependencies() {
        if (Bukkit.getPluginManager().getPlugin("WorldGuard") == null) {
            this.getLogger().severe("WorldGuard not found! This plugin requires WorldGuard to function.");
            return false;
        }
        return true;
    }

    private void initializeManagers() {
        this.configManager = new ConfigManager(this);
        this.messageManager = new MessageManager(this);
        this.regionManager = new RegionManager(this, this.configManager);
        this.autoHealManager = new AutoHealManager(this, this.configManager, this.regionManager, this.messageManager);
        this.borderDamageManager = new BorderDamageManager(this, this.configManager, this.regionManager, this.messageManager);
        this.getLogger().info("Managers initialized successfully");
    }

    private void registerListeners() {
        this.blockProtectionListener = new BlockProtectionListener(this.regionManager, this.messageManager);
        this.enderPearlListener = new EnderPearlListener(this.regionManager, this.messageManager);
        this.potionControlListener = new PotionControlListener(this.regionManager, this.messageManager);
        this.elytraListener = new dev.trick.zonecontrolplus.listeners.ElytraListener(this.regionManager, this.messageManager);
        Bukkit.getPluginManager().registerEvents(this.blockProtectionListener, this);
        Bukkit.getPluginManager().registerEvents(this.enderPearlListener, this);
        Bukkit.getPluginManager().registerEvents(this.potionControlListener, this);
        Bukkit.getPluginManager().registerEvents(this.elytraListener, this);
        this.getLogger().info("Event listeners registered");
    }

    private void registerCommands() {
        this.getLogger().info("Commands registered");
    }

    private void startTasks() {
        new BukkitRunnable(){

            public void run() {
                Bukkit.getOnlinePlayers().forEach(player -> ZoneControlPlus.this.regionManager.updatePlayerRegions(player));
            }
        }.runTaskTimer(this, 20L, (long)this.configManager.getRegionCheckInterval());
        this.autoHealManager.runTaskTimer(this, 20L, this.configManager.getAutoHealInterval());
        this.borderDamageManager.runTaskTimer(this, 20L, this.configManager.getBorderDamageInterval());
        this.getLogger().info("Background tasks started");
    }

    public void reloadConfiguration() {
        this.configManager.loadConfig();
        this.messageManager.loadMessages();
        this.getLogger().info("Configuration reloaded");
    }

    public ConfigManager getConfigManager() {
        return this.configManager;
    }

    public MessageManager getMessageManager() {
        return this.messageManager;
    }

    public RegionManager getRegionManager() {
        return this.regionManager;
    }
}


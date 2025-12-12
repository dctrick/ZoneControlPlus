/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 * 
 * Could not load the following classes:
 *  org.bukkit.Location
 *  org.bukkit.Material
 *  org.bukkit.command.CommandSender
 *  org.bukkit.entity.EnderPearl
 *  org.bukkit.entity.Player
 *  org.bukkit.event.EventHandler
 *  org.bukkit.event.EventPriority
 *  org.bukkit.event.Listener
 *  org.bukkit.event.block.Action
 *  org.bukkit.event.entity.ProjectileLaunchEvent
 *  org.bukkit.event.player.PlayerInteractEvent
 *  org.bukkit.event.player.PlayerTeleportEvent
 *  org.bukkit.event.player.PlayerTeleportEvent$TeleportCause
 *  org.bukkit.inventory.ItemStack
 */
package dev.trick.zonecontrolplus.listeners;

import dev.trick.zonecontrolplus.config.RegionConfig;
import dev.trick.zonecontrolplus.managers.RegionManager;
import dev.trick.zonecontrolplus.utils.MessageManager;
import java.util.Set;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;

public class EnderPearlListener
implements Listener {
    private final RegionManager regionManager;
    private final MessageManager messageManager;

    public EnderPearlListener(RegionManager regionManager, MessageManager messageManager) {
        this.regionManager = regionManager;
        this.messageManager = messageManager;
    }

    @EventHandler(priority=EventPriority.HIGH, ignoreCancelled=true)
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        if (!(event.getEntity() instanceof EnderPearl)) {
            return;
        }
        if (!(event.getEntity().getShooter() instanceof Player)) {
            return;
        }
        Player player = (Player)event.getEntity().getShooter();
        Location fromLocation = player.getLocation();
        Set<String> fromRegions = this.regionManager.getRegionsAtLocation(fromLocation);
        for (String regionName : fromRegions) {
            RegionConfig config = this.regionManager.configManager.getRegionConfig(regionName);
            if (!config.isNoPearl()) continue;
            event.setCancelled(true);
            this.messageManager.sendMessage((CommandSender)player, "pearl.no-pearl");
            return;
        }
    }

    @EventHandler(priority=EventPriority.HIGH, ignoreCancelled=true)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        RegionConfig config;
        if (event.getCause() != PlayerTeleportEvent.TeleportCause.ENDER_PEARL) {
            return;
        }
        Player player = event.getPlayer();
        Location fromLocation = event.getFrom();
        Location toLocation = event.getTo();
        if (toLocation == null) {
            return;
        }
        Set<String> fromRegions = this.regionManager.getRegionsAtLocation(fromLocation);
        Set<String> toRegions = this.regionManager.getRegionsAtLocation(toLocation);
        for (String regionName : toRegions) {
            config = this.regionManager.configManager.getRegionConfig(regionName);
            if (!config.isNoPearl()) continue;
            event.setCancelled(true);
            this.messageManager.sendMessage((CommandSender)player, "pearl.no-pearl");
            return;
        }
        for (String regionName : fromRegions) {
            config = this.regionManager.configManager.getRegionConfig(regionName);
            if (!config.isNoOutpearl() || toRegions.contains(regionName)) continue;
            event.setCancelled(true);
            this.messageManager.sendMessage((CommandSender)player, "pearl.no-outpearl");
            return;
        }
    }

    @EventHandler(priority=EventPriority.HIGH, ignoreCancelled=true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getItem() == null) {
            return;
        }
        ItemStack item = event.getItem();
        if (item.getType() != Material.ENDER_PEARL) {
            return;
        }
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        Player player = event.getPlayer();
        Set<String> playerRegions = this.regionManager.getPlayerRegions(player);
        for (String regionName : playerRegions) {
            RegionConfig config = this.regionManager.configManager.getRegionConfig(regionName);
            if (!config.isNoPearl()) continue;
            event.setCancelled(true);
            this.messageManager.sendMessage((CommandSender)player, "pearl.no-pearl");
            return;
        }
    }
}



package dev.trick.zonecontrolplus.listeners;

import dev.trick.zonecontrolplus.config.RegionConfig;
import dev.trick.zonecontrolplus.managers.RegionManager;
import dev.trick.zonecontrolplus.utils.MessageManager;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class BlockProtectionListener
implements Listener {
    private final RegionManager regionManager;
    private final MessageManager messageManager;

    public BlockProtectionListener(RegionManager regionManager, MessageManager messageManager) {
        this.regionManager = regionManager;
        this.messageManager = messageManager;
    }

    @EventHandler(priority=EventPriority.HIGH, ignoreCancelled=true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Material material = event.getBlock().getType();
        Set<String> playerRegions = this.regionManager.getPlayerRegions(player);
        for (String regionName : playerRegions) {
            RegionConfig config = this.regionManager.configManager.getRegionConfig(regionName);
            if (!config.isUndestroyable(material)) continue;
            event.setCancelled(true);
            this.messageManager.sendMessage((CommandSender)player, "block-protection.no-break", "block", material.name().toLowerCase().replace("_", " "));
            return;
        }
    }

    @EventHandler(priority=EventPriority.HIGH, ignoreCancelled=true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Material material = event.getBlock().getType();
        Set<String> playerRegions = this.regionManager.getPlayerRegions(player);
        for (String regionName : playerRegions) {
            RegionConfig config = this.regionManager.configManager.getRegionConfig(regionName);
            if (!config.isUnplaceable(material)) continue;
            event.setCancelled(true);
            this.messageManager.sendMessage((CommandSender)player, "block-protection.no-place", "block", material.name().toLowerCase().replace("_", " "));
            return;
        }
    }

    @EventHandler(priority=EventPriority.HIGH, ignoreCancelled=true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) {
            return;
        }
        Player player = event.getPlayer();
        Material material = event.getClickedBlock().getType();
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        Set<String> playerRegions = this.regionManager.getPlayerRegions(player);
        for (String regionName : playerRegions) {
            RegionConfig config = this.regionManager.configManager.getRegionConfig(regionName);
            if (!config.isInteractionBlocked(material)) continue;
            event.setCancelled(true);
            this.messageManager.sendMessage((CommandSender)player, "block-protection.no-interact", "block", material.name().toLowerCase().replace("_", " "));
            return;
        }
    }
}


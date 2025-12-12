package dev.trick.zonecontrolplus.listeners;

import dev.trick.zonecontrolplus.config.RegionConfig;
import dev.trick.zonecontrolplus.managers.RegionManager;
import dev.trick.zonecontrolplus.utils.MessageManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityToggleGlideEvent;

import java.util.Set;

public class ElytraListener implements Listener {

    private final RegionManager regionManager;
    private final MessageManager messageManager;

    public ElytraListener(RegionManager regionManager, MessageManager messageManager) {
        this.regionManager = regionManager;
        this.messageManager = messageManager;
    }

    @EventHandler
    public void onGlideToggle(EntityToggleGlideEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getEntity();

        // only check if they are TRYING to glide (isGliding becomes true)
        if (!event.isGliding()) {
            return; 
        }

        Set<String> regions = this.regionManager.getPlayerRegions(player);
        for (String regionName : regions) {
            RegionConfig config = this.regionManager.configManager.getRegionConfig(regionName);
            if (config != null && config.isElytraDisabled()) {
                event.setCancelled(true);
                // Optional: Send message if configured, defaulting to a generic warning for now if you want
                // messageManager.sendMessage(player, "elytra-disabled"); 
                // For now just cancel silently or maybe actionbar? Leaving silent as per request core functionality.
                return; 
            }
        }
    }
}

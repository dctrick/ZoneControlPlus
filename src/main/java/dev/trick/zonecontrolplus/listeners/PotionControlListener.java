/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 * 
 * Could not load the following classes:
 *  org.bukkit.command.CommandSender
 *  org.bukkit.entity.Player
 *  org.bukkit.event.EventHandler
 *  org.bukkit.event.EventPriority
 *  org.bukkit.event.Listener
 *  org.bukkit.event.player.PlayerItemConsumeEvent
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.inventory.meta.PotionMeta
 *  org.bukkit.potion.PotionEffect
 *  org.bukkit.potion.PotionEffectType
 */
package dev.trick.zonecontrolplus.listeners;

import dev.trick.zonecontrolplus.config.RegionConfig;
import dev.trick.zonecontrolplus.managers.RegionManager;
import dev.trick.zonecontrolplus.utils.MessageManager;
import java.util.Set;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class PotionControlListener
implements Listener {
    private final RegionManager regionManager;
    private final MessageManager messageManager;

    public PotionControlListener(RegionManager regionManager, MessageManager messageManager) {
        this.regionManager = regionManager;
        this.messageManager = messageManager;
    }

    @EventHandler(priority=EventPriority.HIGH, ignoreCancelled=true)
    public void onPlayerConsume(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (!(item.getItemMeta() instanceof PotionMeta)) {
            return;
        }
        PotionMeta potionMeta = (PotionMeta)item.getItemMeta();
        if (potionMeta == null) {
            return;
        }
        Set<String> playerRegions = this.regionManager.getPlayerRegions(player);
        for (String regionName : playerRegions) {
            PotionEffectType effectType;
            RegionConfig config = this.regionManager.configManager.getRegionConfig(regionName);
            if (potionMeta.getBasePotionData() != null && (effectType = potionMeta.getBasePotionData().getType().getEffectType()) != null && config.isPotionBlocked(effectType)) {
                event.setCancelled(true);
                this.messageManager.sendMessage((CommandSender)player, "potion.blocked-consume");
                return;
            }
            for (PotionEffect effect : potionMeta.getCustomEffects()) {
                if (!config.isPotionBlocked(effect.getType())) continue;
                event.setCancelled(true);
                this.messageManager.sendMessage((CommandSender)player, "potion.blocked-consume");
                return;
            }
        }
    }
}


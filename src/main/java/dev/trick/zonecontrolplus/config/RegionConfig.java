
package dev.trick.zonecontrolplus.config;

import dev.trick.zonecontrolplus.config.PotionConfig;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.potion.PotionEffectType;

public class RegionConfig {
    private final String regionName;
    private final Set<Material> undestroyableBlocks;
    private final Set<Material> unplaceableBlocks;
    private final Set<Material> blockedInteractions;
    private boolean noPearl;
    private boolean noOutpearl;
    private boolean damageOnExitEnabled;
    private double damagePerSecond;
    private boolean pushBack;
    private boolean elytraDisabled;
    private final Set<PotionEffectType> blockedPotions;
    private final Set<PotionEffectType> clearOnEnterEffects;
    private final Map<PotionEffectType, PotionConfig> giveOnEnterEffects;
    private boolean autoHealEnabled;
    private double heartsPerFiveSeconds;
    private int maxHealth;
    private boolean restoreFood;
    private int foodLevel;

    public RegionConfig(String regionName) {
        this.regionName = regionName;
        this.undestroyableBlocks = new HashSet<>();
        this.unplaceableBlocks = new HashSet<>();
        this.blockedInteractions = new HashSet<>();
        this.blockedPotions = new HashSet<>();
        this.clearOnEnterEffects = new HashSet<>();
        this.giveOnEnterEffects = new HashMap<>();
        this.noPearl = false;
        this.noOutpearl = false;
        this.damageOnExitEnabled = false;
        this.damagePerSecond = 1.0;
        this.pushBack = false;
        this.elytraDisabled = false;
        this.autoHealEnabled = false;
        this.heartsPerFiveSeconds = 0.5;
        this.maxHealth = -1;
        this.restoreFood = false;
        this.foodLevel = 20;
    }

    public RegionConfig(String regionName, RegionConfig defaults) {
        this(regionName);
        if (defaults != null) {
            this.undestroyableBlocks.addAll(defaults.undestroyableBlocks);
            this.unplaceableBlocks.addAll(defaults.unplaceableBlocks);
            this.blockedInteractions.addAll(defaults.blockedInteractions);
            this.blockedPotions.addAll(defaults.blockedPotions);
            this.clearOnEnterEffects.addAll(defaults.clearOnEnterEffects);
            this.giveOnEnterEffects.putAll(defaults.giveOnEnterEffects);
            this.noPearl = defaults.noPearl;
            this.noOutpearl = defaults.noOutpearl;
            this.damageOnExitEnabled = defaults.damageOnExitEnabled;
            this.damagePerSecond = defaults.damagePerSecond;
            this.pushBack = defaults.pushBack;
            this.elytraDisabled = defaults.elytraDisabled;
            this.autoHealEnabled = defaults.autoHealEnabled;
            this.heartsPerFiveSeconds = defaults.heartsPerFiveSeconds;
            this.maxHealth = defaults.maxHealth;
            this.restoreFood = defaults.restoreFood;
            this.foodLevel = defaults.foodLevel;
        }
    }

    public String getRegionName() {
        return this.regionName;
    }

    public Set<Material> getUndestroyableBlocks() {
        return this.undestroyableBlocks;
    }

    public Set<Material> getUnplaceableBlocks() {
        return this.unplaceableBlocks;
    }

    public Set<Material> getBlockedInteractions() {
        return this.blockedInteractions;
    }

    public boolean isNoPearl() {
        return this.noPearl;
    }

    public boolean isNoOutpearl() {
        return this.noOutpearl;
    }

    public boolean isDamageOnExitEnabled() {
        return this.damageOnExitEnabled;
    }

    public double getDamagePerSecond() {
        return this.damagePerSecond;
    }

    public boolean isPushBack() {
        return this.pushBack;
    }

    public boolean isElytraDisabled() {
        return this.elytraDisabled;
    }

    public Set<PotionEffectType> getBlockedPotions() {
        return this.blockedPotions;
    }

    public Set<PotionEffectType> getClearOnEnterEffects() {
        return this.clearOnEnterEffects;
    }

    public Map<PotionEffectType, PotionConfig> getGiveOnEnterEffects() {
        return this.giveOnEnterEffects;
    }

    public boolean isAutoHealEnabled() {
        return this.autoHealEnabled;
    }

    public double getHeartsPerFiveSeconds() {
        return this.heartsPerFiveSeconds;
    }

    public int getMaxHealth() {
        return this.maxHealth;
    }

    public boolean isRestoreFood() {
        return this.restoreFood;
    }

    public int getFoodLevel() {
        return this.foodLevel;
    }

    public void setNoPearl(boolean noPearl) {
        this.noPearl = noPearl;
    }

    public void setNoOutpearl(boolean noOutpearl) {
        this.noOutpearl = noOutpearl;
    }

    public void setDamageOnExitEnabled(boolean damageOnExitEnabled) {
        this.damageOnExitEnabled = damageOnExitEnabled;
    }

    public void setDamagePerSecond(double damagePerSecond) {
        this.damagePerSecond = damagePerSecond;
    }

    public void setPushBack(boolean pushBack) {
        this.pushBack = pushBack;
    }

    public void setElytraDisabled(boolean elytraDisabled) {
        this.elytraDisabled = elytraDisabled;
    }

    public void setAutoHealEnabled(boolean autoHealEnabled) {
        this.autoHealEnabled = autoHealEnabled;
    }

    public void setHeartsPerFiveSeconds(double heartsPerFiveSeconds) {
        this.heartsPerFiveSeconds = heartsPerFiveSeconds;
    }

    public void setMaxHealth(int maxHealth) {
        this.maxHealth = maxHealth;
    }

    public void setRestoreFood(boolean restoreFood) {
        this.restoreFood = restoreFood;
    }

    public void setFoodLevel(int foodLevel) {
        this.foodLevel = foodLevel;
    }

    public boolean isUndestroyable(Material material) {
        return this.undestroyableBlocks.contains(material);
    }

    public boolean isUnplaceable(Material material) {
        return this.unplaceableBlocks.contains(material);
    }

    public boolean isInteractionBlocked(Material material) {
        return this.blockedInteractions.contains(material);
    }

    public boolean isPotionBlocked(PotionEffectType effect) {
        return this.blockedPotions.contains(effect);
    }

    public boolean shouldClearOnEnter(PotionEffectType effect) {
        return this.clearOnEnterEffects.contains(effect);
    }

    public PotionConfig getGiveOnEnter(PotionEffectType effect) {
        return this.giveOnEnterEffects.get(effect);
    }

    public String toString() {
        return "RegionConfig{regionName='" + this.regionName + "', undestroyableBlocks=" + this.undestroyableBlocks.size() + ", unplaceableBlocks=" + this.unplaceableBlocks.size() + ", noPearl=" + this.noPearl + ", autoHealEnabled=" + this.autoHealEnabled + "}";
    }
}


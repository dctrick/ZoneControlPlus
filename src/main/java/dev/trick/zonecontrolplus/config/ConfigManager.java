package dev.trick.zonecontrolplus.config;

import dev.trick.zonecontrolplus.config.PotionConfig;
import dev.trick.zonecontrolplus.config.RegionConfig;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;

public class ConfigManager {
    public final JavaPlugin plugin;
    private FileConfiguration config;
    private final Map<String, RegionConfig> regionConfigs;
    private RegionConfig defaultConfig;
    private int regionCheckInterval;
    private boolean debugMode;
    private int autoHealInterval;
    private int borderDamageInterval;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.regionConfigs = new HashMap<>();
        this.loadConfig();
    }

    public void loadConfig() {
        File configFile = new File(this.plugin.getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            this.plugin.getDataFolder().mkdirs();
            try (InputStream input = this.plugin.getResource("config.yml");){
                if (input != null) {
                    Files.copy(input, configFile.toPath());
                }
            } catch (IOException e) {
                this.plugin.getLogger().log(Level.WARNING, "Could not create config.yml", e);
            }
        }
        this.config = YamlConfiguration.loadConfiguration((File)configFile);
        this.loadSettings();
        this.loadRegionConfigs();
        this.plugin.getLogger().info("Loaded configuration for " + this.regionConfigs.size() + " regions");
    }

    private void loadSettings() {
        this.regionCheckInterval = this.config.getInt("settings.region-check-interval", 10);
        this.debugMode = this.config.getBoolean("settings.debug", false);
        this.autoHealInterval = this.config.getInt("settings.auto-heal-interval", 100);
        this.borderDamageInterval = this.config.getInt("settings.border-damage-interval", 20);
    }

    private void loadRegionConfigs() {
        this.regionConfigs.clear();
        ConfigurationSection defaultSection = this.config.getConfigurationSection("default-settings");
        this.defaultConfig = this.loadRegionConfig("default", defaultSection);
        ConfigurationSection regionsSection = this.config.getConfigurationSection("regions");
        if (regionsSection != null) {
            for (String regionName : regionsSection.getKeys(false)) {
                ConfigurationSection regionSection = regionsSection.getConfigurationSection(regionName);
                if (regionSection == null) continue;
                RegionConfig regionConfig = this.loadRegionConfig(regionName, regionSection);
                this.regionConfigs.put(regionName.toLowerCase(), regionConfig);
            }
        }
    }

    private RegionConfig loadRegionConfig(String regionName, ConfigurationSection section) {
        ConfigurationSection potionSection;
        if (section == null) {
            return new RegionConfig(regionName, this.defaultConfig);
        }
        RegionConfig config = new RegionConfig(regionName);
        List<String> undestroyableList = section.getStringList("undestroyable-blocks");
        for (String materialName : undestroyableList) {
            try {
                Material material = Material.valueOf(materialName.toUpperCase());
                config.getUndestroyableBlocks().add(material);
            } catch (IllegalArgumentException e) {
                this.plugin.getLogger().warning("Invalid material in undestroyable-blocks for region " + regionName + ": " + materialName);
            }
        }
        List<String> unplaceableList = section.getStringList("unplaceable-blocks");
        for (String materialName : unplaceableList) {
            try {
                Material material = Material.valueOf(materialName.toUpperCase());
                config.getUnplaceableBlocks().add(material);
            } catch (IllegalArgumentException e) {
                this.plugin.getLogger().warning("Invalid material in unplaceable-blocks for region " + regionName + ": " + materialName);
            }
        }
        config.setNoPearl(section.getBoolean("no-pearl", false));
        config.setNoOutpearl(section.getBoolean("no-outpearl", false));
        config.setElytraDisabled(section.getBoolean("elytra-disabled", false));
        ConfigurationSection damageSection;
        if ((damageSection = section.getConfigurationSection("damage-on-exit")) != null) {
            config.setDamageOnExitEnabled(damageSection.getBoolean("enabled", false));
            config.setDamagePerSecond(damageSection.getDouble("damage-per-second", 1.0));
            config.setPushBack(damageSection.getBoolean("push-back", false));
        }
        if ((potionSection = section.getConfigurationSection("potion-control")) != null) {
            List<String> blockedPotions = potionSection.getStringList("blocked");
            for (String potionName : blockedPotions) {
                try {
                    PotionEffectType effect = PotionEffectType.getByName(potionName.toUpperCase());
                    if (effect == null) continue;
                    config.getBlockedPotions().add(effect);
                } catch (Exception e) {
                    this.plugin.getLogger().warning("Invalid potion effect in blocked potions for region " + regionName + ": " + potionName);
                }
            }
            List<String> clearOnEnter = potionSection.getStringList("clear-on-enter");
            for (String potionName : clearOnEnter) {
                try {
                    PotionEffectType effect = PotionEffectType.getByName(potionName.toUpperCase());
                    if (effect == null) continue;
                    config.getClearOnEnterEffects().add(effect);
                } catch (Exception e) {
                    this.plugin.getLogger().warning("Invalid potion effect in clear-on-enter for region " + regionName + ": " + potionName);
                }
            }
            ConfigurationSection giveOnEnterSection = potionSection.getConfigurationSection("give-on-enter");
            if (giveOnEnterSection != null) {
                for (String potionName4 : giveOnEnterSection.getKeys(false)) {
                    try {
                        PotionEffectType effect = PotionEffectType.getByName((String)potionName4.toUpperCase());
                        if (effect == null) continue;
                        ConfigurationSection effectSection = giveOnEnterSection.getConfigurationSection(potionName4);
                        int duration = effectSection.getInt("duration", 600);
                        int amplifier = effectSection.getInt("amplifier", 0);
                        config.getGiveOnEnterEffects().put(effect, new PotionConfig(duration, amplifier));
                    } catch (Exception e) {
                        this.plugin.getLogger().warning("Invalid potion effect in give-on-enter for region " + regionName + ": " + potionName4);
                    }
                }
            }
        }
        List<String> blockInteractionsList = section.getStringList("block-interactions");
        for (String materialName : blockInteractionsList) {
            try {
                Material material = Material.valueOf(materialName.toUpperCase());
                config.getBlockedInteractions().add(material);
            } catch (IllegalArgumentException e) {
                this.plugin.getLogger().warning("Invalid material in block-interactions for region " + regionName + ": " + materialName);
            }
        }
        ConfigurationSection healSection = section.getConfigurationSection("auto-heal");
        if (healSection != null) {
            config.setAutoHealEnabled(healSection.getBoolean("enabled", false));
            config.setHeartsPerFiveSeconds(healSection.getDouble("hearts-per-5s", 0.5));
            config.setMaxHealth(healSection.getInt("max-health", -1));
            config.setRestoreFood(healSection.getBoolean("restore-food", false));
            config.setFoodLevel(healSection.getInt("food-level", 20));
        }
        return config;
    }

    public RegionConfig getRegionConfig(String regionName) {
        return this.regionConfigs.getOrDefault(regionName.toLowerCase(), this.defaultConfig);
    }

    public boolean hasRegionConfig(String regionName) {
        return this.regionConfigs.containsKey(regionName.toLowerCase());
    }

    public void saveConfig() {
        try {
            this.config.save(new File(this.plugin.getDataFolder(), "config.yml"));
        } catch (IOException e) {
            this.plugin.getLogger().log(Level.SEVERE, "Could not save config.yml", e);
        }
    }

    public int getRegionCheckInterval() {
        return this.regionCheckInterval;
    }

    public boolean isDebugMode() {
        return this.debugMode;
    }

    public int getAutoHealInterval() {
        return this.autoHealInterval;
    }

    public int getBorderDamageInterval() {
        return this.borderDamageInterval;
    }

    public RegionConfig getDefaultConfig() {
        return this.defaultConfig;
    }

    public Map<String, RegionConfig> getAllRegionConfigs() {
        return new HashMap<>(this.regionConfigs);
    }
}


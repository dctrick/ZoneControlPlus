/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 * 
 * Could not load the following classes:
 *  org.bukkit.command.CommandSender
 *  org.bukkit.configuration.file.FileConfiguration
 *  org.bukkit.configuration.file.YamlConfiguration
 *  org.bukkit.entity.Player
 *  org.bukkit.plugin.java.JavaPlugin
 */
package dev.trick.zonecontrolplus.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class MessageManager {
    private final JavaPlugin plugin;
    private final MiniMessage miniMessage;
    private FileConfiguration messages;
    private final Map<String, String> cachedMessages;

    public MessageManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.miniMessage = MiniMessage.miniMessage();
        this.cachedMessages = new HashMap<>();
        this.loadMessages();
    }

    public void loadMessages() {
        File messagesFile = new File(this.plugin.getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            this.plugin.getDataFolder().mkdirs();
            try (InputStream input = this.plugin.getResource("messages.yml");){
                if (input != null) {
                    Files.copy(input, messagesFile.toPath());
                }
            } catch (IOException e) {
                this.plugin.getLogger().log(Level.WARNING, "Could not create messages.yml", e);
            }
        }
        this.messages = YamlConfiguration.loadConfiguration(messagesFile);
        this.cachedMessages.clear();
        this.plugin.getLogger().info("Loaded messages configuration with " + this.getAllKeys(this.messages).size() + " message keys");
    }

    public String getMessage(String key) {
        return this.cachedMessages.computeIfAbsent(key, k -> {
            String message = this.messages.getString(k, "");
            if (message.isEmpty()) {
                this.plugin.getLogger().warning("Missing message key: " + k);
                return "<gradient:#ff0000:#aa0000>\u274c Missing message: " + k + "</gradient>";
            }
            return message;
        });
    }

    public List<String> getMessageList(String key) {
        return this.messages.getStringList(key);
    }

    public void sendMessage(CommandSender sender, String key, Map<String, String> placeholders) {
        String message = this.getMessage(key);
        if (placeholders != null) {
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                message = message.replace("<" + entry.getKey() + ">", entry.getValue());
            }
        }
        Component component = this.miniMessage.deserialize(message);
        Audience audience = (Audience) sender;
        audience.sendMessage(component);
    }

    public void sendMessage(CommandSender sender, String key) {
        this.sendMessage(sender, key, null);
    }

    public void sendMessage(CommandSender sender, String key, String placeholder, String value) {
        HashMap<String, String> placeholders = new HashMap<>();
        placeholders.put(placeholder, value);
        this.sendMessage(sender, key, placeholders);
    }

    public void sendMessageList(CommandSender sender, String key) {
        List<String> messageList = this.getMessageList(key);
        for (String message : messageList) {
            Component component = this.miniMessage.deserialize(message);
            Audience audience = (Audience) sender;
            audience.sendMessage(component);
        }
    }

    public Component formatMessage(String key, Map<String, String> placeholders) {
        String message = this.getMessage(key);
        if (placeholders != null) {
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                message = message.replace("<" + entry.getKey() + ">", entry.getValue());
            }
        }
        return this.miniMessage.deserialize(message);
    }

    public Component formatMessage(String key) {
        return this.formatMessage(key, null);
    }

    public void sendRaw(CommandSender sender, String miniMessageString) {
        Component component = this.miniMessage.deserialize(miniMessageString);
        Audience audience = (Audience) sender;
        audience.sendMessage(component);
    }

    private Map<String, Object> getAllKeys(FileConfiguration config) {
        HashMap<String, Object> keys = new HashMap<>();
        if (config != null) {
            for (String key : config.getKeys(true)) {
                keys.put(key, config.get(key));
            }
        }
        return keys;
    }
}


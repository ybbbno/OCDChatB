package com.dralgut.ocd_chat.chat.config;

import com.dralgut.ocd_chat.chat.config.types.ChatType;
import com.dralgut.ocd_chat.chat.config.types.MeConfig;
import com.dralgut.ocd_chat.chat.config.types.PingConfig;
import com.dralgut.ocd_chat.chat.config.types.MessagesConfig;
import me.deadybbb.ybmj.BasicConfigHandler;
import me.deadybbb.ybmj.PluginProvider;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashSet;
import java.util.Set;

public class ChatConfigManager extends BasicConfigHandler {
    private static final int MIN_VALID_DISTANCE = -2;

    public ChatConfigManager(PluginProvider plugin) {
        super(plugin, "config.yml");
    }

    public ChatConfig getConfig() {
        reloadConfig();

        MessagesConfig messages = getMessages();
        Set<ChatType> types = getTypes();
        MeConfig me = getMe();
        PingConfig ping = getPing();

        return new ChatConfig(messages, types, me, ping);
    }

    private MessagesConfig getMessages() {
        ConfigurationSection section = config.getConfigurationSection("messages");
        if (section == null) return new MessagesConfig(
            "§4Nobody saw your message",
            "§4You do not have access to this type of chat"
        );

        String nobodySaw = section.getString("nobody_saw", "§4Nobody saw your message");
        String noAccessToChat = section.getString("no_access_to_chat", "§4You do not have access to this type of chat");

        return new MessagesConfig(nobodySaw, noAccessToChat);
    }

    private Set<ChatType> getTypes() {
        Set<ChatType> types = new HashSet<>();

        ConfigurationSection section = config.getConfigurationSection("chat.types");
        if (section == null) return types;

        for (String key : section.getKeys(false)) {
            ConfigurationSection dataSection = section.getConfigurationSection(key);
            if (dataSection == null) continue;

            String prefix = dataSection.getString("prefix");
            String permission = dataSection.getString("permission");
            String format = dataSection.getString("format");
            int distance = dataSection.getInt("distance", MIN_VALID_DISTANCE-1);

            if (!isValidChatType(prefix, permission, format, distance, key)) continue;

            ChatType type = new ChatType(key, prefix, permission, format, distance);
            types.add(type);
        }

        return types;
    }

    private MeConfig getMe() {
        ConfigurationSection section = config.getConfigurationSection("me");
        if (section == null) return new MeConfig(false, "", "", 0);

        boolean isEnable = section.getBoolean("enable", false);
        if (!isEnable) return new MeConfig(false, "", "", 0);

        String permission = section.getString("permission", "");
        String format = section.getString("format", "* {player} {message}");
        int distance = section.getInt("distance", MIN_VALID_DISTANCE-1);

        return new MeConfig(true, permission, format, distance);
    }

    private PingConfig getPing() {
        ConfigurationSection section = config.getConfigurationSection("ping");
        if (section == null) return new PingConfig(false, "", "", "", "");

        boolean isEnable = section.getBoolean("enable", false);
        if (!isEnable) return new PingConfig(false, "", "", "", "");

        String prefix = section.getString("prefix", "@");
        String format = section.getString("format", "§f§l§{player}§r");
        String activeFormat = section.getString("activeFormat", "§6§l§n{player}§r");
        String sound = section.getString("sound", "minecraft:block.note_block.pling");

        return new PingConfig(true, prefix, format, activeFormat, sound);
    }

    private boolean isValidChatType(String prefix, String permission, String format, int distance, String name) {
        String errorMessage = "Invalid or missing '%s' value for chat type '" + name + "' :(";
        boolean isValid = true;

        if (prefix == null) {
            plugin.logger.warning(String.format(errorMessage, "prefix"));
            isValid = false;
        }
        if (permission == null) {
            plugin.logger.warning(String.format(errorMessage, "permission"));
            isValid = false;
        }
        if (format == null || format.isBlank()) {
            plugin.logger.warning(String.format(errorMessage, "format"));
            isValid = false;
        }
        if (distance < MIN_VALID_DISTANCE) {
            plugin.logger.warning(String.format(errorMessage, "distance"));
            isValid = false;
        }

        return isValid;
    }
}

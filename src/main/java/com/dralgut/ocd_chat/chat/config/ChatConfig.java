package com.dralgut.ocd_chat.chat.config;

import com.dralgut.ocd_chat.chat.config.types.ChatType;
import com.dralgut.ocd_chat.chat.config.types.MeConfig;
import com.dralgut.ocd_chat.chat.config.types.PingConfig;
import com.dralgut.ocd_chat.chat.config.types.MessagesConfig;

import java.util.Comparator;
import java.util.Set;

public record ChatConfig(MessagesConfig messages, Set<ChatType> chatTypes, MeConfig me, PingConfig ping) {

    public String nobodySawMessage() {
        return messages.nobodySaw();
    }

    public String noAccessToChatMessage() {
        return messages.noAccessToChat();
    }

    public boolean isPingEnabled() {
        return ping.isEnable();
    }

    public ChatType getTypeByName(String name) {
        if (name == null || name.isEmpty()) return null;

        return chatTypes.stream().filter(t -> t.name().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public ChatType getType(String message){
        if (message == null || message.isEmpty()) return null;

        return chatTypes.stream()
                .filter(e -> (message.startsWith(e.prefix()))
                        && (!message.substring(e.prefix().length()).trim().isEmpty()))
                .max(Comparator.comparingInt(e -> e.prefix().length()))
                .orElse(null);
    }
}

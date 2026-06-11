package com.dralgut.ocd_chat.chat.config.types;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public record MeConfig (boolean isEnable, String permission, String format, int distance) {

    @Contract(" -> new")
    public @NotNull ChatType getChatType() {
        return new ChatType("me", "", permission, format, distance);
    }
}

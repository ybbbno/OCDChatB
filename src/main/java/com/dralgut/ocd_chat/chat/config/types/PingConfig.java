package com.dralgut.ocd_chat.chat.config.types;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

public record PingConfig(boolean isEnable, String prefix, String format, String activeFormat, String sound) {

    @Contract(" -> new")
    public @NotNull Pattern pattern() {
        return Pattern.compile(Pattern.quote(prefix)+"(\\w+)");
    }
}

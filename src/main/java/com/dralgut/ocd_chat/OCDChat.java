package com.dralgut.ocd_chat;

import com.dralgut.ocd_chat.chat.ChatCore;
import com.dralgut.ocd_chat.chat.server_messages.ServerMessageManager;
import com.dralgut.ocd_chat.chat.type.ChatTypeManager;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public final class OCDChat extends JavaPlugin {

    public static Logger logger;
    public static OCDChat instance;
    public static FileConfiguration config;
    public static boolean PAPI;

    @Override
    public void onEnable() {
        logger = getLogger();
        logger.info("Hello World");

        load();
        Bukkit.getPluginManager().registerEvents(new ChatCore(),this);
    }

    public void load(){
        instance = this;
        PAPI = Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null;

        saveDefaultConfig();

        reloadConfig();
        config = getConfig();

        ChatCore.load();
        ChatTypeManager.load();
        ServerMessageManager.load();

        logger.info("OCD chat is loaded :3");
    }

    public static OCDChat getInstance() {
        return instance;
    }
}


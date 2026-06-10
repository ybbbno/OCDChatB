package com.dralgut.ocd_chat;

import com.dralgut.ocd_chat.chat.ChatProcessingManager;
import me.deadybbb.ybmj.PluginProvider;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

public final class OCDChat extends PluginProvider {

    public static OCDChat instance;
    public static FileConfiguration config;
    public static boolean PAPI;

    private ChatProcessingManager chatCore;

    @Override
    public void onEnable() {
        load();
        Bukkit.getPluginManager().registerEvents(chatCore, this);

        int pluginId = 29746;
        Metrics metrics = new Metrics(this, pluginId);

        metrics.addCustomChart(
                new SimplePie("server_messages_status", () -> "Inactive"));

        metrics.addCustomChart(
                new SimplePie("ping_status", () ->
                        ChatProcessingManager.isPingEnable ? "Active" : "Inactive"
                ));
    }

    public void load(){
        instance = this;
        PAPI = Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null;

        saveDefaultConfig();

        chatCore = new ChatProcessingManager(this);
        chatCore.init();

        logger.info("OCD chat is loaded :3");
    }

    public static OCDChat getInstance() {
        return instance;
    }
}


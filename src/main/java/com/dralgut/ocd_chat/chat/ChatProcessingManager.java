package com.dralgut.ocd_chat.chat;

import com.dralgut.ocd_chat.OCDChat;
import com.dralgut.ocd_chat.chat.config.ChatConfig;
import com.dralgut.ocd_chat.chat.config.ChatConfigManager;
import com.dralgut.ocd_chat.chat.config.types.ChatType;
import com.dralgut.ocd_chat.chat.config.types.PingConfig;
import com.samvolvo.prefixPro.PrefixManager;
import com.samvolvo.prefixPro.managers.AfkManager;
import com.samvolvo.prefixPro.utils.ColorUtil;
import me.clip.placeholderapi.PlaceholderAPI;
import me.deadybbb.ybmj.BasicManagerHandler;
import me.deadybbb.ybmj.PluginProvider;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.*;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

public class ChatProcessingManager extends BasicManagerHandler implements Listener {
    private final static int UNLIMITED = -2;
    private final static int UNLIMITED_FOR_WORLD = -1;

    public static boolean isPingEnable = false;

    private final ChatConfigManager manager;
    private ChatConfig config;

    public ChatProcessingManager(PluginProvider plugin) {
        super(plugin);

        manager = new ChatConfigManager(plugin);
        config = null;
    }

    @Override
    protected void onInit() {
        config = manager.getConfig();
        isPingEnable = config.isPingEnabled();
    }

    @Override
    protected void onDeinit() {
        config = null;
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent e) {
        if (e.isCancelled() || config == null) return;

        Player sender = e.getPlayer();
        String message = e.getMessage();

        ChatType type = config.me().getChatType();

        if (!message.trim().toLowerCase().startsWith("/me") || !config.me().isEnable()) return;
        e.setCancelled(true);

        if (!hasPermission(type.permission(), sender)) return;

        if (!sendMessages(message.replace("/me ", ""), sender, type)) {
            printError(sender, config.nobodySawMessage());
        }
    }

    @EventHandler
    public void onChatEvent(AsyncPlayerChatEvent e){
        if (e.isCancelled() || config == null) return;

        Player sender = e.getPlayer();
        String message = e.getMessage();

        ChatType type = config.getType(message);

        if (type == null) return;
        e.setCancelled(true);

        plugin.logger.info(sender.getName()+": "+message);

        if (!hasPermission(type.permission(), sender)) return;

        if (!sendMessages(message, sender, type)) {
            printError(sender, config.nobodySawMessage());
        }
    }

    private boolean sendMessages(String message, Player sender, ChatType type) {
        Set<Player> recipients = getRecipients(sender, type.distance());

        for (Player recipient : recipients) {
            String processedMessage = processMessage(message, recipient, sender, type);

            recipient.sendMessage(processedMessage);
        }

        return recipients.size() > 1;
    }

    private boolean hasPermission(String permission, Player sender) {
        if (!permission.isEmpty() && !sender.hasPermission(permission)) {
            printError(sender, config.noAccessToChatMessage());
            return false;
        }
        return true;
    }

    private String processMessage(String message, Player recipient, Player sender, ChatType type) {
        String processedMessage = message;
        if (config.ping().isEnable() && message.contains(config.ping().prefix())) {
            processedMessage = processMentions(processedMessage, recipient);
        }
        return processFormat(type, sender, processedMessage);
    }

    private String processMentions(String message, Player recipient) {
        PingConfig ping = config.ping();

        if (!ping.isEnable() || !message.contains(ping.prefix())) return message;

        Matcher matcher = ping.pattern().matcher(message);
        StringBuilder builder = new StringBuilder();

        while (matcher.find()) {
            String playerName = matcher.group(1);
            Player target = Bukkit.getPlayerExact(playerName);

            if (target == null) {
                matcher.appendReplacement(builder, matcher.group(0));
                continue;
            }

            String replacement;
            if (target.getName().equals(recipient.getName())) {
                replacement = ping.activeFormat().replace("{player}", playerName);
                recipient.playSound(recipient.getLocation(), ping.sound(), 1.0f, 1.0f);
            } else {
                replacement = ping.format().replace("{player}", playerName);
            }
            matcher.appendReplacement(builder, replacement);
        }
        matcher.appendTail(builder);

        return builder.toString();
    }

    private void printError(Player player, String errorMessage){
        if (errorMessage.isEmpty()) return;

        plugin.logger.severe(errorMessage);
        player.sendMessage(errorMessage);
    }

    private Set<Player> getRecipients(Player sender, int distance) {

        if (distance == UNLIMITED) {
            return new HashSet<>(Bukkit.getOnlinePlayers());
        }

        List<Player> worldPlayers = sender.getWorld().getPlayers();
        if (distance == UNLIMITED_FOR_WORLD) {
            return new HashSet<>(worldPlayers);
        }

        final int finalDistance = Math.max(distance, 0);
        final int squaredDistance = finalDistance*finalDistance;
        final Location senderLocation = sender.getLocation();

        return worldPlayers.stream()
                .filter(player -> {
                    Location playerLocation = player.getLocation();
                    if (!Objects.equals(senderLocation.getWorld(), playerLocation.getWorld())) return false;
                    return senderLocation.distanceSquared(playerLocation) <= squaredDistance;
                })
                .collect(Collectors.toSet());
    }

    private String processPlayerPrefix(Player sender) {
        if (OCDChat.PrefixProAPI != null && OCDChat.PrefixProAPI.getConfig().getBoolean("display.chat", true)) {
            PrefixManager prefixManager = OCDChat.PrefixProAPI.getPrefixManager();
            return prefixManager.getPrefix(sender);
        }

        return "";
    }

    private String processPlayerSuffix(Player sender) {
        if (OCDChat.PrefixProAPI != null && OCDChat.PrefixProAPI.getConfig().getBoolean("display.chat", true)) {
            AfkManager afkManager = OCDChat.PrefixProAPI.getAfkManager();
            return afkManager.isAfk(sender)
                    ? ColorUtil.colorize(OCDChat.PrefixProAPI.getConfig().getString("afk.suffix", " &7[AFK]")) + ChatColor.RESET
                    : "";
        }

        return "";
    }

    private String processFormat(ChatType type, Player sender, String message){
        String format = ChatColor.translateAlternateColorCodes('&', type.format());

        if (OCDChat.PAPI) format = PlaceholderAPI.setPlaceholders(sender, format);

        String prefix = type.prefix();
        String actualMessage = message;
        if (!prefix.isEmpty() && message.startsWith(prefix)) {
            actualMessage = message.substring(prefix.length()).trim();
        }

        String playerPrefix = processPlayerPrefix(sender);
        String playerSuffix = processPlayerSuffix(sender);

        return format
                .replace("{player}",playerPrefix + sender.getName() + playerSuffix)
                .replace("{message}", actualMessage);
    }
}

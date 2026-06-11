package com.dralgut.ocd_chat;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import me.deadybbb.ybmj.LegacyTextHandler;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.List;

public class OCDChatCommand implements BasicCommand {
    private OCDChat plugin;

    public OCDChatCommand(OCDChat plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSourceStack source, String[] args) {
        CommandSender s = source.getSender();

        if (args[0].equals("reload")) {
            Bukkit.getScheduler().runTask(plugin, () -> plugin.chatCore.deinit());
            Bukkit.getScheduler().runTask(plugin, () -> plugin.chatCore.init());
            LegacyTextHandler.sendFormattedMessage(s, "<green>OCDChat reloaded.");
            return;
        }

        LegacyTextHandler.sendFormattedMessage(s, "<red>Usage: /ocd reload");
    }

    @Override
    public Collection<String> suggest(CommandSourceStack source, String[] args) {
        return List.of("reload");
    }

    @Override
    public boolean canUse(CommandSender sender) {
        final String permission = this.permission();
        return sender.hasPermission(permission) &&
                sender instanceof Player;
    }

    @Override
    public @Nullable String permission() {
        return "ocd.reload";
    }
}

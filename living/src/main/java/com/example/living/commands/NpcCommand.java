package com.example.living.commands;

import com.example.living.LivingPlugin;
import com.example.living.npc.NPC;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Command to manage NPC tasks and parameters.
 */
public class NpcCommand implements CommandExecutor, TabCompleter {
    private final LivingPlugin plugin;

    public NpcCommand(LivingPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (sender instanceof Player player) {
                plugin.getNpcManager().openNpcListGui(player);
            } else {
                sender.sendMessage("Player command only");
            }
            return true;
        }
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            plugin.reloadConfig();
            sender.sendMessage("Config reloaded");
            return true;
        }
        if (args.length < 2) {
            sender.sendMessage("Usage: /" + label + " reload | <uuid> <start|stop|set <key> <value>>");
            return true;
        }
        UUID uuid;
        try {
            uuid = UUID.fromString(args[0]);
        } catch (IllegalArgumentException e) {
            sender.sendMessage("Invalid UUID");
            return true;
        }
        NPC npc = plugin.getNpcManager().getNpc(uuid);
        if (npc == null) {
            sender.sendMessage("NPC not found");
            return true;
        }
        switch (args[1].toLowerCase()) {
            case "start" -> {
                npc.setActive(true);
                plugin.getNpcManager().saveNpcSettings(npc);
                sender.sendMessage("NPC tasks started");
            }
            case "stop" -> {
                npc.setActive(false);
                plugin.getNpcManager().saveNpcSettings(npc);
                sender.sendMessage("NPC tasks stopped");
            }
            case "set" -> {
                if (args.length < 4) {
                    sender.sendMessage("Usage: /" + label + " <uuid> set <key> <value>");
                    return true;
                }
                npc.setTaskParameter(args[2], args[3]);
                plugin.getNpcManager().saveNpcSettings(npc);
                sender.sendMessage("Parameter set");
            }
            default -> sender.sendMessage("Unknown action");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            completions.add("reload");
        } else if (args.length == 2) {
            completions.add("start");
            completions.add("stop");
            completions.add("set");
        } else if (args.length == 3 && args[1].equalsIgnoreCase("set")) {
            completions.add("tree");
        }
        return completions;
    }
}
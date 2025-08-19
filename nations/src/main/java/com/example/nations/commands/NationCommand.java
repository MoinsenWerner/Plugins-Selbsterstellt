package com.example.nations.commands;

import com.example.nations.NationsPlugin;
import com.example.nations.model.Nation;
import com.example.nations.model.NationRegistry;
import com.example.nations.territory.ChunkPosition;
import org.bukkit.Chunk;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class NationCommand implements CommandExecutor, TabCompleter {
    private final NationsPlugin plugin;

    public NationCommand(NationsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage("Usage: /" + label + " <create|claim>");
            return true;
        }

        NationRegistry registry = plugin.getNationRegistry();

        switch (args[0].toLowerCase()) {
            case "create" -> {
                if (args.length < 2) {
                    sender.sendMessage("Usage: /" + label + " create <name>");
                    return true;
                }
                String name = args[1];
                if (registry.getNation(name) != null) {
                    sender.sendMessage("Nation already exists.");
                    return true;
                }
                registry.createNation(name, player.getUniqueId());
                sender.sendMessage("Nation " + name + " created.");
            }
            case "claim" -> {
                Nation nation = registry.getNationByMember(player.getUniqueId());
                if (nation == null) {
                    sender.sendMessage("You are not part of a nation.");
                    return true;
                }
                Chunk chunk = player.getLocation().getChunk();
                ChunkPosition pos = ChunkPosition.fromChunk(chunk);
                if (nation.getTerritory().contains(pos)) {
                    sender.sendMessage("This chunk is already claimed by your nation.");
                    return true;
                }
                nation.getTerritory().add(pos);
                sender.sendMessage("Chunk claimed for " + nation.getName() + ".");
            }
            default -> sender.sendMessage("Unknown subcommand.");
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            completions.add("create");
            completions.add("claim");
        }
        return completions;
    }
}

package com.example.chrono.listener;

import com.example.chrono.ChronoPlugin;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

/**
 * Listener to restrict block interactions based on configured snapshots.
 */
public class SnapshotProtectionListener implements Listener {

    private final ChronoPlugin plugin;

    public SnapshotProtectionListener(ChronoPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        String snapshot = event.getBlock().getWorld().getName();
        if (!plugin.getBuildSnapshots().contains(snapshot)) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "Building is disabled in snapshot " + snapshot);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        String snapshot = event.getBlock().getWorld().getName();
        if (!plugin.getBreakSnapshots().contains(snapshot)) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "Breaking is disabled in snapshot " + snapshot);
        }
    }
}

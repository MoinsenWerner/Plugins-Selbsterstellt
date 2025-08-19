package net.timeux.chrono;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.EnumSet;
import java.util.Set;

public class ChronoPlugin extends JavaPlugin implements Listener {

    private final Set<Material> allowedBreak = EnumSet.of(
            Material.STONE,
            Material.COAL_ORE,
            Material.IRON_ORE,
            Material.DIAMOND_ORE,
            Material.GOLD_ORE
    );

    private SnapshotManager snapshotManager;

    @Override
    public void onEnable() {
        this.snapshotManager = new SnapshotManager(this);
        getServer().getPluginManager().registerEvents(this, this);

        getCommand("chronosnapshot").setExecutor((sender, command, label, args) -> {
            if (args.length != 1) {
                sender.sendMessage("Usage: /chronosnapshot <name>");
                return true;
            }
            try {
                snapshotManager.createSnapshot(args[0]);
                sender.sendMessage("Snapshot " + args[0] + " erstellt.");
            } catch (IOException e) {
                sender.sendMessage("Snapshot fehlgeschlagen: " + e.getMessage());
                e.printStackTrace();
            }
            return true;
        });

        getCommand("chronoportal").setExecutor((sender, command, label, args) -> {
            if (!(sender instanceof org.bukkit.entity.Player player)) {
                sender.sendMessage("Nur Spieler können dieses Kommando nutzen.");
                return true;
            }
            if (args.length == 1 && args[0].equalsIgnoreCase("return")) {
                World main = Bukkit.getWorlds().get(0);
                player.teleport(main.getSpawnLocation(), PlayerTeleportEvent.TeleportCause.PLUGIN);
                ItemFilter.filterInventory(player.getInventory());
                return true;
            }
            if (args.length != 1) {
                sender.sendMessage("Usage: /chronoportal <snapshot>");
                return true;
            }
            World snapshotWorld = Bukkit.getWorld(args[0]);
            if (snapshotWorld == null) {
                sender.sendMessage("Snapshot nicht gefunden.");
                return true;
            }
            player.teleport(snapshotWorld.getSpawnLocation(), PlayerTeleportEvent.TeleportCause.PLUGIN);
            return true;
        });
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (!allowedBreak.contains(event.getBlock().getType())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        event.setCancelled(true);
    }
}

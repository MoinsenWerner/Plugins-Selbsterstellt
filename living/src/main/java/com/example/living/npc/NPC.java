package com.example.living.npc;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Villager;

import com.example.living.LivingPlugin;

/**
 * Basic representation of an NPC controlled by the plugin.
 * The implementation here is intentionally lightweight and is
 * meant to be expanded with behavior logic, pathfinding and
 * interaction with the Minecraft world.
 */
public class NPC {

    private final UUID uuid;
    private final Job job;
    private final Map<String, String> taskParameters = new HashMap<>();
    private boolean active = true;

    public NPC(UUID uuid, Job job) {
        this.uuid = uuid;
        this.job = job;
        LivingPlugin.getInstance().getLogger()
                .info("NPC created with job " + job + " and UUID " + uuid);
    }

    public UUID getUuid() {
        return uuid;
    }

    public Job getJob() {
        return job;
    }

    public void setTaskParameter(String key, String value) {
        taskParameters.put(key, value);
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * Simple AI execution for the NPC based on its job. The
     * implementation is intentionally lightweight and serves as
     * a placeholder for more advanced behavior.
     */
    public void performTask() {
        if (!active) {
            return;
        }

        LivingPlugin plugin = LivingPlugin.getInstance();
        Entity entity = plugin.getServer().getEntity(uuid);
        if (!(entity instanceof Villager villager)) {
            return;
        }

        switch (job) {
            case WOODCUTTER -> performWoodcutterTask(villager);
            case MINER -> performMinerTask(villager);
            case FARMER -> performFarmerTask(villager);
            case BUILDER -> performBuilderTask(villager);
        }
    }

    private void performWoodcutterTask(Villager villager) {
        LivingPlugin plugin = LivingPlugin.getInstance();
        String tree = taskParameters.getOrDefault("tree", "OAK").toUpperCase();
        Material logMaterial = Material.matchMaterial(tree + "_LOG");
        if (logMaterial == null) {
            plugin.getLogger().warning("Unknown tree type: " + tree);
            return;
        }

        Location loc = villager.getLocation();
        int radius = 5;
        for (int x = -radius; x <= radius; x++) {
            for (int y = -1; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    Block block = loc.getBlock().getRelative(x, y, z);
                    if (block.getType() == logMaterial) {
                        block.breakNaturally();
                        plugin.getLogger().info("NPC " + uuid + " cut a " + tree + " log.");
                        return;
                    }
                }
            }
        }
    }

    private void performMinerTask(Villager villager) {
        LivingPlugin plugin = LivingPlugin.getInstance();
        Location loc = villager.getLocation();
        int radius = 5;
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    Block block = loc.getBlock().getRelative(x, y, z);
                    if (block.getType() == Material.STONE) {
                        block.breakNaturally();
                        plugin.getLogger().info("NPC " + uuid + " mined stone.");
                        return;
                    }
                }
            }
        }
    }

    private void performFarmerTask(Villager villager) {
        LivingPlugin plugin = LivingPlugin.getInstance();
        Location loc = villager.getLocation();
        int radius = 5;
        for (int x = -radius; x <= radius; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -radius; z <= radius; z++) {
                    Block block = loc.getBlock().getRelative(x, y, z);
                    if (block.getType() == Material.WHEAT) {
                        block.breakNaturally();
                        block.setType(Material.WHEAT);
                        plugin.getLogger().info("NPC " + uuid + " harvested wheat.");
                        return;
                    }
                }
            }
        }
    }

    private void performBuilderTask(Villager villager) {
        LivingPlugin plugin = LivingPlugin.getInstance();
        Location loc = villager.getLocation();
        int radius = 5;
        for (int x = -radius; x <= radius; x++) {
            for (int y = 0; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    Block block = loc.getBlock().getRelative(x, y, z);
                    if (block.getType() == Material.AIR && block.getRelative(0, -1, 0).getType().isSolid()) {
                        block.setType(Material.COBBLESTONE);
                        plugin.getLogger().info("NPC " + uuid + " placed a block.");
                        return;
                    }
                }
            }
        }
    }
}
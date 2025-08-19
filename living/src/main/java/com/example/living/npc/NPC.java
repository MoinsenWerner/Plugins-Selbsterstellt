package com.example.living.npc;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.example.living.LivingPlugin;
import com.example.living.city.City;

/**
 * Basic representation of an NPC controlled by the plugin.
 * Lightweight and intended to be expanded with behavior logic,
 * pathfinding, and world interaction.
 */
public class NPC {

    private final UUID uuid;
    private final Job job;
    private final Map<String, String> taskParameters = new HashMap<>();
    private boolean active = true;
    private City city;

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

    public void setCity(City city) {
        this.city = city;
    }

    public City getCity() {
        return city;
    }

    public void setTaskParameter(String key, String value) {
        if (key == null) return;
        if (value == null) {
            taskParameters.remove(key);
        } else {
            taskParameters.put(key, value);
        }
    }

    public Map<String, String> getTaskParameters() {
        return taskParameters;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isActive() {
        return active;
    }

    /**
     * Simple AI execution for the NPC based on its job.
     * Placeholder for more advanced behavior.
     */
    public void performTask() {
        if (!active) return;

        Villager villager = resolveVillager();
        if (villager == null) {
            // Entity not found -> skip this tick
            return;
        }

        switch (job) {
            case WOODCUTTER -> performWoodcutterTask(villager);
            case MINER -> performMinerTask(villager);
            case FARMER -> performFarmerTask(villager);
            case BUILDER -> performBuilderTask(villager);
            case CRAFTER -> performCrafterTask(villager);
            default -> {
                // no-op for unhandled jobs
            }
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
                        addChestItems(logMaterial, 1);
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
        Material[] mineables = {Material.STONE, Material.SAND, Material.GRASS_BLOCK, Material.DIRT};
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    Block block = loc.getBlock().getRelative(x, y, z);
                    for (Material mat : mineables) {
                        if (block.getType() == mat) {
                            block.breakNaturally();
                            addChestItems(mat, 1);
                            plugin.getLogger().info("NPC " + uuid + " mined " + mat.name().toLowerCase() + ".");
                            return;
                        }
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
                        addChestItems(Material.WHEAT, 1);
                        plugin.getLogger().info("NPC " + uuid + " harvested wheat.");
                        return;
                    }
                }
            }
        }

        // Try to plant any available saplings on nearby dirt or grass
        Material[] saplings = {
            Material.OAK_SAPLING,
            Material.BIRCH_SAPLING,
            Material.SPRUCE_SAPLING,
            Material.JUNGLE_SAPLING,
            Material.ACACIA_SAPLING
        };
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                Block soil = loc.getBlock().getRelative(x, -1, z);
                Block above = soil.getRelative(0, 1, 0);
                if ((soil.getType() == Material.GRASS_BLOCK || soil.getType() == Material.DIRT) && above.getType() == Material.AIR) {
                    for (Material sap : saplings) {
                        if (getChestItemCount(sap) > 0) {
                            above.setType(sap);
                            removeChestItems(sap, 1);
                            plugin.getLogger().info("NPC " + uuid + " planted " + sap.name().toLowerCase() + ".");
                            return;
                        }
                    }
                }
            }
        }
    }

    private void performCrafterTask(Villager villager) {
        LivingPlugin plugin = LivingPlugin.getInstance();
        if (city == null) return;

        Material[] logs = {
            Material.OAK_LOG,
            Material.BIRCH_LOG,
            Material.SPRUCE_LOG,
            Material.JUNGLE_LOG,
            Material.ACACIA_LOG
        };
        for (Material log : logs) {
            if (getChestItemCount(log) > 0) {
                removeChestItems(log, 1);
                Material planks = Material.matchMaterial(log.name().replace("_LOG", "_PLANKS"));
                if (planks != null) {
                    addChestItems(planks, 4);
                    plugin.getLogger().info("NPC " + uuid + " crafted planks from " + log.name().toLowerCase() + ".");
                }
                return;
            }
        }

        if (getChestItemCount(Material.WHEAT) >= 3) {
            removeChestItems(Material.WHEAT, 3);
            addChestItems(Material.BREAD, 1);
            plugin.getLogger().info("NPC " + uuid + " crafted bread.");
            return;
        }

        if (getChestItemCount(Material.STONE) >= 4) {
            removeChestItems(Material.STONE, 4);
            addChestItems(Material.STONE_BRICKS, 4);
            plugin.getLogger().info("NPC " + uuid + " crafted stone bricks.");
        }
    }

    private void performBuilderTask(Villager villager) {
        LivingPlugin plugin = LivingPlugin.getInstance();
        if (Boolean.parseBoolean(taskParameters.getOrDefault("built", "false"))) {
            return;
        }
        if (city == null) {
            plugin.getLogger().warning("Builder NPC has no city assigned.");
            return;
        }

        int logsRequired = 100;
        int stoneRequired = 20;
        int wheatRequired = 10;

        int logsAvailable = city.getResource(Material.OAK_LOG) + getChestItemCount(Material.OAK_LOG);
        int stoneAvailable = city.getResource(Material.STONE) + getChestItemCount(Material.STONE);
        int wheatAvailable = city.getResource(Material.WHEAT) + getChestItemCount(Material.WHEAT);

        if (logsAvailable < logsRequired || stoneAvailable < stoneRequired || wheatAvailable < wheatRequired) {
            plugin.getLogger().info("NPC " + uuid + " lacks resources to build.");
            return;
        }

        int logsFromCity = Math.min(logsRequired, city.getResource(Material.OAK_LOG));
        city.consumeResource(Material.OAK_LOG, logsFromCity);
        removeChestItems(Material.OAK_LOG, logsRequired - logsFromCity);

        int stoneFromCity = Math.min(stoneRequired, city.getResource(Material.STONE));
        city.consumeResource(Material.STONE, stoneFromCity);
        removeChestItems(Material.STONE, stoneRequired - stoneFromCity);

        int wheatFromCity = Math.min(wheatRequired, city.getResource(Material.WHEAT));
        city.consumeResource(Material.WHEAT, wheatFromCity);
        removeChestItems(Material.WHEAT, wheatRequired - wheatFromCity);

        buildSimpleHouse(villager.getLocation());
        taskParameters.put("built", "true");
        plugin.getLogger().info("NPC " + uuid + " built a house using stored resources.");
    }

    private void buildSimpleHouse(Location base) {
        if (base == null || base.getWorld() == null) return;

        // floor 5x5
        for (int x = 0; x < 5; x++) {
            for (int z = 0; z < 5; z++) {
                base.clone().add(x, 0, z).getBlock().setType(Material.OAK_PLANKS);
            }
        }
        // walls
        for (int y = 1; y < 4; y++) {
            for (int x = 0; x < 5; x++) {
                for (int z = 0; z < 5; z++) {
                    if (x == 0 || x == 4 || z == 0 || z == 4) {
                        base.clone().add(x, y, z).getBlock().setType(Material.OAK_LOG);
                    }
                }
            }
        }
        // doorway (simple)
        base.clone().add(2, 1, 0).getBlock().setType(Material.AIR);
        base.clone().add(2, 2, 0).getBlock().setType(Material.AIR);
        base.clone().add(2, 1, 0).getBlock().setType(Material.OAK_DOOR);
        // roof
        for (int x = 0; x < 5; x++) {
            for (int z = 0; z < 5; z++) {
                base.clone().add(x, 4, z).getBlock().setType(Material.OAK_PLANKS);
            }
        }
    }

    private int getChestItemCount(Material material) {
        LivingPlugin plugin = LivingPlugin.getInstance();
        if (city == null) return 0;

        Location core = city.getCoreLocation();
        if (core == null || core.getWorld() == null) return 0;

        int radius = plugin.getConfig().getInt("storage.chest-radius", 10);
        int count = 0;
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    Block block = core.getBlock().getRelative(x, y, z);
                    if (block.getType() == Material.CHEST) {
                        Chest chest = (Chest) block.getState();
                        Inventory inv = chest.getBlockInventory();
                        for (ItemStack stack : inv.getContents()) {
                            if (stack != null && stack.getType() == material) {
                                count += stack.getAmount();
                            }
                        }
                    }
                }
            }
        }
        return count;
    }

    private void removeChestItems(Material material, int amount) {
        if (amount <= 0 || city == null) return;

        LivingPlugin plugin = LivingPlugin.getInstance();
        Location core = city.getCoreLocation();
        if (core == null || core.getWorld() == null) return;

        int radius = plugin.getConfig().getInt("storage.chest-radius", 10);
        for (int x = -radius; x <= radius && amount > 0; x++) {
            for (int y = -radius; y <= radius && amount > 0; y++) {
                for (int z = -radius; z <= radius && amount > 0; z++) {
                    Block block = core.getBlock().getRelative(x, y, z);
                    if (block.getType() == Material.CHEST) {
                        Chest chest = (Chest) block.getState();
                        Inventory inv = chest.getBlockInventory();
                        for (int i = 0; i < inv.getSize() && amount > 0; i++) {
                            ItemStack stack = inv.getItem(i);
                            if (stack != null && stack.getType() == material) {
                                int take = Math.min(amount, stack.getAmount());
                                int newAmount = stack.getAmount() - take;
                                if (newAmount <= 0) {
                                    inv.setItem(i, null);
                                } else {
                                    stack.setAmount(newAmount);
                                    inv.setItem(i, stack);
                                }
                                amount -= take;
                            }
                        }
                        chest.update();
                    }
                }
            }
        }
    }

    private void addChestItems(Material material, int amount) {
        if (amount <= 0 || city == null) return;

        LivingPlugin plugin = LivingPlugin.getInstance();
        Location core = city.getCoreLocation();
        if (core == null || core.getWorld() == null) return;

        int radius = plugin.getConfig().getInt("storage.chest-radius", 10);
        int remaining = amount;
        for (int x = -radius; x <= radius && remaining > 0; x++) {
            for (int y = -radius; y <= radius && remaining > 0; y++) {
                for (int z = -radius; z <= radius && remaining > 0; z++) {
                    Block block = core.getBlock().getRelative(x, y, z);
                    if (block.getType() == Material.CHEST) {
                        Chest chest = (Chest) block.getState();
                        Inventory inv = chest.getBlockInventory();
                        ItemStack toAdd = new ItemStack(material, remaining);
                        Map<Integer, ItemStack> leftover = inv.addItem(toAdd);
                        chest.update();
                        if (leftover.isEmpty()) {
                            return;
                        }
                        remaining = leftover.values().stream().mapToInt(ItemStack::getAmount).sum();
                    }
                }
            }
        }
    }

    /** Resolve the Villager entity by UUID or return null if missing. */
    private Villager resolveVillager() {
        Entity e = Bukkit.getEntity(uuid);
        if (e instanceof Villager v) return v;
        return null;
    }
}

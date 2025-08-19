package com.example.living.npc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Cow;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

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
            case HUNTER -> performHunterTask(villager);
            case COLLECTOR -> performCollectorTask(villager);
            default -> {
                // no-op for unhandled jobs
            }
        }
    }

    private void performWoodcutterTask(Villager villager) {
        LivingPlugin plugin = LivingPlugin.getInstance();
        List<Material> logs = plugin.getWoodcutterLogs();
        if (logs.isEmpty()) return;

        Location loc = villager.getLocation();
        int radius = 5;
        for (int x = -radius; x <= radius; x++) {
            for (int y = -1; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    Block block = loc.getBlock().getRelative(x, y, z);
                    if (logs.contains(block.getType())) {
                        block.breakNaturally();
                        addChestItems(block.getType(), 1);
                        plugin.getLogger().info("NPC " + uuid + " cut a " + block.getType().name().toLowerCase() + ".");
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
        List<Material> mineables = plugin.getMinerBlocks();
        mineables.remove(Material.DIRT);
        mineables.remove(Material.GRASS_BLOCK);
        if (mineables.isEmpty()) return;
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    Block block = loc.getBlock().getRelative(x, y, z);
                    if (mineables.contains(block.getType())) {
                        block.breakNaturally();
                        addChestItems(block.getType(), 1);
                        plugin.getLogger().info("NPC " + uuid + " mined " + block.getType().name().toLowerCase() + ".");
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
        List<Material> harvestables = plugin.getFarmerHarvestBlocks();
        for (int x = -radius; x <= radius; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -radius; z <= radius; z++) {
                    Block block = loc.getBlock().getRelative(x, y, z);
                    if (harvestables.contains(block.getType())) {
                        Material crop = block.getType();
                        block.breakNaturally();
                        block.setType(crop);
                        addChestItems(crop, 1);
                        plugin.getLogger().info("NPC " + uuid + " harvested " + crop.name().toLowerCase() + ".");
                        return;
                    }
                }
            }
        }

        // Try to plant any available seeds or saplings on nearby dirt or grass
        List<Material> plantables = plugin.getFarmerPlantBlocks();
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                Block soil = loc.getBlock().getRelative(x, -1, z);
                Block above = soil.getRelative(0, 1, 0);
                if ((soil.getType() == Material.GRASS_BLOCK || soil.getType() == Material.DIRT) && above.getType() == Material.AIR) {
                    for (Material sap : plantables) {
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

        // Create farmland from dirt or grass
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                Block soil = loc.getBlock().getRelative(x, -1, z);
                if (soil.getType() == Material.DIRT || soil.getType() == Material.GRASS_BLOCK) {
                    soil.setType(Material.FARMLAND);
                    plugin.getLogger().info("NPC " + uuid + " tilled soil into farmland.");
                    return;
                }
            }
        }

        // Place water if available
        if (getChestItemCount(Material.WATER_BUCKET) > 0) {
            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    Block ground = loc.getBlock().getRelative(x, -1, z);
                    Block above = ground.getRelative(0, 1, 0);
                    if (above.getType() == Material.AIR) {
                        above.setType(Material.WATER);
                        removeChestItems(Material.WATER_BUCKET, 1);
                        addChestItems(Material.BUCKET, 1);
                        plugin.getLogger().info("NPC " + uuid + " placed water.");
                        return;
                    }
                }
            }
        }
    }

    private void performCrafterTask(Villager villager) {
        LivingPlugin plugin = LivingPlugin.getInstance();
        if (city == null) return;

        // 1) Logs -> Planks (4)
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

        // 2) Planks (6) -> Doors (3)
        Material[] planks = {
            Material.OAK_PLANKS,
            Material.BIRCH_PLANKS,
            Material.SPRUCE_PLANKS,
            Material.JUNGLE_PLANKS,
            Material.ACACIA_PLANKS
        };
        for (Material plank : planks) {
            if (getChestItemCount(plank) >= 6) {
                removeChestItems(plank, 6);
                Material door = Material.matchMaterial(plank.name().replace("_PLANKS", "_DOOR"));
                if (door != null) {
                    addChestItems(door, 3);
                    plugin.getLogger().info("NPC " + uuid + " crafted doors from " + plank.name().toLowerCase() + ".");
                }
                return;
            }
        }

        // 3) Sand (6) -> Glass Panes (16)
        if (getChestItemCount(Material.SAND) >= 6) {
            removeChestItems(Material.SAND, 6);
            addChestItems(Material.GLASS_PANE, 16);
            plugin.getLogger().info("NPC " + uuid + " crafted windows.");
            return;
        }

        // 4) Wheat (3) -> Bread (1)
        if (getChestItemCount(Material.WHEAT) >= 3) {
            removeChestItems(Material.WHEAT, 3);
            addChestItems(Material.BREAD, 1);
            plugin.getLogger().info("NPC " + uuid + " crafted bread.");
            return;
        }

        // 5) Stone (4) -> Stone Bricks (4)
        if (getChestItemCount(Material.STONE) >= 4) {
            removeChestItems(Material.STONE, 4);
            addChestItems(Material.STONE_BRICKS, 4);
            plugin.getLogger().info("NPC " + uuid + " crafted stone bricks.");
        }
    }

    private void performHunterTask(Villager villager) {
        LivingPlugin plugin = LivingPlugin.getInstance();
        Location loc = villager.getLocation();
        int radius = 5;

        // Shear sheep if available
        for (Entity e : villager.getNearbyEntities(radius, radius, radius)) {
            if (e instanceof Sheep sheep && !sheep.isSheared()) {
                sheep.setSheared(true);
                Material woolMat = Material.matchMaterial(sheep.getColor().name() + "_WOOL");
                if (woolMat != null) {
                    addChestItems(woolMat, 1);
                }
                plugin.getLogger().info("NPC " + uuid + " sheared a sheep.");
                return;
            }
        }

        // Breed cows if possible
        if (getChestItemCount(Material.WHEAT) > 0) {
            int cowCount = 0;
            Location cowLoc = null;
            for (Entity e : villager.getNearbyEntities(radius, radius, radius)) {
                if (e instanceof Cow cow) {
                    cowCount++;
                    if (cowLoc == null) cowLoc = cow.getLocation();
                }
            }
            if (cowCount >= 2 && cowLoc != null) {
                Cow calf = (Cow) loc.getWorld().spawnEntity(cowLoc, EntityType.COW);
                calf.setBaby();
                removeChestItems(Material.WHEAT, 1);
                plugin.getLogger().info("NPC " + uuid + " bred cows.");
                return;
            }
        }

        // Breed pigs if possible
        if (getChestItemCount(Material.CARROT) > 0) {
            int pigCount = 0;
            Location pigLoc = null;
            for (Entity e : villager.getNearbyEntities(radius, radius, radius)) {
                if (e instanceof Pig pig) {
                    pigCount++;
                    if (pigLoc == null) pigLoc = pig.getLocation();
                }
            }
            if (pigCount >= 2 && pigLoc != null) {
                Pig piglet = (Pig) loc.getWorld().spawnEntity(pigLoc, EntityType.PIG);
                piglet.setBaby();
                removeChestItems(Material.CARROT, 1);
                plugin.getLogger().info("NPC " + uuid + " bred pigs.");
                return;
            }
        }

        // Hunt cows or pigs
        for (Entity e : villager.getNearbyEntities(radius, radius, radius)) {
            if (e instanceof Cow) {
                e.remove();
                addChestItems(Material.BEEF, 1);
                addChestItems(Material.LEATHER, 1);
                plugin.getLogger().info("NPC " + uuid + " hunted a cow.");
                return;
            } else if (e instanceof Pig) {
                e.remove();
                addChestItems(Material.PORKCHOP, 1);
                plugin.getLogger().info("NPC " + uuid + " hunted a pig.");
                return;
            }
        }
    }

    private void performCollectorTask(Villager villager) {
        LivingPlugin plugin = LivingPlugin.getInstance();
        int radius = plugin.getConfig().getInt("collector.pickup-radius", 5);
        for (Entity e : villager.getNearbyEntities(radius, radius, radius)) {
            if (e instanceof Item item) {
                ItemStack stack = item.getItemStack();
                addChestItems(stack.getType(), stack.getAmount());
                item.remove();
                plugin.getLogger().info("NPC " + uuid + " collected " + stack.getAmount() + " " + stack.getType().name().toLowerCase() + ".");
                return;
            }
        }

        // Collect water with buckets
        if (getChestItemCount(Material.BUCKET) > 0) {
            Location loc = villager.getLocation();
            for (int x = -1; x <= 1; x++) {
                for (int y = -1; y <= 1; y++) {
                    for (int z = -1; z <= 1; z++) {
                        Block block = loc.getBlock().getRelative(x, y, z);
                        if (block.getType() == Material.WATER) {
                            block.setType(Material.AIR);
                            removeChestItems(Material.BUCKET, 1);
                            addChestItems(Material.WATER_BUCKET, 1);
                            plugin.getLogger().info("NPC " + uuid + " collected water.");
                            return;
                        }
                    }
                }
            }
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

        LivingPlugin plugin = LivingPlugin.getInstance();
        FileConfiguration cfg = plugin.getBuilderHouseConfig();
        ConfigurationSection section = cfg.getConfigurationSection("house");
        int width = section != null ? section.getInt("width", 5) : 5;
        int length = section != null ? section.getInt("length", 5) : 5;
        int height = section != null ? section.getInt("height", 4) : 4;
        ConfigurationSection mats = section != null ? section.getConfigurationSection("materials") : null;
        Material floorMat = getMaterial(mats, "floor", Material.OAK_PLANKS);
        Material wallMat = getMaterial(mats, "wall", Material.OAK_LOG);
        Material roofMat = getMaterial(mats, "roof", Material.OAK_PLANKS);
        Material doorMat = getMaterial(mats, "door", Material.OAK_DOOR);
        Material bedMat = getMaterial(mats, "bed", Material.RED_BED);

        for (int x = 0; x < width; x++) {
            for (int z = 0; z < length; z++) {
                base.clone().add(x, 0, z).getBlock().setType(floorMat);
            }
        }

        for (int y = 1; y < height; y++) {
            for (int x = 0; x < width; x++) {
                for (int z = 0; z < length; z++) {
                    if (x == 0 || x == width - 1 || z == 0 || z == length - 1) {
                        base.clone().add(x, y, z).getBlock().setType(wallMat);
                    }
                }
            }
        }

        int doorX = width / 2;
        base.clone().add(doorX, 1, 0).getBlock().setType(Material.AIR);
        base.clone().add(doorX, 2, 0).getBlock().setType(Material.AIR);
        base.clone().add(doorX, 1, 0).getBlock().setType(doorMat);

        // Place bed inside the house
        base.clone().add(1, 1, 1).getBlock().setType(bedMat);

        for (int x = 0; x < width; x++) {
            for (int z = 0; z < length; z++) {
                base.clone().add(x, height, z).getBlock().setType(roofMat);
            }
        }
    }

    private Material getMaterial(ConfigurationSection section, String key, Material def) {
        if (section == null) return def;
        String name = section.getString(key);
        if (name == null) return def;
        Material mat = Material.matchMaterial(name);
        return mat != null ? mat : def;
    }

    private int getChestItemCount(Material material) {
        LivingPlugin plugin = LivingPlugin.getInstance();
        Location center = city != null ? city.getCoreLocation() : null;
        if (center == null || center.getWorld() == null) {
            Villager v = resolveVillager();
            if (v != null) {
                center = v.getLocation();
            }
        }
        if (center == null || center.getWorld() == null) return 0;

        int radius = plugin.getConfig().getInt("storage.chest-radius", 10);
        int count = 0;
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    Block block = center.getBlock().getRelative(x, y, z);
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
        if (amount <= 0) return;

        LivingPlugin plugin = LivingPlugin.getInstance();
        Location center = city != null ? city.getCoreLocation() : null;
        if (center == null || center.getWorld() == null) {
            Villager v = resolveVillager();
            if (v != null) {
                center = v.getLocation();
            }
        }
        if (center == null || center.getWorld() == null) return;

        int radius = plugin.getConfig().getInt("storage.chest-radius", 10);
        for (int x = -radius; x <= radius && amount > 0; x++) {
            for (int y = -radius; y <= radius && amount > 0; y++) {
                for (int z = -radius; z <= radius && amount > 0; z++) {
                    Block block = center.getBlock().getRelative(x, y, z);
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
                    }
                }
            }
        }
    }

    private void addChestItems(Material material, int amount) {
        if (amount <= 0) return;

        LivingPlugin plugin = LivingPlugin.getInstance();
        Location center = city != null ? city.getCoreLocation() : null;
        if (center == null || center.getWorld() == null) {
            Villager v = resolveVillager();
            if (v != null) {
                center = v.getLocation();
            }
        }
        if (center == null || center.getWorld() == null) return;

        int radius = plugin.getConfig().getInt("storage.chest-radius", 10);
        int remaining = amount;
        for (int x = -radius; x <= radius && remaining > 0; x++) {
            for (int y = -radius; y <= radius && remaining > 0; y++) {
                for (int z = -radius; z <= radius && remaining > 0; z++) {
                    Block block = center.getBlock().getRelative(x, y, z);
                    if (block.getType() == Material.CHEST) {
                        Chest chest = (Chest) block.getState();
                        Inventory inv = chest.getBlockInventory();
                        ItemStack toAdd = new ItemStack(material, remaining);
                        Map<Integer, ItemStack> leftover = inv.addItem(toAdd);
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

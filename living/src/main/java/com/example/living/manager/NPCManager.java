package com.example.living.manager;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitScheduler;

import com.example.living.LivingPlugin;
import com.example.living.city.City;
import com.example.living.npc.Job;
import com.example.living.npc.NPC;

/**
 * Erzeugt und verwaltet NPC-Entitäten für Städte.
 */
public class NPCManager {
    private final LivingPlugin plugin;
    private final Map<UUID, NPC> npcs = new HashMap<>();
    private final File dataFile;
    private final FileConfiguration dataConfig;

    public NPCManager(LivingPlugin plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "npc-settings.yml");
        this.dataConfig = YamlConfiguration.loadConfiguration(dataFile);
    }

    public void spawnInitialNpcs(City city) {
        Map<Job, Integer> jobs = plugin.getInitialJobs();
        for (Map.Entry<Job, Integer> entry : jobs.entrySet()) {
            for (int i = 0; i < entry.getValue(); i++) {
                spawnNpc(city, entry.getKey());
            }
        }
    }

    public NPC spawnNpc(City city, Job job) {
        if (city.getNpcs().size() >= plugin.getMaxNpcs()) {
            plugin.getLogger().warning("Max NPCs reached for city " + city.getName());
            return null;
        }
        Location loc = city.getCoreLocation();
        Villager villager = loc.getWorld().spawn(loc, Villager.class);
        villager.setCustomName(job.name());
        villager.setCustomNameVisible(true);
        NPC npc = new NPC(villager.getUniqueId(), job);
        city.addNpc(npc);
        npcs.put(npc.getUuid(), npc);
        loadNpcSettings(npc);
        BukkitScheduler scheduler = plugin.getServer().getScheduler();
        scheduler.runTaskTimer(plugin, npc::performTask, plugin.getNpcTaskInterval(), plugin.getNpcTaskInterval());
        return npc;
    }

    public NPC getNpc(UUID uuid) {
        return npcs.get(uuid);
    }

    public void openNpcListGui(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, "NPC Manager");
        NamespacedKey key = new NamespacedKey(plugin, "npc-uuid");
        int slot = 0;
        for (NPC npc : npcs.values()) {
            ItemStack item = new ItemStack(Material.VILLAGER_SPAWN_EGG);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(npc.getJob() + " - " + npc.getUuid());
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, npc.getUuid().toString());
            item.setItemMeta(meta);
            inv.setItem(slot++, item);
        }
        player.openInventory(inv);
    }

    public void openNpcSettingsGui(Player player, NPC npc) {
        Inventory inv = Bukkit.createInventory(null, 9, "NPC Settings: " + npc.getUuid());
        ItemStack toggle = new ItemStack(npc.isActive() ? Material.LIME_WOOL : Material.RED_WOOL);
        ItemMeta meta = toggle.getItemMeta();
        meta.setDisplayName(npc.isActive() ? "Stop" : "Start");
        NamespacedKey key = new NamespacedKey(plugin, "npc-uuid");
        meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, npc.getUuid().toString());
        toggle.setItemMeta(meta);
        inv.setItem(0, toggle);

        if (npc.getJob() == Job.WOODCUTTER) {
            ItemStack tree = new ItemStack(Material.OAK_SAPLING);
            ItemMeta treeMeta = tree.getItemMeta();
            String type = npc.getTaskParameters().getOrDefault("tree", "OAK");
            treeMeta.setDisplayName("Tree: " + type);
            treeMeta.getPersistentDataContainer().set(key, PersistentDataType.STRING, npc.getUuid().toString());
            tree.setItemMeta(treeMeta);
            inv.setItem(1, tree);
        }
        player.openInventory(inv);
    }

    public void handleInventoryClick(Player player, Inventory inv, ItemStack item) {
        if (inv.getTitle().equals("NPC Manager")) {
            NamespacedKey key = new NamespacedKey(plugin, "npc-uuid");
            ItemMeta meta = item.getItemMeta();
            if (meta == null) return;
            PersistentDataContainer container = meta.getPersistentDataContainer();
            String uuidStr = container.get(key, PersistentDataType.STRING);
            if (uuidStr == null) return;
            NPC npc = getNpc(UUID.fromString(uuidStr));
            if (npc != null) {
                openNpcSettingsGui(player, npc);
            }
        } else if (inv.getTitle().startsWith("NPC Settings")) {
            NamespacedKey key = new NamespacedKey(plugin, "npc-uuid");
            ItemMeta meta = item.getItemMeta();
            if (meta == null) return;
            String uuidStr = meta.getPersistentDataContainer().get(key, PersistentDataType.STRING);
            if (uuidStr == null) return;
            NPC npc = getNpc(UUID.fromString(uuidStr));
            if (npc == null) return;
            if (item.getType() == Material.LIME_WOOL || item.getType() == Material.RED_WOOL) {
                npc.setActive(!npc.isActive());
                saveNpcSettings(npc);
                openNpcSettingsGui(player, npc);
            } else if (item.getType() == Material.OAK_SAPLING) {
                String current = npc.getTaskParameters().getOrDefault("tree", "OAK");
                String next = switch (current) {
                    case "OAK" -> "BIRCH";
                    case "BIRCH" -> "SPRUCE";
                    default -> "OAK";
                };
                npc.setTaskParameter("tree", next);
                saveNpcSettings(npc);
                openNpcSettingsGui(player, npc);
            }
        }
    }

    public void saveNpcSettings(NPC npc) {
        String base = npc.getUuid().toString();
        dataConfig.set(base + ".active", npc.isActive());
        dataConfig.set(base + ".job", npc.getJob().name());
        dataConfig.set(base + ".params", npc.getTaskParameters());
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Could not save NPC settings: " + e.getMessage());
        }
    }

    private void loadNpcSettings(NPC npc) {
        String base = npc.getUuid().toString();
        if (dataConfig.contains(base)) {
            npc.setActive(dataConfig.getBoolean(base + ".active", true));
            if (dataConfig.isConfigurationSection(base + ".params")) {
                for (Map.Entry<String, Object> entry : dataConfig.getConfigurationSection(base + ".params").getValues(false).entrySet()) {
                    npc.setTaskParameter(entry.getKey(), entry.getValue().toString());
                }
            }
        }
    }
}

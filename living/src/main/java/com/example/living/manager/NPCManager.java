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

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import com.example.living.LivingPlugin;
import com.example.living.city.City;
import com.example.living.npc.Job;
import com.example.living.npc.NPC;

/**
 * Erzeugt und verwaltet NPC-Entitäten für Städte.
 */
public class NPCManager {
    private static final Component TITLE_NPC_LIST = Component.text("NPC Verwaltung");
    private static final String TITLE_SETTINGS_PREFIX = "NPC Settings: ";

    private final LivingPlugin plugin;
    private final Map<UUID, NPC> npcs = new HashMap<>();
    private final File dataFile;
    private final FileConfiguration dataConfig;

    /** Map, um den GUI-Titel pro Inventory zuzuordnen (ersetzt getTitle()). */
    private final Map<Inventory, Component> guiTitles = new HashMap<>();

    /** Einheitlicher PDC-Key für Item-Metadaten. */
    private final NamespacedKey npcUuidKey;

    public NPCManager(LivingPlugin plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "npc-settings.yml");
        this.dataConfig = YamlConfiguration.loadConfiguration(dataFile);
        this.npcUuidKey = new NamespacedKey(plugin, "npc-uuid");
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
        if (loc == null || loc.getWorld() == null) {
            plugin.getLogger().warning("Cannot spawn NPC: city core location/world is null for " + city.getName());
            return null;
        }

        Villager villager = loc.getWorld().spawn(loc, Villager.class);
        // Sichtbarer Name (Adventure, falls deine API dies unterstützt)
        villager.customName(Component.text(job.name()));
        villager.setCustomNameVisible(true);

        NPC npc = new NPC(villager.getUniqueId(), job);
        city.addNpc(npc);
        npcs.put(npc.getUuid(), npc);

        loadNpcSettings(npc);

        BukkitScheduler scheduler = plugin.getServer().getScheduler();
        long interval = Math.max(1L, plugin.getNpcTaskInterval());
        scheduler.runTaskTimer(plugin, npc::performTask, interval, interval);
        return npc;
    }

    public NPC getNpc(UUID uuid) {
        return npcs.get(uuid);
    }

    public void openNpcListGui(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, TITLE_NPC_LIST);
        guiTitles.put(inv, TITLE_NPC_LIST);

        int slot = 0;
        for (NPC npc : npcs.values()) {
            ItemStack item = new ItemStack(Material.VILLAGER_SPAWN_EGG);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.displayName(Component.text(npc.getJob() + " - " + npc.getUuid()));
                meta.getPersistentDataContainer().set(npcUuidKey, PersistentDataType.STRING, npc.getUuid().toString());
                item.setItemMeta(meta);
            }
            if (slot < inv.getSize()) {
                inv.setItem(slot++, item);
            } else {
                break; // GUI voll
            }
        }
        player.openInventory(inv);
    }

    public void openNpcSettingsGui(Player player, NPC npc) {
        Component title = Component.text(TITLE_SETTINGS_PREFIX + npc.getUuid());
        Inventory inv = Bukkit.createInventory(null, 9, title);
        guiTitles.put(inv, title);

        // Start/Stop Toggle
        ItemStack toggle = new ItemStack(npc.isActive() ? Material.LIME_WOOL : Material.RED_WOOL);
        ItemMeta meta = toggle.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text(npc.isActive() ? "Stop" : "Start"));
            meta.getPersistentDataContainer().set(npcUuidKey, PersistentDataType.STRING, npc.getUuid().toString());
            toggle.setItemMeta(meta);
        }
        inv.setItem(0, toggle);

        // Job-spezifische Optionen
        if (npc.getJob() == Job.WOODCUTTER) {
            String type = npc.getTaskParameters().getOrDefault("tree", "OAK");
            Material saplingMat = Material.matchMaterial(type + "_SAPLING");
            ItemStack tree = new ItemStack(saplingMat != null ? saplingMat : Material.OAK_SAPLING);
            ItemMeta treeMeta = tree.getItemMeta();
            if (treeMeta != null) {
                treeMeta.displayName(Component.text("Tree: " + type));
                treeMeta.getPersistentDataContainer().set(npcUuidKey, PersistentDataType.STRING, npc.getUuid().toString());
                tree.setItemMeta(treeMeta);
            }
            inv.setItem(1, tree);
        }
        player.openInventory(inv);
    }

    /**
     * Bevorzugte Variante: Listener reicht den echten View-Titel (Component) mit.
     */
    public void handleInventoryClick(Player player, Component viewTitle, Inventory inv, ItemStack item) {
        if (player == null || inv == null || item == null) return;

        Component effectiveTitle = (viewTitle != null) ? viewTitle : guiTitles.get(inv);
        if (effectiveTitle == null) {
            // Fallback: unbekanntes Inventar – nichts zu tun
            return;
        }

        String plainTitle = PlainTextComponentSerializer.plainText().serialize(effectiveTitle);

        if (TITLE_NPC_LIST.equals(effectiveTitle) || "NPC Verwaltung".equals(plainTitle)) {
            handleNpcListClick(player, item);
            return;
        }

        if (plainTitle.startsWith(TITLE_SETTINGS_PREFIX)) {
            handleNpcSettingsClick(player, item);
        }
    }

    /**
     * Abwärtskompatibel: Falls der Listener den View-Titel nicht liefert.
     * Versucht, den Titel aus der internen Map zu bestimmen.
     */
    @Deprecated
    public void handleInventoryClick(Player player, Inventory inv, ItemStack item) {
        Component mapped = guiTitles.get(inv);
        handleInventoryClick(player, mapped, inv, item);
    }

    private void handleNpcListClick(Player player, ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        PersistentDataContainer container = meta.getPersistentDataContainer();
        String uuidStr = container.get(npcUuidKey, PersistentDataType.STRING);
        if (uuidStr == null) return;

        NPC npc = getNpc(safeUuid(uuidStr));
        if (npc != null) {
            openNpcSettingsGui(player, npc);
            player.sendMessage("Settings: active=" + npc.isActive() + ", params=" + npc.getTaskParameters());
        }
    }

    private void handleNpcSettingsClick(Player player, ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        String uuidStr = meta.getPersistentDataContainer().get(npcUuidKey, PersistentDataType.STRING);
        if (uuidStr == null) return;

        NPC npc = getNpc(safeUuid(uuidStr));
        if (npc == null) return;

        Material type = item.getType();
        if (type == Material.LIME_WOOL || type == Material.RED_WOOL) {
            npc.setActive(!npc.isActive());
            saveNpcSettings(npc);
            openNpcSettingsGui(player, npc);
        } else if (type.name().endsWith("_SAPLING")) {
            String current = npc.getTaskParameters().getOrDefault("tree", "OAK");
            String next = switch (current) {
                case "OAK" -> "BIRCH";
                case "BIRCH" -> "SPRUCE";
                case "SPRUCE" -> "JUNGLE";
                case "JUNGLE" -> "ACACIA";
                case "ACACIA" -> "OAK";
                default -> "OAK";
            };
            npc.setTaskParameter("tree", next);
            saveNpcSettings(npc);
            openNpcSettingsGui(player, npc);
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
                    npc.setTaskParameter(entry.getKey(), String.valueOf(entry.getValue()));
                }
            }
        }
    }

    private static UUID safeUuid(String s) {
        try {
            return UUID.fromString(s);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}

package com.example.living;

import java.io.File;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import com.example.living.commands.NpcCommand;
import com.example.living.npc.Job;
import com.example.living.manager.CityManager;
import com.example.living.manager.NPCManager;
import com.example.living.listener.CityCoreListener;
import com.example.living.listener.NpcGuiListener;

/**
 * Main plugin class for the Living NPC city simulation.
 * This class is responsible for initializing managers that handle
 * cities, NPCs and their tasks. The real game logic is still to be
 * implemented and currently acts as a scaffold.
 */
public class LivingPlugin extends JavaPlugin {

    private static LivingPlugin instance;
    private String disableReason = "Server shutdown or reload";
    private CityManager cityManager;
    private NPCManager npcManager;
    private FileConfiguration builderHouseConfig;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        saveResource("builder_house.yml", false);
        builderHouseConfig = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "builder_house.yml"));
        this.cityManager = new CityManager(this);
        this.npcManager = new NPCManager(this);
        getServer().getPluginManager().registerEvents(new CityCoreListener(this), this);
        getServer().getPluginManager().registerEvents(new NpcGuiListener(this), this);
        NpcCommand npcCommand = new NpcCommand(this);
        getCommand("l-m").setExecutor(npcCommand);
        getCommand("l-m").setTabCompleter(npcCommand);
        getLogger().info("Living plugin enabled. Placeholder simulation initialized.");
    }

    @Override
    public void onDisable() {
        getLogger().info("Living plugin disabled: " + disableReason);
        instance = null;
    }

    /**
     * Disable this plugin with a specific reason that will be logged to the console.
     *
     * @param reason explanation why the plugin is being disabled
     */
    public void disablePlugin(String reason) {
        this.disableReason = reason;
        getLogger().warning("Disabling Living plugin: " + reason);
        Bukkit.getPluginManager().disablePlugin(this);
    }

    public static LivingPlugin getInstance() {
        return instance;
    }

    public CityManager getCityManager() {
        return cityManager;
    }

    public NPCManager getNpcManager() {
        return npcManager;
    }

    public Material getCoreMaterial() {
        String matName = getConfig().getString("city.core-material", "LODESTONE");
        Material material = Material.matchMaterial(matName);
        return material != null ? material : Material.LODESTONE;
    }

    public int getMaxNpcs() {
        return getConfig().getInt("city.max-npcs", 50);
    }

    public int getSpawnInterval() {
        return getConfig().getInt("city.spawn-interval", 600);
    }

    public int getNpcTaskInterval() {
        return getConfig().getInt("npc.task-interval", 200);
    }

    public Map<Job, Integer> getInitialJobs() {
        Map<Job, Integer> map = new EnumMap<>(Job.class);
        ConfigurationSection section = getConfig().getConfigurationSection("city.initial-jobs");
        for (Job job : Job.values()) {
            int amount = section != null ? section.getInt(job.name().toLowerCase(), 0) : 0;
            map.put(job, amount);
        }
        return map;
    }

    public List<Material> getWoodcutterLogs() {
        return getMaterialList("woodcutter.logs");
    }

    public List<Material> getMinerBlocks() {
        return getMaterialList("miner.blocks");
    }

    public List<Material> getFarmerHarvestBlocks() {
        return getMaterialList("farmer.harvest");
    }

    public List<Material> getFarmerPlantBlocks() {
        return getMaterialList("farmer.plant");
    }

    public FileConfiguration getBuilderHouseConfig() {
        return builderHouseConfig;
    }

    private List<Material> getMaterialList(String path) {
        List<String> names = getConfig().getStringList(path);
        List<Material> mats = new ArrayList<>();
        for (String name : names) {
            Material mat = Material.matchMaterial(name);
            if (mat != null) {
                mats.add(mat);
            } else {
                getLogger().warning("Unknown material '" + name + "' in config path " + path);
            }
        }
        return mats;
    }
}
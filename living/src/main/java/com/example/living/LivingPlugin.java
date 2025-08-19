package com.example.living;

import org.bukkit.plugin.java.JavaPlugin;

/**
 * Main plugin class for the Living NPC city simulation.
 * This class is responsible for initializing managers that handle
 * cities, NPCs and their tasks. The real game logic is still to be
 * implemented and currently acts as a scaffold.
 */
public class LivingPlugin extends JavaPlugin {

    private static LivingPlugin instance;

    @Override
    public void onEnable() {
        instance = this;
        getLogger().info("Living plugin enabled. Placeholder simulation initialized.");
        // Future initialization of managers such as CityManager, NPCManager etc.
    }

    @Override
    public void onDisable() {
        getLogger().info("Living plugin disabled.");
        instance = null;
    }

    public static LivingPlugin getInstance() {
        return instance;
    }
}

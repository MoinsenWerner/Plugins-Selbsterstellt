package com.example.living;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Main plugin class for the Living NPC city simulation.
 * This class is responsible for initializing managers that handle
 * cities, NPCs and their tasks. The real game logic is still to be
 * implemented and currently acts as a scaffold.
 */
public class LivingPlugin extends JavaPlugin {

    private static LivingPlugin instance;
    private String disableReason = "Server shutdown or reload";

    @Override
    public void onEnable() {
        instance = this;
        getLogger().info("Living plugin enabled. Placeholder simulation initialized.");
        // Future initialization of managers such as CityManager, NPCManager etc.
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
}
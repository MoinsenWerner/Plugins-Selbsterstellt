package com.example.chrono;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;

import com.example.chrono.listener.SnapshotProtectionListener;

/**
 * Chrono plugin manages which snapshots allow block placement and breaking.
 */
public class ChronoPlugin extends JavaPlugin {

    private static ChronoPlugin instance;
    private String disableReason = "Server shutdown or reload";
    private Set<String> buildSnapshots = new HashSet<>();
    private Set<String> breakSnapshots = new HashSet<>();

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        reloadSnapshotLists();
        getServer().getPluginManager().registerEvents(new SnapshotProtectionListener(this), this);
        getLogger().info("Chrono plugin enabled. Snapshot build/break rules loaded.");
    }

    @Override
    public void onDisable() {
        getLogger().info("Chrono plugin disabled: " + disableReason);
        instance = null;
    }

    /**
     * Disable the plugin programmatically with a reason logged to console.
     */
    public void disablePlugin(String reason) {
        this.disableReason = reason;
        getLogger().warning("Disabling Chrono plugin: " + reason);
        Bukkit.getPluginManager().disablePlugin(this);
    }

    public void reloadSnapshotLists() {
        buildSnapshots = new HashSet<>(getConfig().getStringList("buildSnapshots"));
        breakSnapshots = new HashSet<>(getConfig().getStringList("breakSnapshots"));
    }

    public Set<String> getBuildSnapshots() {
        return buildSnapshots;
    }

    public Set<String> getBreakSnapshots() {
        return breakSnapshots;
    }

    public static ChronoPlugin getInstance() {
        return instance;
    }
}

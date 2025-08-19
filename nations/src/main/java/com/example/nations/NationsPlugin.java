package com.example.nations;

import com.example.nations.commands.NationCommand;
import com.example.nations.diplomacy.DiplomacyManager;
import com.example.nations.diplomacy.RelationType;
import com.example.nations.model.NationRegistry;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class NationsPlugin extends JavaPlugin {
    private NationRegistry nationRegistry;
    private DiplomacyManager diplomacyManager;
    private int maxCitizens;
    private int maxTerritory;
    private boolean warEnabled;
    private RelationType defaultRelation;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        this.nationRegistry = new NationRegistry();
        this.diplomacyManager = new DiplomacyManager();
        reloadSettings();
        PluginCommand cmd = getCommand("nation");
        if (cmd != null) {
            NationCommand executor = new NationCommand(this);
            cmd.setExecutor(executor);
            cmd.setTabCompleter(executor);
        }
        getLogger().info("Nations plugin enabled");
    }

    @Override
    public void onDisable() {
        getLogger().info("Nations plugin disabled");
    }

    public void reloadSettings() {
        this.maxCitizens = getConfig().getInt("limits.max-citizens", 50);
        this.maxTerritory = getConfig().getInt("limits.max-territory-chunks", 100);
        this.warEnabled = getConfig().getBoolean("war.enabled", true);
        String rel = getConfig().getString("diplomacy.default-relation", "NEUTRAL");
        try {
            this.defaultRelation = RelationType.valueOf(rel.toUpperCase());
        } catch (IllegalArgumentException e) {
            this.defaultRelation = RelationType.NEUTRAL;
        }
    }

    public NationRegistry getNationRegistry() {
        return nationRegistry;
    }

    public DiplomacyManager getDiplomacyManager() {
        return diplomacyManager;
    }

    public int getMaxCitizens() {
        return maxCitizens;
    }

    public int getMaxTerritory() {
        return maxTerritory;
    }

    public boolean isWarEnabled() {
        return warEnabled;
    }

    public RelationType getDefaultRelation() {
        return defaultRelation;
    }
}

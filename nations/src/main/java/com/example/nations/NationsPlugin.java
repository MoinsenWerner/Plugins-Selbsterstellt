package com.example.nations;

import com.example.nations.commands.NationCommand;
import com.example.nations.model.NationRegistry;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class NationsPlugin extends JavaPlugin {
    private NationRegistry nationRegistry;

    @Override
    public void onEnable() {
        this.nationRegistry = new NationRegistry();
        PluginCommand cmd = getCommand("nation");
        if (cmd != null) {
            cmd.setExecutor(new NationCommand(this));
            cmd.setTabCompleter(new NationCommand(this));
        }
        getLogger().info("Nations plugin enabled");
    }

    @Override
    public void onDisable() {
        getLogger().info("Nations plugin disabled");
    }

    public NationRegistry getNationRegistry() {
        return nationRegistry;
    }
}

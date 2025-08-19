package com.example.living.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

import com.example.living.LivingPlugin;

/**
 * Reagiert auf das Platzieren eines Stadtkern-Blocks und erstellt eine neue Stadt.
 */
public class CityCoreListener implements Listener {

    private final LivingPlugin plugin;

    public CityCoreListener(LivingPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.getBlockPlaced().getType() != plugin.getCoreMaterial()) {
            return;
        }
        String name = event.getPlayer().getName() + "'s City";
        plugin.getCityManager().createCity(name, event.getBlockPlaced().getLocation());
        event.getPlayer().sendMessage("Stadt '" + name + "' gegründet!");
    }
}


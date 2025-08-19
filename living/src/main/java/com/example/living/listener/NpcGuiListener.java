package com.example.living.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;

import com.example.living.LivingPlugin;

/**
 * Handles interactions with NPC management inventories.
 */
public class NpcGuiListener implements Listener {
    private final LivingPlugin plugin;

    public NpcGuiListener(LivingPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        String title = event.getView().getTitle();
        if (title.equals("NPC Manager") || title.startsWith("NPC Settings")) {
            if (event.getCurrentItem() != null &&
                (event.getAction().name().startsWith("PICKUP") || event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY)) {
                plugin.getNpcManager().handleInventoryClick(player, event.getView().getTopInventory(), event.getCurrentItem());
            }
            event.setCancelled(true);
        }
    }
}


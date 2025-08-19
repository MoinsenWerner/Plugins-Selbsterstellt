package com.example.living.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

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
        Component viewTitle = event.getView().title();
        String plain = PlainTextComponentSerializer.plainText().serialize(viewTitle);
        if (plain.equals("NPC Verwaltung") || plain.startsWith("NPC Settings")) {
            if (event.getCurrentItem() != null &&
                (event.getAction().name().startsWith("PICKUP") || event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY)) {
                plugin.getNpcManager().handleInventoryClick(player, viewTitle, event.getView().getTopInventory(), event.getCurrentItem());
            }
            event.setCancelled(true);
        }
    }
}


package net.timeux.chrono;

import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.EnumSet;
import java.util.Set;

public final class ItemFilter {

    private static final Set<Material> ALLOWED_ITEMS = EnumSet.of(
            Material.STONE,
            Material.COBBLESTONE,
            Material.COAL,
            Material.IRON_ORE,
            Material.DIAMOND
    );

    private ItemFilter() {
    }

    public static void filterInventory(Inventory inv) {
        for (ItemStack stack : inv.getContents()) {
            if (stack == null) {
                continue;
            }
            if (!ALLOWED_ITEMS.contains(stack.getType())) {
                inv.remove(stack);
            }
        }
    }
}

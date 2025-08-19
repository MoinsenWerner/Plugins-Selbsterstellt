package com.example.living.city;

import com.example.living.LivingPlugin;
import com.example.living.npc.NPC;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Location;
import org.bukkit.Material;

/**
 * Represents a city in the simulation. Cities hold NPCs and track
 * resources and buildings. Only minimal data structures are provided
 * for now to allow future expansion.
 */
public class City {

    private final String name;
    private final Location coreLocation;
    private final List<NPC> npcs = new ArrayList<>();
    private final Map<Material, Integer> resources = new EnumMap<>(Material.class);

    public City(String name, Location coreLocation) {
        this.name = name;
        this.coreLocation = coreLocation;
        LivingPlugin.getInstance().getLogger().info("City created: " + name);
    }

    public String getName() {
        return name;
    }

    public Location getCoreLocation() {
        return coreLocation;
    }

    public List<NPC> getNpcs() {
        return npcs;
    }

    public void addNpc(NPC npc) {
        npc.setCity(this);
        npcs.add(npc);
        LivingPlugin.getInstance().getLogger()
                .info("NPC with job " + npc.getJob() + " added to city " + name);
    }

    public void addResource(Material material, int amount) {
        resources.merge(material, amount, Integer::sum);
    }

    public int getResource(Material material) {
        return resources.getOrDefault(material, 0);
    }

    public boolean consumeResource(Material material, int amount) {
        int current = getResource(material);
        if (current < amount) {
            return false;
        }
        resources.put(material, current - amount);
        return true;
    }
}
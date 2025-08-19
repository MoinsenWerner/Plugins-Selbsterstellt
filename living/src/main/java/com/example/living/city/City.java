package com.example.living.city;

import com.example.living.npc.NPC;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Location;

/**
 * Represents a city in the simulation. Cities hold NPCs and track
 * resources and buildings. Only minimal data structures are provided
 * for now to allow future expansion.
 */
public class City {

    private final String name;
    private final Location coreLocation;
    private final List<NPC> npcs = new ArrayList<>();

    public City(String name, Location coreLocation) {
        this.name = name;
        this.coreLocation = coreLocation;
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
        npcs.add(npc);
    }
}

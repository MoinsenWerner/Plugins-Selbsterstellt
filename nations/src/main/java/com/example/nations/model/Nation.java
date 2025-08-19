package com.example.nations.model;

import com.example.nations.territory.ChunkPosition;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Nation {
    private final String name;
    private final UUID founder;
    private final Set<UUID> citizens = new HashSet<>();
    private final Set<ChunkPosition> territory = new HashSet<>();

    public Nation(String name, UUID founder) {
        this.name = name;
        this.founder = founder;
        this.citizens.add(founder);
    }

    public String getName() {
        return name;
    }

    public UUID getFounder() {
        return founder;
    }

    public Set<UUID> getCitizens() {
        return citizens;
    }

    public Set<ChunkPosition> getTerritory() {
        return territory;
    }
}

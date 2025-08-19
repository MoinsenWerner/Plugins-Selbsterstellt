package com.example.nations.model;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class NationRegistry {
    private final Map<String, Nation> nations = new HashMap<>();

    public Nation getNation(String name) {
        return nations.get(name.toLowerCase());
    }

    public Nation getNationByMember(UUID uuid) {
        return nations.values().stream()
                .filter(n -> n.getCitizens().contains(uuid))
                .findFirst()
                .orElse(null);
    }

    public Nation createNation(String name, UUID founder) {
        Nation nation = new Nation(name, founder);
        nations.put(name.toLowerCase(), nation);
        return nation;
    }
}

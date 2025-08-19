package com.example.nations.diplomacy;

import com.example.nations.model.Nation;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Tracks relationships and treaties between nations.
 * This is a placeholder for future diplomacy features.
 */
public class DiplomacyManager {
    private final Map<UUID, Map<UUID, RelationType>> relations = new HashMap<>();

    public RelationType getRelation(Nation a, Nation b) {
        return relations.getOrDefault(a.getFounder(), Map.of())
                .getOrDefault(b.getFounder(), RelationType.NEUTRAL);
    }

    public void setRelation(Nation a, Nation b, RelationType type) {
        relations.computeIfAbsent(a.getFounder(), k -> new HashMap<>())
                .put(b.getFounder(), type);
        relations.computeIfAbsent(b.getFounder(), k -> new HashMap<>())
                .put(a.getFounder(), type);
    }
}

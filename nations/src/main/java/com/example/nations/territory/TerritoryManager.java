package com.example.nations.territory;

import com.example.nations.model.Nation;

/**
 * Handles claiming and unclaiming chunks for nations.
 */
public class TerritoryManager {
    public void claim(Nation nation, ChunkPosition pos) {
        nation.getTerritory().add(pos);
    }
}

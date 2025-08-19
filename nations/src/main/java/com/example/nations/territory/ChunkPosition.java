package com.example.nations.territory;

import org.bukkit.Chunk;

import java.util.Objects;
import java.util.UUID;

public record ChunkPosition(UUID world, int x, int z) {
    public static ChunkPosition fromChunk(Chunk chunk) {
        return new ChunkPosition(chunk.getWorld().getUID(), chunk.getX(), chunk.getZ());
    }

    @Override
    public String toString() {
        return world + ":" + x + ":" + z;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChunkPosition that)) return false;
        return x == that.x && z == that.z && world.equals(that.world);
    }

    @Override
    public int hashCode() {
        return Objects.hash(world, x, z);
    }
}

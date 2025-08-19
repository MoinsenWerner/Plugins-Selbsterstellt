package com.example.living.npc;

import java.util.UUID;

/**
 * Basic representation of an NPC controlled by the plugin.
 * The implementation here is intentionally lightweight and is
 * meant to be expanded with behavior logic, pathfinding and
 * interaction with the Minecraft world.
 */
public class NPC {

    private final UUID uuid;
    private final Job job;

    public NPC(UUID uuid, Job job) {
        this.uuid = uuid;
        this.job = job;
    }

    public UUID getUuid() {
        return uuid;
    }

    public Job getJob() {
        return job;
    }

    /**
     * Placeholder method representing a task execution.
     * Actual implementation would use server APIs to interact
     * with blocks, inventories and other entities.
     */
    public void performTask() {
        // TODO: implement actual job logic
    }
}

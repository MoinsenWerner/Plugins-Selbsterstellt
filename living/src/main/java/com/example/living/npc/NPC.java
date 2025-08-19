package com.example.living.npc;

import java.util.UUID;
import com.example.living.LivingPlugin;

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
        LivingPlugin.getInstance().getLogger()
                .info("NPC created with job " + job + " and UUID " + uuid);
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
        LivingPlugin.getInstance().getLogger()
                .info("NPC " + uuid + " performing job " + job + " task.");
        // TODO: implement actual job logic
    }
}
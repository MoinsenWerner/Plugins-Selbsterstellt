package com.example.living.manager;

import java.util.Map;

import org.bukkit.Location;
import org.bukkit.entity.Villager;
import org.bukkit.scheduler.BukkitScheduler;

import com.example.living.LivingPlugin;
import com.example.living.city.City;
import com.example.living.npc.Job;
import com.example.living.npc.NPC;

/**
 * Erzeugt und verwaltet NPC-Entitäten für Städte.
 */
public class NPCManager {
    private final LivingPlugin plugin;

    public NPCManager(LivingPlugin plugin) {
        this.plugin = plugin;
    }

    public void spawnInitialNpcs(City city) {
        Map<Job, Integer> jobs = plugin.getInitialJobs();
        for (Map.Entry<Job, Integer> entry : jobs.entrySet()) {
            for (int i = 0; i < entry.getValue(); i++) {
                spawnNpc(city, entry.getKey());
            }
        }
    }

    public NPC spawnNpc(City city, Job job) {
        if (city.getNpcs().size() >= plugin.getMaxNpcs()) {
            plugin.getLogger().warning("Max NPCs reached for city " + city.getName());
            return null;
        }
        Location loc = city.getCoreLocation();
        Villager villager = loc.getWorld().spawn(loc, Villager.class);
        villager.setCustomName(job.name());
        villager.setCustomNameVisible(true);
        NPC npc = new NPC(villager.getUniqueId(), job);
        city.addNpc(npc);
        BukkitScheduler scheduler = plugin.getServer().getScheduler();
        scheduler.runTaskTimer(plugin, npc::performTask, plugin.getNpcTaskInterval(), plugin.getNpcTaskInterval());
        return npc;
    }
}


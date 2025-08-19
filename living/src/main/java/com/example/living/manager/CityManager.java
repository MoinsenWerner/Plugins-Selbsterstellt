package com.example.living.manager;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import com.example.living.LivingPlugin;
import com.example.living.city.City;
import com.example.living.npc.Job;

/**
 * Verwaltet alle existierenden Städte und kümmert sich um das
 * periodische Erweitern der Bevölkerung.
 */
public class CityManager {
    private final LivingPlugin plugin;
    private final List<City> cities = new ArrayList<>();
    private final Random random = new Random();

    public CityManager(LivingPlugin plugin) {
        this.plugin = plugin;
        Bukkit.getScheduler().runTaskTimer(plugin, this::tickCities, plugin.getSpawnInterval(), plugin.getSpawnInterval());
    }

    public City createCity(String name, Location core) {
        City city = new City(name, core);
        cities.add(city);
        plugin.getNpcManager().spawnInitialNpcs(city);
        return city;
    }

    public List<City> getCities() {
        return cities;
    }

    private void tickCities() {
        for (City city : cities) {
            if (city.getNpcs().size() < plugin.getMaxNpcs()) {
                Job job = Job.values()[random.nextInt(Job.values().length)];
                plugin.getNpcManager().spawnNpc(city, job);
            }
        }
    }
}


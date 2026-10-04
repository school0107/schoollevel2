package com.schoollevel2.data;

import com.schoollevel2.SchoolLevel2;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DataManager {

    private final SchoolLevel2 plugin;
    private final Map<UUID, PlayerData> cache = new ConcurrentHashMap<>();
    private final File dataFolder;

    public DataManager(SchoolLevel2 plugin) {
        this.plugin = plugin;
        this.dataFolder = new File(plugin.getDataFolder(), "playerdata");
        if (!dataFolder.exists()) dataFolder.mkdirs();
    }

    public PlayerData get(UUID uuid) {
        return cache.get(uuid);
    }

    public PlayerData getOrCreate(UUID uuid, String name) {
        return cache.computeIfAbsent(uuid, k -> {
            PlayerData data = load(uuid, name);
            if (data == null) data = new PlayerData(uuid, name);
            return data;
        });
    }

    private File fileOf(UUID uuid) {
        return new File(dataFolder, uuid.toString() + ".yml");
    }

    public PlayerData load(UUID uuid, String name) {
        File f = fileOf(uuid);
        if (!f.exists()) return null;
        YamlConfiguration yml = YamlConfiguration.loadConfiguration(f);
        PlayerData data = new PlayerData(uuid, yml.getString("name", name));
        data.setLevel(yml.getInt("level", 1));
        data.setExp(yml.getLong("exp", 0));
        data.setPotentialPoints(yml.getInt("points", 0));
        for (String s : List.of("melee", "ranged", "health", "agility")) {
            data.setStat(s, yml.getInt("stats." + s, 0));
        }
        data.setDirty(false);
        return data;
    }

    public void save(PlayerData data) {
        if (data == null) return;
        File f = fileOf(data.getUuid());
        YamlConfiguration yml = new YamlConfiguration();
        yml.set("name", data.getName());
        yml.set("level", data.getLevel());
        yml.set("exp", data.getExp());
        yml.set("points", data.getPotentialPoints());
        for (var e : data.getStats().entrySet()) {
            yml.set("stats." + e.getKey(), e.getValue());
        }
        try {
            yml.save(f);
            data.setDirty(false);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save data for " + data.getUuid() + ": " + e.getMessage());
        }
    }

    public void loadAll() {
        File[] files = dataFolder.listFiles((dir, n) -> n.endsWith(".yml"));
        if (files == null) return;
        for (File f : files) {
            try {
                UUID uuid = UUID.fromString(f.getName().replace(".yml", ""));
                PlayerData data = load(uuid, "unknown");
                if (data != null) cache.put(uuid, data);
            } catch (Exception ignored) {}
        }
        plugin.getLogger().info("Loaded " + cache.size() + " player data files.");
    }

    public void saveAll() {
        int saved = 0;
        for (PlayerData data : cache.values()) {
            if (data.isDirty()) {
                save(data);
                saved++;
            }
        }
        if (saved > 0) plugin.getLogger().info("Saved " + saved + " player data files.");
    }

    public void unload(UUID uuid) {
        PlayerData data = cache.get(uuid);
        if (data != null && data.isDirty()) save(data);
    }

    public void removeFromCache(UUID uuid) {
        cache.remove(uuid);
    }

    public Collection<PlayerData> getAll() { return cache.values(); }
}

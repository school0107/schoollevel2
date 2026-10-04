package com.schoollevel2.data;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerData {
    private final UUID uuid;
    private String name;
    private int level;
    private long exp;
    private int potentialPoints;
    private final Map<String, Integer> stats = new HashMap<>();
    private boolean dirty;

    public PlayerData(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
        this.level = 1;
        this.exp = 0;
        this.potentialPoints = 0;
        stats.put("melee", 0);
        stats.put("ranged", 0);
        stats.put("health", 0);
        stats.put("agility", 0);
    }

    public UUID getUuid() { return uuid; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; this.dirty = true; }

    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; this.dirty = true; }

    public long getExp() { return exp; }
    public void setExp(long exp) { this.exp = exp; this.dirty = true; }

    public int getPotentialPoints() { return potentialPoints; }
    public void setPotentialPoints(int p) { this.potentialPoints = p; this.dirty = true; }
    public void addPotentialPoints(int p) { this.potentialPoints += p; this.dirty = true; }

    public int getStat(String key) { return stats.getOrDefault(key, 0); }
    public void setStat(String key, int v) { stats.put(key, v); this.dirty = true; }
    public Map<String, Integer> getStats() { return stats; }

    public boolean isDirty() { return dirty; }
    public void setDirty(boolean dirty) { this.dirty = dirty; }
}

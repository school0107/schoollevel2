package com.schoollevel2.level;

import com.schoollevel2.SchoolLevel2;
import com.schoollevel2.data.PlayerData;
import com.schoollevel2.utils.ColorUtil;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class LevelManager {

    private final SchoolLevel2 plugin;

    public LevelManager(SchoolLevel2 plugin) {
        this.plugin = plugin;
    }

    public void addExp(Player player, long amount) {
        PlayerData data = plugin.getDataManager().get(player.getUniqueId());
        if (data == null) return;
        if (data.getLevel() >= plugin.getConfigManager().getMaxLevel()) return;

        data.setExp(data.getExp() + amount);
        checkLevelUp(player, data);
    }

    public void checkLevelUp(Player player, PlayerData data) {
        int max = plugin.getConfigManager().getMaxLevel();
        boolean leveled = false;

        while (data.getLevel() < max) {
            long needed = plugin.getConfigManager().getExpForLevel(data.getLevel());
            if (data.getExp() >= needed) {
                data.setExp(data.getExp() - needed);
                data.setLevel(data.getLevel() + 1);
                data.addPotentialPoints(plugin.getConfigManager().getPointsPerLevel());
                leveled = true;
            } else break;
        }
        if (data.getLevel() >= max) {
            data.setExp(0);
            data.setLevel(max);
        }

        if (leveled) {
            String msg = plugin.getConfigManager().msg("level-up")
                    .replace("{level}", String.valueOf(data.getLevel()))
                    .replace("{points}", String.valueOf(plugin.getConfigManager().getPointsPerLevel()));
            player.sendMessage(msg);
            try {
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.5f);
                player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
            } catch (Exception ignored) {}
            applyLevelBar(player, data);
        }
    }

    public void setLevel(Player player, int level) {
        PlayerData data = plugin.getDataManager().get(player.getUniqueId());
        if (data == null) return;
        int old = data.getLevel();
        data.setLevel(Math.max(1, Math.min(level, plugin.getConfigManager().getMaxLevel())));
        data.setExp(0);
        int gained = data.getLevel() - old;
        if (gained > 0) {
            data.addPotentialPoints(gained * plugin.getConfigManager().getPointsPerLevel());
        }
        applyLevelBar(player, data);
        plugin.getStatsManager().applyStats(player);
    }

    public void addLevels(Player player, int amount) {
        PlayerData data = plugin.getDataManager().get(player.getUniqueId());
        if (data == null) return;
        setLevel(player, data.getLevel() + amount);
    }

    public void addExpDirect(Player player, long amount) {
        PlayerData data = plugin.getDataManager().get(player.getUniqueId());
        if (data == null) return;
        data.setExp(Math.max(0, data.getExp() + amount));
        checkLevelUp(player, data);
        applyLevelBar(player, data);
    }

    public void applyLevelBar(Player player, PlayerData data) {
        if (!plugin.getConfigManager().getCfg().getBoolean("level-bar.enabled", true)) return;
        int lvl = Math.min(data.getLevel(), 200);
        player.setLevel(lvl);

        long needed = plugin.getConfigManager().getExpForLevel(data.getLevel());
        float progress;
        if (data.getLevel() >= plugin.getConfigManager().getMaxLevel()) progress = 1f;
        else progress = needed > 0 ? (float) Math.min(1.0, (double) data.getExp() / needed) : 0f;
        player.setExp(Math.max(0f, Math.min(0.999f, progress)));
    }

    public String buildExpBar(PlayerData data) {
        long needed = plugin.getConfigManager().getExpForLevel(data.getLevel());
        int bars = 20;
        int filled = needed > 0 ? (int) Math.min(bars, (data.getExp() * bars) / needed) : 0;
        StringBuilder sb = new StringBuilder();
        sb.append("<green>");
        for (int i = 0; i < filled; i++) sb.append("█");
        sb.append("<dark_gray>");
        for (int i = filled; i < bars; i++) sb.append("█");
        return ColorUtil.color(sb.toString());
    }
}

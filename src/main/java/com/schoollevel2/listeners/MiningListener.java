package com.schoollevel2.listeners;

import com.schoollevel2.SchoolLevel2;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class MiningListener implements Listener {

    private final SchoolLevel2 plugin;
    public MiningListener(SchoolLevel2 plugin) { this.plugin = plugin; }

    // Không dùng ignoreCancelled để vẫn nhận exp khi plugin khác cancel event
    // Nhưng phải check gamemode + player còn online
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBreak(BlockBreakEvent e) {
        Player p = e.getPlayer();
        if (!p.isOnline()) return;
        if (p.getGameMode() == GameMode.CREATIVE || p.getGameMode() == GameMode.SPECTATOR) return;

        int exp = plugin.getConfigManager().getMiningExp(e.getBlock().getType());
        if (exp <= 0) return;

        // Debug log để check
        if (plugin.getConfig().getBoolean("settings.debug-mining", false)) {
            plugin.getLogger().info("[DEBUG] " + p.getName() + " mined " + e.getBlock().getType() + " -> +" + exp + " exp");
        }

        plugin.getLevelManager().addExp(p, exp);
        var data = plugin.getDataManager().get(p.getUniqueId());
        if (data != null) {
            plugin.getLevelManager().applyLevelBar(p, data);
        }
    }
}
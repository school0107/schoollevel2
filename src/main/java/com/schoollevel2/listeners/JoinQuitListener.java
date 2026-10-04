package com.schoollevel2.listeners;

import com.schoollevel2.SchoolLevel2;
import com.schoollevel2.data.PlayerData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class JoinQuitListener implements Listener {

    private final SchoolLevel2 plugin;
    public JoinQuitListener(SchoolLevel2 plugin) { this.plugin = plugin; }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent e) {
        var p = e.getPlayer();
        PlayerData data = plugin.getDataManager().getOrCreate(p.getUniqueId(), p.getName());
        data.setName(p.getName());

        // Delay 1 tick so attributes register
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (!p.isOnline()) return;
            plugin.getStatsManager().applyStats(p);
            plugin.getLevelManager().applyLevelBar(p, data);
        }, 5L);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent e) {
        var p = e.getPlayer();
        PlayerData data = plugin.getDataManager().get(p.getUniqueId());
        if (data != null) {
            data.setName(p.getName());
            plugin.getDataManager().save(data);
        }
        // Keep in cache for offline operations but periodically saved
        // Do not remove, since they may re-join quickly
    }
}

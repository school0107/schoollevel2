package com.schoollevel2.listeners;

import com.schoollevel2.SchoolLevel2;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class MiningListener implements Listener {

    private final SchoolLevel2 plugin;
    public MiningListener(SchoolLevel2 plugin) { this.plugin = plugin; }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent e) {
        var p = e.getPlayer();
        if (p.getGameMode() == org.bukkit.GameMode.CREATIVE) return;
        int exp = plugin.getConfigManager().getMiningExp(e.getBlock().getType());
        if (exp <= 0) return;
        plugin.getLevelManager().addExp(p, exp);
        var data = plugin.getDataManager().get(p.getUniqueId());
        if (data != null) plugin.getLevelManager().applyLevelBar(p, data);
    }
}

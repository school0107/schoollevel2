package com.schoollevel2.listeners;

import com.schoollevel2.SchoolLevel2;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;

public class MiningListener implements Listener {

    private final SchoolLevel2 plugin;
    public MiningListener(SchoolLevel2 plugin) { this.plugin = plugin; }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBreak(BlockBreakEvent e) {
        Player p = e.getPlayer();
        if (!p.isOnline()) return;
        if (p.getGameMode() == GameMode.CREATIVE || p.getGameMode() == GameMode.SPECTATOR) return;

        int exp = plugin.getConfigManager().getMiningExp(e.getBlock().getType());
        if (exp <= 0) return;

        if (plugin.getConfig().getBoolean("settings.debug-mining", false)) {
            plugin.getLogger().info("[DEBUG] " + p.getName() + " mined " + e.getBlock().getType() + " -> +" + exp + " exp");
        }

        plugin.getLevelManager().addExp(p, exp);
        var data = plugin.getDataManager().get(p.getUniqueId());
        if (data != null) plugin.getLevelManager().applyLevelBar(p, data);
    }

    /**
     * Chặn hoàn toàn vanilla exp bar (khi đào quặng, giết mob, nấu ăn...)
     * để thanh exp chỉ hiển thị exp của plugin.
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onVanillaExp(PlayerExpChangeEvent e) {
        e.setAmount(0);
    }

    /**
     * Chặn luôn khi pickup orb exp (bảo hiểm thêm cho một số trường hợp).
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPickup(EntityPickupItemEvent e) {
        if (!(e.getEntity() instanceof Player)) return;
        var item = e.getItem().getItemStack();
        if (item.getType() == org.bukkit.Material.EXPERIENCE_BOTTLE) return;
        // Không cần chặn item, chỉ chặn exp orb — nhưng exp orb không phải item nên skip
    }
}
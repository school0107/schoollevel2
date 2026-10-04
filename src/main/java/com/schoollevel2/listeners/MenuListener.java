package com.schoollevel2.listeners;

import com.schoollevel2.SchoolLevel2;
import com.schoollevel2.utils.ItemUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

public class MenuListener implements Listener {

    private final SchoolLevel2 plugin;
    public MenuListener(SchoolLevel2 plugin) { this.plugin = plugin; }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player p)) return;
        if (!(e.getInventory().getHolder() instanceof com.schoollevel2.menu.StatsMenu.StatsHolder)) return;
        e.setCancelled(true);
        plugin.getStatsMenu().handleClick(p, e.getRawSlot(), e.isShiftClick(), e.isRightClick());
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (e.getHand() != EquipmentSlot.HAND) return;
        Player p = e.getPlayer();
        var item = e.getItem();
        if (item == null) return;
        if (!ItemUtil.isResetItem(item, plugin.getConfigManager().getResetMaterial(), plugin.getConfigManager().getResetName()))
            return;
        e.setCancelled(true);
        int refund = plugin.getStatsManager().resetStats(p);
        p.sendMessage(plugin.getConfigManager().msg("reset-success").replace("{points}", String.valueOf(refund)));
        if (item.getAmount() > 1) item.setAmount(item.getAmount() - 1);
        else p.getInventory().setItemInMainHand(null);
    }
}

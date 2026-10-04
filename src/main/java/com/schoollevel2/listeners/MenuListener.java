package com.schoollevel2.listeners;

import com.schoollevel2.SchoolLevel2;
import com.schoollevel2.utils.ItemUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class MenuListener implements Listener {

    private final SchoolLevel2 plugin;
    public MenuListener(SchoolLevel2 plugin) { this.plugin = plugin; }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player p)) return;
        if (!(e.getInventory().getHolder() instanceof com.schoollevel2.menu.StatsMenu.StatsHolder)) return;
        e.setCancelled(true);
        if (e.getClickedInventory() == null) return;
        if (e.getClickedInventory() != e.getView().getTopInventory()) return;
        plugin.getStatsMenu().handleClick(p, e.getRawSlot(), e.isShiftClick(), e.isRightClick());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = false)
    public void onInteract(PlayerInteractEvent e) {
        if (e.getHand() != EquipmentSlot.HAND) return;
        Action action = e.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) return;

        Player p = e.getPlayer();
        ItemStack item = e.getItem();
        if (item == null) return;
        if (!ItemUtil.isResetItem(item, plugin.getConfigManager().getResetMaterial(), plugin.getConfigManager().getResetName()))
            return;

        e.setCancelled(true);
        int refund = plugin.getStatsManager().resetStats(p);

        plugin.getConfigManager().send(p, "reset-success", Map.of("points", String.valueOf(refund)));

        if (item.getAmount() > 1) item.setAmount(item.getAmount() - 1);
        else p.getInventory().setItemInMainHand(null);

        try {
            p.playSound(p.getLocation(), org.bukkit.Sound.BLOCK_BEACON_DEACTIVATE, 1f, 1.5f);
        } catch (Exception ignored) {}
    }
}
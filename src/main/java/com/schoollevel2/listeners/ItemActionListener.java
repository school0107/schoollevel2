package com.schoollevel2.listeners;

import com.schoollevel2.SchoolLevel2;
import com.schoollevel2.data.PlayerData;
import com.schoollevel2.utils.ColorUtil;
import com.schoollevel2.utils.ItemUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class ItemActionListener implements Listener {

    private final SchoolLevel2 plugin;
    public ItemActionListener(SchoolLevel2 plugin) { this.plugin = plugin; }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInteract(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Player p = e.getPlayer();
        ItemStack item = e.getItem();
        
        if (item != null && !item.getType().isAir()) {
            checkLevel(p, item, e);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onArmorEquip(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player p)) return;
        ItemStack cursor = e.getCursor();
        ItemStack current = e.getCurrentItem();

        // Nếu đang cố mặc giáp hoặc cầm vũ khí
        if (cursor != null && !cursor.getType().isAir()) {
            if (isArmor(cursor.getType()) || isWeapon(cursor.getType())) {
                if (e.getRawSlot() >= 5 && e.getRawSlot() <= 8) { // Armor slot
                    checkLevel(p, cursor, e);
                }
            }
        }

        // Kiểm tra khi shift-click
        if (e.isShiftClick() && current != null && !current.getType().isAir()) {
            if (isArmor(current.getType()) || isWeapon(current.getType())) {
                checkLevel(p, current, e);
            }
        }
    }

    private void checkLevel(Player p, ItemStack item, org.bukkit.event.Cancellable e) {
        int req = ItemUtil.getRequiredLevel(item);
        if (req <= 0) return;

        PlayerData data = plugin.getDataManager().get(p.getUniqueId());
        if (data == null) return;

        if (data.getLevel() < req) {
            e.setCancelled(true);
            String msg = plugin.getConfigManager().getCfg().getString("rpg-item.level-required-msg", "&cBạn cần Level {level}");
            msg = msg.replace("{level}", String.valueOf(req));
            p.sendMessage(ColorUtil.mm(msg));
            try { p.playSound(p.getLocation(), org.bukkit.Sound.ENTITY_VILLAGER_NO, 1f, 1f); } catch (Exception ignored) {}
        }
    }

    private boolean isArmor(Material m) {
        String n = m.name();
        return n.endsWith("_HELMET") || n.endsWith("_CHESTPLATE") || n.endsWith("_LEGGINGS") || n.endsWith("_BOOTS") || m == Material.ELYTRA;
    }

    private boolean isWeapon(Material m) {
        String n = m.name();
        return n.endsWith("_SWORD") || n.endsWith("_AXE") || m == Material.BOW || m == Material.CROSSBOW || m == Material.TRIDENT;
    }
}
package com.schoollevel2.commands;

import com.schoollevel2.SchoolLevel2;
import com.schoollevel2.utils.ColorUtil;
import com.schoollevel2.utils.ItemUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ItemCommand implements CommandExecutor {

    private final SchoolLevel2 plugin;
    public ItemCommand(SchoolLevel2 plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player p)) {
            plugin.getConfigManager().send(sender, "player-only");
            return true;
        }
        if (!p.hasPermission("schoollevel.admin")) {
            plugin.getConfigManager().send(p, "no-permission");
            return true;
        }

        if (args.length < 2) {
            p.sendMessage(ColorUtil.mm("&cSử dụng: /sl item <melee|ranged|health|agility|chakra|crit> <amount> [req_level]"));
            return true;
        }

        String stat = args[0].toLowerCase();
        if (!isValidStat(stat)) {
            p.sendMessage(ColorUtil.mm("&cChỉ số không hợp lệ! Chọn: melee, ranged, health, agility, chakra, crit"));
            return true;
        }

        double amount;
        try { amount = Double.parseDouble(args[1]); } catch (Exception e) {
            plugin.getConfigManager().send(p, "invalid-number");
            return true;
        }
        
        int reqLevel = 0;
        if (args.length >= 3) {
            try { reqLevel = Integer.parseInt(args[2]); } catch (Exception ignored) {}
        }

        ItemStack held = p.getInventory().getItemInMainHand();
        if (held.getType().isAir()) {
            p.sendMessage(ColorUtil.mm("&cBạn phải cầm vật phẩm trên tay!"));
            return true;
        }

        ItemStack newItem = ItemUtil.applyRPGStats(held, stat, amount, reqLevel);
        if (newItem != null) {
            p.getInventory().setItemInMainHand(newItem);
            p.sendMessage(ColorUtil.mm("&a✔ Đã ép &e" + stat + " &a(+ " + amount + ") vào vật phẩm!"));
            try { p.playSound(p.getLocation(), org.bukkit.Sound.BLOCK_ANVIL_USE, 1f, 1.5f); } catch (Exception ignored) {}
        }
        return true;
    }

    private boolean isValidStat(String s) {
        return s.equals("melee") || s.equals("ranged") || s.equals("health") || s.equals("agility") || s.equals("chakra") || s.equals("crit");
    }
}
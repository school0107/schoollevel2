package com.schoollevel2.commands;

import com.schoollevel2.SchoolLevel2;
import com.schoollevel2.data.PlayerData;
import com.schoollevel2.utils.ColorUtil;
import com.schoollevel2.utils.ItemUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class SchoolLevelCommand implements CommandExecutor, TabCompleter {

    private final SchoolLevel2 plugin;
    public SchoolLevelCommand(SchoolLevel2 plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }
        String sub = args[0].toLowerCase();
        var cm = plugin.getConfigManager();

        switch (sub) {
            case "help" -> sendHelp(sender);
            case "menu", "stats" -> {
                if (!(sender instanceof Player p)) { cm.send(sender, "player-only"); return true; }
                plugin.getStatsMenu().open(p);
            }
            case "reload" -> {
                if (!sender.hasPermission("schoollevel.admin")) { cm.send(sender, "no-permission"); return true; }
                plugin.reload();
                cm.send(sender, "reload-success");
            }
            case "give", "reset-item" -> {
                if (!sender.hasPermission("schoollevel.admin")) { cm.send(sender, "no-permission"); return true; }
                if (args.length < 2) { cm.send(sender, "player-not-found"); return true; }
                Player target = Bukkit.getPlayerExact(args[1]);
                if (target == null) { cm.send(sender, "player-not-found"); return true; }
                var item = ItemUtil.make(cm.getResetMaterial(), cm.getResetName(), cm.getResetLore());
                target.getInventory().addItem(item);
                cm.send(target, "reset-item-given");
                cm.send(sender, "reset-item-given");
            }
            case "addexp" -> {
                if (!sender.hasPermission("schoollevel.admin")) { cm.send(sender, "no-permission"); return true; }
                if (args.length < 3) { sendHelp(sender); return true; }
                Player target = Bukkit.getPlayerExact(args[1]);
                if (target == null) { cm.send(sender, "player-not-found"); return true; }
                long amount = parseLong(args[2]);
                if (amount < 0) { cm.send(sender, "invalid-number"); return true; }
                plugin.getLevelManager().addExpDirect(target, amount);
                cm.send(sender, "exp-added", Map.of(
                        "amount", String.valueOf(amount),
                        "player", target.getName()
                ));
            }
            case "setlevel" -> {
                if (!sender.hasPermission("schoollevel.admin")) { cm.send(sender, "no-permission"); return true; }
                if (args.length < 3) { sendHelp(sender); return true; }
                Player target = Bukkit.getPlayerExact(args[1]);
                if (target == null) { cm.send(sender, "player-not-found"); return true; }
                int lvl = parseInt(args[2]);
                if (lvl < 1) { cm.send(sender, "invalid-number"); return true; }
                plugin.getLevelManager().setLevel(target, lvl);
                cm.send(sender, "level-set", Map.of(
                        "player", target.getName(),
                        "level", String.valueOf(lvl)
                ));
            }
            case "addlevel" -> {
                if (!sender.hasPermission("schoollevel.admin")) { cm.send(sender, "no-permission"); return true; }
                if (args.length < 3) { sendHelp(sender); return true; }
                Player target = Bukkit.getPlayerExact(args[1]);
                if (target == null) { cm.send(sender, "player-not-found"); return true; }
                int amount = parseInt(args[2]);
                if (amount == 0) { cm.send(sender, "invalid-number"); return true; }
                plugin.getLevelManager().addLevels(target, amount);
                cm.send(sender, "level-added", Map.of(
                        "amount", String.valueOf(amount),
                        "player", target.getName()
                ));
            }
            case "addpoints" -> {
                if (!sender.hasPermission("schoollevel.admin")) { cm.send(sender, "no-permission"); return true; }
                if (args.length < 3) { sendHelp(sender); return true; }
                Player target = Bukkit.getPlayerExact(args[1]);
                if (target == null) { cm.send(sender, "player-not-found"); return true; }
                int amount = parseInt(args[2]);
                PlayerData data = plugin.getDataManager().getOrCreate(target.getUniqueId(), target.getName());
                data.addPotentialPoints(amount);
                cm.send(sender, "stats-added", Map.of(
                        "amount", String.valueOf(amount),
                        "stat", "points"
                ));
            }
            case "info" -> {
                if (!(sender instanceof Player p)) { cm.send(sender, "player-only"); return true; }
                PlayerData data = plugin.getDataManager().getOrCreate(p.getUniqueId(), p.getName());
                String prefix = cm.getCfg().getString("messages.prefix", "");
                p.sendMessage(ColorUtil.mm(prefix + "&7Level: &b" + data.getLevel()
                        + " &8| &7EXP: &a" + data.getExp()
                        + " &8| &7Points: &6" + data.getPotentialPoints()));
            }
            case "item", "ep", "setlevelitem", "reqlevel" -> {
                if (!sender.hasPermission("schoollevel.admin")) { cm.send(sender, "no-permission"); return true; }
                
                // Xử lý chuyển tiếp lệnh sang ItemCommand
                String[] subArgs;
                if (sub.equals("setlevelitem") || sub.equals("reqlevel")) {
                    // Giữ nguyên args vì ItemCommand sẽ check args[0]
                    subArgs = args;
                } else {
                    // Bỏ chữ "item" hoặc "ep" ở đầu
                    subArgs = Arrays.copyOfRange(args, 1, args.length);
                }
                new ItemCommand(plugin).onCommand(sender, cmd, label, subArgs);
            }
            default -> sendHelp(sender);
        }
        return true;
    }

    private void sendHelp(CommandSender s) {
        s.sendMessage(ColorUtil.mm("&b&l✦ SchoolLevel2 Commands ✦"));
        s.sendMessage(ColorUtil.mm("&6/sl help &7- &fTrợ giúp"));
        s.sendMessage(ColorUtil.mm("&6/sl menu &7- &fMở menu tiềm năng"));
        s.sendMessage(ColorUtil.mm("&6/sl info &7- &fXem thông tin"));
        s.sendMessage(ColorUtil.mm("&6/sl reload &7- &fReload config"));
        s.sendMessage(ColorUtil.mm("&6/sl give <player> &7- &fCho item reset"));
        s.sendMessage(ColorUtil.mm("&6/sl addexp <player> <amount> &7- &fCộng exp"));
        s.sendMessage(ColorUtil.mm("&6/sl setlevel <player> <level> &7- &fĐặt cấp"));
        s.sendMessage(ColorUtil.mm("&6/sl addlevel <player> <amount> &7- &fCộng cấp"));
        s.sendMessage(ColorUtil.mm("&6/sl addpoints <player> <amount> &7- &fCộng điểm"));
        s.sendMessage(ColorUtil.mm("&6/sl item <stat> <amount> &7- &fÉp chỉ số vào đồ đang cầm"));
        s.sendMessage(ColorUtil.mm("&6/sl setlevelitem <level> &7- &fÉp yêu cầu level vào đồ"));
    }

    private long parseLong(String s) { try { return Long.parseLong(s); } catch (Exception e) { return -1; } }
    private int parseInt(String s) { try { return Integer.parseInt(s); } catch (Exception e) { return Integer.MIN_VALUE; } }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return filter(List.of("help", "menu", "info", "reload", "give", "addexp", "setlevel", "addlevel", "addpoints", "item", "setlevelitem"), args[0]);
        }
        
        if (args.length == 2) {
            String sub = args[0].toLowerCase();
            // Gợi ý tên người chơi cho các lệnh admin
            if (List.of("give", "addexp", "setlevel", "addlevel", "addpoints").contains(sub)) {
                List<String> names = new ArrayList<>();
                for (Player p : Bukkit.getOnlinePlayers()) names.add(p.getName());
                return filter(names, args[1]);
            }
            // Gợi ý stat cho lệnh ép chỉ số
            if (sub.equals("item") || sub.equals("ep")) {
                return filter(List.of("melee", "ranged", "health", "agility", "chakra", "crit"), args[1]);
            }
        }
        return Collections.emptyList();
    }

    private List<String> filter(List<String> list, String prefix) {
        List<String> out = new ArrayList<>();
        for (String s : list) if (s.toLowerCase().startsWith(prefix.toLowerCase())) out.add(s);
        return out;
    }
}

package com.schoollevel2.commands;

import com.schoollevel2.SchoolLevel2;
import com.schoollevel2.data.PlayerData;
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

        switch (sub) {
            case "help" -> sendHelp(sender);
            case "menu", "stats" -> {
                if (!(sender instanceof Player p)) { sender.sendMessage(plugin.getConfigManager().msg("player-only")); return true; }
                plugin.getStatsMenu().open(p);
            }
            case "reload" -> {
                if (!sender.hasPermission("schoollevel.admin")) { sender.sendMessage(plugin.getConfigManager().msg("no-permission")); return true; }
                plugin.reload();
                sender.sendMessage(plugin.getConfigManager().msg("reload-success"));
            }
            case "give", "reset-item" -> {
                if (!sender.hasPermission("schoollevel.admin")) { sender.sendMessage(plugin.getConfigManager().msg("no-permission")); return true; }
                if (args.length < 2) { sender.sendMessage(plugin.getConfigManager().msg("player-not-found")); return true; }
                Player target = Bukkit.getPlayerExact(args[1]);
                if (target == null) { sender.sendMessage(plugin.getConfigManager().msg("player-not-found")); return true; }
                var item = ItemUtil.make(plugin.getConfigManager().getResetMaterial(),
                        plugin.getConfigManager().getResetName(),
                        plugin.getConfigManager().getResetLore());
                target.getInventory().addItem(item);
                target.sendMessage(plugin.getConfigManager().msg("reset-item-given"));
                sender.sendMessage(plugin.getConfigManager().msg("reset-item-given"));
            }
            case "addexp" -> {
                if (!sender.hasPermission("schoollevel.admin")) { sender.sendMessage(plugin.getConfigManager().msg("no-permission")); return true; }
                if (args.length < 3) { sendHelp(sender); return true; }
                Player target = Bukkit.getPlayerExact(args[1]);
                if (target == null) { sender.sendMessage(plugin.getConfigManager().msg("player-not-found")); return true; }
                long amount = parseLong(args[2]);
                if (amount < 0) { sender.sendMessage(plugin.getConfigManager().msg("invalid-number")); return true; }
                plugin.getLevelManager().addExpDirect(target, amount);
                sender.sendMessage(plugin.getConfigManager().msg("exp-added")
                        .replace("{amount}", String.valueOf(amount))
                        .replace("{player}", target.getName()));
            }
            case "setlevel" -> {
                if (!sender.hasPermission("schoollevel.admin")) { sender.sendMessage(plugin.getConfigManager().msg("no-permission")); return true; }
                if (args.length < 3) { sendHelp(sender); return true; }
                Player target = Bukkit.getPlayerExact(args[1]);
                if (target == null) { sender.sendMessage(plugin.getConfigManager().msg("player-not-found")); return true; }
                int lvl = parseInt(args[2]);
                if (lvl < 1) { sender.sendMessage(plugin.getConfigManager().msg("invalid-number")); return true; }
                plugin.getLevelManager().setLevel(target, lvl);
                sender.sendMessage(plugin.getConfigManager().msg("level-set")
                        .replace("{player}", target.getName())
                        .replace("{level}", String.valueOf(lvl)));
            }
            case "addlevel" -> {
                if (!sender.hasPermission("schoollevel.admin")) { sender.sendMessage(plugin.getConfigManager().msg("no-permission")); return true; }
                if (args.length < 3) { sendHelp(sender); return true; }
                Player target = Bukkit.getPlayerExact(args[1]);
                if (target == null) { sender.sendMessage(plugin.getConfigManager().msg("player-not-found")); return true; }
                int amount = parseInt(args[2]);
                if (amount == 0) { sender.sendMessage(plugin.getConfigManager().msg("invalid-number")); return true; }
                plugin.getLevelManager().addLevels(target, amount);
                sender.sendMessage(plugin.getConfigManager().msg("level-added")
                        .replace("{amount}", String.valueOf(amount))
                        .replace("{player}", target.getName()));
            }
            case "addpoints" -> {
                if (!sender.hasPermission("schoollevel.admin")) { sender.sendMessage(plugin.getConfigManager().msg("no-permission")); return true; }
                if (args.length < 3) { sendHelp(sender); return true; }
                Player target = Bukkit.getPlayerExact(args[1]);
                if (target == null) { sender.sendMessage(plugin.getConfigManager().msg("player-not-found")); return true; }
                int amount = parseInt(args[2]);
                PlayerData data = plugin.getDataManager().getOrCreate(target.getUniqueId(), target.getName());
                data.addPotentialPoints(amount);
                sender.sendMessage(plugin.getConfigManager().msg("stats-added")
                        .replace("{amount}", String.valueOf(amount))
                        .replace("{stat}", "points"));
            }
            case "info" -> {
                if (!(sender instanceof Player p)) { sender.sendMessage(plugin.getConfigManager().msg("player-only")); return true; }
                PlayerData data = plugin.getDataManager().getOrCreate(p.getUniqueId(), p.getName());
                p.sendMessage(plugin.getConfigManager().msg("prefix") + "<gray>Level: <aqua>" + data.getLevel()
                        + "</aqua> | EXP: <green>" + data.getExp() + "</green> | Points: <gold>" + data.getPotentialPoints());
            }
            default -> sendHelp(sender);
        }
        return true;
    }

    private void sendHelp(CommandSender s) {
        s.sendMessage(plugin.getConfigManager().msg("prefix") + "<yellow>SchoolLevel2 Commands:");
        s.sendMessage("<gold>/sl help <gray>- Trợ giúp");
        s.sendMessage("<gold>/sl menu <gray>- Mở menu tiềm năng");
        s.sendMessage("<gold>/sl info <gray>- Xem thông tin cấp độ");
        s.sendMessage("<gold>/sl reload <gray>- Reload config");
        s.sendMessage("<gold>/sl give <player> <gray>- Cho item reset");
        s.sendMessage("<gold>/sl addexp <player> <amount> <gray>- Cộng exp");
        s.sendMessage("<gold>/sl setlevel <player> <level> <gray>- Đặt cấp");
        s.sendMessage("<gold>/sl addlevel <player> <amount> <gray>- Cộng cấp");
        s.sendMessage("<gold>/sl addpoints <player> <amount> <gray>- Cộng điểm tiềm năng");
    }

    private long parseLong(String s) {
        try { return Long.parseLong(s); } catch (Exception e) { return -1; }
    }
    private int parseInt(String s) {
        try { return Integer.parseInt(s); } catch (Exception e) { return Integer.MIN_VALUE; }
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return filter(List.of("help", "menu", "info", "reload", "give", "addexp", "setlevel", "addlevel", "addpoints"), args[0]);
        }
        if (args.length == 2 && List.of("give", "addexp", "setlevel", "addlevel", "addpoints").contains(args[0].toLowerCase())) {
            List<String> names = new ArrayList<>();
            for (Player p : Bukkit.getOnlinePlayers()) names.add(p.getName());
            return filter(names, args[1]);
        }
        return Collections.emptyList();
    }

    private List<String> filter(List<String> list, String prefix) {
        List<String> out = new ArrayList<>();
        for (String s : list) if (s.toLowerCase().startsWith(prefix.toLowerCase())) out.add(s);
        return out;
    }
}

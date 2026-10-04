package com.schoollevel2.listeners;

import com.schoollevel2.SchoolLevel2;
import com.schoollevel2.data.PlayerData;
import com.schoollevel2.utils.ColorUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class ActionBarTask extends BukkitRunnable {

    private final SchoolLevel2 plugin;
    private int tickCounter = 0;

    public ActionBarTask(SchoolLevel2 plugin) { this.plugin = plugin; }

    @Override
    public void run() {
        tickCounter++;
        boolean doActionbar = tickCounter % 20 == 0; // every 1 second
        boolean doStats = tickCounter % 200 == 0; // every 10 seconds reapply agility

        for (Player p : Bukkit.getOnlinePlayers()) {
            PlayerData data = plugin.getDataManager().get(p.getUniqueId());
            if (data == null) continue;

            if (doActionbar) {
                String bar = plugin.getLevelManager().buildExpBar(data);
                long needed = plugin.getConfigManager().getExpForLevel(data.getLevel());
                String raw = plugin.getConfigManager().getActionbar()
                        .replace("{level}", String.valueOf(data.getLevel()))
                        .replace("{exp_bar}", bar)
                        .replace("{current}", String.valueOf(data.getExp()))
                        .replace("{needed}", String.valueOf(needed))
                        .replace("{points}", String.valueOf(data.getPotentialPoints()));
                Component c = ColorUtil.mm(raw);
                p.sendActionBar(c);
            }

            if (doStats) {
                plugin.getLevelManager().applyLevelBar(p, data);
                plugin.getStatsManager().applyStats(p);
            }
        }
    }
}

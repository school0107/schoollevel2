package com.schoollevel2;

import com.schoollevel2.commands.SchoolLevelCommand;
import com.schoollevel2.config.ConfigManager;
import com.schoollevel2.data.DataManager;
import com.schoollevel2.level.LevelManager;
import com.schoollevel2.listeners.*;
import com.schoollevel2.menu.StatsMenu;
import com.schoollevel2.stats.StatsManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class SchoolLevel2 extends JavaPlugin {

    private static SchoolLevel2 instance;
    private ConfigManager configManager;
    private DataManager dataManager;
    private LevelManager levelManager;
    private StatsManager statsManager;
    private StatsMenu statsMenu;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();

        this.configManager = new ConfigManager(this);
        this.dataManager = new DataManager(this);
        this.levelManager = new LevelManager(this);
        this.statsManager = new StatsManager(this);
        this.statsMenu = new StatsMenu(this);

        dataManager.loadAll();

        // Commands
        var cmd = getCommand("schoollevel");
        if (cmd != null) {
            SchoolLevelCommand executor = new SchoolLevelCommand(this);
            cmd.setExecutor(executor);
            cmd.setTabCompleter(executor);
        }
        var statsCmd = getCommand("stats");
        if (statsCmd != null) {
            statsCmd.setExecutor((sender, command, label, args) -> {
                if (!(sender instanceof Player p)) {
                    configManager.send(sender, "player-only");
                    return true;
                }
                statsMenu.open(p);
                return true;
            });
        }

        // Listeners
        Bukkit.getPluginManager().registerEvents(new MiningListener(this), this);
        Bukkit.getPluginManager().registerEvents(new CombatListener(this), this);
        Bukkit.getPluginManager().registerEvents(new JoinQuitListener(this), this);
        Bukkit.getPluginManager().registerEvents(new MenuListener(this), this);

        // Tasks
        new ActionBarTask(this).runTaskTimer(this, 0L, configManager.getActionbarInterval());

        long saveInterval = configManager.getSaveInterval() * 20L;
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> dataManager.saveAll(), saveInterval, saveInterval);

        getLogger().info("SchoolLevel2 enabled successfully!");
    }

    @Override
    public void onDisable() {
        if (dataManager != null) dataManager.saveAll();
        getLogger().info("SchoolLevel2 disabled!");
    }

    public void reload() {
        reloadConfig();
        configManager.reload();
        getLogger().info("Configuration reloaded!");
    }

    public static SchoolLevel2 getInstance() { return instance; }
    public ConfigManager getConfigManager() { return configManager; }
    public DataManager getDataManager() { return dataManager; }
    public LevelManager getLevelManager() { return levelManager; }
    public StatsManager getStatsManager() { return statsManager; }
    public StatsMenu getStatsMenu() { return statsMenu; }
}
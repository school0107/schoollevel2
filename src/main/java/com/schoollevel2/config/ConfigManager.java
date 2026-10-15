package com.schoollevel2.config;

import com.schoollevel2.SchoolLevel2;
import com.schoollevel2.utils.ColorUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;

public class ConfigManager {

    private final SchoolLevel2 plugin;
    private FileConfiguration cfg;

    private int maxLevel, pointsPerLevel, startLevel, actionbarInterval, saveInterval;
    private boolean showActionbar, showLevelBar, showTitleOnLevelup, showChatOnLevelup;

    private double expBase, expExponent;
    private Map<Material, Integer> miningExp;

    private Map<String, Double> statPerPoint;
    private Map<String, Integer> statMaxPoints;
    private double agilitySpeedPerPoint, agilityDodgePerPoint, dodgeCap, speedEffectStep;

    // Chakra
    private double chakraCritPerPoint, chakraCritCap, chakraCritMultiplier;
    private double chakraHealthPerPoint, chakraSpeedPerPoint, chakraDodgePerPoint;

    private Material resetMaterial;
    private String resetName;
    private List<String> resetLore;
    private boolean resetRefund;

    public ConfigManager(SchoolLevel2 plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        plugin.reloadConfig();
        this.cfg = plugin.getConfig();

        this.maxLevel = cfg.getInt("settings.max-level", 200);
        this.pointsPerLevel = cfg.getInt("settings.points-per-level", 2);
        this.startLevel = cfg.getInt("settings.start-level", 1);
        this.actionbarInterval = cfg.getInt("settings.actionbar-interval-ticks", 10);
        this.saveInterval = cfg.getInt("settings.save-interval-seconds", 300);
        this.showActionbar = cfg.getBoolean("settings.show-actionbar", true);
        this.showLevelBar = cfg.getBoolean("settings.show-level-bar", true);
        this.showTitleOnLevelup = cfg.getBoolean("settings.show-title-on-levelup", true);
        this.showChatOnLevelup = cfg.getBoolean("settings.show-chat-on-levelup", true);

        this.expBase = cfg.getDouble("exp-formula.base", 100);
        this.expExponent = cfg.getDouble("exp-formula.exponent", 1.5);

        this.miningExp = new HashMap<>();
        var section = cfg.getConfigurationSection("mining-exp");
        if (section != null) {
            for (String key : section.getKeys(false)) {
                Material mat = Material.matchMaterial(key);
                if (mat != null) miningExp.put(mat, section.getInt(key));
            }
        }

        this.statPerPoint = new HashMap<>();
        this.statMaxPoints = new HashMap<>();
        for (String s : List.of("melee", "ranged", "health")) {
            statPerPoint.put(s, cfg.getDouble("stats." + s + ".per-point", 1.0));
            statMaxPoints.put(s, cfg.getInt("stats." + s + ".max-points", 100));
        }
        this.agilitySpeedPerPoint = cfg.getDouble("stats.agility.speed-per-point", 1.0);
        this.agilityDodgePerPoint = cfg.getDouble("stats.agility.dodge-per-point", 0.25);
        this.statMaxPoints.put("agility", cfg.getInt("stats.agility.max-points", 20));
        this.dodgeCap = cfg.getDouble("stats.agility.dodge-chance-cap", 5.0);
        this.speedEffectStep = cfg.getDouble("stats.agility.speed-effect-step", 20.0);

        this.statMaxPoints.put("chakra", cfg.getInt("stats.chakra.max-points", 50));
        this.chakraCritPerPoint = cfg.getDouble("stats.chakra.crit-chance-per-point", 0.5);
        this.chakraCritCap = cfg.getDouble("stats.chakra.crit-chance-cap", 25.0);
        this.chakraCritMultiplier = cfg.getDouble("stats.chakra.crit-damage-multiplier", 1.5);
        this.chakraHealthPerPoint = cfg.getDouble("stats.chakra.health-per-point", 1.0);
        this.chakraSpeedPerPoint = cfg.getDouble("stats.chakra.speed-per-point", 0.3);
        this.chakraDodgePerPoint = cfg.getDouble("stats.chakra.dodge-per-point", 0.1);

        this.resetMaterial = Material.matchMaterial(cfg.getString("reset-item.material", "NETHER_STAR"));
        if (resetMaterial == null) resetMaterial = Material.NETHER_STAR;
        this.resetName = cfg.getString("reset-item.name", "&cReset");
        this.resetLore = cfg.getStringList("reset-item.lore");
        this.resetRefund = cfg.getBoolean("reset-item.refund-points", true);
    }

    public long getExpForLevel(int level) {
        return (long) (expBase * Math.pow(level, expExponent));
    }

    public int getMiningExp(Material mat) {
        return miningExp.getOrDefault(mat, 0);
    }

    // -------- Message --------
    public Component msgComponent(String key) {
        String prefix = cfg.getString("messages.prefix", "");
        String msg = cfg.getString("messages." + key, "&cMissing message: " + key);
        return ColorUtil.mm(prefix + msg);
    }

    public Component msgComponent(String key, Map<String, String> placeholders) {
        String prefix = cfg.getString("messages.prefix", "");
        String msg = cfg.getString("messages." + key, "&cMissing message: " + key);
        String full = prefix + msg;
        if (placeholders != null) {
            for (var e : placeholders.entrySet()) {
                full = full.replace("{" + e.getKey() + "}", e.getValue());
            }
        }
        return ColorUtil.mm(full);
    }

    public void send(CommandSender sender, String key) { sender.sendMessage(msgComponent(key)); }
    public void send(CommandSender sender, String key, Map<String, String> ph) { sender.sendMessage(msgComponent(key, ph)); }

    // -------- Getters --------
    public int getMaxLevel() { return maxLevel; }
    public int getPointsPerLevel() { return pointsPerLevel; }
    public int getStartLevel() { return startLevel; }
    public int getActionbarInterval() { return Math.max(1, actionbarInterval); }
    public int getSaveInterval() { return Math.max(30, saveInterval); }
    public boolean isShowActionbar() { return showActionbar; }
    public boolean isShowLevelBar() { return showLevelBar; }
    public boolean isShowTitleOnLevelup() { return showTitleOnLevelup; }
    public boolean isShowChatOnLevelup() { return showChatOnLevelup; }

    public double getStatPerPoint(String s) { return statPerPoint.getOrDefault(s, 1.0); }
    public int getStatMaxPoints(String s) { return statMaxPoints.getOrDefault(s, 100); }
    public double getAgilitySpeedPerPoint() { return agilitySpeedPerPoint; }
    public double getAgilityDodgePerPoint() { return agilityDodgePerPoint; }
    public double getDodgeCap() { return dodgeCap; }
    public double getSpeedEffectStep() { return speedEffectStep; }

    public double getChakraCritPerPoint() { return chakraCritPerPoint; }
    public double getChakraCritCap() { return chakraCritCap; }
    public double getChakraCritMultiplier() { return chakraCritMultiplier; }
    public double getChakraHealthPerPoint() { return chakraHealthPerPoint; }
    public double getChakraSpeedPerPoint() { return chakraSpeedPerPoint; }
    public double getChakraDodgePerPoint() { return chakraDodgePerPoint; }

    public Material getResetMaterial() { return resetMaterial; }
    public String getResetName() { return resetName; }
    public List<String> getResetLore() { return resetLore; }
    public boolean isResetRefund() { return resetRefund; }
    public FileConfiguration getCfg() { return cfg; }
}
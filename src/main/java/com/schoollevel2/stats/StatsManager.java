package com.schoollevel2.stats;

import com.schoollevel2.SchoolLevel2;
import com.schoollevel2.data.PlayerData;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Random;

public class StatsManager {

    private final SchoolLevel2 plugin;
    private final Random rng = new Random();

    public StatsManager(SchoolLevel2 plugin) {
        this.plugin = plugin;
    }

    /**
     * Áp dụng stat (health, speed) cho player.
     * Gọi khi: join, đổi stat, định kỳ (để chống mất effect).
     */
    public void applyStats(Player player) {
        PlayerData data = plugin.getDataManager().get(player.getUniqueId());
        if (data == null) return;
        var cfg = plugin.getConfigManager();

        // ----- HEALTH -----
        int hpPoints = data.getStat("health");
        int chakraPoints = data.getStat("chakra");

        double bonusHp = hpPoints * cfg.getStatPerPoint("health")
                + chakraPoints * cfg.getChakraHealthPerPoint();

        AttributeInstance attr = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (attr != null) {
            double target = 20.0 + bonusHp;
            if (Math.abs(attr.getBaseValue() - target) > 0.01) {
                attr.setBaseValue(target);
            }
            if (player.getHealth() > target) player.setHealth(target);
        }

        // ----- SPEED (agility + chakra) -----
        int agiPoints = data.getStat("agility");
        double speedPct = agiPoints * cfg.getAgilitySpeedPerPoint()
                + chakraPoints * cfg.getChakraSpeedPerPoint();

        player.removePotionEffect(PotionEffectType.SPEED);
        double step = cfg.getSpeedEffectStep();
        if (step > 0 && speedPct >= step) {
            int amplifier = (int) Math.min(4, Math.floor(speedPct / step));
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED,
                    Integer.MAX_VALUE, amplifier, true, false, false));
        }
    }

    // ----- MELEE -----
    public double getMeleeBonus(Player player) {
        PlayerData data = plugin.getDataManager().get(player.getUniqueId());
        if (data == null) return 0;
        return data.getStat("melee") * plugin.getConfigManager().getStatPerPoint("melee");
    }

    // ----- RANGED -----
    public double getRangedBonus(Player player) {
        PlayerData data = plugin.getDataManager().get(player.getUniqueId());
        if (data == null) return 0;
        return data.getStat("ranged") * plugin.getConfigManager().getStatPerPoint("ranged");
    }

    // ----- DODGE (agility + chakra) -----
    public double getDodgeChance(Player player) {
        PlayerData data = plugin.getDataManager().get(player.getUniqueId());
        if (data == null) return 0;
        var cfg = plugin.getConfigManager();
        double chance = data.getStat("agility") * cfg.getAgilityDodgePerPoint()
                + data.getStat("chakra") * cfg.getChakraDodgePerPoint();
        return Math.min(chance, cfg.getDodgeCap());
    }

    public double getAgilitySpeedPercent(Player player) {
        PlayerData data = plugin.getDataManager().get(player.getUniqueId());
        if (data == null) return 0;
        return data.getStat("agility") * plugin.getConfigManager().getAgilitySpeedPerPoint();
    }

    // ----- CHAKRA crit -----
    public double getCritChance(Player player) {
        PlayerData data = plugin.getDataManager().get(player.getUniqueId());
        if (data == null) return 0;
        var cfg = plugin.getConfigManager();
        double chance = data.getStat("chakra") * cfg.getChakraCritPerPoint();
        return Math.min(chance, cfg.getChakraCritCap());
    }

    public double getCritMultiplier() {
        return plugin.getConfigManager().getChakraCritMultiplier();
    }

    public boolean rollCrit(Player player) {
        double chance = getCritChance(player);
        if (chance <= 0) return false;
        return rng.nextDouble() * 100.0 < chance;
    }

    // ----- ADD / RESET -----
    public boolean tryAddStat(Player player, String stat, int amount) {
        PlayerData data = plugin.getDataManager().get(player.getUniqueId());
        if (data == null) return false;
        if (amount <= 0) return false;
        if (data.getPotentialPoints() < amount) return false;

        int max = plugin.getConfigManager().getStatMaxPoints(stat);
        int current = data.getStat(stat);
        if (current >= max) return false;

        int toAdd = Math.min(amount, Math.min(max - current, data.getPotentialPoints()));
        if (toAdd <= 0) return false;

        data.setStat(stat, current + toAdd);
        data.setPotentialPoints(data.getPotentialPoints() - toAdd);
        applyStats(player);
        return true;
    }

    public int resetStats(Player player) {
        PlayerData data = plugin.getDataManager().get(player.getUniqueId());
        if (data == null) return 0;
        int refund = 0;
        for (String s : new String[]{"melee", "ranged", "health", "agility", "chakra"}) {
            refund += data.getStat(s);
            data.setStat(s, 0);
        }
        if (plugin.getConfigManager().isResetRefund()) {
            data.addPotentialPoints(refund);
        }
        applyStats(player);
        return refund;
    }
}
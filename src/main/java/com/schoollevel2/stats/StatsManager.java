package com.schoollevel2.stats;

import com.schoollevel2.SchoolLevel2;
import com.schoollevel2.data.PlayerData;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class StatsManager {

    private final SchoolLevel2 plugin;

    public StatsManager(SchoolLevel2 plugin) {
        this.plugin = plugin;
    }

    public void applyStats(Player player) {
        PlayerData data = plugin.getDataManager().get(player.getUniqueId());
        if (data == null) return;

        // HEALTH
        int hpPoints = data.getStat("health");
        double bonusHp = hpPoints * plugin.getConfigManager().getStatPerPoint("health");
        AttributeInstance attr = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (attr != null) {
            double target = 20.0 + bonusHp;
            if (Math.abs(attr.getBaseValue() - target) > 0.01) {
                attr.setBaseValue(target);
            }
            if (player.getHealth() > target) player.setHealth(target);
        }

        // AGILITY -> speed dùng potion effect
        int agiPoints = data.getStat("agility");
        double speedPct = agiPoints * plugin.getConfigManager().getAgilitySpeedPerPoint();
        player.removePotionEffect(PotionEffectType.SPEED);
        double step = plugin.getConfigManager().getSpeedEffectStep();
        if (step > 0 && speedPct >= step) {
            int amplifier = (int) Math.min(4, Math.floor(speedPct / step));
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED,
                    Integer.MAX_VALUE, amplifier, true, false, false));
        }
    }

    public double getMeleeBonus(Player player) {
        PlayerData data = plugin.getDataManager().get(player.getUniqueId());
        if (data == null) return 0;
        return data.getStat("melee") * plugin.getConfigManager().getStatPerPoint("melee");
    }

    public double getRangedBonus(Player player) {
        PlayerData data = plugin.getDataManager().get(player.getUniqueId());
        if (data == null) return 0;
        return data.getStat("ranged") * plugin.getConfigManager().getStatPerPoint("ranged");
    }

    public double getDodgeChance(Player player) {
        PlayerData data = plugin.getDataManager().get(player.getUniqueId());
        if (data == null) return 0;
        double chance = data.getStat("agility") * plugin.getConfigManager().getAgilityDodgePerPoint();
        return Math.min(chance, plugin.getConfigManager().getDodgeCap());
    }

    public double getAgilitySpeedPercent(Player player) {
        PlayerData data = plugin.getDataManager().get(player.getUniqueId());
        if (data == null) return 0;
        return data.getStat("agility") * plugin.getConfigManager().getAgilitySpeedPerPoint();
    }

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
        for (String s : new String[]{"melee", "ranged", "health", "agility"}) {
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
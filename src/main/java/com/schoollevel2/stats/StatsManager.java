package com.schoollevel2.stats;

import com.schoollevel2.SchoolLevel2;
import com.schoollevel2.data.PlayerData;
import com.schoollevel2.utils.ItemUtil;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Map;
import java.util.Random;

public class StatsManager {

    private final SchoolLevel2 plugin;
    private final Random rng = new Random();

    public StatsManager(SchoolLevel2 plugin) {
        this.plugin = plugin;
    }

    public void applyStats(Player player) {
        PlayerData data = plugin.getDataManager().get(player.getUniqueId());
        if (data == null) return;
        var cfg = plugin.getConfigManager();

        // 1. Tính stat từ PlayerData
        int hpPoints = data.getStat("health");
        int chakraPoints = data.getStat("chakra");
        double bonusHp = hpPoints * cfg.getStatPerPoint("health") + chakraPoints * cfg.getChakraHealthPerPoint();
        double speedPct = data.getStat("agility") * cfg.getAgilitySpeedPerPoint() + chakraPoints * cfg.getChakraSpeedPerPoint();

        // 2. Cộng thêm stat từ Trang bị (Giáp, vũ khí trên tay)
        for (ItemStack item : player.getInventory().getArmorContents()) {
            if (item != null) {
                Map<String, Double> itemStats = ItemUtil.getItemStats(item);
                if (itemStats.containsKey("health")) bonusHp += itemStats.get("health");
                if (itemStats.containsKey("chakra")) {
                    double chk = itemStats.get("chakra");
                    bonusHp += chk * cfg.getChakraHealthPerPoint();
                    speedPct += chk * cfg.getChakraSpeedPerPoint();
                }
                if (itemStats.containsKey("agility")) {
                    speedPct += itemStats.get("agility") * cfg.getAgilitySpeedPerPoint();
                }
            }
        }

        // 3. Áp dụng Máu
        AttributeInstance attr = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (attr != null) {
            double target = 20.0 + bonusHp;
            if (Math.abs(attr.getBaseValue() - target) > 0.01) {
                attr.setBaseValue(target);
            }
            if (player.getHealth() > target) player.setHealth(target);
        }

        // 4. Áp dụng Tốc độ
        player.removePotionEffect(PotionEffectType.SPEED);
        double step = cfg.getSpeedEffectStep();
        if (step > 0 && speedPct >= step) {
            int amplifier = (int) Math.min(4, Math.floor(speedPct / step));
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, amplifier, true, false, false));
        }
    }

    // Tính tổng Sát thương cận chiến (Data + Item)
    public double getMeleeBonus(Player player) {
        PlayerData data = plugin.getDataManager().get(player.getUniqueId());
        double bonus = data.getStat("melee") * plugin.getConfigManager().getStatPerPoint("melee");
        
        // Cộng từ item đang cầm trên tay
        ItemStack hand = player.getInventory().getItemInMainHand();
        Map<String, Double> itemStats = ItemUtil.getItemStats(hand);
        if (itemStats.containsKey("melee")) bonus += itemStats.get("melee");

        return bonus;
    }

    // Tính tổng Sát thương tầm xa (Data + Item)
    public double getRangedBonus(Player player) {
        PlayerData data = plugin.getDataManager().get(player.getUniqueId());
        double bonus = data.getStat("ranged") * plugin.getConfigManager().getStatPerPoint("ranged");
        
        ItemStack hand = player.getInventory().getItemInMainHand();
        Map<String, Double> itemStats = ItemUtil.getItemStats(hand);
        if (itemStats.containsKey("ranged")) bonus += itemStats.get("ranged");

        return bonus;
    }

    // Tính tỉ lệ né (Data + Item)
    public double getDodgeChance(Player player) {
        PlayerData data = plugin.getDataManager().get(player.getUniqueId());
        var cfg = plugin.getConfigManager();
        double chance = data.getStat("agility") * cfg.getAgilityDodgePerPoint() + data.getStat("chakra") * cfg.getChakraDodgePerPoint();

        // Cộng từ trang bị
        for (ItemStack item : player.getInventory().getArmorContents()) {
            if (item != null) {
                Map<String, Double> itemStats = ItemUtil.getItemStats(item);
                if (itemStats.containsKey("agility")) chance += itemStats.get("agility") * cfg.getAgilityDodgePerPoint();
                if (itemStats.containsKey("chakra")) chance += itemStats.get("chakra") * cfg.getChakraDodgePerPoint();
            }
        }
        return Math.min(chance, cfg.getDodgeCap());
    }

    // Tính tỉ lệ Crit (Data + Item)
    public double getCritChance(Player player) {
        PlayerData data = plugin.getDataManager().get(player.getUniqueId());
        var cfg = plugin.getConfigManager();
        double chance = data.getStat("chakra") * cfg.getChakraCritPerPoint();

        ItemStack hand = player.getInventory().getItemInMainHand();
        Map<String, Double> itemStats = ItemUtil.getItemStats(hand);
        if (itemStats.containsKey("crit")) chance += itemStats.get("crit");
        if (itemStats.containsKey("chakra")) chance += itemStats.get("chakra") * cfg.getChakraCritPerPoint();

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

    public boolean tryAddStat(Player player, String stat, int amount) {
        PlayerData data = plugin.getDataManager().get(player.getUniqueId());
        if (data == null || amount <= 0 || data.getPotentialPoints() < amount) return false;

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
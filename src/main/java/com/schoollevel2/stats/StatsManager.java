package com.schoollevel2.stats;

import com.schoollevel2.SchoolLevel2;
import com.schoollevel2.data.PlayerData;
import com.schoollevel2.utils.ItemUtil;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class StatsManager {

    private final SchoolLevel2 plugin;
    private final Random rng = new Random();
    
    // Key dùng cho AttributeModifier
    private static final UUID SPEED_UUID = UUID.fromString("1a2b3c4d-5e6f-7a8b-9c0d-1e2f3a4b5c6d");

    public StatsManager(SchoolLevel2 plugin) {
        this.plugin = plugin;
    }

    public void applyStats(Player player) {
        PlayerData data = plugin.getDataManager().get(player.getUniqueId());
        if (data == null) return;
        var cfg = plugin.getConfigManager();

        // 1. Tính Máu
        double bonusHp = data.getStat("health") * cfg.getStatPerPoint("health") 
                       + data.getStat("chakra") * cfg.getChakraHealthPerPoint();
        
        // 2. Tính Tốc độ (Nhanh nhẹn + Chakra + Trang bị)
        double speedPct = data.getStat("agility") * cfg.getAgilitySpeedPerPoint() 
                        + data.getStat("chakra") * cfg.getChakraSpeedPerPoint();

        // Cộng thêm từ Trang bị
        for (ItemStack item : player.getInventory().getArmorContents()) {
            if (item != null && !item.getType().isAir()) {
                Map<String, Double> stats = ItemUtil.getItemStats(item);
                if (stats.containsKey("health")) bonusHp += stats.get("health");
                if (stats.containsKey("agility")) speedPct += stats.get("agility") * cfg.getAgilitySpeedPerPoint();
                if (stats.containsKey("chakra")) {
                    double chk = stats.get("chakra");
                    bonusHp += chk * cfg.getChakraHealthPerPoint();
                    speedPct += chk * cfg.getChakraSpeedPerPoint();
                }
            }
        }

        // Áp dụng Máu
        AttributeInstance hpAttr = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (hpAttr != null) {
            double target = 20.0 + bonusHp;
            if (Math.abs(hpAttr.getBaseValue() - target) > 0.01) {
                hpAttr.setBaseValue(target);
            }
            if (player.getHealth() > target) player.setHealth(target);
        }

        // Áp dụng Tốc độ (Direct Attribute)
        AttributeInstance speedAttr = player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
        if (speedAttr != null) {
            // Xóa modifier cũ nếu có để tránh cộng dồn
            for (AttributeModifier mod : speedAttr.getModifiers()) {
                if (mod.getUniqueId().equals(SPEED_UUID)) {
                    speedAttr.removeModifier(mod);
                }
            }
            // Speed gốc của player là 0.1 (100%). Tăng % dựa trên mốc này.
            if (speedPct > 0) {
                double finalSpeedBonus = 0.1 * (speedPct / 100.0);
                AttributeModifier speedMod = new AttributeModifier(
                        SPEED_UUID, 
                        "schoollevel2_speed", 
                        finalSpeedBonus, 
                        AttributeModifier.Operation.ADD_NUMBER
                );
                speedAttr.addModifier(speedMod);
            }
        }
    }

    // ==========================================
    // CÁC HÀM LẤY TỔNG CHỈ SỐ (BAO GỒM CẢ TRANG BỊ)
    // ==========================================

    public double getTotalMelee(Player player) {
        PlayerData data = plugin.getDataManager().get(player.getUniqueId());
        if (data == null) return 0;
        double total = data.getStat("melee") * plugin.getConfigManager().getStatPerPoint("melee");
        
        ItemStack hand = player.getInventory().getItemInMainHand();
        Map<String, Double> handStats = ItemUtil.getItemStats(hand);
        if (handStats.containsKey("melee")) total += handStats.get("melee");
        
        return total;
    }

    public double getTotalRanged(Player player) {
        PlayerData data = plugin.getDataManager().get(player.getUniqueId());
        if (data == null) return 0;
        double total = data.getStat("ranged") * plugin.getConfigManager().getStatPerPoint("ranged");
        
        ItemStack hand = player.getInventory().getItemInMainHand();
        Map<String, Double> handStats = ItemUtil.getItemStats(hand);
        if (handStats.containsKey("ranged")) total += handStats.get("ranged");
        
        return total;
    }

    public double getTotalHealth(Player player) {
        PlayerData data = plugin.getDataManager().get(player.getUniqueId());
        if (data == null) return 0;
        var cfg = plugin.getConfigManager();
        
        double total = data.getStat("health") * cfg.getStatPerPoint("health")
                     + data.getStat("chakra") * cfg.getChakraHealthPerPoint();
        
        for (ItemStack item : player.getInventory().getArmorContents()) {
            if (item != null && !item.getType().isAir()) {
                Map<String, Double> stats = ItemUtil.getItemStats(item);
                if (stats.containsKey("health")) total += stats.get("health");
                if (stats.containsKey("chakra")) total += stats.get("chakra") * cfg.getChakraHealthPerPoint();
            }
        }
        return total;
    }

    public double getTotalSpeedPercent(Player player) {
        PlayerData data = plugin.getDataManager().get(player.getUniqueId());
        if (data == null) return 0;
        var cfg = plugin.getConfigManager();
        
        double total = data.getStat("agility") * cfg.getAgilitySpeedPerPoint()
                     + data.getStat("chakra") * cfg.getChakraSpeedPerPoint();
                     
        for (ItemStack item : player.getInventory().getArmorContents()) {
            if (item != null && !item.getType().isAir()) {
                Map<String, Double> stats = ItemUtil.getItemStats(item);
                if (stats.containsKey("agility")) total += stats.get("agility") * cfg.getAgilitySpeedPerPoint();
                if (stats.containsKey("chakra")) total += stats.get("chakra") * cfg.getChakraSpeedPerPoint();
            }
        }
        return total;
    }

    public double getTotalDodge(Player player) {
        PlayerData data = plugin.getDataManager().get(player.getUniqueId());
        if (data == null) return 0;
        var cfg = plugin.getConfigManager();
        
        double total = data.getStat("agility") * cfg.getAgilityDodgePerPoint()
                     + data.getStat("chakra") * cfg.getChakraDodgePerPoint();
                     
        for (ItemStack item : player.getInventory().getArmorContents()) {
            if (item != null && !item.getType().isAir()) {
                Map<String, Double> stats = ItemUtil.getItemStats(item);
                if (stats.containsKey("agility")) total += stats.get("agility") * cfg.getAgilityDodgePerPoint();
                if (stats.containsKey("chakra")) total += stats.get("chakra") * cfg.getChakraDodgePerPoint();
            }
        }
        return Math.min(total, cfg.getDodgeCap());
    }

    public double getTotalCrit(Player player) {
        PlayerData data = plugin.getDataManager().get(player.getUniqueId());
        if (data == null) return 0;
        var cfg = plugin.getConfigManager();
        
        double total = data.getStat("chakra") * cfg.getChakraCritPerPoint();
        
        ItemStack hand = player.getInventory().getItemInMainHand();
        Map<String, Double> handStats = ItemUtil.getItemStats(hand);
        if (handStats.containsKey("crit")) total += handStats.get("crit");
        if (handStats.containsKey("chakra")) total += handStats.get("chakra") * cfg.getChakraCritPerPoint();
        
        return Math.min(total, cfg.getChakraCritCap());
    }

    public double getCritMultiplier() {
        return plugin.getConfigManager().getChakraCritMultiplier();
    }

    public boolean rollCrit(Player player) {
        double chance = getTotalCrit(player);
        if (chance <= 0) return false;
        return rng.nextDouble() * 100.0 < chance;
    }

    // ==========================================
    // QUẢN LÝ ĐIỂM
    // ==========================================

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
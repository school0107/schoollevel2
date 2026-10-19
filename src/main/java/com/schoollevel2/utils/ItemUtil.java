package com.schoollevel2.utils;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemUtil {

    private static NamespacedKey getKey(String name) {
        return new NamespacedKey(JavaPlugin.getProvidingPlugin(ItemUtil.class), "rpg_" + name);
    }

    public static ItemStack make(Material mat, String name, List<String> lore) {
        return new ItemBuilder(mat).name(name).lore(lore).build();
    }

    public static boolean isResetItem(ItemStack item, Material resetMat, String resetName) {
        if (item == null || item.getType() != resetMat) return false;
        if (!item.hasItemMeta()) return false;
        var meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return false;

        Component displayComp = meta.displayName();
        if (displayComp == null) return false;
        String plain = ColorUtil.stripTags(net.kyori.adventure.text.minimessage.MiniMessage.miniMessage().serialize(displayComp));
        String expected = ColorUtil.stripTags(resetName);
        return plain.trim().equalsIgnoreCase(expected.trim());
    }

    // ==========================================
    // ÉP CHỈ SỐ VÀO ITEM
    // ==========================================
    public static ItemStack applyRPGStats(ItemStack item, String statKey, double amount) {
        if (item == null || item.getType().isAir()) return null;
        
        ItemStack clone = item.clone();
        ItemMeta meta = clone.getItemMeta();
        if (meta == null) return clone;

        PersistentDataContainer pdc = meta.getPersistentDataContainer();

        if (amount != 0) {
            NamespacedKey statKeyObj = getKey(statKey);
            double current = pdc.getOrDefault(statKeyObj, PersistentDataType.DOUBLE, 0.0);
            pdc.set(statKeyObj, PersistentDataType.DOUBLE, current + amount);
        }

        clone.setItemMeta(meta);
        return rebuildLore(clone);
    }

    // ==========================================
    // ÉP YÊU CẦU LEVEL
    // ==========================================
    public static ItemStack applyLevelRequirement(ItemStack item, int reqLevel) {
        if (item == null || item.getType().isAir()) return null;
        
        ItemStack clone = item.clone();
        ItemMeta meta = clone.getItemMeta();
        if (meta == null) return clone;

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        NamespacedKey reqKey = getKey("req_level");
        
        // Ghi đè hoặc set mới
        if (reqLevel <= 0) {
            pdc.remove(reqKey);
        } else {
            pdc.set(reqKey, PersistentDataType.INTEGER, reqLevel);
        }

        clone.setItemMeta(meta);
        return rebuildLore(clone);
    }

    private static ItemStack rebuildLore(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        List<Component> lore = meta.lore() != null ? new ArrayList<>(meta.lore()) : new ArrayList<>();

        // Xoá lore cũ do plugin tạo (dùng chung các icon để check)
        lore.removeIf(c -> {
            String plain = ColorUtil.stripTags(net.kyori.adventure.text.minimessage.MiniMessage.miniMessage().serialize(c));
            return plain.contains("⚔") || plain.contains("🏹") || plain.contains("❤") || 
                   plain.contains("💨") || plain.contains("🔥") || plain.contains("💥") || 
                   plain.contains("Yêu cầu:") || plain.contains("Vật phẩm RPG") || 
                   plain.contains("▬▬▬▬");
        });

        String[] stats = {"melee", "ranged", "health", "agility", "chakra", "crit"};
        boolean hasStat = false;

        for (String s : stats) {
            double val = pdc.getOrDefault(getKey(s), PersistentDataType.DOUBLE, 0.0);
            if (val > 0) {
                hasStat = true;
                String format = getLoreFormat(s);
                // Thay thế {value}
                String formattedVal = format.replace("{value}", fmt(val));
                lore.add(ColorUtil.mm(formattedVal));
            }
        }

        int reqLevel = pdc.getOrDefault(getKey("req_level"), PersistentDataType.INTEGER, 0);
        if (reqLevel > 0) {
            hasStat = true;
            lore.add(ColorUtil.mm("&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
            lore.add(ColorUtil.mm("&c✖ Yêu cầu: &eLevel " + reqLevel));
        }

        if (hasStat) {
            lore.add(ColorUtil.mm("&8&oVật phẩm RPG"));
            meta.lore(lore);
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            item.setItemMeta(meta);
        } else {
            meta.lore(lore);
            item.setItemMeta(meta);
        }

        return item;
    }

    private static String getLoreFormat(String stat) {
        return switch (stat) {
            case "melee" -> "&c ⚔ Sát thương cận chiến: &f+{value}";
            case "ranged" -> "&a 🏹 Sát thương tầm xa: &f+{value}";
            case "health" -> "&4 ❤ Máu tối đa: &f+{value}";
            case "agility" -> "&b 💨 Nhanh nhẹn: &f+{value}";
            case "chakra" -> "&d 🔥 Chakra: &f+{value}";
            case "crit" -> "&6 💥 Tỉ lệ chí mạng: &f+{value}%";
            default -> "&f?";
        };
    }

    public static Map<String, Double> getItemStats(ItemStack item) {
        Map<String, Double> map = new HashMap<>();
        if (item == null || item.getType().isAir() || !item.hasItemMeta()) return map;
        
        PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
        for (String s : new String[]{"melee", "ranged", "health", "agility", "chakra", "crit"}) {
            double val = pdc.getOrDefault(getKey(s), PersistentDataType.DOUBLE, 0.0);
            if (val != 0) map.put(s, val);
        }
        return map;
    }

    public static int getRequiredLevel(ItemStack item) {
        if (item == null || item.getType().isAir() || !item.hasItemMeta()) return 0;
        return item.getItemMeta().getPersistentDataContainer().getOrDefault(getKey("req_level"), PersistentDataType.INTEGER, 0);
    }

    private static String fmt(double d) {
        if (d == Math.floor(d)) return String.valueOf((int) d);
        return String.format("%.2f", d);
    }
}
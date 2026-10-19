package com.schoollevel2.utils;

import de.tr7zw.changeme.nbtapi.NBTItem;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ItemUtil {

    private static final String NBT_TAG = "SchoolLevel2_ItemData";

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
    public static ItemStack applyRPGStats(ItemStack item, String statKey, double amount, int reqLevel) {
        if (item == null || item.getType().isAir()) return null;
        
        ItemStack clone = item.clone();
        NBTItem nbt = new NBTItem(clone);

        // Lưu data ẩn
        nbt.setDouble(NBT_TAG + "_" + statKey, nbt.getDouble(NBT_TAG + "_" + statKey) + amount);
        if (reqLevel > 0) {
            int currentReq = nbt.getInteger(NBT_TAG + "_req_level");
            nbt.setInteger(NBT_TAG + "_req_level", Math.max(currentReq, reqLevel));
        }

        nbt.applyNBT(clone);

        // Build lại Lore RPG
        return rebuildLore(clone);
    }

    private static ItemStack rebuildLore(ItemStack item) {
        NBTItem nbt = new NBTItem(item);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        List<Component> lore = meta.lore() != null ? new ArrayList<>(meta.lore()) : new ArrayList<>();

        // Xoá lore cũ do plugin tạo (nếu có)
        lore.removeIf(c -> ColorUtil.stripTags(net.kyori.adventure.text.minimessage.MiniMessage.miniMessage().serialize(c)).contains("⚡") ||
                           ColorUtil.stripTags(net.kyori.adventure.text.minimessage.MiniMessage.miniMessage().serialize(c)).contains("✧") ||
                           ColorUtil.stripTags(net.kyori.adventure.text.minimessage.MiniMessage.miniMessage().serialize(c)).contains("✖ Yêu cầu"));

        String[] stats = {"melee", "ranged", "health", "agility", "chakra", "crit"};
        String[] statNames = {"⚔ Sát thương cận chiến", "🏹 Sát thương tầm xa", "❤ Máu tối đa", "💨 Nhanh nhẹn", "🔥 Chakra", "💥 Tỉ lệ chí mạng"};
        String[] colors = {"&c", "&a", "&4", "&b", "&d", "&6"};

        boolean hasStat = false;
        for (int i = 0; i < stats.length; i++) {
            double val = nbt.getDouble(NBT_TAG + "_" + stats[i]);
            if (val > 0) {
                hasStat = true;
                lore.add(ColorUtil.mm(colors[i] + " " + statNames[i] + ": &f+" + fmt(val)));
            }
        }

        int reqLevel = nbt.getInteger(NBT_TAG + "_req_level");
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
        }

        return item;
    }

    public static Map<String, Double> getItemStats(ItemStack item) {
        Map<String, Double> map = new java.util.HashMap<>();
        if (item == null || item.getType().isAir()) return map;
        NBTItem nbt = new NBTItem(item);
        for (String s : new String[]{"melee", "ranged", "health", "agility", "chakra", "crit"}) {
            double val = nbt.getDouble(NBT_TAG + "_" + s);
            if (val != 0) map.put(s, val);
        }
        return map;
    }

    public static int getRequiredLevel(ItemStack item) {
        if (item == null || item.getType().isAir()) return 0;
        NBTItem nbt = new NBTItem(item);
        return nbt.getInteger(NBT_TAG + "_req_level");
    }

    private static String fmt(double d) {
        if (d == Math.floor(d)) return String.valueOf((int) d);
        return String.format("%.2f", d);
    }
}
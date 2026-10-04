package com.schoollevel2.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ItemUtil {

    public static ItemStack make(Material mat, String name, List<String> lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(ColorUtil.mm(name));
            List<Component> loreComp = new ArrayList<>();
            for (String l : lore) loreComp.add(ColorUtil.mm(l));
            meta.lore(loreComp);
            item.setItemMeta(meta);
        }
        return item;
    }

    public static boolean isResetItem(ItemStack item, Material resetMat, String resetName) {
        if (item == null || item.getType() != resetMat) return false;
        if (!item.hasItemMeta()) return false;
        var meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return false;

        Component displayComp = meta.displayName();
        if (displayComp == null) return false;

        String plain;
        try {
            plain = MiniMessage.miniMessage().serialize(displayComp);
            plain = ColorUtil.stripTags(plain);
        } catch (Exception e) {
            plain = displayComp.toString();
        }
        String expected = ColorUtil.stripTags(resetName);
        return plain.trim().equalsIgnoreCase(expected.trim());
    }
}
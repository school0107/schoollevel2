package com.schoollevel2.utils;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ItemBuilder {
    private final ItemStack item;
    private ItemMeta meta;

    public ItemBuilder(Material material) {
        this.item = new ItemStack(material);
        this.meta = item.getItemMeta();
    }

    public ItemBuilder name(String name) {
        if (meta != null) meta.displayName(ColorUtil.mm(name));
        return this;
    }

    public ItemBuilder lore(List<String> lore) {
        if (meta != null) {
            List<Component> comps = new ArrayList<>();
            for (String l : lore) comps.add(ColorUtil.mm(l));
            meta.lore(comps);
        }
        return this;
    }

    public ItemStack build() {
        if (meta != null) item.setItemMeta(meta);
        return item;
    }
}
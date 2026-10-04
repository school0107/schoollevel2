package com.schoollevel2.menu;

import com.schoollevel2.SchoolLevel2;
import com.schoollevel2.data.PlayerData;
import com.schoollevel2.utils.ColorUtil;
import com.schoollevel2.utils.ItemUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.*;

public class StatsMenu {

    private final SchoolLevel2 plugin;
    private static final String[] STAT_KEYS = {"melee", "ranged", "health", "agility"};
    private final int[] SLOTS = {19, 21, 23, 25};

    public StatsMenu(SchoolLevel2 plugin) { this.plugin = plugin; }

    public static class StatsHolder implements InventoryHolder {
        public final UUID uuid;
        public StatsHolder(UUID uuid) { this.uuid = uuid; }
        @Override public Inventory getInventory() { return null; }
    }

    public void open(Player p) {
        PlayerData data = plugin.getDataManager().getOrCreate(p.getUniqueId(), p.getName());
        var cfg = plugin.getConfigManager();
        Component title = ColorUtil.mm(cfg.getCfg().getString("menu.title", "Menu"));
        Inventory inv = Bukkit.createInventory(new StatsHolder(p.getUniqueId()), 54, title);

        Material filler = Material.matchMaterial(cfg.getCfg().getString("menu.filler-material", "GRAY_STAINED_GLASS_PANE"));
        if (filler == null) filler = Material.GRAY_STAINED_GLASS_PANE;
        ItemStack fill = ItemUtil.make(filler, " ", Collections.emptyList());
        for (int i = 0; i < 54; i++) inv.setItem(i, fill);

        String[] displayNames = {
                "<gradient:#FF5555:#AA0000>⚔ Sát thương cận chiến</gradient>",
                "<gradient:#55FF55:#00AA00>🏹 Sát thương tầm xa</gradient>",
                "<gradient:#FF5555:#FFAA00>❤ Máu tối đa</gradient>",
                "<gradient:#55FFFF:#0055FF>💨 Nhanh nhẹn</gradient>"
        };

        for (int i = 0; i < STAT_KEYS.length; i++) {
            String key = STAT_KEYS[i];
            int current = data.getStat(key);
            int max = cfg.getStatMaxPoints(key);
            double per = cfg.getStatPerPoint(key);
            double value = current * per;

            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add("<gray>▸ Cấp hiện tại: <aqua>" + current + "</aqua>/<yellow>" + max + "</yellow>");
            lore.add("<gray>▸ Hiệu ứng: <green>+" + String.format("%.1f", value) + "</green>");
            lore.add("<gray>▸ Điểm khả dụng: <gold>" + data.getPotentialPoints() + "</gold>");
            lore.add("");
            lore.add("<yellow>▶ Click trái <gray>để +1 điểm");
            lore.add("<yellow>▶ Shift + Click trái <gray>để +10 điểm");
            lore.add("<yellow>▶ Click phải <gray>để -1 điểm (hoàn lại)");

            ItemStack item = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) item.getItemMeta();
            if (meta != null) {
                meta.setOwningPlayer(Bukkit.getOfflinePlayer(p.getUniqueId()));
                meta.displayName(ColorUtil.mm(displayNames[i]));
                List<Component> lc = new ArrayList<>();
                for (String s : lore) lc.add(ColorUtil.mm(s));
                meta.lore(lc);
                item.setItemMeta(meta);
            }
            inv.setItem(SLOTS[i], item);
        }

        // Info item
        long needed = cfg.getExpForLevel(data.getLevel());
        List<String> infoLore = List.of(
                "",
                "<gray>▸ Cấp: <aqua>" + data.getLevel() + "</aqua>/<yellow>" + cfg.getMaxLevel() + "</yellow>",
                "<gray>▸ EXP: <green>" + data.getExp() + "</green>/<yellow>" + needed + "</yellow>",
                "<gray>▸ Điểm tiềm năng: <gold>" + data.getPotentialPoints() + "</gold>"
        );
        inv.setItem(4, ItemUtil.make(Material.NETHER_STAR, "<gradient:#FFD700:#FF8C00><bold>✦ THÔNG TIN ✦</bold></gradient>", infoLore));

        p.openInventory(inv);
    }

    public void handleClick(Player p, int slot, boolean shift, boolean right) {
        PlayerData data = plugin.getDataManager().get(p.getUniqueId());
        if (data == null) return;

        for (int i = 0; i < STAT_KEYS.length; i++) {
            if (slot == SLOTS[i]) {
                String key = STAT_KEYS[i];
                if (right) {
                    boolean ok = plugin.getStatsManager().removeStat(p, key, 1);
                    if (!ok) p.sendMessage(plugin.getConfigManager().msg("no-points"));
                } else {
                    int amount = shift ? 10 : 1;
                    boolean ok = plugin.getStatsManager().tryAddStat(p, key, amount);
                    if (!ok) {
                        if (data.getStat(key) >= plugin.getConfigManager().getStatMaxPoints(key))
                            p.sendMessage(plugin.getConfigManager().msg("stats-maxed"));
                        else p.sendMessage(plugin.getConfigManager().msg("no-points"));
                    }
                }
                open(p);
                return;
            }
        }
    }
}

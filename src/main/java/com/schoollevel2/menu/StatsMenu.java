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

        // Info item ở slot 4
        long needed = cfg.getExpForLevel(data.getLevel());
        List<String> infoLore = List.of(
                "",
                "<gray>▸ Cấp hiện tại: <aqua>" + data.getLevel() + "</aqua><gray>/<yellow>" + cfg.getMaxLevel() + "</yellow>",
                "<gray>▸ Kinh nghiệm: <green>" + data.getExp() + "</green><gray>/<yellow>" + needed + "</yellow>",
                "<gray>▸ Điểm tiềm năng: <gold>" + data.getPotentialPoints() + "</gold>",
                "",
                "<dark_gray>Cày quặng để nhận thêm exp!",
                "<dark_gray>Mỗi cấp nhận +" + cfg.getPointsPerLevel() + " điểm."
        );
        inv.setItem(4, ItemUtil.make(Material.NETHER_STAR,
                "<gradient:#FFD700:#FF8C00><bold>✦ THÔNG TIN CỦA BẠN ✦</bold></gradient>",
                infoLore));

        // 4 stat items
        int meleeMax = cfg.getStatMaxPoints("melee");
        int rangedMax = cfg.getStatMaxPoints("ranged");
        int healthMax = cfg.getStatMaxPoints("health");
        int agilityMax = cfg.getStatMaxPoints("agility");

        double meleePer = cfg.getStatPerPoint("melee");
        double rangedPer = cfg.getStatPerPoint("ranged");
        double healthPer = cfg.getStatPerPoint("health");
        double agilityPer = cfg.getStatPerPoint("agility");
        double dodgeCap = cfg.getDodgeCap();

        // MELEE
        addStatItem(inv, p, data, SLOTS[0], "melee",
                "<gradient:#FF5555:#AA0000><bold>⚔ SÁT THƯƠNG CẬN CHIẾN</bold></gradient>",
                meleeMax, meleePer,
                List.of(
                        "<gray>Tăng sát thương khi dùng:",
                        "<white>• Kiếm, Rìu, Cuốc, Xẻng",
                        "<white>• Đánh tay không",
                        "",
                        "<gray>Mỗi điểm: <green>+" + String.format("%.1f", meleePer) + " ❤ sát thương"
                ));

        // RANGED
        addStatItem(inv, p, data, SLOTS[1], "ranged",
                "<gradient:#55FF55:#00AA00><bold>🏹 SÁT THƯƠNG TẦM XA</bold></gradient>",
                rangedMax, rangedPer,
                List.of(
                        "<gray>Tăng sát thương khi dùng:",
                        "<white>• Cung, Nỏ",
                        "<white>• Đinh ba (ném)",
                        "<white>• Trứng, Tuyết, Ngọc Ender",
                        "",
                        "<gray>Mỗi điểm: <green>+" + String.format("%.1f", rangedPer) + " ❤ sát thương"
                ));

        // HEALTH
        addStatItem(inv, p, data, SLOTS[2], "health",
                "<gradient:#FF5555:#FFAA00><bold>❤ MÁU TỐI ĐA</bold></gradient>",
                healthMax, healthPer,
                List.of(
                        "<gray>Tăng lượng máu tối đa:",
                        "<white>• Hiển thị trên thanh máu",
                        "<white>• Áp dụng ngay lập tức",
                        "",
                        "<gray>Mỗi điểm: <green>+" + String.format("%.1f", healthPer) + " ❤ máu",
                        "<gray>(1 ❤ = 2 HP)"
                ));

        // AGILITY
        addStatItem(inv, p, data, SLOTS[3], "agility",
                "<gradient:#55FFFF:#0055FF><bold>💨 NHANH NHẸN</bold></gradient>",
                agilityMax, agilityPer,
                List.of(
                        "<gray>Tăng tốc độ & khả năng né:",
                        "<white>• Tốc độ chạy nhanh hơn",
                        "<white>• Tỉ lệ né đòn vật lý (cận + xa)",
                        "<white>• Cap né: <yellow>" + String.format("%.0f", dodgeCap) + "%</yellow>",
                        "",
                        "<gray>Mỗi điểm: <green>+" + String.format("%.1f", agilityPer) + "% tốc độ",
                        "<gray>Và <green>+" + String.format("%.1f", agilityPer) + "% tỉ lệ né"
                ));

        p.openInventory(inv);
    }

    private void addStatItem(Inventory inv, Player p, PlayerData data, int slot, String key,
                             String displayName, int max, double per, List<String> desc) {
        int current = data.getStat(key);
        double value = current * per;
        String valueStr;
        if (key.equals("health")) valueStr = "+" + String.format("%.1f", value) + " ❤";
        else if (key.equals("agility")) valueStr = "+" + String.format("%.1f", value) + "% speed / dodge";
        else valueStr = "+" + String.format("%.1f", value) + " ❤ dmg";

        List<String> lore = new ArrayList<>();
        lore.addAll(desc);
        lore.add("");
        lore.add("<dark_gray>▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
        lore.add("<gray>Cấp chỉ số: <aqua>" + current + "</aqua> <gray>/ <yellow>" + max);
        lore.add("<gray>Hiệu ứng hiện tại: <green>" + valueStr);
        lore.add("<gray>Điểm khả dụng: <gold>" + data.getPotentialPoints());
        lore.add("<dark_gray>▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
        lore.add("");
        lore.add("<yellow>▶ Click trái <gray>: +1 điểm");
        lore.add("<yellow>▶ Shift + Click trái <gray>: +10 điểm");
        lore.add("<red>✖ Không thể giảm (dùng item Reset)");

        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        if (meta != null) {
            meta.setOwningPlayer(Bukkit.getOfflinePlayer(p.getUniqueId()));
            meta.displayName(ColorUtil.mm(displayName));
            List<Component> lc = new ArrayList<>();
            for (String s : lore) lc.add(ColorUtil.mm(s));
            meta.lore(lc);
            item.setItemMeta(meta);
        }
        inv.setItem(slot, item);
    }

    public void handleClick(Player p, int slot, boolean shift, boolean right) {
        PlayerData data = plugin.getDataManager().get(p.getUniqueId());
        if (data == null) return;

        for (int i = 0; i < STAT_KEYS.length; i++) {
            if (slot == SLOTS[i]) {
                String key = STAT_KEYS[i];
                if (right) {
                    // Chặn hẳn giảm điểm
                    p.sendMessage(ColorUtil.mm("<red>✖ Không thể giảm chỉ số! Dùng <yellow>Reset Item</yellow> để hoàn lại toàn bộ."));
                    return;
                }
                int amount = shift ? 10 : 1;
                boolean ok = plugin.getStatsManager().tryAddStat(p, key, amount);
                if (!ok) {
                    if (data.getStat(key) >= plugin.getConfigManager().getStatMaxPoints(key)) {
                        p.sendMessage(plugin.getConfigManager().msg("stats-maxed"));
                    } else {
                        p.sendMessage(plugin.getConfigManager().msg("no-points"));
                    }
                } else {
                    // Sound feedback
                    try {
                        p.playSound(p.getLocation(), org.bukkit.Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 2f);
                    } catch (Exception ignored) {}
                }
                open(p);
                return;
            }
        }
    }
}
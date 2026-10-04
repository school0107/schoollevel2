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

        long needed = cfg.getExpForLevel(data.getLevel());
        List<String> infoLore = List.of(
                "",
                "<gray>▸ Cấp: <aqua>" + data.getLevel() + "</aqua> <gray>/ <yellow>" + cfg.getMaxLevel(),
                "<gray>▸ Kinh nghiệm: <green>" + data.getExp() + "</green> <gray>/ <yellow>" + needed,
                "<gray>▸ Điểm tiềm năng: <gold>" + data.getPotentialPoints(),
                "",
                "<dark_gray>Cày quặng để nhận thêm exp!",
                "<dark_gray>Mỗi cấp nhận +" + cfg.getPointsPerLevel() + " điểm."
        );
        inv.setItem(4, ItemUtil.make(Material.NETHER_STAR,
                "<gradient:#FFD700:#FF8C00><bold>✦ THÔNG TIN CỦA BẠN ✦</bold></gradient>", infoLore));

        // MELEE
        addStatItem(inv, p, data, SLOTS[0], "melee",
                "<gradient:#FF5555:#AA0000><bold>⚔ SÁT THƯƠNG CẬN CHIẾN</bold></gradient>",
                cfg.getStatMaxPoints("melee"), cfg.getStatPerPoint("melee"),
                List.of(
                        "<gray>Tăng sát thương khi dùng:",
                        "<white> • Kiếm, Rìu, Cuốc, Xẻng",
                        "<white> • Đánh tay không",
                        "",
                        "<gray>Mỗi điểm: <green>+" + fmt(cfg.getStatPerPoint("melee")) + " ❤ sát thương"
                ), "dmg");

        // RANGED
        addStatItem(inv, p, data, SLOTS[1], "ranged",
                "<gradient:#55FF55:#00AA00><bold>🏹 SÁT THƯƠNG TẦM XA</bold></gradient>",
                cfg.getStatMaxPoints("ranged"), cfg.getStatPerPoint("ranged"),
                List.of(
                        "<gray>Tăng sát thương khi dùng:",
                        "<white> • Cung, Nỏ",
                        "<white> • Đinh ba (ném)",
                        "<white> • Trứng, Tuyết, Ngọc Ender",
                        "",
                        "<gray>Mỗi điểm: <green>+" + fmt(cfg.getStatPerPoint("ranged")) + " ❤ sát thương"
                ), "dmg");

        // HEALTH
        addStatItem(inv, p, data, SLOTS[2], "health",
                "<gradient:#FF5555:#FFAA00><bold>❤ MÁU TỐI ĐA</bold></gradient>",
                cfg.getStatMaxPoints("health"), cfg.getStatPerPoint("health"),
                List.of(
                        "<gray>Tăng lượng máu tối đa:",
                        "<white> • Hiển thị trên thanh máu",
                        "<white> • Áp dụng ngay lập tức",
                        "",
                        "<gray>Mỗi điểm: <green>+" + fmt(cfg.getStatPerPoint("health")) + " ❤ máu",
                        "<gray>(1 ❤ = 2 HP)"
                ), "hp");

        // AGILITY (chi tiết)
        int agiMax = cfg.getStatMaxPoints("agility");
        int agiCurrent = data.getStat("agility");
        double speedPct = agiCurrent * cfg.getAgilitySpeedPerPoint();
        double dodgePct = Math.min(agiCurrent * cfg.getAgilityDodgePerPoint(), cfg.getDodgeCap());
        double speedMax = agiMax * cfg.getAgilitySpeedPerPoint();
        double dodgeMax = Math.min(agiMax * cfg.getAgilityDodgePerPoint(), cfg.getDodgeCap());

        List<String> agiLore = new ArrayList<>();
        agiLore.add("<gray>Chỉ số tổng hợp: <aqua>Tốc độ</aqua> + <aqua>Né tránh</aqua>");
        agiLore.add("");
        agiLore.add("<yellow>⚡ TỐC ĐỘ CHẠY");
        agiLore.add("<gray> • Mỗi điểm: <green>+" + fmt(cfg.getAgilitySpeedPerPoint()) + "% tốc độ");
        agiLore.add("<gray> • Hiện tại: <green>+" + fmt(speedPct) + "%");
        agiLore.add("<gray> • Tối đa: <green>+" + fmt(speedMax) + "% (khi full " + agiMax + " điểm)");
        agiLore.add("");
        agiLore.add("<yellow>💨 TỈ LỆ NÉ ĐÒN");
        agiLore.add("<gray> • Mỗi điểm: <green>+" + fmt(cfg.getAgilityDodgePerPoint()) + "% né");
        agiLore.add("<gray> • Hiện tại: <green>" + fmt(dodgePct) + "%");
        agiLore.add("<gray> • Trần né: <red>" + fmt(cfg.getDodgeCap()) + "%</red> <dark_gray>(cap)");
        agiLore.add("<gray> • Áp dụng cho: <white>đòn cận chiến & tầm xa");
        agiLore.add("<gray> • Khi né: hiện hạt mây + huỷ sát thương");
        agiLore.add("");
        agiLore.add("<dark_gray>▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
        agiLore.add("<gray>Cấp chỉ số: <aqua>" + agiCurrent + "</aqua> <gray>/ <yellow>" + agiMax);
        agiLore.add("<gray>Điểm khả dụng: <gold>" + data.getPotentialPoints());
        agiLore.add("<dark_gray>▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
        agiLore.add("");
        agiLore.add("<yellow>▶ Click trái <gray>: +1 điểm");
        agiLore.add("<yellow>▶ Shift + Click trái <gray>: +10 điểm");
        agiLore.add("<red>✖ Không thể giảm (dùng item Reset)");

        inv.setItem(SLOTS[3], buildHead(p,
                "<gradient:#55FFFF:#0055FF><bold>💨 NHANH NHẸN</bold></gradient>", agiLore));

        p.openInventory(inv);
    }

    private void addStatItem(Inventory inv, Player p, PlayerData data, int slot, String key,
                             String displayName, int max, double per, List<String> desc, String type) {
        int current = data.getStat(key);
        double value = current * per;

        String valueStr = switch (type) {
            case "hp" -> "+" + fmt(value) + " ❤";
            case "dmg" -> "+" + fmt(value) + " ❤ sát thương";
            default -> "+" + fmt(value);
        };

        List<String> lore = new ArrayList<>(desc);
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

        inv.setItem(slot, buildHead(p, displayName, lore));
    }

    private ItemStack buildHead(Player p, String displayName, List<String> lore) {
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
        return item;
    }

    private String fmt(double d) {
        if (d == Math.floor(d)) return String.valueOf((int) d);
        return String.format("%.2f", d);
    }

    public void handleClick(Player p, int slot, boolean shift, boolean right) {
        PlayerData data = plugin.getDataManager().get(p.getUniqueId());
        if (data == null) return;

        for (int i = 0; i < STAT_KEYS.length; i++) {
            if (slot == SLOTS[i]) {
                String key = STAT_KEYS[i];
                if (right) {
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
                    try { p.playSound(p.getLocation(), org.bukkit.Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 2f); } catch (Exception ignored) {}
                }
                open(p);
                return;
            }
        }
    }
}
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
                "&7▸ Cấp: &b" + data.getLevel() + " &7/ &e" + cfg.getMaxLevel(),
                "&7▸ Kinh nghiệm: &a" + data.getExp() + " &7/ &e" + needed,
                "&7▸ Điểm tiềm năng: &6" + data.getPotentialPoints(),
                "",
                "&8Cày quặng để nhận thêm exp!",
                "&8Mỗi cấp nhận +" + cfg.getPointsPerLevel() + " điểm."
        );
        inv.setItem(4, ItemUtil.make(Material.NETHER_STAR,
                "&6&l✦ THÔNG TIN CỦA BẠN ✦", infoLore));

        // MELEE
        addStatItem(inv, p, data, SLOTS[0], "melee",
                "&c&l⚔ SÁT THƯƠNG CẬN CHIẾN",
                cfg.getStatMaxPoints("melee"), cfg.getStatPerPoint("melee"),
                List.of(
                        "&7Tăng sát thương khi dùng:",
                        "&f • Kiếm, Rìu, Cuốc, Xẻng",
                        "&f • Đánh tay không",
                        "",
                        "&7Mỗi điểm: &a+" + fmt(cfg.getStatPerPoint("melee")) + " ❤ sát thương"
                ), "dmg");

        // RANGED
        addStatItem(inv, p, data, SLOTS[1], "ranged",
                "&a&l🏹 SÁT THƯƠNG TẦM XA",
                cfg.getStatMaxPoints("ranged"), cfg.getStatPerPoint("ranged"),
                List.of(
                        "&7Tăng sát thương khi dùng:",
                        "&f • Cung, Nỏ",
                        "&f • Đinh ba (ném)",
                        "&f • Trứng, Tuyết, Ngọc Ender",
                        "",
                        "&7Mỗi điểm: &a+" + fmt(cfg.getStatPerPoint("ranged")) + " ❤ sát thương"
                ), "dmg");

        // HEALTH
        addStatItem(inv, p, data, SLOTS[2], "health",
                "&c&l❤ MÁU TỐI ĐA",
                cfg.getStatMaxPoints("health"), cfg.getStatPerPoint("health"),
                List.of(
                        "&7Tăng lượng máu tối đa:",
                        "&f • Hiển thị trên thanh máu",
                        "&f • Áp dụng ngay lập tức",
                        "",
                        "&7Mỗi điểm: &a+" + fmt(cfg.getStatPerPoint("health")) + " ❤ máu",
                        "&7(1 ❤ = 2 HP)"
                ), "hp");

        // AGILITY
        int agiMax = cfg.getStatMaxPoints("agility");
        int agiCurrent = data.getStat("agility");
        double speedPct = agiCurrent * cfg.getAgilitySpeedPerPoint();
        double dodgePct = Math.min(agiCurrent * cfg.getAgilityDodgePerPoint(), cfg.getDodgeCap());
        double speedMax = agiMax * cfg.getAgilitySpeedPerPoint();
        double dodgeMax = Math.min(agiMax * cfg.getAgilityDodgePerPoint(), cfg.getDodgeCap());

        List<String> agiLore = new ArrayList<>();
        agiLore.add("&7Chỉ số tổng hợp: &bTốc độ &7+ &bNé tránh");
        agiLore.add("");
        agiLore.add("&e⚡ TỐC ĐỘ CHẠY");
        agiLore.add("&7 • Mỗi điểm: &a+" + fmt(cfg.getAgilitySpeedPerPoint()) + "% tốc độ");
        agiLore.add("&7 • Hiện tại: &a+" + fmt(speedPct) + "%");
        agiLore.add("&7 • Tối đa: &a+" + fmt(speedMax) + "% (full " + agiMax + " điểm)");
        agiLore.add("");
        agiLore.add("&e💨 TỈ LỆ NÉ ĐÒN");
        agiLore.add("&7 • Mỗi điểm: &a+" + fmt(cfg.getAgilityDodgePerPoint()) + "% né");
        agiLore.add("&7 • Hiện tại: &a" + fmt(dodgePct) + "%");
        agiLore.add("&7 • Trần né: &c" + fmt(cfg.getDodgeCap()) + "% &8(cap)");
        agiLore.add("&7 • Áp dụng: &fđòn cận chiến & tầm xa");
        agiLore.add("&7 • Khi né: hiện hạt mây + huỷ sát thương");
        agiLore.add("");
        agiLore.add("&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
        agiLore.add("&7Cấp chỉ số: &b" + agiCurrent + " &7/ &e" + agiMax);
        agiLore.add("&7Điểm khả dụng: &6" + data.getPotentialPoints());
        agiLore.add("&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
        agiLore.add("");
        agiLore.add("&e▶ Click trái &7: +1 điểm");
        agiLore.add("&e▶ Shift + Click trái &7: +10 điểm");
        agiLore.add("&c✖ Không thể giảm (dùng item Reset)");

        inv.setItem(SLOTS[3], buildHead(p,
                "&b&l💨 NHANH NHẸN", agiLore));

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
        lore.add("&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
        lore.add("&7Cấp chỉ số: &b" + current + " &7/ &e" + max);
        lore.add("&7Hiệu ứng hiện tại: &a" + valueStr);
        lore.add("&7Điểm khả dụng: &6" + data.getPotentialPoints());
        lore.add("&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
        lore.add("");
        lore.add("&e▶ Click trái &7: +1 điểm");
        lore.add("&e▶ Shift + Click trái &7: +10 điểm");
        lore.add("&c✖ Không thể giảm (dùng item Reset)");

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
                    p.sendMessage(ColorUtil.mm("&c✖ Không thể giảm chỉ số! Dùng &eReset Item&c để hoàn lại toàn bộ."));
                    return;
                }
                int amount = shift ? 10 : 1;
                boolean ok = plugin.getStatsManager().tryAddStat(p, key, amount);
                if (!ok) {
                    if (data.getStat(key) >= plugin.getConfigManager().getStatMaxPoints(key)) {
                        plugin.getConfigManager().send(p, "stats-maxed");
                    } else {
                        plugin.getConfigManager().send(p, "no-points");
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
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
    private static final String[] STAT_KEYS = {"melee", "ranged", "health", "agility", "chakra"};
    private final int[] SLOTS = {19, 21, 23, 25, 31};

    public StatsMenu(SchoolLevel2 plugin) { this.plugin = plugin; }

    public static class StatsHolder implements InventoryHolder {
        public final UUID uuid;
        public StatsHolder(UUID uuid) { this.uuid = uuid; }
        @Override public Inventory getInventory() { return null; }
    }

    public void open(Player p) {
        PlayerData data = plugin.getDataManager().getOrCreate(p.getUniqueId(), p.getName());
        var cfg = plugin.getConfigManager();
        var statsMgr = plugin.getStatsManager();

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
        inv.setItem(4, ItemUtil.make(Material.NETHER_STAR, "&6&l✦ THÔNG TIN CỦA BẠN ✦", infoLore));

        // ================= MELEE =================
        double meleeBase = data.getStat("melee") * cfg.getStatPerPoint("melee");
        double meleeTotal = statsMgr.getTotalMelee(p);
        String meleeFormat = buildFormat(meleeTotal, meleeBase, "dmg");
        
        addStatItem(inv, p, data, SLOTS[0], "melee",
                "&c&l⚔ SÁT THƯƠNG CẬN CHIẾN",
                cfg.getStatMaxPoints("melee"),
                List.of("&7Tăng sát thương vũ khí cận chiến.", "", meleeFormat), "dmg");

        // ================= RANGED =================
        double rangedBase = data.getStat("ranged") * cfg.getStatPerPoint("ranged");
        double rangedTotal = statsMgr.getTotalRanged(p);
        String rangedFormat = buildFormat(rangedTotal, rangedBase, "dmg");

        addStatItem(inv, p, data, SLOTS[1], "ranged",
                "&a&l🏹 SÁT THƯƠNG TẦM XA",
                cfg.getStatMaxPoints("ranged"),
                List.of("&7Tăng sát thương cung, nỏ, đinh ba.", "", rangedFormat), "dmg");

        // ================= HEALTH =================
        double hpBase = data.getStat("health") * cfg.getStatPerPoint("health") + data.getStat("chakra") * cfg.getChakraHealthPerPoint();
        double hpTotal = statsMgr.getTotalHealth(p);
        String hpFormat = buildFormat(hpTotal, hpBase, "hp");

        addStatItem(inv, p, data, SLOTS[2], "health",
                "&c&l❤ MÁU TỐI ĐA",
                cfg.getStatMaxPoints("health"),
                List.of("&7Tăng lượng máu tối đa.", "", hpFormat), "hp");

        // ================= AGILITY =================
        int agiMax = cfg.getStatMaxPoints("agility");
        int agiCurrent = data.getStat("agility");
        
        double agiSpeedBase = agiCurrent * cfg.getAgilitySpeedPerPoint() + data.getStat("chakra") * cfg.getChakraSpeedPerPoint();
        double agiSpeedTotal = statsMgr.getTotalSpeedPercent(p);
        
        double agiDodgeBase = agiCurrent * cfg.getAgilityDodgePerPoint() + data.getStat("chakra") * cfg.getChakraDodgePerPoint();
        double agiDodgeTotal = statsMgr.getTotalDodge(p);

        List<String> agiLore = new ArrayList<>();
        agiLore.add("&7Tăng tốc độ chạy và né đòn.");
        agiLore.add("");
        agiLore.add("&e⚡ TỐC ĐỘ CHẠY: &a+" + fmt(agiSpeedTotal) + "%");
        if (agiSpeedTotal > agiSpeedBase) agiLore.add("&8 (Gốc: +" + fmt(agiSpeedBase) + "% | Đồ: +" + fmt(agiSpeedTotal - agiSpeedBase) + "%)");
        agiLore.add("");
        agiLore.add("&e💨 TỈ LỆ NÉ: &a" + fmt(agiDodgeTotal) + "%");
        if (agiDodgeTotal > agiDodgeBase) agiLore.add("&8 (Gốc: +" + fmt(agiDodgeBase) + "% | Đồ: +" + fmt(agiDodgeTotal - agiDodgeBase) + "%)");
        agiLore.add("&7 • Trần né: &c" + fmt(cfg.getDodgeCap()) + "%");
        
        addStatItemCustom(inv, p, data, SLOTS[3], "agility",
                "&b&l💨 NHANH NHẸN", agiMax, agiLore);

        // ================= CHAKRA =================
        int chkMax = cfg.getStatMaxPoints("chakra");
        int chkCurrent = data.getStat("chakra");
        
        double chkCritTotal = statsMgr.getTotalCrit(p);
        double chkHpTotal = statsMgr.getTotalHealth(p);
        double chkSpeedTotal = statsMgr.getTotalSpeedPercent(p);
        double chkDodgeTotal = statsMgr.getTotalDodge(p);

        List<String> chkLore = new ArrayList<>();
        chkLore.add("&7Chỉ số tổng hợp: tăng nhiều thứ");
        chkLore.add("&7cùng lúc, dành cho người cân bằng.");
        chkLore.add("");
        chkLore.add("&d⚡ SÁT THƯƠNG CHÍ MẠNG: &d" + fmt(chkCritTotal) + "% &8(x" + fmt(cfg.getChakraCritMultiplier()) + ")");
        chkLore.add("&7 • Điều kiện: &fnhảy lên đánh");
        chkLore.add("");
        chkLore.add("&c❤ MÁU TỐI ĐA: &a+" + fmt(chkHpTotal) + " HP");
        chkLore.add("");
        chkLore.add("&e⚡ TỐC ĐỘ CHẠY: &a+" + fmt(chkSpeedTotal) + "%");
        chkLore.add("");
        chkLore.add("&b💨 TỈ LỆ NÉ: &a" + fmt(chkDodgeTotal) + "%");
        chkLore.add("&8(Cộng dồn với Agility, cap " + fmt(cfg.getDodgeCap()) + "%)");

        addStatItemCustom(inv, p, data, SLOTS[4], "chakra",
                "&d&l🔥 CHAKRA", chkMax, chkLore);

        p.openInventory(inv);
    }

    private String buildFormat(double total, double base, String type) {
        String strTotal = type.equals("hp") ? "+" + fmt(total) + " ❤" : "+" + fmt(total) + " ❤";
        if (total <= base) return "&7Tổng cộng: &a" + strTotal;
        
        String strBase = type.equals("hp") ? "+" + fmt(base) + " ❤" : "+" + fmt(base) + " ❤";
        String strItem = type.equals("hp") ? "+" + fmt(total - base) + " ❤" : "+" + fmt(total - base) + " ❤";
        return "&7Tổng cộng: &a" + strTotal + "\n&8 (Gốc: " + strBase + " | Đồ: " + strItem + ")";
    }

    private void addStatItem(Inventory inv, Player p, PlayerData data, int slot, String key,
                             String displayName, int max, List<String> desc, String type) {
        int current = data.getStat(key);
        List<String> lore = new ArrayList<>(desc);
        
        lore.add("");
        lore.add("&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
        lore.add("&7Cấp chỉ số: &b" + current + " &7/ &e" + max);
        lore.add("&7Điểm khả dụng: &6" + data.getPotentialPoints());
        lore.add("&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
        lore.add("");
        lore.add("&e▶ Click trái &7: +1 điểm");
        lore.add("&e▶ Shift + Click trái &7: +10 điểm");
        lore.add("&c✖ Không thể giảm (dùng item Reset)");

        inv.setItem(slot, buildHead(p, displayName, lore));
    }

    private void addStatItemCustom(Inventory inv, Player p, PlayerData data, int slot, String key,
                                   String displayName, int max, List<String> desc) {
        int current = data.getStat(key);
        List<String> lore = new ArrayList<>(desc);
        
        lore.add("");
        lore.add("&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
        lore.add("&7Cấp chỉ số: &b" + current + " &7/ &e" + max);
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
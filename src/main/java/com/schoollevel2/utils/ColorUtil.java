package com.schoollevel2.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ColorUtil {

    private static final MiniMessage MM = MiniMessage.miniMessage();
    private static final Pattern HEX = Pattern.compile("&#([A-Fa-f0-9]{6})");

    public static String color(String input) {
        if (input == null) return "";
        Matcher m = HEX.matcher(input);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            m.appendReplacement(sb, "<color:#" + m.group(1) + ">");
        }
        m.appendTail(sb);
        String out = sb.toString();

        // Legacy color codes
        out = out.replace("&0", "<black>").replace("&1", "<dark_blue>")
                .replace("&2", "<dark_green>").replace("&3", "<dark_aqua>")
                .replace("&4", "<dark_red>").replace("&5", "<dark_purple>")
                .replace("&6", "<gold>").replace("&7", "<gray>")
                .replace("&8", "<dark_gray>").replace("&9", "<blue>")
                .replace("&a", "<green>").replace("&b", "<aqua>")
                .replace("&c", "<red>").replace("&d", "<light_purple>")
                .replace("&e", "<yellow>").replace("&f", "<white>")
                .replace("&k", "<obfuscated>").replace("&l", "<bold>")
                .replace("&m", "<strikethrough>").replace("&n", "<underlined>")
                .replace("&o", "<italic>");

        // Fix <reset> — MiniMessage không có <reset>, thay bằng <white> để tránh lỗi
        out = out.replace("<reset>", "<white>");

        return out;
    }

    public static Component mm(String input) {
        try {
            return MM.deserialize(color(input));
        } catch (Exception e) {
            // Fallback nếu parse lỗi
            return Component.text(stripTags(input));
        }
    }

    public static String stripTags(String mini) {
        try {
            return MiniMessage.miniMessage().stripTags(color(mini));
        } catch (Exception e) {
            return mini.replaceAll("<[^>]+>", "");
        }
    }
}
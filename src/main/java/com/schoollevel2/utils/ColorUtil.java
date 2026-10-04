package com.schoollevel2.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ColorUtil {

    private static final MiniMessage MM = MiniMessage.miniMessage();
    private static final Pattern HEX = Pattern.compile("&#([A-Fa-f0-9]{6})");

    /**
     * Convert legacy codes (&a, &c) + hex (&#RRGGBB) + MiniMessage tags to legacy string.
     * For MiniMessage-based API (Paper 1.21), we prefer returning Component.
     */
    public static String color(String input) {
        if (input == null) return "";
        // Convert & hex to mini tags
        Matcher m = HEX.matcher(input);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            m.appendReplacement(sb, "<color:#" + m.group(1) + ">");
        }
        m.appendTail(sb);
        String out = sb.toString();
        // Convert legacy & codes to mini
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
                .replace("&o", "<italic>").replace("&r", "<reset>");
        // Return as MiniMessage string (caller can parse with MM)
        return out;
    }

    public static Component mm(String input) {
        return MM.deserialize(color(input));
    }

    public static String stripTags(String mini) {
        return MiniMessage.miniMessage().stripTags(mini);
    }
}

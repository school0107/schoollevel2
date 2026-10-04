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

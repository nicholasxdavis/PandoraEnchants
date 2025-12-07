package com.pandora.enchants.util;

import org.bukkit.ChatColor;

/**
 * Utility class for consistent color formatting throughout the plugin
 * Uses &7 for normal, &e for special, &6 for values, &c for errors
 */
public class ColorUtil {
    
    public static final String PREFIX = "&e&lPandora &8» &r";
    public static final String HEADER = "&e&l";
    public static final String HIGHLIGHT = "&6";
    public static final String TEXT = "&7";
    public static final String ERROR = "&c";
    
    /**
     * Colorizes a string with Minecraft color codes
     */
    public static String colorize(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }
    
    /**
     * Returns a formatted message with prefix
     */
    public static String format(String message) {
        return colorize(PREFIX + message);
    }
    
    /**
     * Returns an error message with prefix
     */
    public static String error(String message) {
        return colorize(PREFIX + ERROR + message);
    }
    
    /**
     * Returns a highlighted value (gold/orange)
     */
    public static String highlight(String value) {
        return colorize(HIGHLIGHT + value);
    }
    
    /**
     * Returns a header text (bold yellow)
     */
    public static String header(String text) {
        return colorize(HEADER + text);
    }
    
    /**
     * Returns normal text (gray)
     */
    public static String text(String text) {
        return colorize(TEXT + text);
    }
}



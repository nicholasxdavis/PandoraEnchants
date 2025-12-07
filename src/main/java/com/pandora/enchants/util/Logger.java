package com.pandora.enchants.util;

import com.pandora.enchants.PandoraEnchants;
import org.bukkit.Bukkit;

import java.util.logging.Level;

/**
 * Centralized logging utility
 */
public class Logger {
    
    public static void info(String message) {
        Bukkit.getLogger().info("[PandoraEnchants] " + message);
    }
    
    public static void warn(String message) {
        Bukkit.getLogger().warning("[PandoraEnchants] " + message);
    }
    
    public static void error(String message) {
        Bukkit.getLogger().log(Level.SEVERE, "[PandoraEnchants] " + message);
    }
    
    public static void debug(String message) {
        Bukkit.getLogger().info("[PandoraEnchants-DEBUG] " + message);
    }
}



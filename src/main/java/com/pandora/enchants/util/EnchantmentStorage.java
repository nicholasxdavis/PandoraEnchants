package com.pandora.enchants.util;

import com.pandora.enchants.engine.PandoraEnchant;
import com.pandora.enchants.PandoraEnchants;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

/**
 * Lore-based enchantment storage system
 * Stores enchantments in item lore for reliability
 */
public class EnchantmentStorage {
    
    private static final String ENCHANT_PREFIX = ChatColor.GRAY.toString();
    
    /**
     * Gets the custom enchant on an item from lore
     */
    public static PandoraEnchant getEnchant(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasLore()) return null;
        
        List<String> lore = meta.getLore();
        if (lore == null) return null;
        
        for (String line : lore) {
            String stripped = ChatColor.stripColor(line);
            PandoraEnchant enchant = parseEnchantFromLore(stripped);
            if (enchant != null) {
                return enchant;
            }
        }
        
        return null;
    }
    
    /**
     * Gets the level of a custom enchant on an item
     */
    public static int getEnchantLevel(ItemStack item, PandoraEnchant enchant) {
        if (item == null || !item.hasItemMeta() || enchant == null) return 0;
        
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasLore()) return 0;
        
        List<String> lore = meta.getLore();
        if (lore == null) return 0;
        
        for (String line : lore) {
            String stripped = ChatColor.stripColor(line);
            if (stripped.contains(enchant.getName())) {
                // Parse level from lore
                String[] parts = stripped.split(" ");
                if (parts.length > 1) {
                    String levelStr = parts[parts.length - 1];
                    return parseRoman(levelStr);
                }
                return 1;
            }
        }
        
        return 0;
    }
    
    /**
     * Applies a custom enchant to an item via lore
     */
    public static void applyEnchant(ItemStack item, PandoraEnchant enchant, int level) {
        if (item == null || enchant == null || level < 1) return;
        
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        
        List<String> lore = meta.getLore();
        if (lore == null) {
            lore = new ArrayList<>();
        } else {
            lore = new ArrayList<>(lore);
        }
        
        // Remove existing custom enchant lore
        lore.removeIf(line -> {
            String stripped = ChatColor.stripColor(line);
            return parseEnchantFromLore(stripped) != null;
        });
        
        // Add new enchant lore
        String enchantLore = ColorUtil.colorize("&7" + enchant.getName());
        if (enchant.getMaxLevel() > 1) {
            enchantLore += " " + PandoraEnchant.getLevelRoman(level);
        }
        lore.add(0, enchantLore); // Add at top
        
        meta.setLore(lore);
        item.setItemMeta(meta);
        
        // Convert book to enchanted book if needed
        if (item.getType() == org.bukkit.Material.BOOK) {
            item.setType(org.bukkit.Material.ENCHANTED_BOOK);
        }
        
        // Add glow if needed
        addGlow(item);
    }
    
    /**
     * Removes custom enchant from item
     */
    public static void removeEnchant(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return;
        
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasLore()) return;
        
        List<String> lore = meta.getLore();
        if (lore == null) return;
        
        List<String> newLore = new ArrayList<>();
        for (String line : lore) {
            String stripped = ChatColor.stripColor(line);
            if (parseEnchantFromLore(stripped) == null) {
                newLore.add(line); // Keep non-enchant lore
            }
        }
        
        meta.setLore(newLore.isEmpty() ? null : newLore);
        item.setItemMeta(meta);
    }
    
    /**
     * Checks if item has any custom enchant
     */
    public static boolean hasCustomEnchant(ItemStack item) {
        return getEnchant(item) != null;
    }
    
    /**
     * Parses enchant from lore line
     */
    private static PandoraEnchant parseEnchantFromLore(String loreLine) {
        if (loreLine == null) return null;
        
        // Remove color codes and trim
        String cleaned = ChatColor.stripColor(loreLine).trim();
        if (cleaned.isEmpty()) return null;
        
        // Extract enchant name (before roman numeral if present)
        // Split by space and remove roman numerals from end
        String[] parts = cleaned.split(" ");
        if (parts.length == 0) return null;
        
        // Build enchant name (everything except last part if it's a roman numeral)
        StringBuilder nameBuilder = new StringBuilder();
        String possibleRoman = parts.length > 1 ? parts[parts.length - 1].toUpperCase() : null;
        
        // Check if last part is a roman numeral
        boolean lastIsRoman = possibleRoman != null && 
            (possibleRoman.equals("I") || possibleRoman.equals("II") || possibleRoman.equals("III") ||
             possibleRoman.equals("IV") || possibleRoman.equals("V") || possibleRoman.equals("VI") ||
             possibleRoman.equals("VII") || possibleRoman.equals("VIII") || possibleRoman.equals("IX") ||
             possibleRoman.equals("X"));
        
        int nameEnd = lastIsRoman ? parts.length - 1 : parts.length;
        for (int i = 0; i < nameEnd; i++) {
            if (i > 0) nameBuilder.append(" ");
            nameBuilder.append(parts[i]);
        }
        
        String enchantName = nameBuilder.toString().trim();
        if (enchantName.isEmpty()) return null;
        
        // Try to find enchant by name (case-insensitive)
        for (PandoraEnchant enchant : PandoraEnchants.getInstance().getEnchantManager().getAllEnchantments()) {
            // Direct match
            if (enchant.getName().equalsIgnoreCase(enchantName) || 
                enchant.getNamespacedName().equalsIgnoreCase(enchantName.replace(" ", "_"))) {
                return enchant;
            }
            
            // Match against display name or formatted name
            String namespaced = enchantName.toLowerCase().replaceAll(" ", "_");
            if (enchant.getNamespacedName().equalsIgnoreCase(namespaced)) {
                return enchant;
            }
            
            // Check if cleaned line starts with enchant name
            if (cleaned.toLowerCase().startsWith(enchant.getName().toLowerCase())) {
                return enchant;
            }
        }
        
        return null;
    }
    
    /**
     * Parses roman numeral to int
     */
    private static int parseRoman(String roman) {
        Map<String, Integer> romanMap = new HashMap<>();
        romanMap.put("I", 1);
        romanMap.put("II", 2);
        romanMap.put("III", 3);
        romanMap.put("IV", 4);
        romanMap.put("V", 5);
        romanMap.put("VI", 6);
        romanMap.put("VII", 7);
        romanMap.put("VIII", 8);
        romanMap.put("IX", 9);
        romanMap.put("X", 10);
        
        return romanMap.getOrDefault(roman.toUpperCase(), 1);
    }
    
    /**
     * Adds glow effect to item
     */
    private static void addGlow(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        
        // Add a hidden enchantment for glow effect
        try {
            // Use MENDING as it's available in 1.21 - add it with ignore restrictions
            org.bukkit.enchantments.Enchantment glowEnchant = org.bukkit.enchantments.Enchantment.MENDING;
            if (glowEnchant != null) {
                meta.addEnchant(glowEnchant, 1, true); // true = ignore restrictions
                meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
                item.setItemMeta(meta);
            }
        } catch (Exception e) {
            // Ignore - glow is optional, don't break if it fails
        }
    }
}


package com.pandora.enchants.util;

import com.pandora.enchants.engine.PandoraEnchant;
import com.pandora.enchants.engine.PandoraEnchantManager;
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
        if (item == null) {
            Logger.debug("EnchantmentStorage.getEnchant: Item is null");
            return null;
        }
        
        if (!item.hasItemMeta()) {
            Logger.debug("EnchantmentStorage.getEnchant: Item has no meta - " + item.getType());
            return null;
        }
        
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasLore()) {
            Logger.debug("EnchantmentStorage.getEnchant: Item has no lore - " + item.getType());
            return null;
        }
        
        List<String> lore = meta.getLore();
        if (lore == null) {
            Logger.debug("EnchantmentStorage.getEnchant: Lore is null");
            return null;
        }
        
        Logger.debug("EnchantmentStorage.getEnchant: Checking " + lore.size() + " lore lines for " + item.getType());
        
        for (String line : lore) {
            String stripped = ChatColor.stripColor(line);
            Logger.debug("  -> Checking lore line: '" + stripped + "'");
            PandoraEnchant enchant = parseEnchantFromLore(stripped);
            if (enchant != null) {
                Logger.debug("  -> Found enchant: " + enchant.getNamespacedName());
                return enchant;
            }
        }
        
        Logger.debug("EnchantmentStorage.getEnchant: No enchant found in lore for " + item.getType());
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
        
        // Check if this is a godset item (allows multiple enchants)
        boolean isGodset = com.pandora.enchants.util.GodSetManager.isGodsetItem(item);
        
        if (!isGodset) {
            // Remove existing custom enchant lore (one-enchant rule)
            lore.removeIf(line -> {
                String stripped = ChatColor.stripColor(line);
                return parseEnchantFromLore(stripped) != null;
            });
        } else {
            // For godset items, only remove if the same enchant already exists (upgrade)
            String enchantName = enchant.getName();
            lore.removeIf(line -> {
                String stripped = ChatColor.stripColor(line);
                PandoraEnchant existing = parseEnchantFromLore(stripped);
                return existing != null && existing.getNamespacedName().equals(enchant.getNamespacedName());
            });
        }
        
        // Add new enchant lore
        String enchantLore = ColorUtil.colorize("&7" + enchant.getName());
        if (enchant.getMaxLevel() > 1) {
            enchantLore += " " + PandoraEnchant.getLevelRoman(level);
        }
        
        if (isGodset) {
            // For godset items, find the "Enchantments:" line and add after it
            int insertIndex = -1;
            for (int i = 0; i < lore.size(); i++) {
                if (lore.get(i).contains("Enchantments:")) {
                    insertIndex = i + 1;
                    break;
                }
            }
            if (insertIndex >= 0) {
                lore.add(insertIndex, enchantLore);
            } else {
                lore.add(enchantLore);
            }
        } else {
            lore.add(0, enchantLore); // Add at top for normal items
        }
        
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
     * Checks if item has a specific enchant by namespaced name
     */
    public static boolean hasEnchant(ItemStack item, String namespacedName) {
        if (item == null || !item.hasItemMeta() || namespacedName == null) return false;
        
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasLore()) return false;
        
        List<String> lore = meta.getLore();
        if (lore == null) return false;
        
        for (String line : lore) {
            String stripped = ChatColor.stripColor(line);
            PandoraEnchant enchant = parseEnchantFromLore(stripped);
            if (enchant != null && enchant.getNamespacedName().equalsIgnoreCase(namespacedName)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Gets a specific enchant from an item by namespaced name
     */
    public static PandoraEnchant getEnchant(ItemStack item, String namespacedName) {
        if (item == null || !item.hasItemMeta() || namespacedName == null) return null;
        
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasLore()) return null;
        
        List<String> lore = meta.getLore();
        if (lore == null) return null;
        
        for (String line : lore) {
            String stripped = ChatColor.stripColor(line);
            PandoraEnchant enchant = parseEnchantFromLore(stripped);
            if (enchant != null && enchant.getNamespacedName().equalsIgnoreCase(namespacedName)) {
                return enchant;
            }
        }
        
        return null;
    }
    
    /**
     * Gets all enchants from an item
     */
    public static List<PandoraEnchant> getAllEnchants(ItemStack item) {
        List<PandoraEnchant> enchants = new ArrayList<>();
        if (item == null || !item.hasItemMeta()) return enchants;
        
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasLore()) return enchants;
        
        List<String> lore = meta.getLore();
        if (lore == null) return enchants;
        
        for (String line : lore) {
            String stripped = ChatColor.stripColor(line);
            PandoraEnchant enchant = parseEnchantFromLore(stripped);
            if (enchant != null) {
                enchants.add(enchant);
            }
        }
        
        return enchants;
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
        PandoraEnchantManager manager = PandoraEnchants.getInstance().getEnchantManager();
        if (manager == null) {
            Logger.debug("parseEnchantFromLore: EnchantManager is null!");
            return null;
        }
        
        Collection<PandoraEnchant> allEnchants = manager.getAllEnchantments();
        Logger.debug("parseEnchantFromLore: Checking " + allEnchants.size() + " enchants for name: '" + enchantName + "'");
        
        // Normalize the enchant name for matching
        String normalizedEnchantName = enchantName.toLowerCase().replaceAll(" ", "_").replaceAll("-", "_");
        
        for (PandoraEnchant enchant : allEnchants) {
            if (enchant == null) continue;
            
            String enchantDisplayName = enchant.getName().toLowerCase();
            String enchantNamespaced = enchant.getNamespacedName().toLowerCase();
            String normalizedInput = enchantName.toLowerCase();
            
            // Direct match on display name
            if (enchantDisplayName.equals(normalizedInput)) {
                Logger.debug("  -> Display name match: " + enchant.getNamespacedName());
                return enchant;
            }
            
            // Match against namespaced name
            if (enchantNamespaced.equals(normalizedEnchantName)) {
                Logger.debug("  -> Namespaced match: " + enchant.getNamespacedName());
                return enchant;
            }
            
            // Match display name with spaces converted
            if (enchantDisplayName.replace(" ", "_").equals(normalizedEnchantName)) {
                Logger.debug("  -> Converted display match: " + enchant.getNamespacedName());
                return enchant;
            }
            
            // Check if input starts with enchant display name (for cases with roman numerals)
            if (normalizedInput.startsWith(enchantDisplayName) || 
                normalizedInput.startsWith(enchantNamespaced)) {
                Logger.debug("  -> Starts with match: " + enchant.getNamespacedName());
                return enchant;
            }
            
            // Check if enchant name is contained in input (fuzzy match)
            if (normalizedInput.contains(enchantDisplayName) || 
                normalizedInput.contains(enchantNamespaced) ||
                enchantDisplayName.contains(normalizedInput) ||
                enchantNamespaced.contains(normalizedEnchantName)) {
                Logger.debug("  -> Contains match: " + enchant.getNamespacedName());
                return enchant;
            }
        }
        
        Logger.debug("parseEnchantFromLore: No match found for: '" + enchantName + "'");
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


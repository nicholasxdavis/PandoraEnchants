package com.pandora.enchants.util;

import com.pandora.enchants.util.ConfigManager;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

/**
 * Utility class for item-related operations
 */
public class ItemUtil {
    
    /**
     * Checks if an item can have a custom enchant applied
     * Returns false if item already has a custom enchant (one enchant per item restriction)
     * Also checks material restrictions and world settings
     */
    public static boolean canApplyCustomEnchant(ItemStack item, Enchantment enchantment) {
        // Use lore-based check instead
        return canApplyCustomEnchant(item);
    }
    
    /**
     * Checks if an item can have a custom enchant applied (no specific enchant needed)
     */
    public static boolean canApplyCustomEnchant(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return false;
        }
        
        // Check material restrictions
        if (!ConfigManager.isMaterialAllowed(item.getType())) {
            return false;
        }
        
        // GODSET ITEMS: Allow multiple enchants (bypass one-enchant rule)
        if (com.pandora.enchants.util.GodSetManager.isGodsetItem(item)) {
            return true; // Godset items can have unlimited custom enchants
        }
        
        // Check if item already has ANY custom enchant
        // This enforces the one-enchant-per-item rule
        if (!ConfigManager.isOneEnchantRuleEnabled()) {
            // If rule is disabled, allow multiple (not recommended)
            return true;
        }
        
        // Check lore-based storage first
        if (com.pandora.enchants.util.EnchantmentStorage.hasCustomEnchant(item)) {
            return false;
        }
        
        // Also check enchantments map
        for (Enchantment existingEnch : item.getEnchantments().keySet()) {
            if (isCustomEnchantment(existingEnch)) {
                return false; // Already has a custom enchant
            }
        }
        
        return true;
    }
    
    /**
     * Overload that accepts PandoraEnchant directly
     */
    public static boolean canApplyCustomEnchant(ItemStack item, com.pandora.enchants.engine.PandoraEnchant enchant) {
        return canApplyCustomEnchant(item);
    }
    
    /**
     * Checks if item can have custom enchant in given world
     */
    public static boolean canApplyInWorld(World world) {
        if (world == null) return false;
        return ConfigManager.isWorldEnabled(world.getName());
    }
    
    /**
     * Checks if an enchantment is a custom enchantment
     */
    public static boolean isCustomEnchantment(Enchantment enchantment) {
        if (enchantment == null) return false;
        
        String key = enchantment.getKey().getKey();
        // Custom enchantments use our namespace
        return key.startsWith("pandora_") || enchantment.getKey().getNamespace().equals("pandora");
    }
    
    /**
     * Checks if item has a custom enchant (lore-based)
     */
    public static boolean hasCustomEnchant(ItemStack item) {
        return com.pandora.enchants.util.EnchantmentStorage.hasCustomEnchant(item);
    }
    
    /**
     * Gets the custom enchant on an item (if any) - uses lore-based storage
     */
    public static org.bukkit.enchantments.Enchantment getCustomEnchant(ItemStack item) {
        // Use lore-based storage instead
        com.pandora.enchants.engine.PandoraEnchant enchant = 
            com.pandora.enchants.util.EnchantmentStorage.getEnchant(item);
        if (enchant != null && enchant.getBukkitEnchantment() != null) {
            return enchant.getBukkitEnchantment();
        }
        return null;
    }
    
    /**
     * Removes all custom enchantments from an item
     */
    public static void removeCustomEnchants(ItemStack item) {
        if (item == null) return;
        
        // Use lore-based removal
        com.pandora.enchants.util.EnchantmentStorage.removeEnchant(item);
        
        // Also remove from enchantments map if present
        java.util.Set<Enchantment> toRemove = new java.util.HashSet<>();
        for (Enchantment enchant : item.getEnchantments().keySet()) {
            if (isCustomEnchantment(enchant)) {
                toRemove.add(enchant);
            }
        }
        
        for (Enchantment enchant : toRemove) {
            item.removeEnchantment(enchant);
        }
    }
    
    /**
     * Gets the item type string for an item (for enchant compatibility checking)
     */
    public static String getItemType(ItemStack item) {
        if (item == null) return null;
        
        Material type = item.getType();
        String typeName = type.name().toLowerCase();
        
        // Weapons
        if (typeName.contains("sword") || typeName.contains("axe")) {
            return "weapon";
        }
        
        // Armor
        if (typeName.contains("helmet")) {
            return "armor_head";
        }
        if (typeName.contains("chestplate")) {
            return "armor_torso";
        }
        if (typeName.contains("leggings")) {
            return "armor_legs";
        }
        if (typeName.contains("boots")) {
            return "armor_feet";
        }
        
        // Tools
        if (typeName.contains("pickaxe") || typeName.contains("shovel") || 
            typeName.contains("hoe") || typeName.contains("axe")) {
            return "tool";
        }
        
        // Bows
        if (typeName.contains("bow")) {
            return "bow";
        }
        if (typeName.contains("crossbow")) {
            return "crossbow";
        }
        
        // Trident
        if (typeName.contains("trident")) {
            return "trident";
        }
        
        // Fishing rod
        if (typeName.contains("fishing_rod")) {
            return "fishing_rod";
        }
        
        return null;
    }
}


package com.pandora.enchants.util;

import com.pandora.enchants.PandoraEnchants;
import com.pandora.enchants.engine.PandoraEnchant;
import com.pandora.enchants.engine.PandoraEnchantManager;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Manages godset creation - allows multiple enchants on godset items
 */
public class GodSetManager {
    
    private static final String GODSET_TAG = "GODSET";
    
    /**
     * Creates a godset based on tier
     */
    public static GodSet createGodSet(String tier) {
        PandoraEnchantManager enchantManager = PandoraEnchants.getInstance().getEnchantManager();
        
        switch (tier.toLowerCase()) {
            case "iron":
                return createIronGodSet(enchantManager);
            case "diamond":
                return createDiamondGodSet(enchantManager);
            case "netherite":
                return createNetheriteGodSet(enchantManager);
            default:
                return null;
        }
    }
    
    private static GodSet createIronGodSet(PandoraEnchantManager enchantManager) {
        GodSet set = new GodSet();
        
        // Helmet - Clarity, Night Vision
        set.helmet = createGodsetItem(Material.IRON_HELMET, "Iron God Helmet", 
                Arrays.asList("clarity", "night_vision"), enchantManager);
        
        // Chestplate - Shield, Tank, Regeneration
        set.chestplate = createGodsetItem(Material.IRON_CHESTPLATE, "Iron God Chestplate",
                Arrays.asList("shield", "tank", "regeneration"), enchantManager);
        
        // Leggings - Reflex, Gears
        set.leggings = createGodsetItem(Material.IRON_LEGGINGS, "Iron God Leggings",
                Arrays.asList("reflex", "gears"), enchantManager);
        
        // Boots - Water Walker, Feather Fall, Double Jump
        set.boots = createGodsetItem(Material.IRON_BOOTS, "Iron God Boots",
                Arrays.asList("water_walker", "feather_fall", "double_jump"), enchantManager);
        
        // Sword - Lifesteal, Execute, Critical
        set.sword = createGodsetItem(Material.IRON_SWORD, "Iron God Sword",
                Arrays.asList("lifesteal", "execute", "critical"), enchantManager);
        
        // Bow - Explosive, Pierce, Multi Shot
        set.bow = createGodsetItem(Material.BOW, "Iron God Bow",
                Arrays.asList("explosive", "pierce", "multi_shot"), enchantManager);
        
        return set;
    }
    
    private static GodSet createDiamondGodSet(PandoraEnchantManager enchantManager) {
        GodSet set = new GodSet();
        
        // Helmet - Clarity, Night Vision, Absorption
        set.helmet = createGodsetItem(Material.DIAMOND_HELMET, "Diamond God Helmet",
                Arrays.asList("clarity", "night_vision", "absorption"), enchantManager);
        
        // Chestplate - Shield, Tank, Regeneration, Immortal
        set.chestplate = createGodsetItem(Material.DIAMOND_CHESTPLATE, "Diamond God Chestplate",
                Arrays.asList("shield", "tank", "regeneration", "immortal"), enchantManager);
        
        // Leggings - Reflex, Gears, Thorns
        set.leggings = createGodsetItem(Material.DIAMOND_LEGGINGS, "Diamond God Leggings",
                Arrays.asList("reflex", "gears", "thorns"), enchantManager);
        
        // Boots - Water Walker, Feather Fall, Double Jump
        set.boots = createGodsetItem(Material.DIAMOND_BOOTS, "Diamond God Boots",
                Arrays.asList("water_walker", "feather_fall", "double_jump"), enchantManager);
        
        // Sword - Lifesteal, Execute, Critical, Bleed, Rage
        set.sword = createGodsetItem(Material.DIAMOND_SWORD, "Diamond God Sword",
                Arrays.asList("lifesteal", "execute", "critical", "bleed", "rage"), enchantManager);
        
        // Bow - Explosive, Pierce, Multi Shot, Homing
        set.bow = createGodsetItem(Material.BOW, "Diamond God Bow",
                Arrays.asList("explosive", "pierce", "multi_shot", "homing"), enchantManager);
        
        return set;
    }
    
    private static GodSet createNetheriteGodSet(PandoraEnchantManager enchantManager) {
        GodSet set = new GodSet();
        
        // Helmet - Clarity, Night Vision, Absorption, Auto Feed
        set.helmet = createGodsetItem(Material.NETHERITE_HELMET, "Netherite God Helmet",
                Arrays.asList("clarity", "night_vision", "absorption", "auto_feed"), enchantManager);
        
        // Chestplate - Shield, Tank, Regeneration, Immortal, Fire Resistance
        set.chestplate = createGodsetItem(Material.NETHERITE_CHESTPLATE, "Netherite God Chestplate",
                Arrays.asList("shield", "tank", "regeneration", "immortal", "fire_resistance"), enchantManager);
        
        // Leggings - Reflex, Gears, Thorns
        set.leggings = createGodsetItem(Material.NETHERITE_LEGGINGS, "Netherite God Leggings",
                Arrays.asList("reflex", "gears", "thorns"), enchantManager);
        
        // Boots - Water Walker, Feather Fall, Double Jump
        set.boots = createGodsetItem(Material.NETHERITE_BOOTS, "Netherite God Boots",
                Arrays.asList("water_walker", "feather_fall", "double_jump"), enchantManager);
        
        // Sword - Lifesteal, Execute Plus, Critical, Bleed, Rage, Vampire, Lightning
        set.sword = createGodsetItem(Material.NETHERITE_SWORD, "Netherite God Sword",
                Arrays.asList("lifesteal", "execute_plus", "critical", "bleed", "rage", "vampire", "lightning"), enchantManager);
        
        // Bow - Explosive, Pierce, Multi Shot, Homing, Poison Arrow
        set.bow = createGodsetItem(Material.BOW, "Netherite God Bow",
                Arrays.asList("explosive", "pierce", "multi_shot", "homing", "poison_arrow"), enchantManager);
        
        return set;
    }
    
    private static ItemStack createGodsetItem(Material material, String displayName, 
                                             List<String> enchantNames, PandoraEnchantManager enchantManager) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;
        
        // Set display name
        meta.setDisplayName(ColorUtil.colorize("&6&l" + displayName));
        
        // Create lore
        List<String> lore = new ArrayList<>();
        lore.add(ColorUtil.colorize("&7&m--------------------------------"));
        lore.add(ColorUtil.colorize("&6&lGODSET ITEM"));
        lore.add(ColorUtil.colorize("&7&m--------------------------------"));
        lore.add("");
        lore.add(ColorUtil.colorize("&6&lEnchantments:"));
        
        // Apply all enchants
        for (String enchantName : enchantNames) {
            PandoraEnchant enchant = enchantManager.getEnchantment(enchantName);
            if (enchant != null) {
                int level = enchant.getMaxLevel();
                String levelStr = enchant.getMaxLevel() > 1 ? " " + PandoraEnchant.getLevelRoman(level) : "";
                lore.add(ColorUtil.colorize("&7• &e" + enchant.getName() + levelStr));
                
                // Apply enchant using special godset method (bypasses one-enchant rule)
                applyGodsetEnchant(item, enchant, level);
            }
        }
        
        lore.add("");
        lore.add(ColorUtil.colorize("&7This item can have multiple"));
        lore.add(ColorUtil.colorize("&7custom enchantments!"));
        lore.add(ColorUtil.colorize("&7&m--------------------------------"));
        
        meta.setLore(lore);
        
        // Add max durability and unbreakable
        meta.setUnbreakable(true);
        
        // Add vanilla enchants for extra power
        if (material.name().contains("SWORD")) {
            meta.addEnchant(Enchantment.SHARPNESS, 5, true);
            meta.addEnchant(Enchantment.UNBREAKING, 3, true);
            meta.addEnchant(Enchantment.MENDING, 1, true);
        } else if (material.name().contains("BOW")) {
            meta.addEnchant(Enchantment.POWER, 5, true);
            meta.addEnchant(Enchantment.UNBREAKING, 3, true);
            meta.addEnchant(Enchantment.MENDING, 1, true);
            meta.addEnchant(Enchantment.INFINITY, 1, true);
        } else if (material.name().contains("HELMET") || material.name().contains("CHESTPLATE") || 
                   material.name().contains("LEGGINGS") || material.name().contains("BOOTS")) {
            meta.addEnchant(Enchantment.PROTECTION, 4, true);
            meta.addEnchant(Enchantment.UNBREAKING, 3, true);
            meta.addEnchant(Enchantment.MENDING, 1, true);
        }
        
        item.setItemMeta(meta);
        return item;
    }
    
    /**
     * Applies enchant to godset item (bypasses one-enchant rule)
     */
    private static void applyGodsetEnchant(ItemStack item, PandoraEnchant enchant, int level) {
        if (item == null || enchant == null) return;
        
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        
        List<String> lore = meta.getLore();
        if (lore == null) {
            lore = new ArrayList<>();
        } else {
            lore = new ArrayList<>(lore);
        }
        
        // Add enchant to lore (godset items can have multiple)
        String enchantLore = ColorUtil.colorize("&7" + enchant.getName());
        if (enchant.getMaxLevel() > 1) {
            enchantLore += " " + PandoraEnchant.getLevelRoman(level);
        }
        
        // Insert after the "Enchantments:" line
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
        
        meta.setLore(lore);
        item.setItemMeta(meta);
        
        // Add glow
        try {
            meta.addEnchant(Enchantment.MENDING, 1, true);
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
            item.setItemMeta(meta);
        } catch (Exception e) {
            // Ignore
        }
    }
    
    /**
     * Checks if an item is a godset item
     */
    public static boolean isGodsetItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        
        // Check lore for godset indicator
        if (meta.hasLore()) {
            List<String> lore = meta.getLore();
            if (lore != null) {
                for (String line : lore) {
                    if (line.contains("GODSET ITEM") || line.contains("GODSET")) {
                        return true;
                    }
                }
            }
        }
        
        // Also check display name
        if (meta.hasDisplayName()) {
            String displayName = meta.getDisplayName();
            if (displayName.contains("God") || displayName.contains("GODSET")) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * GodSet container class
     */
    public static class GodSet {
        public ItemStack helmet;
        public ItemStack chestplate;
        public ItemStack leggings;
        public ItemStack boots;
        public ItemStack sword;
        public ItemStack bow;
    }
}


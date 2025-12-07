package com.pandora.enchants.engine;

import com.pandora.enchants.PandoraEnchants;
import com.pandora.enchants.util.ItemUtil;
import org.bukkit.Bukkit;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

/**
 * Core class for Pandora custom enchantments
 * Enforces one-enchant-per-item restriction
 */
public class PandoraEnchant extends PandoraEnchantRecord {
    
    private static final String[] ROMAN_NUMERALS = {"I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X"};
    private static final Map<String, PandoraEnchant> enchantments = new HashMap<>();
    
    private Enchantment bukkitEnchantment;
    private boolean newlyRegistered = false;
    
    public PandoraEnchant(String name, PandoraEnchantDefinition definition) {
        super(formatName(name), definition);
        
        this.bukkitEnchantment = getEnchantmentByName(this.getNamespacedName());
        if (PandoraEnchants.getInstance().getEnchantmentManager() != null) {
            if (this.bukkitEnchantment == null) {
                this.bukkitEnchantment = PandoraEnchants.getInstance()
                        .getEnchantmentManager()
                        .registerEnchantment(this);
                newlyRegistered = true;
            }
        }
        
        enchantments.put(this.namespacedName, this);
    }
    
    /**
     * Gets the Bukkit enchantment instance
     */
    public Enchantment getBukkitEnchantment() {
        return bukkitEnchantment;
    }
    
    /**
     * Checks if item can have this enchant applied (respects one-enchant rule)
     */
    public boolean canApply(ItemStack item) {
        if (item == null) return false;
        
        // Check one-enchant-per-item restriction
        if (!ItemUtil.canApplyCustomEnchant(item)) {
            return false;
        }
        
        // Additional validation could go here (item type, etc.)
        return true;
    }
    
    /**
     * Gets the enchantment by namespaced name
     */
    public static PandoraEnchant get(String name) {
        String namespaced = name.toLowerCase().replaceAll(" ", "_");
        if (!enchantments.containsKey(namespaced)) {
            throw new IllegalArgumentException(name + " enchantment does not exist");
        }
        return enchantments.get(namespaced);
    }
    
    /**
     * Gets all registered enchantments
     */
    public static Collection<PandoraEnchant> getAll() {
        return enchantments.values();
    }
    
    /**
     * Gets roman numeral for level
     */
    public static String getLevelRoman(int level) {
        if (level < 1 || level > ROMAN_NUMERALS.length) {
            return String.valueOf(level);
        }
        return ROMAN_NUMERALS[level - 1];
    }
    
    /**
     * Formats a name (capitalizes first letter of each word)
     */
    private static String formatName(String name) {
        String[] words = name.replaceAll("_", " ").split(" ");
        StringBuilder result = new StringBuilder();
        
        for (int i = 0; i < words.length; i++) {
            if (i > 0) result.append(" ");
            if (words[i].length() > 0) {
                result.append(words[i].substring(0, 1).toUpperCase())
                      .append(words[i].substring(1).toLowerCase());
            }
        }
        
        return result.toString();
    }
    
    /**
     * Gets Bukkit enchantment by name
     */
    private Enchantment getEnchantmentByName(String name) {
        org.bukkit.NamespacedKey key = new org.bukkit.NamespacedKey("pandora", name);
        return Enchantment.getByKey(key);
    }
    
    /**
     * Checks if enchantment is newly registered
     */
    public boolean isNewlyRegistered() {
        return newlyRegistered;
    }
}


package com.pandora.enchants.util;

import com.pandora.enchants.PandoraEnchants;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Powerful configuration manager with validation and easy access
 */
public class ConfigManager {
    
    private static FileConfiguration config;
    
    public static void load() {
        config = PandoraEnchants.getInstance().getConfig();
        validateConfig();
    }
    
    /**
     * Validates and fixes configuration
     */
    private static void validateConfig() {
        // Validate and set defaults for missing values
        if (config.get("enchant_table.base_chance") == null) {
            config.set("enchant_table.base_chance", 15.0);
        }
        
        // Validate ranges
        double baseChance = config.getDouble("enchant_table.base_chance");
        if (baseChance < 0 || baseChance > 100) {
            Logger.warn("enchant_table.base_chance must be between 0 and 100! Setting to 15.0");
            config.set("enchant_table.base_chance", 15.0);
        }
        
        // Save if we made changes
        PandoraEnchants.getInstance().saveConfig();
    }
    
    // ============================================
    // ENCHANT TABLE GETTERS
    // ============================================
    
    public static boolean isEnchantTableEnabled() {
        return config.getBoolean("enchant_table.enabled", true);
    }
    
    public static double getEnchantTableBaseChance() {
        return config.getDouble("enchant_table.base_chance", 15.0);
    }
    
    public static int getMinLevelRequired() {
        return config.getInt("enchant_table.minimum_level_required", 0);
    }
    
    public static int getMaxLevelFromTable() {
        return config.getInt("enchant_table.max_level_from_table", 0);
    }
    
    public static boolean shouldReplaceVanilla() {
        return config.getBoolean("enchant_table.replace_vanilla", false);
    }
    
    public static double getItemMultiplier(String itemType) {
        return config.getDouble("enchant_table.item_multipliers." + itemType.toLowerCase(), 100.0);
    }
    
    // ============================================
    // ANVIL GETTERS
    // ============================================
    
    public static boolean isAnvilEnabled() {
        return config.getBoolean("anvil.enabled", true);
    }
    
    public static boolean allowLevelUpgrade() {
        return config.getBoolean("anvil.allow_level_upgrade", true);
    }
    
    public static int getMaxAnvilUses() {
        return config.getInt("anvil.max_uses", 0);
    }
    
    public static double getCostMultiplier() {
        return config.getDouble("anvil.cost_multiplier", 1.0);
    }
    
    // ============================================
    // ONE ENCHANT RULE GETTERS
    // ============================================
    
    public static boolean isOneEnchantRuleEnabled() {
        return config.getBoolean("one_enchant_rule.enabled", true);
    }
    
    public static String getOnSecondEnchantAction() {
        return config.getString("one_enchant_rule.on_second_enchant", "block");
    }
    
    public static String getConflictMessage() {
        return config.getString("one_enchant_rule.conflict_message", 
                "&cThis item already has &6%old%&c! One enchant per item only.");
    }
    
    // ============================================
    // ITEM RESTRICTIONS GETTERS
    // ============================================
    
    public static Set<Material> getBlacklistedMaterials() {
        List<String> materials = config.getStringList("item_restrictions.blacklisted_materials");
        Set<Material> result = new HashSet<>();
        for (String matName : materials) {
            try {
                result.add(Material.valueOf(matName.toUpperCase()));
            } catch (IllegalArgumentException e) {
                Logger.warn("Invalid material in blacklist: " + matName);
            }
        }
        return result;
    }
    
    public static Set<Material> getWhitelistedMaterials() {
        List<String> materials = config.getStringList("item_restrictions.whitelisted_materials");
        Set<Material> result = new HashSet<>();
        for (String matName : materials) {
            try {
                result.add(Material.valueOf(matName.toUpperCase()));
            } catch (IllegalArgumentException e) {
                Logger.warn("Invalid material in whitelist: " + matName);
            }
        }
        return result;
    }
    
    public static boolean isMaterialAllowed(Material material) {
        Set<Material> blacklist = getBlacklistedMaterials();
        Set<Material> whitelist = getWhitelistedMaterials();
        
        if (!whitelist.isEmpty()) {
            return whitelist.contains(material);
        }
        
        return !blacklist.contains(material);
    }
    
    // ============================================
    // WORLD SETTINGS GETTERS
    // ============================================
    
    public static List<String> getDisabledWorlds() {
        return config.getStringList("world_settings.disabled_worlds");
    }
    
    public static List<String> getEnabledWorlds() {
        return config.getStringList("world_settings.enabled_worlds");
    }
    
    public static boolean isWorldEnabled(String worldName) {
        List<String> disabled = getDisabledWorlds();
        List<String> enabled = getEnabledWorlds();
        
        if (disabled.contains(worldName)) {
            return false;
        }
        
        if (enabled.isEmpty()) {
            return true;
        }
        
        return enabled.contains(worldName);
    }
    
    // ============================================
    // MESSAGING GETTERS
    // ============================================
    
    public static boolean useActionBar() {
        return config.getBoolean("messaging.use_action_bar", true);
    }
    
    public static boolean useTitles() {
        return config.getBoolean("messaging.use_titles", false);
    }
    
    public static int getTitleDuration() {
        return config.getInt("messaging.title_duration", 60);
    }
    
    public static String getEnchantSound() {
        return config.getString("messaging.enchant_sound", "ENTITY_PLAYER_LEVELUP:1.0:1.2");
    }
    
    public static String getRemoveSound() {
        return config.getString("messaging.remove_sound", "");
    }
    
    // ============================================
    // PERFORMANCE GETTERS
    // ============================================
    
    public static boolean cacheEnchantments() {
        return config.getBoolean("performance.cache_enchantments", true);
    }
    
    public static int getCacheDuration() {
        return config.getInt("performance.cache_duration", 0);
    }
    
    public static boolean asyncLoad() {
        return config.getBoolean("performance.async_load", true);
    }
    
    // ============================================
    // OTHER GETTERS
    // ============================================
    
    public static boolean isVerbose() {
        return config.getBoolean("verbose", false);
    }
    
    public static boolean allowUnsafeEnchantments() {
        return config.getBoolean("allow_unsafe_enchantments", false);
    }
    
    public static FileConfiguration getConfig() {
        return config;
    }
}



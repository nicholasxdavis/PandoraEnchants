package com.pandora.enchants.engine;

import com.pandora.enchants.util.FileManager;
import com.pandora.enchants.util.Logger;
import org.bukkit.configuration.ConfigurationSection;

import java.util.*;

/**
 * Builder for creating PandoraEnchant instances from configuration
 */
public class PandoraEnchantBuilder {
    
    private final String namespacedName;
    private ConfigurationSection config;
    private boolean error = false;
    
    public PandoraEnchantBuilder(String name) {
        this.namespacedName = name.toLowerCase().replaceAll(" ", "_");
        this.config = FileManager.getConfig("enchantments.yml")
                .getConfigurationSection(this.namespacedName);
    }
    
    /**
     * Builds a PandoraEnchant from configuration
     */
    public PandoraEnchant build() {
        if (config == null) {
            Logger.error("Enchantment '" + namespacedName + "' not found in enchantments.yml");
            return null;
        }
        
        if (!config.getBoolean("enabled", true)) {
            Logger.debug("Enchantment '" + namespacedName + "' is disabled");
            return null;
        }
        
        try {
            PandoraEnchantDefinition definition = buildDefinition();
            if (definition == null) {
                return null;
            }
            
            PandoraEnchant enchant = new PandoraEnchant(namespacedName, definition);
            return enchant;
        } catch (Exception e) {
            Logger.error("Error building enchantment '" + namespacedName + "': " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    private PandoraEnchantDefinition buildDefinition() {
        ConfigurationSection defSection = config.getConfigurationSection("definition");
        if (defSection == null) {
            Logger.error("No definition section found for '" + namespacedName + "'");
            return null;
        }
        
        // Parse basic properties
        int maxLevel = defSection.getInt("max_level", 1);
        if (maxLevel <= 0) maxLevel = 1;
        
        int weight = defSection.getInt("enchanting_table.weight", 5);
        if (weight <= 0) weight = 5;
        
        boolean needPermission = defSection.getBoolean("needs_permission", false);
        
        // Parse costs
        ConfigurationSection tableSection = defSection.getConfigurationSection("enchanting_table");
        PandoraEnchantDefinition.EnchantCost minCost = new PandoraEnchantDefinition.EnchantCost(
                tableSection != null ? tableSection.getInt("min_cost_base", 1) : 1,
                tableSection != null ? tableSection.getInt("min_cost_incr", 5) : 5
        );
        PandoraEnchantDefinition.EnchantCost maxCost = new PandoraEnchantDefinition.EnchantCost(
                tableSection != null ? tableSection.getInt("max_cost_base", 10) : 10,
                tableSection != null ? tableSection.getInt("max_cost_incr", 5) : 5
        );
        
        int anvilCost = defSection.getInt("anvil_cost", 2);
        
        // Parse item sets
        Set<String> supportedItems = new HashSet<>(defSection.getStringList("supported"));
        Set<String> primaryItems = new HashSet<>(defSection.getStringList("primary"));
        
        // Parse conflicts
        Set<String> conflicts = new HashSet<>(defSection.getStringList("conflicts_with"));
        
        // Parse tags
        Map<String, Boolean> tags = new HashMap<>();
        ConfigurationSection tagsSection = defSection.getConfigurationSection("tags");
        if (tagsSection != null) {
            for (String key : tagsSection.getKeys(false)) {
                tags.put(key, tagsSection.getBoolean(key, false));
            }
        }
        
        // Parse chances
        double destroyChance = defSection.getDouble("destroy_item_chance", 0.0);
        double removeChance = defSection.getDouble("remove_enchantment_chance", 0.0);
        
        return new PandoraEnchantDefinition(
                supportedItems,
                primaryItems,
                weight,
                maxLevel,
                needPermission,
                minCost,
                maxCost,
                anvilCost,
                conflicts,
                tags,
                destroyChance,
                removeChance
        );
    }
}



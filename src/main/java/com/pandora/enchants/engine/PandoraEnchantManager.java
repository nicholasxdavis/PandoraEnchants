package com.pandora.enchants.engine;

import com.pandora.enchants.PandoraEnchants;
import com.pandora.enchants.util.FileManager;
import com.pandora.enchants.util.Logger;
import org.bukkit.configuration.ConfigurationSection;

import java.util.*;

/**
 * Manages all Pandora enchantments
 */
public class PandoraEnchantManager {
    
    private final Map<String, PandoraEnchant> enchantments = new HashMap<>();
    
    /**
     * Loads all enchantments from configuration
     */
    public void loadEnchantments() {
        Logger.info("Loading Pandora enchantments...");
        
        PandoraEnchants plugin = PandoraEnchants.getInstance();
        if (plugin.getEnchantmentManager() == null) {
            Logger.error("Enchantment manager not initialized!");
            return;
        }
        
        plugin.getEnchantmentManager().unfreezeRegistry();
        
        // Load enchantments from config
        ConfigurationSection enchantsSection = FileManager.getConfig("enchantments.yml")
                .getConfigurationSection("");
        
        if (enchantsSection != null) {
            for (String enchantName : enchantsSection.getKeys(false)) {
                try {
                    PandoraEnchantBuilder builder = new PandoraEnchantBuilder(enchantName);
                    PandoraEnchant enchant = builder.build();
                    
                    if (enchant != null) {
                        enchantments.put(enchant.getNamespacedName(), enchant);
                        
                        // Add exclusives if newly registered
                        if (enchant.isNewlyRegistered()) {
                            plugin.getEnchantmentManager().addExclusives(
                                    enchant.getNamespacedName(),
                                    enchant.getConflictingEnchantments()
                            );
                        }
                    }
                } catch (Exception e) {
                    Logger.error("Error loading enchantment '" + enchantName + "': " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
        
        // Add tags for all enchantments
        for (PandoraEnchant enchant : enchantments.values()) {
            plugin.getEnchantmentManager().addTagsOnReload(enchant);
        }
        
        plugin.getEnchantmentManager().freezeRegistry();
        
        Logger.info("Loaded " + enchantments.size() + " enchantment(s)!");
    }
    
    /**
     * Gets an enchantment by name
     */
    public PandoraEnchant getEnchantment(String name) {
        String namespaced = name.toLowerCase().replaceAll(" ", "_");
        return enchantments.get(namespaced);
    }
    
    /**
     * Gets all enchantments
     */
    public Collection<PandoraEnchant> getAllEnchantments() {
        return enchantments.values();
    }
    
    /**
     * Reloads all enchantments
     */
    public void reload() {
        enchantments.clear();
        FileManager.reloadConfig("enchantments.yml");
        loadEnchantments();
    }
}


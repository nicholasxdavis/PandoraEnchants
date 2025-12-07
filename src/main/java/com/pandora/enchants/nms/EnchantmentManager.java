package com.pandora.enchants.nms;

import com.pandora.enchants.engine.PandoraEnchant;
import org.bukkit.enchantments.Enchantment;

/**
 * Interface for NMS enchantment management
 */
public interface EnchantmentManager {
    
    /**
     * Unfreezes the registry to allow enchantment registration
     */
    void unfreezeRegistry();
    
    /**
     * Freezes the registry after enchantment registration
     */
    void freezeRegistry();
    
    /**
     * Registers a custom enchantment
     */
    Enchantment registerEnchantment(PandoraEnchant enchant);
    
    /**
     * Adds exclusives (conflicting enchantments)
     */
    void addExclusives(String enchantId, java.util.Set<String> exclusives);
    
    /**
     * Adds tags on reload
     */
    void addTagsOnReload(PandoraEnchant enchant);
    
    /**
     * Cleanup on plugin disable
     */
    default void cleanup() {
        // Override if needed
    }
}



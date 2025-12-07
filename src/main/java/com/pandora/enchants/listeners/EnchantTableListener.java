package com.pandora.enchants.listeners;

import com.pandora.enchants.engine.PandoraEnchant;
import com.pandora.enchants.engine.PandoraEnchantManager;
import com.pandora.enchants.PandoraEnchants;
import com.pandora.enchants.util.ConfigManager;
import com.pandora.enchants.util.ItemUtil;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

/**
 * Listener for enchant table integration
 * Allows custom enchantments to be obtained from the enchant table
 */
public class EnchantTableListener implements Listener {
    
    private final PandoraEnchantManager enchantManager;
    
    public EnchantTableListener() {
        this.enchantManager = PandoraEnchants.getInstance().getEnchantManager();
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onEnchant(EnchantItemEvent event) {
        // Check if enchant table is enabled
        if (!ConfigManager.isEnchantTableEnabled()) {
            return;
        }
        
        // Check world restriction
        if (!ItemUtil.canApplyInWorld(event.getEnchanter().getWorld())) {
            return;
        }
        
        // Check minimum level requirement
        int minLevel = ConfigManager.getMinLevelRequired();
        if (minLevel > 0 && event.getExpLevelCost() < minLevel) {
            return;
        }
        
        // Check if item already has a custom enchant (one-enchant rule)
        ItemStack item = event.getItem();
        if (!ItemUtil.canApplyCustomEnchant(item)) {
            // Item already has a custom enchant, skip
            return;
        }
        
        // Calculate chance to get a custom enchant based on weight
        List<PandoraEnchant> availableEnchants = getAvailableEnchants(item);
        if (availableEnchants.isEmpty()) {
            return;
        }
        
        // Apply base chance modifier
        double baseChance = ConfigManager.getEnchantTableBaseChance();
        Random random = new Random();
        if (random.nextDouble() * 100 > baseChance) {
            return; // Failed base chance roll
        }
        
        // Apply item type multiplier
        String itemType = getItemType(item);
        double multiplier = ConfigManager.getItemMultiplier(itemType) / 100.0;
        if (random.nextDouble() > multiplier) {
            return; // Failed multiplier roll
        }
        
        // Calculate total weight
        int totalWeight = availableEnchants.stream()
                .mapToInt(PandoraEnchant::getEnchantmentTableWeight)
                .sum();
        
        if (totalWeight <= 0) {
            return;
        }
        
        // Random chance based on weights
        int roll = random.nextInt(totalWeight);
        
        int currentWeight = 0;
        for (PandoraEnchant enchant : availableEnchants) {
            currentWeight += enchant.getEnchantmentTableWeight();
            if (roll < currentWeight) {
                // Determine level
                int level = 1;
                int maxLevelFromTable = ConfigManager.getMaxLevelFromTable();
                if (maxLevelFromTable > 0) {
                    level = Math.min(enchant.getMaxLevel(), maxLevelFromTable);
                } else {
                    level = enchant.getMaxLevel();
                }
                
                // Apply this enchant
                applyEnchant(item, enchant, level);
                return;
            }
        }
    }
    
    /**
     * Gets item type for multiplier calculation
     */
    private String getItemType(ItemStack item) {
        Material type = item.getType();
        
        if (type.name().contains("SWORD") || type.name().contains("AXE")) {
            return "weapon";
        } else if (type.name().contains("HELMET") || type.name().contains("CHESTPLATE") || 
                   type.name().contains("LEGGINGS") || type.name().contains("BOOTS")) {
            return "armor";
        } else if (type.name().contains("PICKAXE") || type.name().contains("SHOVEL") || 
                   type.name().contains("HOE")) {
            return "tool";
        } else if (type.name().equals("BOW")) {
            return "bow";
        } else if (type.name().equals("BOOK") || type.name().equals("ENCHANTED_BOOK")) {
            return "book";
        }
        
        return "other";
    }
    
    /**
     * Gets available enchantments for an item
     */
    private List<PandoraEnchant> getAvailableEnchants(ItemStack item) {
        List<PandoraEnchant> available = new ArrayList<>();
        
        for (PandoraEnchant enchant : enchantManager.getAllEnchantments()) {
            // Check if enchant can be in enchanting table
            if (!enchant.getTags().getOrDefault("in_enchanting_table", false)) {
                continue;
            }
            
            // Check if enchant can be applied to this item
            if (enchant.canApply(item)) {
                available.add(enchant);
            }
        }
        
        return available;
    }
    
    /**
     * Applies an enchantment to an item
     */
    private void applyEnchant(ItemStack item, PandoraEnchant enchant, int level) {
        if (item == null || enchant == null) return;
        
        // Remove any existing custom enchants (enforcing one-enchant rule)
        ItemUtil.removeCustomEnchants(item);
        
        // Add the new enchant using lore-based storage
        com.pandora.enchants.util.EnchantmentStorage.applyEnchant(item, enchant, level);
    }
}


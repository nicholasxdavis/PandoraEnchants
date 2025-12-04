package com.pandora.enchants.listeners;

import com.pandora.enchants.engine.PandoraEnchant;
import com.pandora.enchants.util.ConfigManager;
import com.pandora.enchants.util.ColorUtil;
import com.pandora.enchants.util.EnchantmentStorage;
import com.pandora.enchants.util.ItemUtil;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Enhanced listener for anvil operations with book support
 */
public class AnvilEnchantListener implements Listener {
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onPrepareAnvil(PrepareAnvilEvent event) {
        AnvilInventory inventory = event.getInventory();
        ItemStack firstItem = inventory.getItem(0);
        ItemStack secondItem = inventory.getItem(1);
        ItemStack result = event.getResult();
        
        if (firstItem == null) {
            return;
        }
        
        // Handle book application to items
        boolean secondIsBook = secondItem != null && 
            (secondItem.getType() == Material.BOOK || secondItem.getType() == Material.ENCHANTED_BOOK);
        
        if (secondIsBook) {
            PandoraEnchant bookEnchant = EnchantmentStorage.getEnchant(secondItem);
            
            if (bookEnchant != null) {
                // Book has custom enchant - handle application
                PandoraEnchant firstEnch = EnchantmentStorage.getEnchant(firstItem);
                
                // Check one-enchant rule (books bypass this)
                boolean firstIsBook = firstItem.getType() == Material.BOOK || 
                                     firstItem.getType() == Material.ENCHANTED_BOOK;
                
                if (!firstIsBook && firstEnch != null && !firstEnch.equals(bookEnchant)) {
                    // Trying to add different enchant to item that already has one
                    event.setResult(null);
                    if (!inventory.getViewers().isEmpty()) {
                        org.bukkit.entity.HumanEntity viewer = inventory.getViewers().get(0);
                        if (viewer instanceof org.bukkit.entity.Player) {
                            ((org.bukkit.entity.Player) viewer).sendMessage(
                                ColorUtil.error("Item already has a custom enchant! One enchant per item only.")
                            );
                        }
                    }
                    return;
                }
                
                // Allow book to be applied - Minecraft will handle result
                // We'll intercept the final result in InventoryClickEvent
                return;
            }
        }
        
        // Standard one-enchant-per-item protection
        if (!ConfigManager.isAnvilEnabled() || !ConfigManager.isOneEnchantRuleEnabled()) {
            return;
        }
        
        // Check if first item has a custom enchant
        PandoraEnchant firstEnch = EnchantmentStorage.getEnchant(firstItem);
        
        // If second item has a different custom enchant, prevent merge
        if (secondItem != null && !secondIsBook) {
            PandoraEnchant secondEnch = EnchantmentStorage.getEnchant(secondItem);
            
            if (firstEnch != null && secondEnch != null && !firstEnch.equals(secondEnch)) {
                // Two different custom enchants - block this
                event.setResult(null);
                if (inventory.getViewers().isEmpty()) return;
                
                org.bukkit.entity.HumanEntity viewer = inventory.getViewers().get(0);
                if (viewer instanceof org.bukkit.entity.Player) {
                    String message = ConfigManager.getConflictMessage()
                            .replace("%old%", firstEnch.getName())
                            .replace("%new%", secondEnch.getName());
                    
                    ((org.bukkit.entity.Player) viewer).sendMessage(ColorUtil.colorize(message));
                }
                return;
            }
        }
        
        // Check result item for multiple custom enchants
        if (result != null) {
            PandoraEnchant resultEnch = EnchantmentStorage.getEnchant(result);
            
            if (firstEnch != null && resultEnch != null && !firstEnch.equals(resultEnch)) {
                event.setResult(null);
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onAnvilClick(InventoryClickEvent event) {
        if (!(event.getInventory() instanceof AnvilInventory)) {
            return;
        }
        
        // Only handle result slot clicks
        if (event.getSlot() != 2) {
            return;
        }
        
        AnvilInventory inventory = (AnvilInventory) event.getInventory();
        ItemStack firstItem = inventory.getItem(0);
        ItemStack secondItem = inventory.getItem(1);
        ItemStack result = inventory.getItem(2);
        
        if (firstItem == null || result == null) {
            return;
        }
        
        // Check if second item was a custom enchant book
        boolean secondIsBook = secondItem != null && 
            (secondItem.getType() == Material.BOOK || secondItem.getType() == Material.ENCHANTED_BOOK);
        
        if (secondIsBook) {
            PandoraEnchant bookEnchant = EnchantmentStorage.getEnchant(secondItem);
            
            if (bookEnchant != null) {
                // Apply enchant from book to result
                boolean firstIsBook = firstItem.getType() == Material.BOOK || 
                                     firstItem.getType() == Material.ENCHANTED_BOOK;
                
                // Only apply if first item is not a book (or if it is, replace it)
                if (!firstIsBook) {
                    // Remove any existing custom enchant from result
                    EnchantmentStorage.removeEnchant(result);
                    
                    // Apply enchant from book
                    int level = EnchantmentStorage.getEnchantLevel(secondItem, bookEnchant);
                    EnchantmentStorage.applyEnchant(result, bookEnchant, level);
                    
                    // Update result in anvil
                    inventory.setItem(2, result);
                }
            }
        }
    }
}

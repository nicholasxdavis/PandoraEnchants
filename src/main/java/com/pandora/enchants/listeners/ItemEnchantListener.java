package com.pandora.enchants.listeners;

import com.pandora.enchants.engine.PandoraEnchantManager;
import com.pandora.enchants.PandoraEnchants;
import com.pandora.enchants.util.ColorUtil;
import com.pandora.enchants.util.ItemUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;

/**
 * Listener to enforce one-enchant-per-item restriction
 */
public class ItemEnchantListener implements Listener {
    
    @EventHandler
    public void onAnvilUse(InventoryClickEvent event) {
        if (event.getInventory().getType() != InventoryType.ANVIL) {
            return;
        }
        
        AnvilInventory anvil = (AnvilInventory) event.getInventory();
        ItemStack firstItem = anvil.getItem(0);
        ItemStack secondItem = anvil.getItem(1);
        
        // Check if trying to combine items with custom enchants
        if (firstItem != null && secondItem != null) {
            com.pandora.enchants.engine.PandoraEnchant firstEnch = 
                com.pandora.enchants.util.EnchantmentStorage.getEnchant(firstItem);
            com.pandora.enchants.engine.PandoraEnchant secondEnch = 
                com.pandora.enchants.util.EnchantmentStorage.getEnchant(secondItem);
            
            // Check if result would have multiple custom enchants
            if (firstEnch != null && secondEnch != null && !firstEnch.equals(secondEnch)) {
                if (event.getWhoClicked() instanceof org.bukkit.entity.Player) {
                    org.bukkit.entity.Player player = (org.bukkit.entity.Player) event.getWhoClicked();
                    player.sendMessage(ColorUtil.error("You can only have one custom enchant per item!"));
                }
                event.setCancelled(true);
                return;
            }
        }
    }
}


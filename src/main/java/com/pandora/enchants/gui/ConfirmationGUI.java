package com.pandora.enchants.gui;

import com.pandora.enchants.util.ColorUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

/**
 * Simple confirmation GUI
 */
public class ConfirmationGUI extends GUI {
    
    private final String message;
    private final Runnable onConfirm;
    private final Runnable onCancel;
    
    public ConfirmationGUI(Player player, String message, Runnable onConfirm, Runnable onCancel) {
        super(player, message, 27);
        this.message = message;
        this.onConfirm = onConfirm;
        this.onCancel = onCancel;
    }
    
    @Override
    protected void buildInventory() {
        fillEmptySlots(Material.GRAY_STAINED_GLASS_PANE);
        
        // Message
        setItem(4, createItem(Material.PAPER, message, 
                "&7Choose an option below"));
        
        // Confirm
        setItem(11, createItem(Material.LIME_CONCRETE, "&a&lConfirm", 
                "&7Click to confirm"));
        
        // Cancel
        setItem(15, createItem(Material.RED_CONCRETE, "&c&lCancel", 
                "&7Click to cancel"));
    }
    
    @Override
    protected void onInventoryClick(InventoryClickEvent event) {
        event.setCancelled(true);
        
        int slot = event.getSlot();
        if (slot == 11) {
            player.closeInventory();
            onConfirm.run();
        } else if (slot == 15) {
            player.closeInventory();
            onCancel.run();
        }
    }
}



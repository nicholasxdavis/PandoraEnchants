package com.pandora.enchants.gui;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

/**
 * Global listener for all GUI interactions
 */
public class GUIListener implements Listener {
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        GUI gui = GUI.getOpenGUI(event.getWhoClicked().getUniqueId());
        if (gui != null) {
            gui.onInventoryClick(event);
        }
    }
    
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getPlayer() != null) {
            GUI gui = GUI.getOpenGUI(event.getPlayer().getUniqueId());
            if (gui != null) {
                gui.onInventoryClose(event);
            }
        }
    }
}


package com.pandora.enchants.gui;

import com.pandora.enchants.util.ColorUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * GUI for selecting item types for an enchant
 */
public class ItemTypeSelectorGUI extends GUI {
    
    private final List<String> selectedItems;
    private final Consumer<List<String>> onComplete;
    
    private static final String[] ITEM_TYPES = {
        "weapon", "armor_torso", "armor_legs", "armor_feet", 
        "armor_head", "tool", "bow", "crossbow", "trident", "fishing_rod"
    };
    
    public ItemTypeSelectorGUI(Player player, List<String> selectedItems, Consumer<List<String>> onComplete) {
        super(player, "&e&lSelect Item Types", 45);
        this.selectedItems = new ArrayList<>(selectedItems);
        this.onComplete = onComplete;
    }
    
    @Override
    protected void buildInventory() {
        fillEmptySlots(Material.GRAY_STAINED_GLASS_PANE);
        
        setItem(0, createItem(Material.PAPER, "&e&lSelect Item Types", 
                "&7Click items to toggle selection"));
        
        setItem(8, createItem(Material.BARRIER, "&cBack", "&7Return to creator"));
        
        // Display item types
        int slot = 9;
        for (String itemType : ITEM_TYPES) {
            boolean selected = selectedItems.contains(itemType);
            Material icon = getIconForItemType(itemType);
            
            List<String> lore = new ArrayList<>();
            lore.add("&7Status: " + (selected ? "&aSelected" : "&7Not Selected"));
            lore.add("");
            lore.add("&6&lClick to toggle");
            
            setItem(slot, createItem(
                    selected ? Material.LIME_DYE : icon,
                    (selected ? "&a&l" : "&7") + formatItemType(itemType),
                    lore.toArray(new String[0])));
            slot++;
        }
        
        // Done button
        setItem(40, createItem(Material.LIME_CONCRETE, "&a&lDone", 
                "&7Finish selecting (" + selectedItems.size() + " selected)"));
    }
    
    private Material getIconForItemType(String type) {
        switch (type) {
            case "weapon": return Material.DIAMOND_SWORD;
            case "armor_torso": return Material.DIAMOND_CHESTPLATE;
            case "armor_legs": return Material.DIAMOND_LEGGINGS;
            case "armor_feet": return Material.DIAMOND_BOOTS;
            case "armor_head": return Material.DIAMOND_HELMET;
            case "tool": return Material.DIAMOND_PICKAXE;
            case "bow": return Material.BOW;
            case "crossbow": return Material.CROSSBOW;
            case "trident": return Material.TRIDENT;
            case "fishing_rod": return Material.FISHING_ROD;
            default: return Material.PAPER;
        }
    }
    
    private String formatItemType(String type) {
        return type.replace("_", " ").substring(0, 1).toUpperCase() + 
               type.replace("_", " ").substring(1);
    }
    
    @Override
    protected void onInventoryClick(InventoryClickEvent event) {
        event.setCancelled(true);
        
        int slot = event.getSlot();
        
        if (slot == 8) {
            player.closeInventory();
            onComplete.accept(selectedItems);
            return;
        }
        
        if (slot == 40) {
            player.closeInventory();
            onComplete.accept(selectedItems);
            return;
        }
        
        // Toggle item type selection
        if (slot >= 9 && slot < 9 + ITEM_TYPES.length) {
            int index = slot - 9;
            String itemType = ITEM_TYPES[index];
            
            if (selectedItems.contains(itemType)) {
                selectedItems.remove(itemType);
            } else {
                selectedItems.add(itemType);
            }
            
            buildInventory();
        }
    }
}



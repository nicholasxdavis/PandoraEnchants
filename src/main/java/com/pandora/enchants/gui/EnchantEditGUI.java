package com.pandora.enchants.gui;

import com.pandora.enchants.engine.PandoraEnchant;
import com.pandora.enchants.util.ColorUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * GUI for editing existing enchantments with live preview
 */
public class EnchantEditGUI extends GUI {
    
    private final PandoraEnchant enchant;
    
    public EnchantEditGUI(Player player, PandoraEnchant enchant) {
        super(player, "&e&lEdit: &6" + enchant.getName(), 54);
        this.enchant = enchant;
    }
    
    @Override
    protected void buildInventory() {
        inventory.clear();
        
        // Header
        setItem(0, createItem(Material.ENCHANTED_BOOK, "&e&lEdit Enchant", 
                "&7Editing: &6" + enchant.getName()));
        
        setItem(8, createItem(Material.BARRIER, "&cBack", "&7Return to editor"));
        
        // Preview
        createPreview();
        
        // Edit options
        createEditOptions();
        
        // Fill
        fillEmptySlots(Material.GRAY_STAINED_GLASS_PANE);
    }
    
    private void createPreview() {
        List<String> lore = new ArrayList<>();
        
        lore.add("&7&m═══════════════════════════════");
        lore.add("&e&l" + enchant.getName() + " &6&lLive Preview");
        lore.add("&7&m═══════════════════════════════");
        lore.add("");
        
        lore.add("&6&lCurrent Stats:");
        lore.add(" &7Max Level: &6" + enchant.getMaxLevel());
        lore.add(" &7Weight: &6" + enchant.getEnchantmentTableWeight());
        lore.add(" &7Anvil Cost: &6" + enchant.getAnvilCost());
        lore.add("");
        
        lore.add("&6&lItem Types:");
        enchant.getSupportedItems().forEach(item -> 
            lore.add(" &7• &e" + item.replace("_", " ")));
        lore.add("");
        
        lore.add("&7&m═══════════════════════════════");
        
        setItem(4, createItem(Material.ENCHANTED_BOOK, 
                "&e&l" + enchant.getName(), 
                lore.toArray(new String[0])));
    }
    
    private void createEditOptions() {
        setItem(19, createItem(Material.PAPER, "&6&lView Config", 
                "&7View YAML configuration"));
        
        setItem(21, createItem(Material.WRITABLE_BOOK, "&6&lEdit Properties", 
                "&7Edit enchant properties",
                "&7(Coming soon)"));
        
        setItem(23, createItem(Material.BOOK, "&6&lView Triggers", 
                "&7View trigger configuration"));
        
        setItem(25, createItem(Material.REDSTONE, "&6&lTest Enchant", 
                "&7Test enchant on held item"));
        
        setItem(40, createItem(Material.LIME_CONCRETE, "&a&lSave Changes", 
                "&7Save modifications"));
    }
    
    @Override
    protected void onInventoryClick(InventoryClickEvent event) {
        event.setCancelled(true);
        
        int slot = event.getSlot();
        
        if (slot == 8) {
            player.closeInventory();
            new EnchantEditorGUI(player).open();
            return;
        }
        
        // Handle edit options
        switch (slot) {
            case 19:
                showConfig();
                break;
            case 21:
                player.sendMessage(ColorUtil.format("&7Property editor coming soon!"));
                break;
            case 23:
                player.sendMessage(ColorUtil.format("&7Trigger viewer coming soon!"));
                break;
            case 25:
                testEnchant();
                break;
            case 40:
                player.sendMessage(ColorUtil.format("&7Save functionality coming soon!"));
                break;
        }
    }
    
    private void showConfig() {
        player.closeInventory();
        player.sendMessage(ColorUtil.format("&e&lConfig for: &6" + enchant.getName()));
        player.sendMessage(" ");
        // TODO: Show actual config
        player.sendMessage(ColorUtil.text("Config viewer coming soon!"));
    }
    
    private void testEnchant() {
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType() == Material.AIR) {
            player.sendMessage(ColorUtil.error("Hold an item in your hand!"));
            return;
        }
        
        // Apply enchant for testing using lore-based storage
        com.pandora.enchants.util.EnchantmentStorage.applyEnchant(item, enchant, 1);
        player.sendMessage(ColorUtil.format("&aApplied " + enchant.getName() + " to your item!"));
        player.playSound(player.getLocation(), 
                org.bukkit.Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
    }
}


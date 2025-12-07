package com.pandora.enchants.gui;

import com.pandora.enchants.PandoraEnchants;
import com.pandora.enchants.util.ColorUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Advanced Enchant Creator GUI with live preview
 */
public class EnchantCreatorGUI extends GUI {
    
    // Editor state
    private String enchantName = "new_enchant";
    private int maxLevel = 1;
    private int weight = 20;
    private int anvilCost = 10;
    private List<String> supportedItems = new ArrayList<>();
    private boolean inEnchantTable = true;
    private boolean treasure = false;
    private Map<String, Object> previewData = new HashMap<>();
    
    // GUI slots
    private static final int PREVIEW_SLOT = 4;
    private static final int NAME_SLOT = 10;
    private static final int MAX_LEVEL_SLOT = 11;
    private static final int WEIGHT_SLOT = 12;
    private static final int ANVIL_COST_SLOT = 13;
    private static final int ITEM_TYPES_SLOT = 19;
    private static final int IN_TABLE_SLOT = 20;
    private static final int TREASURE_SLOT = 21;
    private static final int SAVE_SLOT = 40;
    private static final int CANCEL_SLOT = 44;
    
    public EnchantCreatorGUI(Player player) {
        super(player, "&e&lCreate New Enchant", 54);
    }
    
    @Override
    protected void buildInventory() {
        inventory.clear();
        
        // Create header
        setItem(0, createItem(Material.WRITABLE_BOOK, "&e&lEnchant Creator", 
                "&7Create a new custom enchantment",
                "&7with live preview!"));
        
        setItem(8, createItem(Material.BARRIER, "&cCancel", "&7Discard changes and exit"));
        
        // Editor controls
        createEditorControls();
        
        // Preview
        updatePreview();
        
        // Action buttons
        createActionButtons();
        
        // Fill empty slots
        fillEmptySlots(Material.GRAY_STAINED_GLASS_PANE);
    }
    
    private void createEditorControls() {
        // Enchant Name
        List<String> nameLore = new ArrayList<>();
        nameLore.add("&7Current: &e" + enchantName);
        nameLore.add("");
        nameLore.add("&6&lActions:");
        nameLore.add(" &7• Left Click: &eIncrease");
        nameLore.add(" &7• Right Click: &eDecrease");
        nameLore.add(" &7• Shift+Click: &6Edit via chat");
        setItem(NAME_SLOT, createItem(Material.NAME_TAG, "&6&lEnchant Name", 
                nameLore.toArray(new String[0])));
        
        // Max Level
        List<String> levelLore = new ArrayList<>();
        levelLore.add("&7Current: &e" + maxLevel);
        levelLore.add("");
        levelLore.add("&6&lActions:");
        levelLore.add(" &7• Left Click: &e+1 (Max: 10)");
        levelLore.add(" &7• Right Click: &e-1 (Min: 1)");
        setItem(MAX_LEVEL_SLOT, createItem(Material.EXPERIENCE_BOTTLE, "&6&lMax Level", 
                levelLore.toArray(new String[0])));
        
        // Weight
        List<String> weightLore = new ArrayList<>();
        weightLore.add("&7Current: &e" + weight);
        weightLore.add("");
        weightLore.add("&6&lRarity:");
        weightLore.add(" " + getRarityDisplay(weight));
        weightLore.add("");
        weightLore.add("&6&lActions:");
        weightLore.add(" &7• Left Click: &e+5");
        weightLore.add(" &7• Right Click: &e-5");
        weightLore.add(" &7• Shift+Click: &6+/- 1");
        setItem(WEIGHT_SLOT, createItem(Material.GOLD_INGOT, "&6&lTable Weight", 
                weightLore.toArray(new String[0])));
        
        // Anvil Cost
        List<String> costLore = new ArrayList<>();
        costLore.add("&7Current: &e" + anvilCost);
        costLore.add("");
        costLore.add("&6&lActions:");
        costLore.add(" &7• Left Click: &e+1");
        costLore.add(" &7• Right Click: &e-1");
        setItem(ANVIL_COST_SLOT, createItem(Material.ANVIL, "&6&lAnvil Cost", 
                costLore.toArray(new String[0])));
        
        // Item Types
        List<String> itemsLore = new ArrayList<>();
        itemsLore.add("&7Current: &e" + (supportedItems.isEmpty() ? "None" : supportedItems.size() + " types"));
        if (!supportedItems.isEmpty()) {
            itemsLore.add("");
            supportedItems.forEach(item -> itemsLore.add(" &7• &e" + item));
        }
        itemsLore.add("");
        itemsLore.add("&6&lClick to edit item types");
        setItem(ITEM_TYPES_SLOT, createItem(Material.CRAFTING_TABLE, "&6&lSupported Items", 
                itemsLore.toArray(new String[0])));
        
        // In Enchant Table
        setItem(IN_TABLE_SLOT, createItem(
                inEnchantTable ? Material.LIME_DYE : Material.GRAY_DYE,
                inEnchantTable ? "&a&lIn Enchant Table" : "&7In Enchant Table",
                "&7Current: " + (inEnchantTable ? "&aEnabled" : "&cDisabled"),
                "",
                "&6&lClick to toggle"));
        
        // Treasure
        setItem(TREASURE_SLOT, createItem(
                treasure ? Material.EMERALD : Material.GRAY_DYE,
                treasure ? "&a&lTreasure Enchant" : "&7Treasure Enchant",
                "&7Current: " + (treasure ? "&aYes" : "&cNo"),
                "",
                "&6&lClick to toggle"));
    }
    
    private void updatePreview() {
        List<String> previewLore = new ArrayList<>();
        
        previewLore.add("&7&m═══════════════════════════════");
        previewLore.add("&e&l" + enchantName + " &6&lPreview");
        previewLore.add("&7&m═══════════════════════════════");
        previewLore.add("");
        
        previewLore.add("&6&lStats:");
        previewLore.add(" &7Max Level: &6" + maxLevel);
        previewLore.add(" &7Table Weight: &6" + weight);
        previewLore.add(" &7Anvil Cost: &6" + anvilCost);
        previewLore.add("");
        
        previewLore.add("&6&lRarity: " + getRarityDisplay(weight));
        previewLore.add("");
        
        previewLore.add("&6&lItem Types:");
        if (supportedItems.isEmpty()) {
            previewLore.add(" &7None selected");
        } else {
            supportedItems.forEach(item -> 
                previewLore.add(" &7• &e" + formatItemType(item)));
        }
        previewLore.add("");
        
        previewLore.add("&6&lSettings:");
        previewLore.add(" &7Enchant Table: " + (inEnchantTable ? "&aYes" : "&cNo"));
        previewLore.add(" &7Treasure: " + (treasure ? "&aYes" : "&cNo"));
        previewLore.add("");
        
        previewLore.add("&7&m═══════════════════════════════");
        
        ItemStack preview = createItem(Material.ENCHANTED_BOOK, 
                "&e&l" + enchantName + " &6&lPreview", 
                previewLore.toArray(new String[0]));
        
        setItem(PREVIEW_SLOT, preview);
    }
    
    private String getRarityDisplay(int weight) {
        if (weight >= 1 && weight <= 7) return "&6&lLegendary &7(Extremely Rare)";
        if (weight >= 8 && weight <= 14) return "&d&lEpic &7(Very Rare)";
        if (weight >= 15 && weight <= 29) return "&b&lRare &7(Uncommon)";
        if (weight >= 30 && weight <= 49) return "&a&lUncommon &7(Somewhat Common)";
        return "&7&lCommon &7(Frequent)";
    }
    
    private String formatItemType(String type) {
        return type.replace("_", " ").replace("armor", "Armor")
                .replace("weapon", "Weapon").replace("tool", "Tool");
    }
    
    private void createActionButtons() {
        // Save
        setItem(SAVE_SLOT, createItem(Material.LIME_CONCRETE, "&a&lSave Enchant", 
                "&7Save this enchant to config",
                "",
                "&e&lClick to save!"));
        
        // Cancel
        setItem(CANCEL_SLOT, createItem(Material.RED_CONCRETE, "&c&lCancel", 
                "&7Discard changes and exit"));
    }
    
    @Override
    protected void onInventoryClick(InventoryClickEvent event) {
        event.setCancelled(true);
        
        int slot = event.getSlot();
        
        // Cancel
        if (slot == 8 || slot == CANCEL_SLOT) {
            player.closeInventory();
            new EnchantEditorGUI(player).open();
            return;
        }
        
        // Save
        if (slot == SAVE_SLOT) {
            handleSave();
            return;
        }
        
        // Handle editor controls
        switch (slot) {
            case NAME_SLOT:
                handleNameEdit(event);
                break;
            case MAX_LEVEL_SLOT:
                handleMaxLevelEdit(event);
                break;
            case WEIGHT_SLOT:
                handleWeightEdit(event);
                break;
            case ANVIL_COST_SLOT:
                handleAnvilCostEdit(event);
                break;
            case ITEM_TYPES_SLOT:
                handleItemTypesEdit();
                break;
            case IN_TABLE_SLOT:
                inEnchantTable = !inEnchantTable;
                buildInventory();
                break;
            case TREASURE_SLOT:
                treasure = !treasure;
                buildInventory();
                break;
        }
    }
    
    private void handleNameEdit(InventoryClickEvent event) {
        if (event.isShiftClick()) {
            // Open chat input for name
            player.closeInventory();
            player.sendMessage(ColorUtil.format("&eType the enchant name in chat (or 'cancel' to abort):"));
            // TODO: Implement chat input handler
            player.sendMessage(ColorUtil.format("&7Chat input not yet implemented. Use /pe editor to return."));
        }
        buildInventory();
    }
    
    private void handleMaxLevelEdit(InventoryClickEvent event) {
        if (event.isLeftClick()) {
            maxLevel = Math.min(10, maxLevel + 1);
        } else if (event.isRightClick()) {
            maxLevel = Math.max(1, maxLevel - 1);
        }
        updatePreview();
        buildInventory();
    }
    
    private void handleWeightEdit(InventoryClickEvent event) {
        int change = event.isShiftClick() ? 1 : 5;
        if (event.isLeftClick()) {
            weight = Math.min(999, weight + change);
        } else if (event.isRightClick()) {
            weight = Math.max(1, weight - change);
        }
        updatePreview();
        buildInventory();
    }
    
    private void handleAnvilCostEdit(InventoryClickEvent event) {
        if (event.isLeftClick()) {
            anvilCost = Math.min(100, anvilCost + 1);
        } else if (event.isRightClick()) {
            anvilCost = Math.max(1, anvilCost - 1);
        }
        updatePreview();
        buildInventory();
    }
    
    private void handleItemTypesEdit() {
        player.closeInventory();
        new ItemTypeSelectorGUI(player, supportedItems, (selectedItems) -> {
            supportedItems = selectedItems;
            player.closeInventory();
            new EnchantCreatorGUI(player).open();
        }).open();
    }
    
    private void handleSave() {
        // Validate
        if (supportedItems.isEmpty()) {
            player.sendMessage(ColorUtil.error("You must select at least one item type!"));
            return;
        }
        
        // Save to config
        // TODO: Implement actual save to enchantments.yml
        player.sendMessage(ColorUtil.format("&aEnchant saved! (Save to config not yet implemented)"));
        player.closeInventory();
        new EnchantEditorGUI(player).open();
    }
}



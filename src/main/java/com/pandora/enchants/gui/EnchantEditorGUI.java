package com.pandora.enchants.gui;

import com.pandora.enchants.engine.PandoraEnchant;
import com.pandora.enchants.engine.PandoraEnchantManager;
import com.pandora.enchants.PandoraEnchants;
import com.pandora.enchants.util.ColorUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * Advanced Enchant Editor GUI with live preview
 */
public class EnchantEditorGUI extends GUI {
    
    private final PandoraEnchantManager enchantManager;
    private final List<PandoraEnchant> enchantments;
    private int currentPage = 0;
    private static final int ITEMS_PER_PAGE = 28;
    
    // GUI slots
    private static final int PREVIEW_SLOT = 4;
    private static final int PREV_PAGE_SLOT = 45;
    private static final int NEXT_PAGE_SLOT = 53;
    private static final int CREATE_NEW_SLOT = 49;
    private static final int CLOSE_SLOT = 49;
    
    public EnchantEditorGUI(Player player) {
        super(player, "&e&lPandora Enchant Editor", 54);
        this.enchantManager = PandoraEnchants.getInstance().getEnchantManager();
        this.enchantments = new ArrayList<>(enchantManager.getAllEnchantments());
    }
    
    @Override
    protected void buildInventory() {
        inventory.clear();
        
        // Create header
        createHeader();
        
        // Display enchantments for current page
        displayEnchantments();
        
        // Navigation buttons
        createNavigationButtons();
        
        // Fill empty slots
        fillEmptySlots(Material.GRAY_STAINED_GLASS_PANE);
        
        // Preview item
        createPreviewItem();
    }
    
    private void createHeader() {
        setItem(0, createItem(Material.PAPER, "&e&lEnchant Editor", 
                "&7Edit existing enchantments",
                "&7or create new ones!",
                "",
                "&6&lPage: &e" + (currentPage + 1) + " &7/ &e" + getMaxPages()));
        
        setItem(8, createItem(Material.BARRIER, "&cClose", "&7Click to close"));
    }
    
    private void displayEnchantments() {
        int startIndex = currentPage * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, enchantments.size());
        
        int slot = 9; // Start after first row
        for (int i = startIndex; i < endIndex; i++) {
            PandoraEnchant enchant = enchantments.get(i);
            createEnchantItem(enchant, slot);
            slot++;
            
            // Skip preview slot
            if (slot == PREVIEW_SLOT) {
                slot++;
            }
        }
    }
    
    private void createEnchantItem(PandoraEnchant enchant, int slot) {
        Material icon = getEnchantIcon(enchant);
        List<String> lore = new ArrayList<>();
        
        lore.add("&7&m--------------------------------");
        lore.add("&6&lInformation:");
        lore.add(" &7Max Level: &6" + enchant.getMaxLevel());
        lore.add(" &7Weight: &6" + enchant.getEnchantmentTableWeight());
        lore.add(" &7Anvil Cost: &6" + enchant.getAnvilCost());
        lore.add("");
        lore.add("&6&lItem Types:");
        enchant.getSupportedItems().forEach(item -> 
            lore.add(" &7- &e" + item.replace("_", " ")));
        lore.add("");
        lore.add("&6&lTags:");
        enchant.getTags().entrySet().forEach(entry -> 
            lore.add(" &7" + entry.getKey() + ": &e" + entry.getValue()));
        lore.add("");
        lore.add("&6&lActions:");
        lore.add(" &e▶ &7Left Click: &6Edit Enchant");
        lore.add(" &e▶ &7Right Click: &6Live Preview");
        lore.add(" &e▶ &7Shift+Click: &cDelete");
        lore.add("&7&m--------------------------------");
        
        ItemStack item = createItem(icon, "&e&l" + enchant.getName(), lore.toArray(new String[0]));
        
        // Store enchant name in persistent data container for identification
        // Using display name pattern instead of deprecated localized name
        
        setItem(slot, item);
    }
    
    private Material getEnchantIcon(PandoraEnchant enchant) {
        // Determine icon based on item types
        if (enchant.getSupportedItems().stream().anyMatch(s -> s.contains("weapon"))) {
            return Material.DIAMOND_SWORD;
        } else if (enchant.getSupportedItems().stream().anyMatch(s -> s.contains("armor"))) {
            return Material.DIAMOND_CHESTPLATE;
        } else if (enchant.getSupportedItems().stream().anyMatch(s -> s.contains("tool"))) {
            return Material.DIAMOND_PICKAXE;
        } else if (enchant.getSupportedItems().stream().anyMatch(s -> s.contains("bow"))) {
            return Material.BOW;
        }
        return Material.ENCHANTED_BOOK;
    }
    
    private void createNavigationButtons() {
        // Previous page
        if (currentPage > 0) {
            setItem(PREV_PAGE_SLOT, createItem(Material.ARROW, "&6Previous Page", 
                    "&7Go to page " + currentPage));
        }
        
        // Next page
        if (currentPage < getMaxPages() - 1) {
            setItem(NEXT_PAGE_SLOT, createItem(Material.ARROW, "&6Next Page", 
                    "&7Go to page " + (currentPage + 2)));
        }
        
        // Create new enchant
        setItem(CREATE_NEW_SLOT, createItem(Material.WRITABLE_BOOK, "&a&lCreate New Enchant", 
                "&7Click to create a new enchantment!",
                "",
                "&e▶ Opens the enchant creator"));
    }
    
    private void createPreviewItem() {
        setItem(PREVIEW_SLOT, createItem(Material.ENCHANTED_BOOK, "&6&lLive Preview", 
                "&7Select an enchant to preview",
                "",
                "&eRight-click an enchant",
                "&7to see live preview here!"));
    }
    
    private int getMaxPages() {
        return Math.max(1, (enchantments.size() + ITEMS_PER_PAGE - 1) / ITEMS_PER_PAGE);
    }
    
    @Override
    protected void onInventoryClick(InventoryClickEvent event) {
        event.setCancelled(true);
        
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        
        Player clicker = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();
        
        if (clicked == null || clicked.getType() == Material.AIR) {
            return;
        }
        
        int slot = event.getSlot();
        
        // Handle navigation
        if (slot == PREV_PAGE_SLOT && currentPage > 0) {
            currentPage--;
            buildInventory();
            return;
        }
        
        if (slot == NEXT_PAGE_SLOT && currentPage < getMaxPages() - 1) {
            currentPage++;
            buildInventory();
            return;
        }
        
        if (slot == CREATE_NEW_SLOT) {
            clicker.closeInventory();
            new EnchantCreatorGUI(clicker).open();
            return;
        }
        
        if (slot == 8) {
            clicker.closeInventory();
            return;
        }
        
        // Handle enchant clicks - check display name for enchant identifier
        ItemMeta meta = clicked.getItemMeta();
        if (meta != null && meta.hasDisplayName()) {
            String displayName = meta.getDisplayName();
            // Find matching enchant by name
            PandoraEnchant enchant = null;
            for (PandoraEnchant ench : enchantments) {
                if (displayName.contains(ench.getName())) {
                    enchant = ench;
                    break;
                }
            }
            
            if (enchant != null) {
                if (event.isShiftClick()) {
                    // Delete enchant
                    handleDeleteEnchant(clicker, enchant);
                } else if (event.isRightClick()) {
                    // Live preview
                    showLivePreview(enchant);
                } else {
                    // Edit enchant
                    clicker.closeInventory();
                    new EnchantEditGUI(clicker, enchant).open();
                }
            }
        }
    }
    
    private void showLivePreview(PandoraEnchant enchant) {
        List<String> previewLore = new ArrayList<>();
        
        previewLore.add("&7&m═══════════════════════════════");
        previewLore.add("&e&l" + enchant.getName() + " &6Preview");
        previewLore.add("&7&m═══════════════════════════════");
        previewLore.add("");
        
        // Stats preview
        previewLore.add("&6&lStats:");
        previewLore.add(" &7Max Level: &6" + enchant.getMaxLevel());
        previewLore.add(" &7Table Weight: &6" + enchant.getEnchantmentTableWeight());
        previewLore.add(" &7Anvil Cost: &6" + enchant.getAnvilCost());
        previewLore.add("");
        
        // Rarity calculation
        int weight = enchant.getEnchantmentTableWeight();
        String rarity = getRarity(weight);
        previewLore.add("&6&lRarity: " + rarity);
        previewLore.add("");
        
        // Item compatibility
        previewLore.add("&6&lCompatible Items:");
        enchant.getSupportedItems().forEach(item -> 
            previewLore.add(" &7• &e" + formatItemType(item)));
        previewLore.add("");
        
        // Enchant table info
        boolean inTable = enchant.getTags().getOrDefault("in_enchanting_table", false);
        previewLore.add("&6&lEnchant Table:");
        previewLore.add(" &7Available: " + (inTable ? "&aYes" : "&cNo"));
        if (inTable) {
            previewLore.add(" &7Min Cost: &6" + enchant.getMinCost().base());
            previewLore.add(" &7Max Cost: &6" + enchant.getMaxCost().base());
        }
        previewLore.add("");
        
        // Conflicts
        if (!enchant.getConflictingEnchantments().isEmpty()) {
            previewLore.add("&6&lConflicts:");
            enchant.getConflictingEnchantments().forEach(conflict -> 
                previewLore.add(" &7• &c" + conflict));
            previewLore.add("");
        }
        
        previewLore.add("&7&m═══════════════════════════════");
        
        ItemStack preview = createItem(Material.ENCHANTED_BOOK, 
                "&e&l" + enchant.getName() + " &6&lPreview", 
                previewLore.toArray(new String[0]));
        
        setItem(PREVIEW_SLOT, preview);
        
        // Play sound
        player.playSound(player.getLocation(), 
                org.bukkit.Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
    }
    
    private String getRarity(int weight) {
        if (weight >= 1 && weight <= 5) return "&6&lLegendary";
        if (weight >= 6 && weight <= 12) return "&d&lEpic";
        if (weight >= 13 && weight <= 25) return "&b&lRare";
        if (weight >= 35 && weight <= 55) return "&a&lUncommon";
        return "&7&lCommon";
    }
    
    private String formatItemType(String type) {
        return type.replace("_", " ").replace("armor", "Armor")
                .replace("weapon", "Weapon").replace("tool", "Tool");
    }
    
    private void handleDeleteEnchant(Player player, PandoraEnchant enchant) {
        // Open confirmation GUI
        new ConfirmationGUI(player, "&cDelete Enchant?", 
                () -> {
                    // TODO: Implement delete functionality
                    player.sendMessage(ColorUtil.format("&cEnchant deletion not yet implemented in editor."));
                    player.closeInventory();
                    new EnchantEditorGUI(player).open();
                },
                () -> {
                    new EnchantEditorGUI(player).open();
                }).open();
    }
    
    @Override
    protected void onInventoryClose(InventoryCloseEvent event) {
        super.onInventoryClose(event);
    }
}


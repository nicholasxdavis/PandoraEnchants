package com.pandora.enchants.gui;

import com.pandora.enchants.util.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Base GUI system for PandoraEnchants
 */
public abstract class GUI {
    
    protected Inventory inventory;
    protected String title;
    protected int size;
    protected Player player;
    
    protected static final Map<UUID, GUI> openGUIs = new HashMap<>();
    
    public GUI(Player player, String title, int size) {
        this.player = player;
        this.title = ColorUtil.colorize(title);
        this.size = size;
        this.inventory = Bukkit.createInventory(null, size, this.title);
    }
    
    /**
     * Opens the GUI for the player
     */
    public void open() {
        buildInventory();
        player.openInventory(inventory);
        openGUIs.put(player.getUniqueId(), this);
    }
    
    /**
     * Builds the inventory contents (override in subclasses)
     */
    protected abstract void buildInventory();
    
    /**
     * Handles clicks in the inventory
     */
    protected abstract void onInventoryClick(InventoryClickEvent event);
    
    /**
     * Handles inventory close
     */
    protected void onInventoryClose(InventoryCloseEvent event) {
        openGUIs.remove(player.getUniqueId());
    }
    
    /**
     * Creates a GUI item
     */
    protected ItemStack createItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ColorUtil.colorize(name));
            if (lore.length > 0) {
                java.util.List<String> loreList = new java.util.ArrayList<>();
                for (String line : lore) {
                    loreList.add(ColorUtil.colorize(line));
                }
                meta.setLore(loreList);
            }
            item.setItemMeta(meta);
        }
        return item;
    }
    
    /**
     * Creates a GUI item with click handler
     */
    protected ItemStack createClickableItem(Material material, String name, int slot, Consumer<InventoryClickEvent> clickHandler, String... lore) {
        ItemStack item = createItem(material, name, lore);
        setItem(slot, item);
        return item;
    }
    
    /**
     * Sets an item in the inventory
     */
    protected void setItem(int slot, ItemStack item) {
        inventory.setItem(slot, item);
    }
    
    /**
     * Fills empty slots with a filler item
     */
    protected void fillEmptySlots(Material filler) {
        ItemStack fillerItem = createItem(filler, " ");
        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, fillerItem);
            }
        }
    }
    
    /**
     * Gets the open GUI for a player
     */
    public static GUI getOpenGUI(UUID uuid) {
        return openGUIs.get(uuid);
    }
    
    /**
     * Closes the GUI for a player
     */
    public static void closeGUI(UUID uuid) {
        GUI gui = openGUIs.remove(uuid);
        if (gui != null && gui.player.isOnline()) {
            gui.player.closeInventory();
        }
    }
}


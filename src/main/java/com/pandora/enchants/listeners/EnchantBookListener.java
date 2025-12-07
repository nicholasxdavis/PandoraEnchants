package com.pandora.enchants.listeners;

import com.pandora.enchants.engine.PandoraEnchant;
import com.pandora.enchants.util.ColorUtil;
import com.pandora.enchants.util.EnchantmentStorage;
import com.pandora.enchants.util.ItemUtil;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Handles applying enchantments from books to items via right-click
 */
public class EnchantBookListener implements Listener {
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_AIR && 
            event.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        
        Player player = event.getPlayer();
        ItemStack book = event.getItem();
        
        if (book == null || (book.getType() != Material.BOOK && book.getType() != Material.ENCHANTED_BOOK)) {
            return;
        }
        
        PandoraEnchant enchant = EnchantmentStorage.getEnchant(book);
        if (enchant == null) {
            return;
        }
        
        // Check if player is holding an item to apply to
        ItemStack targetItem = player.getInventory().getItemInMainHand();
        if (targetItem == null || targetItem.getType().isAir()) {
            player.sendMessage(ColorUtil.error("Hold an item in your hand to apply the enchant!"));
            return;
        }
        
        // Don't apply to the book itself
        if (targetItem.equals(book)) {
            targetItem = player.getInventory().getItemInOffHand();
            if (targetItem == null || targetItem.getType().isAir() || targetItem.equals(book)) {
                player.sendMessage(ColorUtil.error("Hold an item in your hand to apply the enchant!"));
                return;
            }
        }
        
        // Check if item can have this enchant
        // Godset items can have multiple enchants, so bypass the check for them
        boolean isGodset = com.pandora.enchants.util.GodSetManager.isGodsetItem(targetItem);
        if (!isGodset && !ItemUtil.canApplyCustomEnchant(targetItem)) {
            player.sendMessage(ColorUtil.error("This item already has a custom enchant! One enchant per item only."));
            return;
        }
        
        // For godset items, check if this specific enchant already exists (to upgrade it)
        if (isGodset) {
            PandoraEnchant existingEnchant = EnchantmentStorage.getEnchant(targetItem);
            if (existingEnchant != null && existingEnchant.getNamespacedName().equals(enchant.getNamespacedName())) {
                // Same enchant, will upgrade level
            }
        }
        
        // Check if item type is supported
        String itemType = ItemUtil.getItemType(targetItem);
        if (itemType == null || !enchant.getSupportedItems().contains(itemType)) {
            player.sendMessage(ColorUtil.error("This enchant cannot be applied to " + 
                    (itemType != null ? itemType.replace("_", " ") : "this item type") + "!"));
            player.sendMessage(ColorUtil.text("Supported types: " + 
                    String.join(", ", enchant.getSupportedItems()).replace("_", " ")));
            return;
        }
        
        int level = EnchantmentStorage.getEnchantLevel(book, enchant);
        
        // Remove enchant from book
        EnchantmentStorage.removeEnchant(book);
        
        // Convert book back to regular book if no other enchants
        if (book.getType() == Material.ENCHANTED_BOOK) {
            ItemMeta meta = book.getItemMeta();
            if (meta != null && (!meta.hasLore() || meta.getLore().isEmpty())) {
                book.setType(Material.BOOK);
            }
        }
        
        // Apply enchant to item
        EnchantmentStorage.applyEnchant(targetItem, enchant, level);
        
        // Messages and sound
        player.sendMessage(ColorUtil.format(
                ColorUtil.text("Applied ") + ColorUtil.highlight(enchant.getName() + " " + PandoraEnchant.getLevelRoman(level)) +
                ColorUtil.text(" to your item!")
        ));
        
        player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1.0f, 1.5f);
        
        event.setCancelled(true);
    }
}


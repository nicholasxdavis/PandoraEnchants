package com.pandora.enchants.commands;

import com.pandora.enchants.PandoraEnchants;
import com.pandora.enchants.engine.PandoraEnchant;
import com.pandora.enchants.engine.PandoraEnchantManager;
import com.pandora.enchants.util.ColorUtil;
import com.pandora.enchants.util.ItemUtil;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Main command handler for PandoraEnchants
 */
public class PandoraEnchantCommand implements CommandExecutor, TabCompleter {
    
    private final PandoraEnchantManager enchantManager;
    
    public PandoraEnchantCommand() {
        this.enchantManager = PandoraEnchants.getInstance().getEnchantManager();
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("pandoraenchants.admin")) {
            sender.sendMessage(ColorUtil.error("You don't have permission to use this command!"));
            return true;
        }
        
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "add":
                return handleAdd(sender, args);
            case "remove":
                return handleRemove(sender, args);
            case "info":
                return handleInfo(sender, args);
            case "reload":
                return handleReload(sender);
            case "list":
                return handleList(sender, args);
            case "book":
            case "givebook":
                return handleBook(sender, args);
            case "editor":
            case "edit":
                return handleEditor(sender);
            default:
                sendHelp(sender);
                return true;
        }
    }
    
    private boolean handleAdd(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ColorUtil.error("This command can only be used by players!"));
            return true;
        }
        
        if (args.length < 2) {
            sender.sendMessage(ColorUtil.error("Usage: /pe add <enchant> [level]"));
            return true;
        }
        
        Player player = (Player) sender;
        ItemStack item = player.getInventory().getItemInMainHand();
        
        if (item == null || item.getType().isAir()) {
            player.sendMessage(ColorUtil.error("You must hold an item in your hand!"));
            return true;
        }
        
        String enchantName = args[1];
        PandoraEnchant enchant = enchantManager.getEnchantment(enchantName);
        
        if (enchant == null) {
            player.sendMessage(ColorUtil.error("Enchantment '" + ColorUtil.highlight(enchantName) + "' not found!"));
            return true;
        }
        
        int level = 1;
        if (args.length >= 3) {
            try {
                level = Integer.parseInt(args[2]);
                if (level < 1 || level > enchant.getMaxLevel()) {
                    player.sendMessage(ColorUtil.error("Level must be between 1 and " + enchant.getMaxLevel() + "!"));
                    return true;
                }
            } catch (NumberFormatException e) {
                player.sendMessage(ColorUtil.error("Invalid level: " + args[2]));
                return true;
            }
        }
        
        // Books always allowed, other items check one-enchant rule
        boolean isBook = item.getType() == Material.BOOK || item.getType() == Material.ENCHANTED_BOOK;
        
        if (!isBook) {
            // Check one-enchant-per-item restriction
            if (!ItemUtil.canApplyCustomEnchant(item)) {
                player.sendMessage(ColorUtil.error("This item already has a custom enchant! One enchant per item only."));
                return true;
            }
        }
        
        // Remove any existing custom enchants (safety check)
        ItemUtil.removeCustomEnchants(item);
        
        // Apply enchant using lore-based storage
        com.pandora.enchants.util.EnchantmentStorage.applyEnchant(item, enchant, level);
        
        player.sendMessage(ColorUtil.format(
                ColorUtil.text("Added ") + ColorUtil.highlight(enchant.getName() + " " + PandoraEnchant.getLevelRoman(level))
                + ColorUtil.text(" to your item!")
        ));
        
        // Play sound
        player.playSound(player.getLocation(), 
                org.bukkit.Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1.0f, 1.2f);
        
        return true;
    }
    
    private boolean handleRemove(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ColorUtil.error("This command can only be used by players!"));
            return true;
        }
        
        Player player = (Player) sender;
        ItemStack item = player.getInventory().getItemInMainHand();
        
        if (item == null || item.getType().isAir()) {
            player.sendMessage(ColorUtil.error("You must hold an item in your hand!"));
            return true;
        }
        
        if (!com.pandora.enchants.util.EnchantmentStorage.hasCustomEnchant(item)) {
            player.sendMessage(ColorUtil.error("This item doesn't have a custom enchant!"));
            return true;
        }
        
        com.pandora.enchants.util.EnchantmentStorage.removeEnchant(item);
        player.sendMessage(ColorUtil.format(ColorUtil.text("Removed custom enchant from your item!")));
        
        return true;
    }
    
    private boolean handleInfo(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ColorUtil.error("Usage: /pe info <enchant>"));
            return true;
        }
        
        String enchantName = args[1];
        PandoraEnchant enchant = enchantManager.getEnchantment(enchantName);
        
        if (enchant == null) {
            sender.sendMessage(ColorUtil.error("Enchantment '" + ColorUtil.highlight(enchantName) + "' not found!"));
            return true;
        }
        
        // Send info in pages to avoid long messages
        List<String> infoLines = new ArrayList<>();
        infoLines.add(ColorUtil.header("=== " + enchant.getName() + " ==="));
        infoLines.add("");
        infoLines.add(ColorUtil.text("Max Level: ") + ColorUtil.highlight(String.valueOf(enchant.getMaxLevel())));
        infoLines.add(ColorUtil.text("Weight: ") + ColorUtil.highlight(String.valueOf(enchant.getEnchantmentTableWeight())));
        infoLines.add(ColorUtil.text("Rarity: ") + getRarityDisplay(enchant.getEnchantmentTableWeight()));
        infoLines.add(ColorUtil.text("Anvil Cost: ") + ColorUtil.highlight(String.valueOf(enchant.getAnvilCost())));
        infoLines.add("");
        infoLines.add(ColorUtil.text("In Enchant Table: ") + 
                ColorUtil.highlight(enchant.getTags().getOrDefault("in_enchanting_table", false) ? "Yes" : "No"));
        infoLines.add(ColorUtil.text("Treasure: ") + 
                ColorUtil.highlight(enchant.getTags().getOrDefault("treasure", false) ? "Yes" : "No"));
        infoLines.add("");
        infoLines.add(ColorUtil.text("Supported Items:"));
        for (String itemType : enchant.getSupportedItems()) {
            infoLines.add(ColorUtil.text("  • ") + ColorUtil.highlight(itemType.replace("_", " ")));
        }
        
        if (!enchant.getConflictingEnchantments().isEmpty()) {
            infoLines.add("");
            infoLines.add(ColorUtil.text("Conflicts With:"));
            for (String conflict : enchant.getConflictingEnchantments()) {
                infoLines.add(ColorUtil.text("  • ") + ColorUtil.highlight(conflict));
            }
        }
        
        // Send in pages
        int page = 1;
        if (args.length >= 3) {
            try {
                page = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                page = 1;
            }
        }
        
        int itemsPerPage = 10;
        int totalPages = (int) Math.ceil((double) infoLines.size() / itemsPerPage);
        if (page < 1) page = 1;
        if (page > totalPages) page = totalPages;
        
        int startIndex = (page - 1) * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, infoLines.size());
        
        for (int i = startIndex; i < endIndex; i++) {
            sender.sendMessage(infoLines.get(i));
        }
        
        if (totalPages > 1) {
            sender.sendMessage("");
            sender.sendMessage(ColorUtil.colorize("&7Page &6" + page + " &7/ &6" + totalPages));
            if (page < totalPages) {
                sender.sendMessage(ColorUtil.colorize("&7Type &e/pe info " + enchantName + " " + (page + 1) + " &7for next page"));
            }
            if (page > 1) {
                sender.sendMessage(ColorUtil.colorize("&7Type &e/pe info " + enchantName + " " + (page - 1) + " &7for previous page"));
            }
        }
        
        return true;
    }
    
    private boolean handleReload(CommandSender sender) {
        PandoraEnchants.getInstance().getEnchantManager().reload();
        sender.sendMessage(ColorUtil.format(ColorUtil.text("Reloaded all enchantments!")));
        return true;
    }
    
    private boolean handleList(CommandSender sender, String[] args) {
        List<PandoraEnchant> enchants = new ArrayList<>(enchantManager.getAllEnchantments());
        
        if (enchants.isEmpty()) {
            sender.sendMessage(ColorUtil.format(ColorUtil.text("No enchantments loaded.")));
            return true;
        }
        
        // Parse page number
        int page = 1;
        if (args.length >= 2) {
            try {
                page = Integer.parseInt(args[1]);
                if (page < 1) page = 1;
            } catch (NumberFormatException e) {
                page = 1;
            }
        }
        
        int totalPages = com.pandora.enchants.util.PaginationUtil.getTotalPages(enchants);
        if (page > totalPages) page = totalPages;
        
        // Get page items
        List<PandoraEnchant> pageEnchants = new ArrayList<>();
        int itemsPerPage = 8;
        int startIndex = (page - 1) * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, enchants.size());
        
        for (int i = startIndex; i < endIndex; i++) {
            pageEnchants.add(enchants.get(i));
        }
        
        // Send header
        sender.sendMessage(ColorUtil.colorize("&e&l&m==========&r &e&lPandora Enchantments &e&l&m=========="));
        sender.sendMessage(ColorUtil.colorize("&7Page &6" + page + " &7/ &6" + totalPages));
        sender.sendMessage("");
        
        // Send enchantments
        for (PandoraEnchant enchant : pageEnchants) {
            String rarity = getRarityDisplay(enchant.getEnchantmentTableWeight());
            sender.sendMessage(ColorUtil.text("  • ") + rarity + " " + ColorUtil.highlight(enchant.getName()));
        }
        
        // Send footer
        sender.sendMessage("");
        if (totalPages > 1) {
            if (page < totalPages) {
                sender.sendMessage(ColorUtil.colorize("&7Type &e/pe list " + (page + 1) + " &7for next page"));
            }
            if (page > 1) {
                sender.sendMessage(ColorUtil.colorize("&7Type &e/pe list " + (page - 1) + " &7for previous page"));
            }
        }
        
        return true;
    }
    
    private String getRarityDisplay(int weight) {
        if (weight >= 1 && weight <= 5) return ColorUtil.colorize("&6&l[LEGENDARY]");
        if (weight >= 6 && weight <= 12) return ColorUtil.colorize("&d&l[EPIC]");
        if (weight >= 13 && weight <= 25) return ColorUtil.colorize("&b&l[RARE]");
        if (weight >= 35 && weight <= 55) return ColorUtil.colorize("&a&l[UNCOMMON]");
        return ColorUtil.colorize("&7&l[COMMON]");
    }
    
    private boolean handleBook(CommandSender sender, String[] args) {
        // If no args, show list of all enchants
        if (args.length < 2) {
            sender.sendMessage(ColorUtil.header("=== Enchant Book Spawner ==="));
            sender.sendMessage(ColorUtil.text("Usage: /pe book <enchant> [level] [player]"));
            sender.sendMessage(ColorUtil.text("Usage: /pe book list [page] - List all enchantments"));
            sender.sendMessage("");
            sender.sendMessage(ColorUtil.text("Available enchants: " + enchantManager.getAllEnchantments().size()));
            return true;
        }
        
        // Handle list subcommand
        if (args[1].equalsIgnoreCase("list")) {
            return handleBookList(sender, args);
        }
        
        String enchantName = args[1];
        PandoraEnchant enchant = enchantManager.getEnchantment(enchantName);
        
        if (enchant == null) {
            sender.sendMessage(ColorUtil.error("Enchantment '" + ColorUtil.highlight(enchantName) + "' not found!"));
            sender.sendMessage(ColorUtil.text("Use /pe book list to see all available enchantments."));
            return true;
        }
        
        int level = 1;
        if (args.length >= 3) {
            try {
                level = Integer.parseInt(args[2]);
                if (level < 1 || level > enchant.getMaxLevel()) {
                    sender.sendMessage(ColorUtil.error("Level must be between 1 and " + enchant.getMaxLevel() + "!"));
                    return true;
                }
            } catch (NumberFormatException e) {
                sender.sendMessage(ColorUtil.error("Invalid level: " + args[2]));
                return true;
            }
        }
        
        // Determine target player
        Player target = null;
        if (sender instanceof Player) {
            target = (Player) sender;
        }
        
        if (args.length >= 4) {
            target = org.bukkit.Bukkit.getPlayer(args[3]);
            if (target == null) {
                sender.sendMessage(ColorUtil.error("Player '" + ColorUtil.highlight(args[3]) + "' not found!"));
                return true;
            }
        }
        
        if (target == null) {
            sender.sendMessage(ColorUtil.error("You must be a player or specify a target player!"));
            return true;
        }
        
        // Create enchant book with enhanced lore
        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta meta = book.getItemMeta();
        if (meta != null) {
            List<String> lore = new ArrayList<>();
            String rarity = getRarityDisplay(enchant.getEnchantmentTableWeight());
            lore.add(rarity + " " + ColorUtil.colorize("&7" + enchant.getName()));
            if (enchant.getMaxLevel() > 1) {
                lore.set(0, lore.get(0) + " " + PandoraEnchant.getLevelRoman(level));
            }
            lore.add("");
            lore.add(ColorUtil.colorize("&7Right-click while holding"));
            lore.add(ColorUtil.colorize("&7an item to apply this enchant"));
            lore.add("");
            lore.add(ColorUtil.colorize("&7Max Level: &6" + enchant.getMaxLevel()));
            lore.add(ColorUtil.colorize("&7Anvil Cost: &6" + enchant.getAnvilCost()));
            meta.setLore(lore);
            meta.setDisplayName(ColorUtil.colorize("&e&lEnchanted Book"));
            book.setItemMeta(meta);
        }
        
        // Apply enchant to book
        com.pandora.enchants.util.EnchantmentStorage.applyEnchant(book, enchant, level);
        
        // Give book to player
        if (target.getInventory().firstEmpty() == -1) {
            target.getWorld().dropItemNaturally(target.getLocation(), book);
            target.sendMessage(ColorUtil.format(ColorUtil.text("Your inventory is full! Book dropped at your location.")));
        } else {
            target.getInventory().addItem(book);
        }
        
        // Messages
        String levelStr = enchant.getMaxLevel() > 1 ? " " + PandoraEnchant.getLevelRoman(level) : "";
        target.sendMessage(ColorUtil.format(
                ColorUtil.text("Received ") + ColorUtil.highlight(enchant.getName() + levelStr) + 
                ColorUtil.text(" enchant book!")
        ));
        
        if (sender != target) {
            sender.sendMessage(ColorUtil.format(
                    ColorUtil.text("Gave ") + ColorUtil.highlight(target.getName()) + 
                    ColorUtil.text(" a ") + ColorUtil.highlight(enchant.getName() + levelStr) + 
                    ColorUtil.text(" enchant book!")
            ));
        }
        
        // Play sound
        target.playSound(target.getLocation(), 
                org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.5f);
        
        return true;
    }
    
    private boolean handleBookList(CommandSender sender, String[] args) {
        List<PandoraEnchant> enchants = new ArrayList<>(enchantManager.getAllEnchantments());
        
        if (enchants.isEmpty()) {
            sender.sendMessage(ColorUtil.format(ColorUtil.text("No enchantments loaded.")));
            return true;
        }
        
        int page = 1;
        if (args.length >= 3) {
            try {
                page = Integer.parseInt(args[2]);
                if (page < 1) page = 1;
            } catch (NumberFormatException e) {
                page = 1;
            }
        }
        
        int totalPages = com.pandora.enchants.util.PaginationUtil.getTotalPages(enchants);
        if (page > totalPages) page = totalPages;
        
        List<PandoraEnchant> pageEnchants = new ArrayList<>();
        int itemsPerPage = 8;
        int startIndex = (page - 1) * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, enchants.size());
        
        for (int i = startIndex; i < endIndex; i++) {
            pageEnchants.add(enchants.get(i));
        }
        
        sender.sendMessage(ColorUtil.colorize("&e&l&m==========&r &e&lEnchant Books &e&l&m=========="));
        sender.sendMessage(ColorUtil.colorize("&7Page &6" + page + " &7/ &6" + totalPages));
        sender.sendMessage("");
        sender.sendMessage(ColorUtil.colorize("&7Use: &e/pe book <enchant> [level] [player]"));
        sender.sendMessage("");
        
        for (PandoraEnchant enchant : pageEnchants) {
            String rarity = getRarityDisplay(enchant.getEnchantmentTableWeight());
            sender.sendMessage(ColorUtil.text("  • ") + rarity + " " + ColorUtil.highlight(enchant.getName()) + 
                    ColorUtil.text(" &7(Max: &6" + enchant.getMaxLevel() + "&7)"));
        }
        
        sender.sendMessage("");
        if (totalPages > 1) {
            if (page < totalPages) {
                sender.sendMessage(ColorUtil.colorize("&7Type &e/pe book list " + (page + 1) + " &7for next page"));
            }
            if (page > 1) {
                sender.sendMessage(ColorUtil.colorize("&7Type &e/pe book list " + (page - 1) + " &7for previous page"));
            }
        }
        
        return true;
    }
    
    private boolean handleEditor(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ColorUtil.error("This command can only be used by players!"));
            return true;
        }
        
        Player player = (Player) sender;
        player.sendMessage(ColorUtil.format(ColorUtil.text("Opening enchant editor...")));
        new com.pandora.enchants.gui.EnchantEditorGUI(player).open();
        return true;
    }
    
    private void sendHelp(CommandSender sender) {
        List<String> helpLines = new ArrayList<>();
        helpLines.add(ColorUtil.header("=== PandoraEnchants Commands ==="));
        helpLines.add("");
        helpLines.add(ColorUtil.text("/pe add <enchant> [level]") + " - Add enchant to held item");
        helpLines.add(ColorUtil.text("/pe remove") + " - Remove custom enchant from held item");
        helpLines.add(ColorUtil.text("/pe info <enchant> [page]") + " - Get info about an enchant");
        helpLines.add(ColorUtil.text("/pe list [page]") + " - List all enchantments");
        helpLines.add(ColorUtil.text("/pe reload") + " - Reload enchantments");
        helpLines.add(ColorUtil.text("/pe editor") + " - &e&lOpen Live Preview Enchant Editor");
        helpLines.add(ColorUtil.text("/pe book <enchant> [level] [player]") + " - Spawn enchant book");
        helpLines.add(ColorUtil.text("/pe book list [page]") + " - List all enchantments for books");
        
        // Send in pages if needed
        int itemsPerPage = 10;
        for (int i = 0; i < helpLines.size(); i += itemsPerPage) {
            int end = Math.min(i + itemsPerPage, helpLines.size());
            for (int j = i; j < end; j++) {
                sender.sendMessage(helpLines.get(j));
            }
            if (end < helpLines.size()) {
                sender.sendMessage("");
                sender.sendMessage(ColorUtil.colorize("&7... (continued)"));
            }
        }
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> completions = new ArrayList<>(Arrays.asList("add", "remove", "info", "list", "reload", "editor", "edit", "book", "givebook"));
            String input = args[0].toLowerCase();
            completions.removeIf(s -> !s.toLowerCase().startsWith(input));
            return completions;
        }
        
        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("info") || 
                args[0].equalsIgnoreCase("book") || args[0].equalsIgnoreCase("givebook")) {
                List<String> enchantNames = new ArrayList<>();
                String input = args[1].toLowerCase();
                for (PandoraEnchant enchant : enchantManager.getAllEnchantments()) {
                    String name = enchant.getNamespacedName();
                    String displayName = enchant.getName().toLowerCase();
                    if (name.toLowerCase().startsWith(input) || displayName.startsWith(input)) {
                        enchantNames.add(name);
                    }
                }
                return enchantNames;
            }
        }
        
        if (args.length == 3 && args[0].equalsIgnoreCase("book") && args[1].equalsIgnoreCase("list")) {
            // Tab complete for page numbers
            return Arrays.asList("1", "2", "3");
        }
        
        return new ArrayList<>();
    }
}

package com.pandora.enchants;

import com.pandora.enchants.commands.PandoraEnchantCommand;
import com.pandora.enchants.engine.PandoraEnchantManager;
import com.pandora.enchants.listeners.EnchantTableListener;
import com.pandora.enchants.listeners.ItemEnchantListener;
import com.pandora.enchants.nms.EnchantmentManager;
import com.pandora.enchants.util.ConfigManager;
import com.pandora.enchants.util.Logger;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Main plugin class for PandoraEnchants
 * Custom enchant plugin for factions servers with one-enchant-per-item restriction
 */
public class PandoraEnchants extends JavaPlugin {
    
    private static PandoraEnchants instance;
    private EnchantmentManager enchantmentManager;
    private PandoraEnchantManager enchantManager;
    
    private static final Pattern VERSION_PATTERN = Pattern.compile("\\(MC: (?<version>[\\d]+\\.[\\d]+(\\.[\\d]+)?)\\)");
    
    @Override
    public void onEnable() {
        instance = this;
        
        Logger.info("Enabling PandoraEnchants v" + getDescription().getVersion());
        
        // Load configuration files
        saveDefaultConfig();
        saveResource("enchantments.yml", false);
        saveResource("messages.yml", false);
        
        // Initialize configuration manager
        ConfigManager.load();
        
        // Initialize NMS
        String version = getMinecraftVersion();
        Logger.info("Detected Minecraft version: " + version);
        
        enchantmentManager = createNMSManager(version);
        if (enchantmentManager == null) {
            Logger.error("Failed to initialize NMS manager for version: " + version);
            Logger.error("PandoraEnchants requires Minecraft 1.21!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        
        // Initialize enchant manager
        enchantManager = new PandoraEnchantManager();
        
        // Register commands
        PandoraEnchantCommand command = new PandoraEnchantCommand();
        getCommand("pandoraenchant").setExecutor(command);
        getCommand("pandoraenchant").setTabCompleter(command);
        
        // Register listeners
        Bukkit.getPluginManager().registerEvents(new EnchantTableListener(), this);
        Bukkit.getPluginManager().registerEvents(new ItemEnchantListener(), this);
        Bukkit.getPluginManager().registerEvents(new com.pandora.enchants.listeners.AnvilEnchantListener(), this);
        Bukkit.getPluginManager().registerEvents(new com.pandora.enchants.gui.GUIListener(), this);
        Bukkit.getPluginManager().registerEvents(new com.pandora.enchants.listeners.DoubleJumpListener(), this);
        Bukkit.getPluginManager().registerEvents(new com.pandora.enchants.effects.EnchantEffectHandler(), this);
        Bukkit.getPluginManager().registerEvents(new com.pandora.enchants.listeners.EnchantBookListener(), this);
        
        // Load enchantments
        enchantManager.loadEnchantments();
        
        Logger.info("PandoraEnchants has been enabled!");
    }
    
    @Override
    public void onDisable() {
        Logger.info("Disabling PandoraEnchants");
        
        if (enchantmentManager != null) {
            enchantmentManager.cleanup();
        }
        
        instance = null;
    }
    
    public static PandoraEnchants getInstance() {
        return instance;
    }
    
    public EnchantmentManager getEnchantmentManager() {
        return enchantmentManager;
    }
    
    public PandoraEnchantManager getEnchantManager() {
        return enchantManager;
    }
    
    private String getMinecraftVersion() {
        String bukkitVersion = Bukkit.getVersion();
        Matcher matcher = VERSION_PATTERN.matcher(bukkitVersion);
        
        if (matcher.find()) {
            String version = matcher.group("version");
            // Normalize version (e.g., "1.21" -> "1.21.0")
            if (version.matches("\\d+\\.\\d+$")) {
                return version + ".0";
            }
            return version;
        }
        
        return "1.21.0";
    }
    
    private EnchantmentManager createNMSManager(String version) {
        try {
            // For 1.21, we'll use a generic implementation
            // You may need to adjust this based on the exact version
            String packageName = "com.pandora.enchants.nms.v1_21";
            String className = "EnchantmentManagerImpl";
            
            Class<?> clazz = Class.forName(packageName + "." + className);
            return (EnchantmentManager) clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            Logger.error("Failed to create NMS manager: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}


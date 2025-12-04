package com.pandora.enchants.util;

import com.pandora.enchants.PandoraEnchants;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages configuration files for the plugin
 */
public class FileManager {
    
    private static final Map<String, FileConfiguration> configs = new HashMap<>();
    private static final Map<String, File> files = new HashMap<>();
    
    /**
     * Loads a configuration file
     */
    public static FileConfiguration loadConfig(String name) {
        if (configs.containsKey(name)) {
            return configs.get(name);
        }
        
        PandoraEnchants plugin = PandoraEnchants.getInstance();
        File configFile = new File(plugin.getDataFolder(), name);
        
        if (!configFile.exists()) {
            plugin.getDataFolder().mkdirs();
            plugin.saveResource(name, false);
        }
        
        FileConfiguration config = new YamlConfiguration();
        try {
            config.load(configFile);
            configs.put(name, config);
            files.put(name, configFile);
            return config;
        } catch (IOException | InvalidConfigurationException e) {
            Logger.error("Error loading config file: " + name);
            e.printStackTrace();
            return config;
        }
    }
    
    /**
     * Saves a configuration file
     */
    public static void saveConfig(String name) {
        FileConfiguration config = configs.get(name);
        File file = files.get(name);
        
        if (config != null && file != null) {
            try {
                config.save(file);
            } catch (IOException e) {
                Logger.error("Error saving config file: " + name);
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Reloads a configuration file
     */
    public static void reloadConfig(String name) {
        configs.remove(name);
        files.remove(name);
        loadConfig(name);
    }
    
    /**
     * Gets a configuration file
     */
    public static FileConfiguration getConfig(String name) {
        if (!configs.containsKey(name)) {
            loadConfig(name);
        }
        return configs.get(name);
    }
}


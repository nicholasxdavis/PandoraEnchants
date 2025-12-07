package com.pandora.enchants.engine;

import java.util.Map;
import java.util.Set;

/**
 * Base record class for Pandora enchantments
 */
public class PandoraEnchantRecord {
    protected final String name;
    protected final String namespacedName;
    protected final PandoraEnchantDefinition definition;
    
    public PandoraEnchantRecord(String name, PandoraEnchantDefinition definition) {
        this.name = name;
        this.namespacedName = toNamespacedName(name);
        this.definition = definition;
    }
    
    public String getName() {
        return name;
    }
    
    public String getNamespacedName() {
        return namespacedName;
    }
    
    public PandoraEnchantDefinition getDefinition() {
        return definition;
    }
    
    public boolean needPermission() {
        return definition.needPermission();
    }
    
    public Set<String> getSupportedItems() {
        return definition.supportedItems();
    }
    
    public Set<String> getPrimaryItems() {
        return definition.primaryItems();
    }
    
    public Map<String, Boolean> getTags() {
        return definition.tags();
    }
    
    public int getEnchantmentTableWeight() {
        return definition.enchantmentTableWeight();
    }
    
    public int getMaxLevel() {
        return definition.maxLevel();
    }
    
    public PandoraEnchantDefinition.EnchantCost getMinCost() {
        return definition.minCost();
    }
    
    public PandoraEnchantDefinition.EnchantCost getMaxCost() {
        return definition.maxCost();
    }
    
    public int getAnvilCost() {
        return definition.anvilCost();
    }
    
    public Set<String> getConflictingEnchantments() {
        return definition.conflictingEnchantments();
    }
    
    public Double getDestroyItemChance() {
        return definition.destroyItemChance();
    }
    
    public Double getRemoveEnchantmentChance() {
        return definition.removeEnchantmentChance();
    }
    
    private String toNamespacedName(String name) {
        return name.toLowerCase().replaceAll(" ", "_");
    }
}



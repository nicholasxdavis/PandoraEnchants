package com.pandora.enchants.engine;

import java.util.Map;
import java.util.Set;

/**
 * Definition record for a Pandora enchantment
 */
public record PandoraEnchantDefinition(
        Set<String> supportedItems,
        Set<String> primaryItems,
        int enchantmentTableWeight,
        int maxLevel,
        boolean needPermission,
        EnchantCost minCost,
        EnchantCost maxCost,
        int anvilCost,
        Set<String> conflictingEnchantments,
        Map<String, Boolean> tags,
        double destroyItemChance,
        double removeEnchantmentChance
) {
    public record EnchantCost(int base, int perLevelAboveFirst) {
    }
}



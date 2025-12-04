package com.pandora.enchants.effects;

import com.pandora.enchants.engine.PandoraEnchant;
import com.pandora.enchants.util.EnchantmentStorage;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import com.pandora.enchants.PandoraEnchants;

import java.util.Random;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Comprehensive enchantment effects handler
 * Adds balanced sounds and effects to all enchantments
 */
public class EnchantEffectHandler implements Listener {
    
    private final Random random = new Random();
    private final Map<UUID, Long> lastThornsDamage = new HashMap<>();
    
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        
        // Check all armor pieces for passive effects
        checkArmorEffects(player);
    }
    
    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;
        
        Player player = (Player) event.getDamager();
        ItemStack weapon = player.getInventory().getItemInMainHand();
        
        if (weapon == null) return;
        
        PandoraEnchant enchant = EnchantmentStorage.getEnchant(weapon);
        if (enchant == null) return;
        
        int level = EnchantmentStorage.getEnchantLevel(weapon, enchant);
        handleWeaponEffects(player, enchant, level, event);
    }
    
    @EventHandler
    public void onEntityKill(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        Player killer = entity.getKiller();
        
        if (killer == null) return;
        
        ItemStack weapon = killer.getInventory().getItemInMainHand();
        if (weapon == null) return;
        
        PandoraEnchant enchant = EnchantmentStorage.getEnchant(weapon);
        if (enchant == null) return;
        
        int level = EnchantmentStorage.getEnchantLevel(weapon, enchant);
        handleKillEffects(killer, enchant, level, entity);
    }
    
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        ItemStack tool = player.getInventory().getItemInMainHand();
        
        if (tool == null) return;
        
        PandoraEnchant enchant = EnchantmentStorage.getEnchant(tool);
        if (enchant == null) return;
        
        int level = EnchantmentStorage.getEnchantLevel(tool, enchant);
        handleToolEffects(player, enchant, level, event);
    }
    
    private void handleWeaponEffects(Player player, PandoraEnchant enchant, int level, EntityDamageByEntityEvent event) {
        String name = enchant.getNamespacedName();
        Location loc = event.getEntity().getLocation();
        
        switch (name) {
            case "lifesteal": {
                double heal = 0.5 + (level * 0.25);
                player.setHealth(Math.min(player.getHealth() + heal, player.getMaxHealth()));
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1.5f);
                player.spawnParticle(Particle.HEART, player.getLocation().add(0, 1, 0), 1, 0.3, 0.3, 0.3, 0);
                break;
            }
                
            case "lightning":
                if (random.nextDouble() < 0.12 * level) {
                    loc.getWorld().strikeLightningEffect(loc);
                    player.playSound(loc, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.6f, 1.1f);
                }
                break;
                
            case "venom":
                if (event.getEntity() instanceof LivingEntity) {
                    LivingEntity target = (LivingEntity) event.getEntity();
                    target.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 40 + (level * 20), 0));
                    player.playSound(loc, Sound.ENTITY_SPIDER_AMBIENT, 0.5f, 1.2f);
                    loc.getWorld().spawnParticle(Particle.ENCHANT, loc.add(0, 1, 0), 5, 0.5, 0.5, 0.5, 0);
                }
                break;
                
            case "wither":
                if (event.getEntity() instanceof LivingEntity) {
                    LivingEntity target = (LivingEntity) event.getEntity();
                    target.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 60 + (level * 20), 0));
                    player.playSound(loc, Sound.ENTITY_WITHER_HURT, 0.4f, 1.3f);
                }
                break;
                
            case "critical":
                if (random.nextDouble() < 0.15 * level) {
                    event.setDamage(event.getDamage() * (1.5 + (level * 0.2)));
                    player.playSound(loc, Sound.ENTITY_PLAYER_ATTACK_CRIT, 1.0f, 1.2f);
                    loc.getWorld().spawnParticle(Particle.CRIT, loc.add(0, 1, 0), 10, 0.5, 0.5, 0.5, 0.1);
                }
                break;
                
            case "execute":
                if (event.getEntity() instanceof LivingEntity) {
                    LivingEntity target = (LivingEntity) event.getEntity();
                    double healthPercent = target.getHealth() / target.getMaxHealth();
                    if (healthPercent < 0.3) {
                        double bonus = 1.0 + ((1.0 - healthPercent) * 0.5 * level);
                        event.setDamage(event.getDamage() * bonus);
                        player.playSound(loc, Sound.ENTITY_PLAYER_ATTACK_STRONG, 0.8f, 1.0f);
                    }
                }
                break;
                
            case "freeze":
                if (event.getEntity() instanceof LivingEntity) {
                    LivingEntity target = (LivingEntity) event.getEntity();
                    target.setFreezeTicks(40 + (level * 20));
                    loc.getWorld().spawnParticle(Particle.SNOWFLAKE, loc, 5, 0.3, 0.3, 0.3, 0);
                    player.playSound(loc, Sound.BLOCK_SNOW_BREAK, 0.5f, 1.0f);
                }
                break;
                
            case "execute_plus":
                if (event.getEntity() instanceof LivingEntity) {
                    LivingEntity target = (LivingEntity) event.getEntity();
                    double healthPercent = target.getHealth() / target.getMaxHealth();
                    if (healthPercent < 0.4) {
                        double bonus = 1.5 + ((1.0 - healthPercent) * 0.8 * level);
                        event.setDamage(event.getDamage() * bonus);
                        player.playSound(loc, Sound.ENTITY_PLAYER_ATTACK_STRONG, 1.0f, 0.9f);
                        loc.getWorld().spawnParticle(Particle.DAMAGE_INDICATOR, loc.add(0, 1, 0), 15, 0.5, 0.5, 0.5, 0.1);
                    }
                }
                break;
                
            case "bleed":
                if (event.getEntity() instanceof LivingEntity) {
                    LivingEntity target = (LivingEntity) event.getEntity();
                    UUID targetId = target.getUniqueId();
                    player.playSound(loc, Sound.ENTITY_PLAYER_HURT, 0.4f, 1.1f);
                    loc.getWorld().spawnParticle(Particle.DUST, loc.add(0, 1, 0), 8, 0.3, 0.3, 0.3, 0);
                    
                    // Apply bleed damage over time
                    new BukkitRunnable() {
                        int ticks = 0;
                        @Override
                        public void run() {
                            if (target.isDead() || ticks >= (level * 20)) {
                                cancel();
                                return;
                            }
                            if (ticks % 20 == 0 && target instanceof LivingEntity) {
                                double damage = 0.5 + (level * 0.3);
                                ((LivingEntity) target).damage(damage);
                                target.getWorld().spawnParticle(Particle.DAMAGE_INDICATOR, target.getLocation().add(0, 1, 0), 3, 0.2, 0.2, 0.2, 0);
                            }
                            ticks++;
                        }
                    }.runTaskTimer(PandoraEnchants.getInstance(), 0L, 1L);
                }
                break;
                
            case "rage":
                if (player.getHealth() < player.getMaxHealth()) {
                    double healthPercent = player.getHealth() / player.getMaxHealth();
                    double damageMultiplier = 1.0 + ((1.0 - healthPercent) * 0.6 * level);
                    event.setDamage(event.getDamage() * damageMultiplier);
                    if (healthPercent < 0.5) {
                        player.playSound(loc, Sound.ENTITY_RAVAGER_ROAR, 0.3f, 1.5f);
                        loc.getWorld().spawnParticle(Particle.ANGRY_VILLAGER, loc.add(0, 1, 0), 3, 0.3, 0.3, 0.3, 0);
                    }
                }
                break;
                
            case "cleave":
                if (random.nextDouble() < 0.25 * level) {
                    double radius = 2.0 + (level * 0.5);
                    for (Entity nearby : loc.getWorld().getNearbyEntities(loc, radius, radius, radius)) {
                        if (nearby instanceof LivingEntity && nearby != event.getEntity() && nearby != player) {
                            LivingEntity nearbyEntity = (LivingEntity) nearby;
                            double damage = event.getDamage() * (0.3 + (level * 0.1));
                            nearbyEntity.damage(damage, player);
                            nearbyEntity.getWorld().spawnParticle(Particle.SWEEP_ATTACK, nearbyEntity.getLocation().add(0, 1, 0), 5, 0.3, 0.3, 0.3, 0);
                        }
                    }
                    player.playSound(loc, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 0.7f, 1.1f);
                }
                break;
                
            case "disarm":
                if (random.nextDouble() < 0.08 * level && event.getEntity() instanceof LivingEntity) {
                    LivingEntity target = (LivingEntity) event.getEntity();
                    if (target instanceof Player) {
                        Player targetPlayer = (Player) target;
                        ItemStack mainHand = targetPlayer.getInventory().getItemInMainHand();
                        if (mainHand != null && !mainHand.getType().isAir()) {
                            targetPlayer.getWorld().dropItemNaturally(targetPlayer.getLocation(), mainHand);
                            targetPlayer.getInventory().setItemInMainHand(null);
                            player.playSound(loc, Sound.ENTITY_ITEM_PICKUP, 0.8f, 0.5f);
                            loc.getWorld().spawnParticle(Particle.ITEM, loc, 10, 0.3, 0.3, 0.3, 0.1);
                        }
                    }
                }
                break;
                
            case "leech":
                if (event.getEntity() instanceof LivingEntity) {
                    LivingEntity target = (LivingEntity) event.getEntity();
                    double steal = 0.3 + (level * 0.2);
                    double newHealth = Math.min(player.getHealth() + steal, player.getMaxHealth());
                    player.setHealth(newHealth);
                    double newTargetHealth = Math.max(0, target.getHealth() - steal);
                    target.setHealth(newTargetHealth);
                    player.playSound(player.getLocation(), Sound.ENTITY_WITCH_DRINK, 0.5f, 1.3f);
                    loc.getWorld().spawnParticle(Particle.HEART, player.getLocation().add(0, 1, 0), 2, 0.2, 0.2, 0.2, 0);
                }
                break;
                
            case "vampire": {
                if (event.getEntity() instanceof LivingEntity) {
                    LivingEntity target = (LivingEntity) event.getEntity();
                    double damage = event.getDamage();
                    double healAmount = damage * (0.15 + (level * 0.1));
                    player.setHealth(Math.min(player.getHealth() + healAmount, player.getMaxHealth()));
                    player.playSound(player.getLocation(), Sound.ENTITY_WITHER_AMBIENT, 0.4f, 1.4f);
                    loc.getWorld().spawnParticle(Particle.HEART, player.getLocation().add(0, 1, 0), 3, 0.3, 0.3, 0.3, 0);
                    loc.getWorld().spawnParticle(Particle.SOUL, loc.add(0, 1, 0), 5, 0.3, 0.3, 0.3, 0.05);
                }
                break;
            }
        }
    }
    
    private void handleKillEffects(Player killer, PandoraEnchant enchant, int level, LivingEntity killed) {
        String name = enchant.getNamespacedName();
        Location loc = killed.getLocation();
        
        switch (name) {
            case "soul_siphon":
                double heal = 2.0 + (level * 1.0);
                killer.setHealth(Math.min(killer.getHealth() + heal, killer.getMaxHealth()));
                killer.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 60 + (level * 20), 0));
                killer.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 60 + (level * 20), 0));
                killer.playSound(loc, Sound.ENTITY_WITHER_AMBIENT, 0.7f, 0.8f);
                loc.getWorld().spawnParticle(Particle.SOUL, loc, 20, 0.5, 0.5, 0.5, 0.1);
                break;
        }
    }
    
    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof LivingEntity)) return;
        LivingEntity entity = (LivingEntity) event.getEntity();
        
        if (!(entity instanceof Player)) return;
        Player player = (Player) entity;
        
        // Check armor for thorns and other defensive enchants
        ItemStack[] armor = player.getInventory().getArmorContents();
        for (ItemStack piece : armor) {
            if (piece == null) continue;
            
            PandoraEnchant enchant = EnchantmentStorage.getEnchant(piece);
            if (enchant == null) continue;
            
            int level = EnchantmentStorage.getEnchantLevel(piece, enchant);
            handleDefensiveEffects(player, enchant, level, event);
        }
    }
    
    private void handleDefensiveEffects(Player player, PandoraEnchant enchant, int level, EntityDamageEvent event) {
        String name = enchant.getNamespacedName();
        Location loc = player.getLocation();
        
        switch (name) {
            case "thorns":
                if (event instanceof EntityDamageByEntityEvent) {
                    EntityDamageByEntityEvent damageEvent = (EntityDamageByEntityEvent) event;
                    Entity damager = damageEvent.getDamager();
                    
                    // Prevent spam
                    UUID playerId = player.getUniqueId();
                    long now = System.currentTimeMillis();
                    if (lastThornsDamage.containsKey(playerId) && now - lastThornsDamage.get(playerId) < 500) {
                        return;
                    }
                    lastThornsDamage.put(playerId, now);
                    
                    if (damager instanceof LivingEntity && damager != player) {
                        LivingEntity attacker = (LivingEntity) damager;
                        double thornsDamage = 0.5 + (level * 0.5);
                        attacker.damage(thornsDamage);
                        player.playSound(loc, Sound.ENTITY_PLAYER_HURT, 0.6f, 1.2f);
                        loc.getWorld().spawnParticle(Particle.DAMAGE_INDICATOR, attacker.getLocation().add(0, 1, 0), 5, 0.3, 0.3, 0.3, 0);
                    }
                }
                break;
                
            case "shield":
                if (random.nextDouble() < 0.15 * level) {
                    double reduction = 0.2 + (level * 0.1);
                    event.setDamage(event.getDamage() * (1.0 - reduction));
                    player.playSound(loc, Sound.ITEM_SHIELD_BLOCK, 0.5f, 1.0f);
                    loc.getWorld().spawnParticle(Particle.ITEM, loc.add(0, 1, 0), 3, 0.3, 0.3, 0.3, 0);
                }
                break;
                
            case "tank":
                double reduction = 0.1 + (level * 0.05);
                event.setDamage(event.getDamage() * (1.0 - reduction));
                if (random.nextDouble() < 0.1) {
                    player.playSound(loc, Sound.BLOCK_ANVIL_LAND, 0.3f, 1.5f);
                }
                break;
                
            case "immortal":
                if (player.getHealth() <= 2.0 && random.nextDouble() < 0.3 * level) {
                    event.setCancelled(true);
                    player.setHealth(Math.min(player.getHealth() + 1.0, player.getMaxHealth()));
                    player.playSound(loc, Sound.ENTITY_PLAYER_LEVELUP, 0.8f, 0.7f);
                    loc.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, loc.add(0, 1, 0), 30, 0.5, 0.5, 0.5, 0.1);
                }
                break;
        }
    }
    
    private void handleToolEffects(Player player, PandoraEnchant enchant, int level, BlockBreakEvent event) {
        String name = enchant.getNamespacedName();
        Location loc = event.getBlock().getLocation();
        
        switch (name) {
            case "vein_miner":
                player.playSound(loc, Sound.BLOCK_STONE_BREAK, 0.4f, 1.1f);
                loc.getWorld().spawnParticle(Particle.BLOCK, loc.add(0.5, 0.5, 0.5), 3, 0.2, 0.2, 0.2, 0.05);
                break;
                
            case "autosmelt":
                player.playSound(loc, Sound.BLOCK_FIRE_EXTINGUISH, 0.3f, 1.0f);
                loc.getWorld().spawnParticle(Particle.FLAME, loc.add(0.5, 0.5, 0.5), 2, 0.2, 0.2, 0.2, 0);
                break;
                
            case "experience":
                if (random.nextDouble() < 0.2 * level) {
                    player.playSound(loc, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.4f, 1.3f);
                    loc.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, loc.add(0.5, 0.5, 0.5), 2, 0.2, 0.2, 0.2, 0);
                }
                break;
                
            case "lumberjack":
                player.playSound(loc, Sound.BLOCK_WOOD_BREAK, 0.5f, 0.8f);
                break;
                
            case "magnet":
                if (random.nextDouble() < 0.15) {
                    player.playSound(loc, Sound.ENTITY_ITEM_PICKUP, 0.3f, 1.5f);
                }
                break;
                
            case "telekinesis":
                if (random.nextDouble() < 0.1) {
                    player.playSound(loc, Sound.ENTITY_ENDERMAN_TELEPORT, 0.2f, 2.0f);
                }
                break;
                
            case "tunnel":
                player.playSound(loc, Sound.BLOCK_STONE_BREAK, 0.3f, 1.3f);
                break;
                
            case "fortune_plus":
                if (random.nextDouble() < 0.3) {
                    player.playSound(loc, Sound.ENTITY_PLAYER_LEVELUP, 0.3f, 1.5f);
                    loc.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, loc.add(0.5, 0.5, 0.5), 3, 0.2, 0.2, 0.2, 0);
                }
                break;
                
            case "jackhammer":
                player.playSound(loc, Sound.BLOCK_STONE_BREAK, 0.5f, 0.7f);
                loc.getWorld().spawnParticle(Particle.BLOCK, loc.add(0.5, 0.5, 0.5), 5, 0.3, 0.3, 0.3, 0.05);
                break;
                
            case "efficiency_plus":
                if (random.nextDouble() < 0.2) {
                    player.playSound(loc, Sound.BLOCK_NOTE_BLOCK_PLING, 0.3f, 1.8f);
                }
                break;
        }
    }
    
    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (!(event.getEntity().getShooter() instanceof Player)) return;
        Player shooter = (Player) event.getEntity().getShooter();
        
        ItemStack bow = shooter.getInventory().getItemInMainHand();
        if (bow == null || (bow.getType() != Material.BOW && bow.getType() != Material.CROSSBOW)) {
            return;
        }
        
        PandoraEnchant enchant = EnchantmentStorage.getEnchant(bow);
        if (enchant == null) return;
        
        int level = EnchantmentStorage.getEnchantLevel(bow, enchant);
        Location loc = event.getEntity().getLocation();
        
        String name = enchant.getNamespacedName();
        
        switch (name) {
            case "explosive":
                loc.getWorld().createExplosion(loc, 1.0f + (level * 0.5f), false, false);
                shooter.playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 0.7f, 1.0f);
                break;
                
            case "poison_arrow":
                if (event.getHitEntity() instanceof LivingEntity) {
                    LivingEntity target = (LivingEntity) event.getHitEntity();
                    target.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 40 + (level * 20), 0));
                    shooter.playSound(loc, Sound.ENTITY_SPIDER_AMBIENT, 0.5f, 1.2f);
                    loc.getWorld().spawnParticle(Particle.ENCHANT, loc.add(0, 1, 0), 5, 0.3, 0.3, 0.3, 0);
                }
                break;
                
            case "homing":
                shooter.playSound(loc, Sound.ENTITY_ENDER_EYE_LAUNCH, 0.5f, 1.3f);
                loc.getWorld().spawnParticle(Particle.PORTAL, loc, 10, 0.3, 0.3, 0.3, 0.1);
                break;
                
            case "multi_shot":
                shooter.playSound(loc, Sound.ENTITY_ARROW_SHOOT, 0.5f, 1.2f);
                break;
                
            case "pierce":
                shooter.playSound(loc, Sound.ENTITY_ARROW_HIT, 0.4f, 1.1f);
                loc.getWorld().spawnParticle(Particle.CRIT, loc, 5, 0.2, 0.2, 0.2, 0.05);
                break;
        }
    }
    
    private void checkArmorEffects(Player player) {
        ItemStack[] armor = player.getInventory().getArmorContents();
        
        for (ItemStack piece : armor) {
            if (piece == null) continue;
            
            PandoraEnchant enchant = EnchantmentStorage.getEnchant(piece);
            if (enchant == null) continue;
            
            int level = EnchantmentStorage.getEnchantLevel(piece, enchant);
            handleArmorEffects(player, enchant, level);
        }
    }
    
    private void handleArmorEffects(Player player, PandoraEnchant enchant, int level) {
        String name = enchant.getNamespacedName();
        Location loc = player.getLocation();
        
        switch (name) {
            case "regeneration":
                if (player.getHealth() < player.getMaxHealth() && random.nextDouble() < 0.03 * level) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 40, 0, true, false));
                    if (random.nextDouble() < 0.1) {
                        player.spawnParticle(Particle.HEART, loc.add(0, 1, 0), 1, 0.2, 0.2, 0.2, 0);
                    }
                }
                break;
                
            case "night_vision":
                player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 300, 0, true, false));
                break;
                
            case "fire_resistance":
                player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 300, 0, true, false));
                break;
                
            case "absorption":
                if (random.nextDouble() < 0.02 * level) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 200 + (level * 100), level - 1, true, false));
                    if (random.nextDouble() < 0.2) {
                        player.spawnParticle(Particle.TOTEM_OF_UNDYING, loc.add(0, 1, 0), 2, 0.2, 0.2, 0.2, 0);
                    }
                }
                break;
                
            case "thorns":
                // Thorns handled in damage event
                break;
                
            case "reflex":
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100, Math.min(level - 1, 1), true, false));
                if (random.nextDouble() < 0.05) {
                    player.spawnParticle(Particle.CLOUD, loc.add(0, 0.5, 0), 2, 0.2, 0.1, 0.2, 0);
                }
                break;
                
            case "gears":
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100, Math.min(level - 1, 1), true, false));
                if (random.nextDouble() < 0.05) {
                    player.spawnParticle(Particle.ENCHANTED_HIT, loc.add(0, 0.5, 0), 2, 0.2, 0.1, 0.2, 0);
                }
                break;
                
            case "clarity":
                // Remove negative effects periodically
                if (random.nextDouble() < 0.02) {
                    player.removePotionEffect(PotionEffectType.POISON);
                    player.removePotionEffect(PotionEffectType.WITHER);
                    player.removePotionEffect(PotionEffectType.NAUSEA);
                    if (random.nextDouble() < 0.3) {
                        player.spawnParticle(Particle.HAPPY_VILLAGER, loc.add(0, 1, 0), 2, 0.2, 0.2, 0.2, 0);
                    }
                }
                break;
                
            case "drunk":
                // Random positive effects
                if (random.nextDouble() < 0.01) {
                    PotionEffectType[] positiveEffects = {
                        PotionEffectType.SPEED, PotionEffectType.STRENGTH
                    };
                    PotionEffectType effect = positiveEffects[random.nextInt(positiveEffects.length)];
                    player.addPotionEffect(new PotionEffect(effect, 60, 0, true, false));
                    player.spawnParticle(Particle.HAPPY_VILLAGER, loc.add(0, 1, 0), 3, 0.3, 0.3, 0.3, 0);
                }
                break;
                
            case "water_walker":
                if (player.isInWater() || loc.getBlock().getType() == Material.WATER) {
                    player.setVelocity(player.getVelocity().setY(0.1));
                    if (random.nextDouble() < 0.1) {
                        player.spawnParticle(Particle.SPLASH, loc, 3, 0.3, 0.1, 0.3, 0);
                    }
                }
                break;
                
            case "auto_feed":
                // Handled in separate listener if needed
                break;
                
            case "feather_fall":
                // Reduces fall damage - handled in fall damage event
                break;
        }
    }
    
    @EventHandler
    public void onPlayerFall(EntityDamageEvent event) {
        if (event.getCause() != EntityDamageEvent.DamageCause.FALL) return;
        if (!(event.getEntity() instanceof Player)) return;
        
        Player player = (Player) event.getEntity();
        ItemStack boots = player.getInventory().getBoots();
        
        if (boots == null) return;
        
        PandoraEnchant enchant = EnchantmentStorage.getEnchant(boots);
        if (enchant == null || !enchant.getNamespacedName().equals("feather_fall")) return;
        
        int level = EnchantmentStorage.getEnchantLevel(boots, enchant);
        double reduction = 0.3 + (level * 0.15);
        event.setDamage(event.getDamage() * (1.0 - reduction));
        
        if (random.nextDouble() < 0.3) {
            player.playSound(player.getLocation(), Sound.ENTITY_CHICKEN_EGG, 0.4f, 1.5f);
            player.spawnParticle(Particle.CLOUD, player.getLocation(), 5, 0.3, 0.1, 0.3, 0);
        }
    }
}

package com.pandora.enchants.listeners;

import com.pandora.enchants.engine.PandoraEnchant;
import com.pandora.enchants.util.EnchantmentStorage;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Handles double jump enchantment for boots
 */
public class DoubleJumpListener implements Listener {
    
    private final Map<UUID, Boolean> hasDoubleJumped = new HashMap<>();
    private final Map<UUID, Boolean> wasOnGround = new HashMap<>();
    
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        
        if (player.getGameMode() == GameMode.CREATIVE || 
            player.getGameMode() == GameMode.SPECTATOR) {
            return;
        }
        
        ItemStack boots = player.getInventory().getBoots();
        if (boots == null) {
            return;
        }
        
        PandoraEnchant enchant = EnchantmentStorage.getEnchant(boots);
        if (enchant == null || !enchant.getNamespacedName().equals("double_jump")) {
            return;
        }
        
        UUID uuid = player.getUniqueId();
        boolean isOnGround = player.isOnGround();
        boolean wasOnGroundBefore = wasOnGround.getOrDefault(uuid, true);
        
        // Reset double jump when landing
        if (isOnGround) {
            hasDoubleJumped.put(uuid, false);
            wasOnGround.put(uuid, true);
        } else {
            wasOnGround.put(uuid, false);
        }
        
        // Enable flight for double jump detection
        if (!isOnGround && !wasOnGroundBefore && !player.getAllowFlight()) {
            player.setAllowFlight(true);
        }
        
        // Disable flight when on ground
        if (isOnGround && player.getAllowFlight() && player.getGameMode() != GameMode.CREATIVE) {
            player.setAllowFlight(false);
        }
    }
    
    @EventHandler
    public void onPlayerToggleFlight(PlayerToggleFlightEvent event) {
        Player player = event.getPlayer();
        
        if (player.getGameMode() == GameMode.CREATIVE || 
            player.getGameMode() == GameMode.SPECTATOR) {
            return;
        }
        
        ItemStack boots = player.getInventory().getBoots();
        if (boots == null) {
            return;
        }
        
        PandoraEnchant enchant = EnchantmentStorage.getEnchant(boots);
        if (enchant == null || !enchant.getNamespacedName().equals("double_jump")) {
            return;
        }
        
        UUID uuid = player.getUniqueId();
        
        // Check if already used double jump
        if (hasDoubleJumped.getOrDefault(uuid, false)) {
            return;
        }
        
        // Check if player is actually in air (not just starting flight)
        if (player.isOnGround()) {
            return;
        }
        
        int level = EnchantmentStorage.getEnchantLevel(boots, enchant);
        if (level < 1) return;
        
        event.setCancelled(true);
        player.setFlying(false);
        player.setAllowFlight(false);
        
        // Perform double jump
        Vector velocity = player.getVelocity();
        double jumpPower = 0.6 + (level * 0.2);
        velocity.setY(Math.min(velocity.getY() + jumpPower, 1.2));
        player.setVelocity(velocity);
        
        // Mark as used
        hasDoubleJumped.put(uuid, true);
        
        // Play sound
        player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 0.7f, 1.5f);
        
        // Particles
        player.spawnParticle(Particle.CLOUD, player.getLocation(), 8, 0.3, 0.1, 0.3, 0.05);
        
        // Schedule re-enable flight check
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnGround() && !hasDoubleJumped.getOrDefault(uuid, false)) {
                    player.setAllowFlight(true);
                }
            }
        }.runTaskLater(com.pandora.enchants.PandoraEnchants.getInstance(), 5L);
    }
}

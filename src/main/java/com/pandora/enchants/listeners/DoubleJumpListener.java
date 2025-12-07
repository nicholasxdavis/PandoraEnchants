package com.pandora.enchants.listeners;

import com.pandora.enchants.engine.PandoraEnchant;
import com.pandora.enchants.util.EnchantmentStorage;
import com.pandora.enchants.util.Logger;
import com.pandora.enchants.PandoraEnchants;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Handles double jump enchantment for boots
 * Based on proven double jump mechanics
 */
public class DoubleJumpListener implements Listener {
    
    private final Map<UUID, Boolean> hasDoubleJumped = new HashMap<>();
    private final Map<UUID, Boolean> wasOnGround = new HashMap<>();
    private final Map<UUID, Long> lastJumpTime = new HashMap<>();
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        
        // Skip creative/spectator
        if (player.getGameMode() == GameMode.CREATIVE || 
            player.getGameMode() == GameMode.SPECTATOR) {
            return;
        }
        
        // Check if player has double jump enchant
        ItemStack boots = player.getInventory().getBoots();
        if (boots == null || boots.getType().isAir()) {
            // Clean up if player no longer has boots
            if (hasDoubleJumped.containsKey(uuid)) {
                hasDoubleJumped.remove(uuid);
                wasOnGround.remove(uuid);
                if (player.getAllowFlight() && player.getGameMode() != GameMode.CREATIVE) {
                    player.setAllowFlight(false);
                }
            }
            return;
        }
        
        // Check for double_jump enchant specifically
        PandoraEnchant enchant = EnchantmentStorage.getEnchant(boots, "double_jump");
        
        if (enchant == null) {
            // Clean up if player no longer has double jump
            if (hasDoubleJumped.containsKey(uuid)) {
                hasDoubleJumped.remove(uuid);
                wasOnGround.remove(uuid);
                if (player.getAllowFlight() && player.getGameMode() != GameMode.CREATIVE) {
                    player.setAllowFlight(false);
                }
            }
            return;
        }
        
        boolean isOnGround = player.isOnGround();
        boolean wasOnGroundBefore = wasOnGround.getOrDefault(uuid, true);
        
        // Track ground state
        wasOnGround.put(uuid, isOnGround);
        
        // Reset double jump when landing on ground
        if (isOnGround) {
            if (hasDoubleJumped.getOrDefault(uuid, false)) {
                hasDoubleJumped.put(uuid, false);
            }
            // Disable flight when on ground
            if (player.getAllowFlight() && player.getGameMode() != GameMode.CREATIVE && !player.isFlying()) {
                player.setAllowFlight(false);
            }
            return;
        }
        
        // Player is in air - enable flight for double jump if they haven't used it yet
        if (!isOnGround && !hasDoubleJumped.getOrDefault(uuid, false)) {
            // Enable flight after a small delay to ensure they're actually in air
            // Only schedule if not already enabled to avoid spam
            if (!player.getAllowFlight()) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (player.isOnline() && !player.isOnGround() && 
                            !hasDoubleJumped.getOrDefault(uuid, false)) {
                            if (player.getGameMode() != GameMode.CREATIVE && 
                                player.getGameMode() != GameMode.SPECTATOR) {
                                player.setAllowFlight(true);
                            }
                        }
                    }
                }.runTaskLater(PandoraEnchants.getInstance(), 2L);
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerToggleFlight(PlayerToggleFlightEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        
        // Skip creative/spectator
        if (player.getGameMode() == GameMode.CREATIVE || 
            player.getGameMode() == GameMode.SPECTATOR) {
            return;
        }
        
        // Check if player has double jump enchant
        ItemStack boots = player.getInventory().getBoots();
        if (boots == null || boots.getType().isAir()) {
            return;
        }
        
        // Check for double_jump enchant specifically
        PandoraEnchant enchant = EnchantmentStorage.getEnchant(boots, "double_jump");
        if (enchant == null) {
            return;
        }
        
        // Check if already used double jump
        if (hasDoubleJumped.getOrDefault(uuid, false)) {
            event.setCancelled(true);
            player.setFlying(false);
            player.setAllowFlight(false);
            return;
        }
        
        // Check if player is actually in air
        if (player.isOnGround()) {
            event.setCancelled(true);
            player.setFlying(false);
            player.setAllowFlight(false);
            return;
        }
        
        // Get enchant level
        int level = EnchantmentStorage.getEnchantLevel(boots, enchant);
        
        if (level < 1) {
            event.setCancelled(true);
            return;
        }
        
        // Prevent spam jumping
        long now = System.currentTimeMillis();
        if (lastJumpTime.containsKey(uuid)) {
            long timeSinceLastJump = now - lastJumpTime.get(uuid);
            if (timeSinceLastJump < 100) { // 100ms cooldown
                event.setCancelled(true);
                return;
            }
        }
        lastJumpTime.put(uuid, now);
        
        // Cancel the flight toggle
        event.setCancelled(true);
        player.setFlying(false);
        player.setAllowFlight(false);
        
        // Perform double jump - MUCH STRONGER
        Vector velocity = player.getVelocity();
        double jumpPower = 0.8 + (level * 0.3); // Base 0.8, +0.3 per level (much stronger!)
        double newY = Math.min(velocity.getY() + jumpPower, 1.5); // Higher max velocity
        velocity.setY(newY);
        player.setVelocity(velocity);
        
        // Mark as used
        hasDoubleJumped.put(uuid, true);
        
        // Play subtle sounds (reduced significantly)
        try {
            player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 0.5f, 1.14f);
            player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.3f, 1.43f);
        } catch (Exception e) {
            Logger.warn("Failed to play double jump sound: " + e.getMessage());
        }
        
        // Subtle particles (reduced significantly)
        try {
            Location loc = player.getLocation();
            // Cloud burst
            player.spawnParticle(Particle.CLOUD, loc, 8, 0.3, 0.15, 0.3, 0.05);
            // Portal effect
            player.spawnParticle(Particle.PORTAL, loc, 5, 0.25, 0.05, 0.25, 0.08);
        } catch (Exception e) {
            Logger.warn("Failed to spawn double jump particles: " + e.getMessage());
        }
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        hasDoubleJumped.remove(uuid);
        wasOnGround.remove(uuid);
        lastJumpTime.remove(uuid);
    }
}

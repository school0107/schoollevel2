package com.schoollevel2.listeners;

import com.schoollevel2.SchoolLevel2;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.Random;

public class CombatListener implements Listener {

    private final SchoolLevel2 plugin;
    private final Random rng = new Random();

    public CombatListener(SchoolLevel2 plugin) { this.plugin = plugin; }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent e) {
        // ===== ATTACKER BONUS + CRIT =====
        Player attacker = null;
        boolean isRanged = false;

        if (e.getDamager() instanceof Player p) {
            attacker = p;
        } else if (e.getDamager() instanceof Arrow arrow && arrow.getShooter() instanceof Player p) {
            attacker = p;
            isRanged = true;
        } else if (e.getDamager() instanceof Trident trident && trident.getShooter() instanceof Player p) {
            attacker = p;
            isRanged = true;
        } else if (e.getDamager() instanceof Projectile proj && proj.getShooter() instanceof Player p) {
            // Trứng, tuyết, ngọc ender...
            attacker = p;
            isRanged = true;
        }

        if (attacker != null) {
            double bonus = isRanged
                    ? plugin.getStatsManager().getRangedBonus(attacker)
                    : plugin.getStatsManager().getMeleeBonus(attacker);
            if (bonus > 0) e.setDamage(e.getDamage() + bonus);

            // CHAKRA CRIT: chỉ crit khi nhảy lên đánh
            // Điều kiện crit: attacker không ở ground (đang nhảy) + roll crit thành công
            boolean isJumping = !attacker.isOnGround();
            if (isJumping && plugin.getStatsManager().rollCrit(attacker)) {
                double mult = plugin.getStatsManager().getCritMultiplier();
                e.setDamage(e.getDamage() * mult);

                // Hiệu ứng crit
                try {
                    attacker.getWorld().spawnParticle(Particle.CRIT,
                            e.getEntity().getLocation().add(0, 1, 0),
                            15, 0.3, 0.3, 0.3, 0.1);
                    attacker.getWorld().playSound(e.getEntity().getLocation(),
                            Sound.ENTITY_PLAYER_ATTACK_CRIT, 1f, 1.2f);
                } catch (Exception ignored) {}
            }
        }

        // ===== DEFENDER DODGE =====
        if (e.getEntity() instanceof Player victim) {
            double dodge = plugin.getStatsManager().getDodgeChance(victim);
            if (dodge > 0 && rng.nextDouble() * 100.0 < dodge) {
                EntityDamageEvent.DamageCause cause = e.getCause();
                if (cause == EntityDamageEvent.DamageCause.ENTITY_ATTACK
                        || cause == EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK
                        || cause == EntityDamageEvent.DamageCause.PROJECTILE) {
                    e.setCancelled(true);
                    try {
                        victim.getWorld().spawnParticle(Particle.CLOUD,
                                victim.getLocation().add(0, 1, 0),
                                8, 0.3, 0.3, 0.3, 0.02);
                    } catch (Exception ignored) {}
                }
            }
        }
    }
}
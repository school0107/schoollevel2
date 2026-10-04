package com.schoollevel2.listeners;

import com.schoollevel2.SchoolLevel2;
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
        // Attacker bonus
        Player attacker = null;
        if (e.getDamager() instanceof Player p) attacker = p;
        else if (e.getDamager() instanceof Arrow arrow && arrow.getShooter() instanceof Player p) attacker = p;
        else if (e.getDamager() instanceof Trident trident && trident.getShooter() instanceof Player p) attacker = p;

        if (attacker != null) {
            boolean ranged = e.getDamager() instanceof Projectile;
            double bonus = ranged ? plugin.getStatsManager().getRangedBonus(attacker)
                                   : plugin.getStatsManager().getMeleeBonus(attacker);
            if (bonus > 0) {
                e.setDamage(e.getDamage() + bonus);
            }
        }

        // Defender dodge
        if (e.getEntity() instanceof Player victim) {
            double dodge = plugin.getStatsManager().getDodgeChance(victim);
            if (dodge > 0 && rng.nextDouble() * 100.0 < dodge) {
                // Only dodge physical damage (melee or projectile)
                EntityDamageEvent.DamageCause cause = e.getCause();
                if (cause == EntityDamageEvent.DamageCause.ENTITY_ATTACK
                        || cause == EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK
                        || cause == EntityDamageEvent.DamageCause.PROJECTILE) {
                    e.setCancelled(true);
                    victim.getWorld().spawnParticle(org.bukkit.Particle.CLOUD, victim.getLocation().add(0, 1, 0), 8, 0.3, 0.3, 0.3, 0.02);
                }
            }
        }
    }
}

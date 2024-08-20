package br.com.plutomc.core.bukkit.listener;

import java.util.HashMap;
import java.util.Map;

import br.com.plutomc.core.bukkit.event.player.PlayerDamagePlayerEvent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class CombatListener implements Listener {
   private static final Map<Material, Double> DAMAGE = new HashMap<>();

   @EventHandler(
      priority = EventPriority.HIGHEST
   )
   public void onEntityDamage(EntityDamageEvent event) {
      if (event.getEntity() instanceof Player) {
         Player player = (Player)event.getEntity();
         if (event.getCause() == DamageCause.FALL && player.hasMetadata("nofall")) {
            MetadataValue metadata = player.getMetadata("nofall").stream().findFirst().orElse(null);
            if (metadata.asLong() > System.currentTimeMillis()) {
               event.setCancelled(true);
            }

            metadata.invalidate();
         }
      }
   }

   @EventHandler(
      priority = EventPriority.HIGHEST
   )
   public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
      if (event.getEntity() instanceof Player) {
         Player player = (Player)event.getEntity();
         Player damager = null;
         if (event.getDamager() instanceof Player) {
            damager = (Player)event.getDamager();
         } else if (event.getDamager() instanceof Projectile) {
            Projectile projectile = (Projectile)event.getDamager();
            if (projectile.getShooter() instanceof Player) {
               damager = (Player)projectile.getShooter();
            }
         }

         if (damager instanceof Player) {
            PlayerDamagePlayerEvent playerDamagePlayerEvent = new PlayerDamagePlayerEvent(
               player, damager, event.isCancelled(), event.getDamage(), event.getFinalDamage()
            );
            Bukkit.getPluginManager().callEvent(playerDamagePlayerEvent);
            event.setCancelled(playerDamagePlayerEvent.isCancelled());
            event.setDamage(playerDamagePlayerEvent.getDamage());
         }
      }
   }

   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void onEntityDamageEvent(EntityDamageByEntityEvent event) {
      if (event.getDamager() instanceof Player) {
         Player p = (Player)event.getDamager();
         ItemStack sword = p.getItemInHand();
         double damage = event.getDamage();
         double danoEspada = this.getDamage(sword.getType());
         boolean isMore = false;
         if (damage > 1.0) {
            isMore = true;
         }

         if (p.hasPotionEffect(PotionEffectType.INCREASE_DAMAGE)) {
            for(PotionEffect effect : p.getActivePotionEffects()) {
               if (effect.getType().equals(PotionEffectType.INCREASE_DAMAGE)) {
                  double minus;
                  if (this.isCrital(p)) {
                     minus = (danoEspada + danoEspada / 2.0) * 1.3 * (double)(effect.getAmplifier() + 1);
                  } else {
                     minus = danoEspada * 1.3 * (double)(effect.getAmplifier() + 1);
                  }

                  damage -= minus;
                  damage += (double)(2 * (effect.getAmplifier() + 1));
                  break;
               }
            }
         }

         if (!sword.getEnchantments().isEmpty()) {
            if (sword.containsEnchantment(Enchantment.DAMAGE_ARTHROPODS) && this.isArthropod(event.getEntityType())) {
               damage -= 1.5 * (double)sword.getEnchantmentLevel(Enchantment.DAMAGE_ARTHROPODS);
               damage += (double)(1 * sword.getEnchantmentLevel(Enchantment.DAMAGE_ARTHROPODS));
            }

            if (sword.containsEnchantment(Enchantment.DAMAGE_UNDEAD) && this.isUndead(event.getEntityType())) {
               damage -= 1.5 * (double)sword.getEnchantmentLevel(Enchantment.DAMAGE_UNDEAD);
               damage += (double)(1 * sword.getEnchantmentLevel(Enchantment.DAMAGE_UNDEAD));
            }

            if (sword.containsEnchantment(Enchantment.DAMAGE_ALL)) {
               damage -= 1.25 * (double)sword.getEnchantmentLevel(Enchantment.DAMAGE_ALL);
               damage += (double)(1 * sword.getEnchantmentLevel(Enchantment.DAMAGE_ALL));
            }
         }

         if (this.isCrital(p)) {
            damage += 0.5;
         }

         if (isMore) {
            damage -= 2.0;
         }

         event.setDamage(damage);
      }
   }

   private boolean isCrital(Player p) {
      return p.getFallDistance() > 0.0F && !p.isOnGround() && !p.hasPotionEffect(PotionEffectType.BLINDNESS);
   }

   private boolean isArthropod(EntityType type) {
      switch(type) {
         case CAVE_SPIDER:
            return true;
         case SPIDER:
            return true;
         case SILVERFISH:
            return true;
         default:
            return false;
      }
   }

   private boolean isUndead(EntityType type) {
      switch(type) {
         case SKELETON:
            return true;
         case ZOMBIE:
            return true;
         case WITHER_SKULL:
            return true;
         case PIG_ZOMBIE:
            return true;
         default:
            return false;
      }
   }

   private double getDamage(Material type) {
      double damage = 1.0;
      if (type.toString().contains("DIAMOND_")) {
         damage = 7.0;
      } else if (type.toString().contains("IRON_")) {
         damage = 5.0;
      } else if (type.toString().contains("STONE_")) {
         damage = 4.0;
      } else if (type.toString().contains("WOOD_")) {
         damage = 3.0;
      } else if (type.toString().contains("GOLD_")) {
         damage = 3.0;
      }

      if (!type.toString().contains("_SWORD")) {
         --damage;
         if (!type.toString().contains("_AXE")) {
            --damage;
            if (!type.toString().contains("_PICKAXE")) {
               --damage;
               if (!type.toString().contains("_SPADE")) {
                  damage = 1.0;
               }
            }
         }
      }

      return damage;
   }

   static {
      DAMAGE.put(Material.WOOD_SWORD, 2.0);
      DAMAGE.put(Material.STONE_SWORD, 4.0);
      DAMAGE.put(Material.IRON_SWORD, 6.0);
      DAMAGE.put(Material.DIAMOND_SWORD, 8.0);
   }
}

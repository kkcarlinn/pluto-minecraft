package br.com.plutomc.game.bedwars.listener;

import java.util.Arrays;

import br.com.plutomc.game.bedwars.GameMain;
import br.com.plutomc.game.bedwars.utils.GamerHelper;
import br.com.plutomc.core.common.CommonConst;
import br.com.plutomc.game.engine.ArcadeCommon;
import br.com.plutomc.core.bukkit.utils.item.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

public class FireballListener implements Listener {
   @EventHandler
   public void onPlayerInteract(PlayerInteractEvent event) {
      if (event.getItem() != null) {
         if (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR) {
            Player player = event.getPlayer();
            if (event.getItem().getType() == Material.FIREBALL) {
               player.setItemInHand(
                  event.getItem().getAmount() > 1
                     ? new ItemBuilder().type(Material.FIREBALL).amount(event.getItem().getAmount() - 1).build()
                     : new ItemStack(Material.AIR)
               );
               Fireball fireball = player.launchProjectile(Fireball.class);
               fireball.setYield(0.0F);
               fireball.setFireTicks(-1);
               fireball.setIsIncendiary(false);
               fireball.setMetadata("boost", ArcadeCommon.getInstance().createMeta(player.getName()));
               player.setMetadata("no-damage-by-entity", ArcadeCommon.getInstance().createMeta(Integer.valueOf(fireball.getEntityId())));
            }
         }
      }
   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onBlockPlace(BlockPlaceEvent event) {
      Block block = event.getBlock();
      if (block.getType() == Material.TNT) {
         block.setType(Material.AIR);
         Player player = event.getPlayer();
         player.setItemInHand(
            player.getItemInHand().getAmount() > 1
               ? new ItemBuilder().type(Material.TNT).amount(player.getItemInHand().getAmount() - 1).build()
               : new ItemStack(Material.AIR)
         );
         TNTPrimed tntPrimed = block.getWorld().spawn(event.getBlock().getLocation().clone().add(0.5, 0.5, 0.5), TNTPrimed.class);
         player.setMetadata("no-damage-by-entity", ArcadeCommon.getInstance().createMeta(Integer.valueOf(tntPrimed.getEntityId())));
         tntPrimed.setFuseTicks(48);
         Bukkit.getScheduler()
            .runTaskLater(
               GameMain.getInstance(),
               () -> tntPrimed.setMetadata(
                     "location", new FixedMetadataValue(GameMain.getInstance(), CommonConst.GSON.toJson(event.getPlayer().getLocation().serialize()))
                  ),
               40L
            );
         tntPrimed.setMetadata("boost", ArcadeCommon.getInstance().createMeta(event.getPlayer().getName()));
         event.setCancelled(true);
      }
   }

   @EventHandler
   public void onProjectileHit(ProjectileHitEvent event) {
      if (event.getEntity() instanceof Fireball) {
         Fireball entity = (Fireball)event.getEntity();
         Location location = entity.getLocation();
         if (entity.hasMetadata("boost")) {
            Player player = Bukkit.getPlayer(entity.getMetadata("boost").get(0).asString());
            if (player == null) {
               player = GamerHelper.getMoreNearbyPlayers(location, 6.0);
            }

            if (player == null) {
               return;
            }

            if (entity.getType() == EntityType.FIREBALL || entity.getType() == EntityType.SMALL_FIREBALL) {
               this.makeExplosion(location, EntityType.FIREBALL);

               for(Player ps : GamerHelper.getPlayersNear(location, 3.0)) {
                  if (ps.hasMetadata("onlyboost-onground") && ps.getMetadata("onlyboost-onground").get(0).asLong() > System.currentTimeMillis()) {
                     ps.removeMetadata("onlyboost-onground", GameMain.getInstance());
                     if (!ps.isOnGround()) {
                        continue;
                     }
                  }

                  ps.setVelocity(ps == player ? this.fireballBoost(ps, entity, true) : this.fireballBoost(ps, entity, false));
               }
            }
         }
      }
   }

   public Vector tntBoost(Location location, Player player) {
      boolean onGround = player.isOnGround();
      double multiplier = onGround ? 1.5 : 1.5;
      double Y = onGround ? 0.5 : 0.9;
      if (player.getLocation().distance(location) <= 2.7
         && (
            GamerHelper.forwardLocationByPlayerRotation(player, location, 3).add(0.0, 1.0, 0.0).getBlock().getType() != Material.AIR
               || GamerHelper.forwardLocationByPlayerRotation(player, location, 3).add(0.0, 2.0, 0.0).getBlock().getType() != Material.AIR
         )) {
         location = GamerHelper.forwardLocationByPlayerRotation(player, location, 3);
      }

      return player.getLocation().subtract(location).toVector().normalize().multiply(multiplier).setY(Y);
   }

   public Vector fireballBoost(Player player, Entity entity, boolean moreBoost) {
      Location entityLocation = entity.getLocation();
      boolean onGround = player.isOnGround();
      double multiplier = moreBoost ? (onGround ? 5.8 : 2.8) : (onGround ? 0.5 : 1.0);
      double Y = moreBoost ? (onGround ? 0.6 : 0.8) : (onGround ? 1.2 : 0.3);
      if (player.getLocation().distance(entityLocation) <= 2.7
         && (
            GamerHelper.forwardLocationByPlayerRotation(player, entityLocation, 3).add(0.0, 1.0, 0.0).getBlock().getType() != Material.AIR
               || GamerHelper.forwardLocationByPlayerRotation(player, entityLocation, 3).add(0.0, 2.0, 0.0).getBlock().getType() != Material.AIR
         )) {
         entityLocation = GamerHelper.forwardLocationByPlayerRotation(player, entityLocation, 3);
      }

      return player.getLocation().subtract(entityLocation).toVector().normalize().multiply(multiplier).setY(Y);
   }

   @EventHandler
   public void onExplosion(BlockExplodeEvent event) {
      event.setCancelled(true);
   }

   @EventHandler
   public void onExplosion(EntityExplodeEvent event) {
      event.setCancelled(true);
   }

   public void makeExplosion(Location explosionLocation, EntityType entityType) {
      explosionLocation.getWorld().createExplosion(explosionLocation, 1.0F);

      for(Block block : GamerHelper.getNearbyBlocks(explosionLocation, entityType != EntityType.PRIMED_TNT && entityType != EntityType.MINECART_TNT ? 2 : 3)) {
         if (GameMain.getInstance().getPlayersBlock().contains(block.getLocation())
            && block.getType() != Material.OBSIDIAN
            && !this.checkIfBlockIsProtected(block)
            && (entityType == EntityType.PRIMED_TNT || entityType == EntityType.MINECART_TNT || block.getType() != Material.ENDER_STONE)) {
            block.setType(Material.AIR);
         }
      }
   }

   public boolean checkIfBlockIsProtected(Block block) {
      for(Block b : Arrays.asList(
         block.getRelative(BlockFace.NORTH),
         block.getRelative(BlockFace.SOUTH),
         block.getRelative(BlockFace.WEST),
         block.getRelative(BlockFace.EAST),
         block.getRelative(BlockFace.UP),
         block.getRelative(BlockFace.DOWN)
      )) {
         if (b.getType().name().contains("GLASS")) {
            return true;
         }
      }

      return false;
   }

   @EventHandler
   public void onExplosionPrime(ExplosionPrimeEvent event) {
      event.setCancelled(true);
      Entity entity = event.getEntity();
      if (entity.getType() != EntityType.FIREBALL && entity.getType() != EntityType.SMALL_FIREBALL) {
         Location loc = event.getEntity().getLocation();
         this.makeExplosion(loc, EntityType.PRIMED_TNT);
         if (entity.hasMetadata("boost")) {
            Player player = Bukkit.getPlayer(entity.getMetadata("boost").get(0).asString());

            for(Player ps : GamerHelper.getPlayersNear(loc, 6.0)) {
               if (ps == player) {
                  ps.setVelocity(this.tntBoost(loc, ps));
               }
            }
         }
      }
   }

   @EventHandler
   public void onBlockIgnite(BlockIgniteEvent event) {
      event.setCancelled(true);
   }

   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void onEntityDamage(EntityDamageEvent event) {
      if (event.getEntity() instanceof Player) {
         Player player = (Player)event.getEntity();
         if (event.getCause() != DamageCause.FALL && event.getCause() != DamageCause.FALLING_BLOCK) {
            if (event.getCause() == DamageCause.BLOCK_EXPLOSION
               || event.getCause() == DamageCause.ENTITY_EXPLOSION
               || event.getCause() == DamageCause.CUSTOM
               || event.getCause() == DamageCause.FIRE_TICK) {
               player.setFireTicks(-1);
               if (player.hasMetadata("no-damage-by-entity")) {
                  event.setDamage(0.0);
                  event.setCancelled(true);
                  player.removeMetadata("no-damage-by-entity", GameMain.getInstance());
               }
            }
         } else if (player.hasMetadata("onlyboost-onground") && player.getMetadata("onlyboost-onground").get(0).asLong() > System.currentTimeMillis()) {
            event.setDamage(event.getDamage() / 2.0);
         }
      }
   }

   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
      if ((event.getDamager() instanceof Fireball || event.getDamager() instanceof TNTPrimed) && event.getEntity() instanceof Player) {
         Player player = (Player)event.getEntity();
         if (player.hasMetadata("no-damage-by-entity")) {
            player.setFireTicks(-1);
            event.setDamage(0.0);
            event.setCancelled(true);
            player.removeMetadata("no-damage-by-entity", GameMain.getInstance());
            player.setMetadata("nofall", ArcadeCommon.getInstance().createMeta(Long.valueOf(System.currentTimeMillis() + 3000L)));
         }
      }
   }
}

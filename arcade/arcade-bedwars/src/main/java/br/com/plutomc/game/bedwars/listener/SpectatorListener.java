package br.com.plutomc.game.bedwars.listener;

import br.com.plutomc.game.bedwars.gamer.Gamer;
import br.com.plutomc.game.engine.ArcadeCommon;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;

public class SpectatorListener implements Listener {
   @EventHandler(
      priority = EventPriority.LOW
   )
   public void onEntityDamage(EntityDamageEvent event) {
      if (event.getEntity() instanceof Player) {
         Player player = (Player)event.getEntity();
         Gamer gamer = ArcadeCommon.getInstance().getGamerManager().getGamer(player.getUniqueId(), Gamer.class);
         if (gamer.isSpectator()) {
            event.setCancelled(true);
            return;
         }
      }
   }

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
      if (event.getEntity() instanceof Player) {
         Player player = (Player)event.getEntity();
         Gamer gamer = ArcadeCommon.getInstance().getGamerManager().getGamer(player.getUniqueId(), Gamer.class);
         if (gamer.isSpectator()) {
            event.setCancelled(true);
            return;
         }

         if (event.getDamager() instanceof Player) {
            Player damager = (Player)event.getDamager();
            Gamer damagerGamer = ArcadeCommon.getInstance().getGamerManager().getGamer(damager.getUniqueId(), Gamer.class);
            if (damagerGamer.isSpectator()) {
               event.setCancelled(true);
               return;
            }
         }
      }
   }

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
      Player player = event.getPlayer();
      Gamer gamer = ArcadeCommon.getInstance().getGamerManager().getGamer(player.getUniqueId(), Gamer.class);
      if (gamer.isSpectator()) {
         event.setCancelled(true);
      }
   }

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void onBlockBreak(BlockBreakEvent event) {
      Player player = event.getPlayer();
      Gamer gamer = ArcadeCommon.getInstance().getGamerManager().getGamer(player.getUniqueId(), Gamer.class);
      if (gamer.isSpectator()) {
         event.setCancelled(true);
      }
   }

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void onBlockPlace(BlockPlaceEvent event) {
      Player player = event.getPlayer();
      Gamer gamer = ArcadeCommon.getInstance().getGamerManager().getGamer(player.getUniqueId(), Gamer.class);
      if (gamer.isSpectator()) {
         event.setCancelled(true);
      }
   }

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void onPlayerInteract(PlayerInteractEvent event) {
      Player player = event.getPlayer();
      Gamer gamer = ArcadeCommon.getInstance().getGamerManager().getGamer(player.getUniqueId(), Gamer.class);
      if (gamer.isSpectator()) {
         event.setCancelled(true);
      }
   }

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void onPlayerDropItem(PlayerDropItemEvent event) {
      Player player = event.getPlayer();
      Gamer gamer = ArcadeCommon.getInstance().getGamerManager().getGamer(player.getUniqueId(), Gamer.class);
      if (gamer.isSpectator()) {
         event.setCancelled(true);
      }
   }

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void onPlayerPickupItem(PlayerPickupItemEvent event) {
      Player player = event.getPlayer();
      Gamer gamer = ArcadeCommon.getInstance().getGamerManager().getGamer(player.getUniqueId(), Gamer.class);
      if (gamer.isSpectator()) {
         event.setCancelled(true);
      }
   }
}

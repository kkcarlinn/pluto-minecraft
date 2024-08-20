package br.com.plutomc.lobby.lobbyhost.listener;

import br.com.plutomc.lobby.lobbyhost.LobbyHost;
import br.com.plutomc.lobby.lobbyhost.wadgets.util.PointManager;
import br.com.plutomc.core.bukkit.event.UpdateEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;

public class WorldListener implements Listener {
   @EventHandler
   public void onCreatureSpawn(CreatureSpawnEvent event) {
      event.setCancelled(event.getSpawnReason() != SpawnReason.CUSTOM);
   }

   @EventHandler
   public void onEntityExplode(EntityExplodeEvent event) {
      event.blockList().clear();
      event.setCancelled(true);
   }

   @EventHandler
   public void onBlockExplode(BlockExplodeEvent event) {
      event.blockList().clear();
      event.setCancelled(true);
   }

   @EventHandler
   public void onBlockBurn(BlockBurnEvent event) {
      event.setCancelled(true);
   }

   @EventHandler
   public void onBlockIgnite(BlockIgniteEvent event) {
      event.setCancelled(true);
   }

   @EventHandler
   public void onBlockSpread(BlockSpreadEvent event) {
      event.setCancelled(true);
   }

   @EventHandler
   public void onEntityRegainHealth(EntityRegainHealthEvent event) {
      event.setCancelled(true);
   }

   @EventHandler
   public void onEntityDamage(EntityDamageEvent event) {
      event.setCancelled(true);
   }

   @EventHandler
   public void onBlockBreak(BlockBreakEvent event) {
      event.setCancelled(true);
   }

   @EventHandler
   public void onBlockPlace(BlockPlaceEvent event) {
      event.setCancelled(true);
   }

   @EventHandler
   public void onBlockPlace(BlockPhysicsEvent event) {
      event.setCancelled(true);
   }

   @EventHandler
   public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
      event.setCancelled(true);
   }

   @EventHandler
   public void onFoodLevelChange(FoodLevelChangeEvent event) {
      event.setCancelled(true);
   }

   @EventHandler
   public void onPlayerPickupItem(PlayerPickupItemEvent event) {
      event.getItem().remove();
      event.setCancelled(true);
   }

   @EventHandler
   public void onPlayerDropItem(PlayerDropItemEvent event) {
      event.setCancelled(true);
   }

   @EventHandler
   public void onPlayerInteract(PlayerInteractEvent event) {
      event.setCancelled(true);
   }

   @EventHandler
   public void onUpdate(UpdateEvent event) {
      if (event.getCurrentTick() % 2L == 0L) {
         LobbyHost.getInstance()
            .getGamerManager()
            .getGamers()
            .stream()
            .filter(gamer -> gamer.getWing() != null || gamer.getParticle() != null)
            .forEach(gamer -> {
               if (gamer.getWing() != null) {
                  PointManager.getInstance().sendPacket(gamer.getPlayer(), gamer.getWing().getParticle());
               } else {
                  PointManager.getInstance().sendPacket(gamer.getPlayer());
               }
            });
      }
   }
}

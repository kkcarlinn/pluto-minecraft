package br.com.plutomc.core.bukkit.anticheat.listener;

import br.com.plutomc.core.common.CommonPlugin;
import br.com.plutomc.core.bukkit.anticheat.StormCore;
import br.com.plutomc.core.bukkit.anticheat.gamer.UserData;
import br.com.plutomc.core.bukkit.account.BukkitAccount;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;

public class PlayerListener implements Listener {
   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void onPlayerDeath(PlayerDeathEvent event) {
      StormCore.getInstance().ignore(event.getEntity(), 4.0);
   }

   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void onPlayerLogin(PlayerLoginEvent event) {
      StormCore.getInstance().ignore(event.getPlayer(), 4.0);
   }

   @EventHandler
   public void onPlayerRespawn(PlayerRespawnEvent event) {
      StormCore.getInstance().ignore(event.getPlayer(), 4.0);
   }

   @EventHandler
   public void onPlayerVelocity(PlayerVelocityEvent event) {
      StormCore.getInstance().ignore(event.getPlayer(), 0.5);
   }

   @EventHandler
   public void onPlayerTeleport(PlayerTeleportEvent event) {
      StormCore.getInstance().ignore(event.getPlayer(), 4.0);
   }

   @EventHandler
   public void onPlayerPortal(PlayerPortalEvent event) {
      StormCore.getInstance().ignore(event.getPlayer(), 4.0);
   }

   @EventHandler
   public void onPlayerJoin(PlayerJoinEvent event) {
      StormCore.getInstance().ignore(event.getPlayer(), 4.0);
   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void onPlayerMove(PlayerMoveEvent event) {
      UserData userData = this.getUserData(event.getPlayer());
      Location lastLocation = userData.getLastLocation() == null ? event.getFrom() : userData.getLastLocation();
      userData.setDistanceY(lastLocation.getY() - event.getTo().getY());
      if (event.getTo().getY() > event.getFrom().getY()) {
         userData.setGoingUp(true);
         userData.setFalling(false);
      } else if (event.getTo().getY() < event.getFrom().getY()) {
         userData.setGoingUp(false);
         userData.setFalling(true);
      } else {
         userData.setGoingUp(false);
         userData.setFalling(false);
      }

      userData.setPing(((CraftPlayer)event.getPlayer()).getHandle().ping);
      userData.setLastLocation(event.getTo().clone());
   }

   public UserData getUserData(Player player) {
      return CommonPlugin.getInstance().getAccountManager().getAccount(player.getUniqueId(), BukkitAccount.class).getUserData();
   }
}

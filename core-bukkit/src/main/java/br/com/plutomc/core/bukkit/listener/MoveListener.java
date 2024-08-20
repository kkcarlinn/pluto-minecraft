package br.com.plutomc.core.bukkit.listener;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import br.com.plutomc.core.bukkit.event.UpdateEvent;
import br.com.plutomc.core.bukkit.event.player.PlayerMoveUpdateEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class MoveListener implements Listener {
   private Map<UUID, Location> locationMap = new HashMap<>();

   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void onUpdate(UpdateEvent event) {
      if (event.getCurrentTick() % 5L == 0L) {
         for(Player player : Bukkit.getOnlinePlayers()) {
            if (this.locationMap.containsKey(player.getUniqueId())) {
               Location location = this.locationMap.get(player.getUniqueId());
               if (location.getX() != player.getLocation().getX()
                  || location.getZ() != player.getLocation().getZ()
                  || location.getY() != player.getLocation().getY()) {
                  PlayerMoveUpdateEvent playerMoveUpdateEvent = new PlayerMoveUpdateEvent(player, location, player.getLocation());
                  Bukkit.getPluginManager().callEvent(playerMoveUpdateEvent);
                  if (playerMoveUpdateEvent.isCancelled()) {
                     player.teleport(
                        new Location(location.getWorld(), location.getX(), player.getLocation().getY(), location.getZ(), location.getYaw(), location.getPitch())
                     );
                  }
               }
            }

            this.locationMap.put(player.getUniqueId(), player.getLocation());
         }
      }
   }
}

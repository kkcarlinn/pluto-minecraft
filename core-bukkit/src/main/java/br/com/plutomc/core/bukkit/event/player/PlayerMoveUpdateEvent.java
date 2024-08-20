package br.com.plutomc.core.bukkit.event.player;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;

public class PlayerMoveUpdateEvent extends PlayerMoveEvent {
   public PlayerMoveUpdateEvent(Player player, Location from, Location to) {
      super(player, from, to);
   }
}

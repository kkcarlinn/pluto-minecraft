package br.com.plutomc.core.bukkit.event;

import org.bukkit.entity.Player;

public class PlayerEvent extends NormalEvent {
   private Player player;

   public PlayerEvent(Player player) {
      this.player = player;
   }

   public Player getPlayer() {
      return this.player;
   }
}

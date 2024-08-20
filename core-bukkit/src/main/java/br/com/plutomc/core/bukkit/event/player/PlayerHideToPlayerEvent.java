package br.com.plutomc.core.bukkit.event.player;

import br.com.plutomc.core.bukkit.event.PlayerCancellableEvent;
import org.bukkit.entity.Player;

public class PlayerHideToPlayerEvent extends PlayerCancellableEvent {
   private Player toPlayer;

   public PlayerHideToPlayerEvent(Player player, Player toPlayer) {
      super(player);
      this.toPlayer = toPlayer;
   }

   public Player getToPlayer() {
      return this.toPlayer;
   }
}

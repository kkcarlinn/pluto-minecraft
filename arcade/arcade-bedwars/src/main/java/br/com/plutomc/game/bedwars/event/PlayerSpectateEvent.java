package br.com.plutomc.game.bedwars.event;

import lombok.NonNull;
import br.com.plutomc.core.bukkit.event.PlayerEvent;
import org.bukkit.entity.Player;

public class PlayerSpectateEvent extends PlayerEvent {
   public PlayerSpectateEvent(@NonNull Player player) {
      super(player);
      if (player == null) {
         throw new NullPointerException("player is marked non-null but is null");
      }
   }
}

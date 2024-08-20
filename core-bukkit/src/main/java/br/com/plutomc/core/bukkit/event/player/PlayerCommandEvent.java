package br.com.plutomc.core.bukkit.event.player;

import br.com.plutomc.core.bukkit.event.PlayerCancellableEvent;
import org.bukkit.entity.Player;

public class PlayerCommandEvent extends PlayerCancellableEvent {
   private String commandLabel;

   public PlayerCommandEvent(Player player, String commandLabel) {
      super(player);
      this.commandLabel = commandLabel;
   }

   public String getCommandLabel() {
      return this.commandLabel;
   }
}

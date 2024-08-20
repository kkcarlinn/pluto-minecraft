package br.com.plutomc.core.bukkit.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

public class PlayerCancellableEvent extends PlayerEvent implements Cancellable {
   private boolean cancelled;

   public PlayerCancellableEvent(Player player) {
      super(player);
   }

   @Override
   public boolean isCancelled() {
      return this.cancelled;
   }

   @Override
   public void setCancelled(boolean cancelled) {
      this.cancelled = cancelled;
   }
}

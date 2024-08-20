package br.com.plutomc.core.bukkit.event.cooldown;

import br.com.plutomc.core.bukkit.utils.cooldown.Cooldown;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

public class CooldownStartEvent extends CooldownEvent implements Cancellable {
   private boolean cancelled;

   public CooldownStartEvent(Player player, Cooldown cooldown) {
      super(player, cooldown);
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

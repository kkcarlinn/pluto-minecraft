package br.com.plutomc.core.bukkit.event.cooldown;

import br.com.plutomc.core.bukkit.utils.cooldown.Cooldown;
import br.com.plutomc.core.bukkit.event.PlayerEvent;
import lombok.NonNull;
import org.bukkit.entity.Player;

public abstract class CooldownEvent extends PlayerEvent {
   @NonNull
   private Cooldown cooldown;

   public CooldownEvent(Player player, Cooldown cooldown) {
      super(player);
      this.cooldown = cooldown;
   }

   @NonNull
   public Cooldown getCooldown() {
      return this.cooldown;
   }
}

package br.com.plutomc.core.bukkit.event.cooldown;

import br.com.plutomc.core.bukkit.utils.cooldown.Cooldown;
import org.bukkit.entity.Player;

public class CooldownFinishEvent extends CooldownStopEvent {
   public CooldownFinishEvent(Player player, Cooldown cooldown) {
      super(player, cooldown);
   }
}

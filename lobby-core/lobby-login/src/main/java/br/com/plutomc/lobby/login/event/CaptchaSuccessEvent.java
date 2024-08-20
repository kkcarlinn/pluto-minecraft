package br.com.plutomc.lobby.login.event;

import br.com.plutomc.core.bukkit.event.PlayerCancellableEvent;
import org.bukkit.entity.Player;

public class CaptchaSuccessEvent extends PlayerCancellableEvent {
   public CaptchaSuccessEvent(Player player) {
      super(player);
   }
}

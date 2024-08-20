package br.com.plutomc.game.engine.event;

import br.com.plutomc.core.bukkit.event.PlayerCancellableEvent;
import br.com.plutomc.game.engine.gamer.Gamer;
import org.bukkit.entity.Player;

public class GamerLoadEvent extends PlayerCancellableEvent {
   private Gamer gamer;
   private String reason;

   public GamerLoadEvent(Player player, Gamer gamer) {
      super(player);
      this.gamer = gamer;
   }

   public Gamer getGamer() {
      return this.gamer;
   }

   public String getReason() {
      return this.reason;
   }

   public void setGamer(Gamer gamer) {
      this.gamer = gamer;
   }

   public void setReason(String reason) {
      this.reason = reason;
   }
}

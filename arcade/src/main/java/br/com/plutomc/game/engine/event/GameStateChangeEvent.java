package br.com.plutomc.game.engine.event;

import br.com.plutomc.core.bukkit.event.NormalEvent;
import br.com.plutomc.core.common.server.loadbalancer.server.MinigameState;

public class GameStateChangeEvent extends NormalEvent {
   private MinigameState oldState;
   private MinigameState state;

   public MinigameState getOldState() {
      return this.oldState;
   }

   public MinigameState getState() {
      return this.state;
   }

   public GameStateChangeEvent(MinigameState oldState, MinigameState state) {
      this.oldState = oldState;
      this.state = state;
   }
}

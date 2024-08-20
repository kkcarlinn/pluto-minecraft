package br.com.plutomc.game.bedwars.event;

import br.com.plutomc.core.bukkit.event.NormalEvent;
import br.com.plutomc.game.bedwars.island.Island;

public class IslandWinEvent extends NormalEvent {
   private Island island;

   public IslandWinEvent(Island island) {
      this.island = island;
   }

   public Island getIsland() {
      return this.island;
   }
}

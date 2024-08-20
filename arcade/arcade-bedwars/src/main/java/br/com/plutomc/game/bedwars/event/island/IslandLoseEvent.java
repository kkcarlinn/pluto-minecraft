package br.com.plutomc.game.bedwars.event.island;

import br.com.plutomc.core.bukkit.event.NormalEvent;
import br.com.plutomc.game.bedwars.island.Island;

public class IslandLoseEvent extends NormalEvent {
   private Island island;

   public Island getIsland() {
      return this.island;
   }

   public IslandLoseEvent(Island island) {
      this.island = island;
   }
}

package br.com.plutomc.game.bedwars.event.island;

import br.com.plutomc.core.bukkit.event.NormalEvent;
import br.com.plutomc.game.bedwars.island.Island;
import br.com.plutomc.game.bedwars.island.IslandUpgrade;

public class IslandUpgradeEvent extends NormalEvent {
   private Island island;
   private IslandUpgrade upgrade;
   private int level;

   public Island getIsland() {
      return this.island;
   }

   public IslandUpgrade getUpgrade() {
      return this.upgrade;
   }

   public int getLevel() {
      return this.level;
   }

   public IslandUpgradeEvent(Island island, IslandUpgrade upgrade, int level) {
      this.island = island;
      this.upgrade = upgrade;
      this.level = level;
   }
}

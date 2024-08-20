package br.com.plutomc.core.bukkit.event.player;

import br.com.plutomc.core.bukkit.event.PlayerEvent;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class PlayerOpenInventoryEvent extends PlayerEvent {
   private Inventory inventory;

   public PlayerOpenInventoryEvent(Player player, Inventory inventory) {
      super(player);
      this.inventory = inventory;
   }

   public Inventory getInventory() {
      return this.inventory;
   }
}

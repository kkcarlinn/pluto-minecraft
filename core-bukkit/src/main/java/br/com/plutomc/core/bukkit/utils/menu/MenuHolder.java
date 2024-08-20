package br.com.plutomc.core.bukkit.utils.menu;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class MenuHolder implements InventoryHolder {
   private MenuInventory menu;

   public MenuHolder(MenuInventory menuInventory) {
      this.menu = menuInventory;
   }

   public MenuInventory getMenu() {
      return this.menu;
   }

   public void setMenu(MenuInventory menu) {
      this.menu = menu;
   }

   public void onClose(Player player) {
      this.menu.onClose(player);
   }

   public boolean isOnePerPlayer() {
      return this.menu.isOnePerPlayer();
   }

   public void destroy() {
      this.menu = null;
   }

   @Override
   public Inventory getInventory() {
      return this.isOnePerPlayer() ? null : this.menu.getInventory();
   }
}

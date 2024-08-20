package br.com.plutomc.core.bukkit.utils.menu;

import br.com.plutomc.core.bukkit.utils.menu.click.ClickType;
import br.com.plutomc.core.bukkit.utils.menu.click.MenuClickHandler;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class MenuItem {
   private ItemStack stack;
   private MenuClickHandler handler;

   public MenuItem(ItemStack itemstack) {
      this.stack = itemstack;
      this.handler = new MenuClickHandler() {
         @Override
         public void onClick(Player p, Inventory inv, ClickType type, ItemStack stack, int slot) {
         }
      };
   }

   public MenuItem(ItemStack itemstack, MenuClickHandler clickHandler) {
      this.stack = itemstack;
      this.handler = clickHandler;
   }

   public ItemStack getStack() {
      return this.stack;
   }

   public MenuClickHandler getHandler() {
      return this.handler;
   }

   public void destroy() {
      this.stack = null;
      this.handler = null;
   }
}

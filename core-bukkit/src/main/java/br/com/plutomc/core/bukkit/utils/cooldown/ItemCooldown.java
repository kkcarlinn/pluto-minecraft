package br.com.plutomc.core.bukkit.utils.cooldown;

import org.bukkit.inventory.ItemStack;

public class ItemCooldown extends Cooldown {
   private ItemStack item;
   private boolean selected;

   public ItemCooldown(ItemStack item, String name, Long duration) {
      super(name, duration);
      this.item = item;
   }

   public ItemStack getItem() {
      return this.item;
   }

   public boolean isSelected() {
      return this.selected;
   }

   public void setSelected(boolean selected) {
      this.selected = selected;
   }
}

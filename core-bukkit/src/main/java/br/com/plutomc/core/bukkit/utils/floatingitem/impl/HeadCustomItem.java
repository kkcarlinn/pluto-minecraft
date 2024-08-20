package br.com.plutomc.core.bukkit.utils.floatingitem.impl;

import br.com.plutomc.core.bukkit.utils.floatingitem.CustomItem;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

public class HeadCustomItem implements CustomItem {
   private Location location;
   private ItemStack itemStack;
   private ArmorStand armorStand;
   private boolean alive;

   public HeadCustomItem(Location location, ItemStack itemStack) {
      this.location = location;
      this.itemStack = itemStack;
   }

   @Override
   public CustomItem spawn() {
      if (this.alive) {
         return this;
      } else {
         this.armorStand = (ArmorStand)this.location.getWorld().spawnEntity(this.location, EntityType.ARMOR_STAND);
         this.armorStand.setHelmet(this.itemStack);
         this.armorStand.setCustomNameVisible(false);
         this.armorStand.setGravity(false);
         this.armorStand.setVisible(false);
         this.alive = true;
         return this;
      }
   }

   @Override
   public CustomItem remove() {
      if (this.alive) {
         this.armorStand.remove();
         this.alive = false;
         return this;
      } else {
         return this;
      }
   }

   @Override
   public void teleport(Location location) {
      this.location = location;
      if (this.alive) {
         this.armorStand.teleport(location);
      }
   }

   @Override
   public Location getLocation() {
      return this.location;
   }

   @Override
   public ItemStack getItemStack() {
      return this.itemStack;
   }

   public ArmorStand getArmorStand() {
      return this.armorStand;
   }

   public boolean isAlive() {
      return this.alive;
   }
}

package br.com.plutomc.core.bukkit.utils.floatingitem;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

public interface CustomItem {
   CustomItem spawn();

   CustomItem remove();

   void teleport(Location var1);

   Location getLocation();

   ItemStack getItemStack();
}

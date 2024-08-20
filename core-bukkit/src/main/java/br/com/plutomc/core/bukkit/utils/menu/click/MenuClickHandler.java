package br.com.plutomc.core.bukkit.utils.menu.click;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public interface MenuClickHandler {
   void onClick(Player var1, Inventory var2, ClickType var3, ItemStack var4, int var5);
}

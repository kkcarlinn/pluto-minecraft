package br.com.plutomc.core.bukkit.utils;

import java.util.List;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ItemUtils {
   public static void dropAndClear(Player p, List<ItemStack> items, org.bukkit.Location l) {
      dropItems(items, l);
      p.closeInventory();
      p.getInventory().setArmorContents(new ItemStack[4]);
      p.getInventory().clear();
      p.setItemOnCursor(null);
   }

   public static void dropItems(List<ItemStack> items, org.bukkit.Location l) {
      World world = l.getWorld();

      for(ItemStack item : items) {
         if (item != null && item.getType() != Material.AIR) {
            if (item.hasItemMeta()) {
               world.dropItemNaturally(l, item.clone()).getItemStack().setItemMeta(item.getItemMeta());
            } else {
               world.dropItemNaturally(l, item);
            }
         }
      }
   }

   public static void addItem(Player player, ItemStack item, org.bukkit.Location location) {
      int slot = player.getInventory().first(item.getType());
      if (slot == -1) {
         slot = player.getInventory().firstEmpty();
         if (slot == -1) {
            boolean needDrop = true;

            for(ItemStack itemContent : player.getInventory().getContents()) {
               if (itemContent.getType() == item.getType()) {
                  if (itemContent.getAmount() + item.getAmount() <= 64) {
                     player.getInventory().addItem(new ItemStack[]{item});
                     needDrop = false;
                  } else {
                     while(itemContent.getAmount() + item.getAmount() <= 64 && item.getAmount() >= 0) {
                        itemContent.setAmount(itemContent.getAmount() + 1);
                        item.setAmount(item.getAmount() - 1);
                     }

                     if (item.getAmount() <= 0) {
                        needDrop = false;
                     }
                  }
               }
            }

            if (needDrop) {
               location.getWorld().dropItem(location, item);
            }
         } else {
            player.getInventory().addItem(new ItemStack[]{item});
         }
      } else if (player.getInventory().getItem(slot).getAmount() + item.getAmount() > 64) {
         slot = player.getInventory().firstEmpty();
         if (slot == -1) {
            boolean needDrop = true;

            for(ItemStack itemContent : player.getInventory().getContents()) {
               if (itemContent.getType() == item.getType()) {
                  if (itemContent.getAmount() + item.getAmount() <= 64) {
                     player.getInventory().addItem(new ItemStack[]{item});
                     needDrop = false;
                  } else {
                     while(itemContent.getAmount() + item.getAmount() <= 64 && item.getAmount() >= 0) {
                        itemContent.setAmount(itemContent.getAmount() + 1);
                        item.setAmount(item.getAmount() - 1);
                     }

                     if (item.getAmount() <= 0) {
                        needDrop = false;
                     }
                  }
               }
            }

            if (needDrop) {
               location.getWorld().dropItem(location, item);
            }
         } else {
            player.getInventory().addItem(new ItemStack[]{item});
         }
      } else {
         player.getInventory().addItem(new ItemStack[]{item});
      }
   }
}

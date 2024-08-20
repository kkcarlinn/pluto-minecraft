package br.com.plutomc.core.bukkit.utils.menu.confirm;

import br.com.plutomc.core.bukkit.utils.menu.MenuInventory;
import br.com.plutomc.core.bukkit.utils.item.ItemBuilder;
import br.com.plutomc.core.bukkit.utils.menu.click.ClickType;
import br.com.plutomc.core.bukkit.utils.menu.click.MenuClickHandler;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class ConfirmInventory {
   public ConfirmInventory(Player player, String confirmTitle, final ConfirmHandler handler, final MenuInventory topInventory) {
      final MenuInventory menu = new MenuInventory(confirmTitle, 4);
      MenuClickHandler confirm = new MenuClickHandler() {
         @Override
         public void onClick(Player p, Inventory inv, ClickType type, ItemStack stack, int slot) {
            handler.onConfirm(true);
         }
      };
      MenuClickHandler noConfirm = new MenuClickHandler() {
         @Override
         public void onClick(Player p, Inventory inv, ClickType type, ItemStack stack, int slot) {
            handler.onConfirm(false);
            if (topInventory != null) {
               topInventory.open(p);
            } else {
               menu.close(p);
            }
         }
      };
      menu.setItem(10, new ItemBuilder().type(Material.STAINED_GLASS_PANE).durability(14).name("§cRejeitar").build(), noConfirm);
      menu.setItem(11, new ItemBuilder().type(Material.STAINED_GLASS_PANE).durability(14).name("§cRejeitar").build(), noConfirm);
      menu.setItem(19, new ItemBuilder().type(Material.STAINED_GLASS_PANE).durability(14).name("§cRejeitar").build(), noConfirm);
      menu.setItem(20, new ItemBuilder().type(Material.STAINED_GLASS_PANE).durability(14).name("§cRejeitar").build(), noConfirm);
      menu.setItem(15, new ItemBuilder().type(Material.STAINED_GLASS_PANE).durability(5).name("§aAceitar").build(), confirm);
      menu.setItem(16, new ItemBuilder().type(Material.STAINED_GLASS_PANE).durability(5).name("§aAceitar").build(), confirm);
      menu.setItem(24, new ItemBuilder().type(Material.STAINED_GLASS_PANE).durability(5).name("§aAceitar").build(), confirm);
      menu.setItem(25, new ItemBuilder().type(Material.STAINED_GLASS_PANE).durability(5).name("§aAceitar").build(), confirm);
      menu.open(player);
   }

   public interface ConfirmHandler {
      void onConfirm(boolean var1);
   }
}

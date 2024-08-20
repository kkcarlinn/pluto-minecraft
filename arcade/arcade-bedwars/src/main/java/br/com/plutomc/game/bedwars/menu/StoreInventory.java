package br.com.plutomc.game.bedwars.menu;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import br.com.plutomc.game.bedwars.GameMain;
import br.com.plutomc.game.bedwars.gamer.Gamer;
import br.com.plutomc.game.bedwars.store.ShopCategory;
import br.com.plutomc.game.bedwars.utils.GamerHelper;
import br.com.plutomc.game.engine.ArcadeCommon;
import br.com.plutomc.game.bedwars.island.Island;
import br.com.plutomc.core.bukkit.utils.item.ItemBuilder;
import br.com.plutomc.core.bukkit.utils.menu.MenuInventory;
import br.com.plutomc.core.bukkit.utils.menu.click.ClickType;
import br.com.plutomc.core.common.language.Language;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class StoreInventory {
   public StoreInventory(Player player) {
      this(player, ShopCategory.FAVORITES);
   }

   public StoreInventory(final Player player, ShopCategory storeCategory) {
      Island island = GameMain.getInstance().getIslandManager().getIsland(player.getUniqueId());
      if (island != null) {
         Gamer gamer = ArcadeCommon.getInstance().getGamerManager().getGamer(player.getUniqueId(), Gamer.class);
         if (gamer.isAlive()) {
            MenuInventory menuInventory = new MenuInventory("§7" + storeCategory.getName(), 6);

            for(int i = 0; i < ShopCategory.values().length; ++i) {
               ShopCategory category = ShopCategory.values()[i];
               menuInventory.setItem(
                  i,
                  new ItemBuilder()
                     .name("§a" + category.getName())
                     .type(category.getMaterial())
                     .lore(category == storeCategory ? "" : "§e§%click-to-see%§")
                     .build(),
                  (p, inv, type, stack, slot) -> {
                     new StoreInventory(player, category);
                     player.playSound(player.getLocation(), Sound.CLICK, 1.0F, 1.0F);
                  }
               );
            }

            for(int i = 0; i < 8; ++i) {
               menuInventory.setItem(
                  9 + i, new ItemBuilder().name(" ").type(Material.STAINED_GLASS_PANE).durability(storeCategory.ordinal() == i ? 5 : 15).build()
               );
            }

            List<ShopCategory.ShopItem> list = new ArrayList<>(storeCategory.getShopItem());
            switch(storeCategory) {
               case FAVORITES:
                  for(Entry<ShopCategory, Set<Integer>> entry : gamer.getFavoriteMap().entrySet()) {
                     ShopCategory shopCategory = entry.getKey();
                     List<ShopCategory.ShopItem> shopItems = shopCategory.getShopItem();

                     for(Integer integer : entry.getValue()) {
                        if (integer >= 0 && integer < shopItems.size()) {
                           list.add(shopItems.get(integer));
                        }
                     }
                  }

                  int w = 19;

                  for(int i = 0; storeCategory == ShopCategory.FAVORITES ? i < 21 : i < list.size(); ++i) {
                     if (i < list.size()) {
                        ShopCategory.ShopItem item = list.get(i);
                        if (item.getStack().getType() == Material.GOLD_AXE) {
                           item = gamer.getAxeLevel().getNext().getAsShopItem();
                        } else if (item.getStack().getType() == Material.GOLD_PICKAXE) {
                           item = gamer.getPickaxeLevel().getNext().getAsShopItem();
                        }

                        this.handleItem(menuInventory, gamer, storeCategory, list.indexOf(item), item, w);
                     } else {
                        menuInventory.setItem(w, new ItemBuilder().name(" ").type(Material.STAINED_GLASS_PANE).durability(15).build());
                     }

                     if (w % 9 == 7) {
                        w += 3;
                     } else {
                        ++w;
                     }
                  }
                  break;
               default:
                  int i1 = 19;

                  for(int i = 0; i < list.size(); ++i) {
                     ShopCategory.ShopItem item = list.get(i);
                     if (item.getStack().getType() == Material.GOLD_AXE) {
                        item = gamer.getAxeLevel().getNext().getAsShopItem();
                     } else if (item.getStack().getType() == Material.GOLD_PICKAXE) {
                        item = gamer.getPickaxeLevel().getNext().getAsShopItem();
                     }

                     this.handleItem(menuInventory, gamer, storeCategory, list.indexOf(item), item, i1);
                     if (i1 % 9 == 7) {
                        i1 += 3;
                     } else {
                        ++i1;
                     }
                  }
            }

            menuInventory.open(player);
            (new BukkitRunnable() {
               @Override
               public void run() {
                  player.updateInventory();
               }
            }).runTaskLater(ArcadeCommon.getInstance(), 1L);
         }
      }
   }

   public void handleItem(MenuInventory menuInventory, Gamer gamer, ShopCategory storeCategory, int index, ShopCategory.ShopItem shopItem, int slot) {
      menuInventory.setItem(slot, this.createItem(gamer.getPlayer(), shopItem), (p, inv, type, stack, s) -> {
         if (type == ClickType.SHIFT) {
            if (storeCategory == ShopCategory.FAVORITES) {
               if (gamer.removeFavorite(shopItem)) {
                  p.sendMessage("§aO item " + ChatColor.stripColor(stack.getItemMeta().getDisplayName()) + " foi removido dos favoritos.");
                  new StoreInventory(p, storeCategory);
               }
            } else {
               new FavoriteConfigInventory(p, storeCategory, index, shopItem);
            }
         } else {
            this.buy(p, shopItem);
            new StoreInventory(p, storeCategory);
         }
      });
   }

   private ItemStack createItem(Player player, ShopCategory.ShopItem shopItem) {
      Language language = Language.getLanguage(player.getUniqueId());
      ItemBuilder itemBuilder = ItemBuilder.fromStack(shopItem.getStack())
         .name(
            (player.getInventory().contains(shopItem.getPrice().getMaterial(), shopItem.getPrice().getAmount()) ? "§a" : "§c")
               + (
                  shopItem.getStack().getItemMeta().hasDisplayName()
                     ? shopItem.getStack().getItemMeta().getDisplayName()
                     : language.t(shopItem.getStack().getType().name().toLowerCase().replace("_", "-"))
               )
         )
         .clearLore()
         .flag(ItemFlag.HIDE_POTION_EFFECTS)
         .lore(
            "§7Preço: §7"
               + this.getColor(shopItem.getPrice().getMaterial())
               + shopItem.getPrice().getAmount()
               + " "
               + language.t("bedwars.buy." + shopItem.getPrice().getMaterial().name().toLowerCase().replace("_", "-"))
         );
      if (shopItem.getStack().getItemMeta().getLore() != null && !shopItem.getStack().getItemMeta().getLore().isEmpty()) {
         itemBuilder.lore(shopItem.getStack().getItemMeta().getLore());
      }

      itemBuilder.lore("").lore(language.t("bedwars.store-inventory." + shopItem.getStack().getType().name().toLowerCase().replace("_", "-") + ".description"));
      return itemBuilder.build();
   }

   public ChatColor getColor(Material material) {
      return material.name().contains("EMERALD")
         ? ChatColor.DARK_GREEN
         : (material.name().contains("GOLD") ? ChatColor.GOLD : (material.name().contains("DIAMOND") ? ChatColor.AQUA : ChatColor.WHITE));
   }

   public void buy(Player player, ShopCategory.ShopItem shopItem) {
      if (player.getInventory().contains(shopItem.getPrice().getMaterial(), shopItem.getPrice().getAmount())) {
         GamerHelper.buyItem(player, shopItem);
      } else {
         player.sendMessage("§cVocê não possui material suficiente.");
         player.playSound(player.getLocation(), Sound.NOTE_BASS, 1.0F, 1.0F);
      }
   }
}

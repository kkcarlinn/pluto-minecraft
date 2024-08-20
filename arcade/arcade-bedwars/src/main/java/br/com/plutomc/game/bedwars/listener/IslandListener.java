package br.com.plutomc.game.bedwars.listener;

import br.com.plutomc.game.bedwars.GameConst;
import br.com.plutomc.game.bedwars.GameMain;
import br.com.plutomc.game.bedwars.event.PlayerBoughtItemEvent;
import br.com.plutomc.game.bedwars.gamer.Gamer;
import br.com.plutomc.game.engine.ArcadeCommon;
import br.com.plutomc.game.bedwars.island.Island;
import br.com.plutomc.game.bedwars.island.IslandUpgrade;
import br.com.plutomc.core.bukkit.utils.item.ItemBuilder;
import br.com.plutomc.core.common.language.Language;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

public class IslandListener implements Listener {
   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onPlayerBoughtItem(PlayerBoughtItemEvent event) {
      Player player = event.getPlayer();
      Gamer gamer = ArcadeCommon.getInstance().getGamerManager().getGamer(player.getUniqueId(), Gamer.class);
      Island island = GameMain.getInstance().getIslandManager().getIsland(player.getUniqueId());
      if (island != null) {
         switch(event.getItemStack().getType()) {
            case COMPASS:
               player.getInventory().remove(Material.COMPASS);
               event.setItemStack(GameConst.FINDER.getItemStack());
               break;
            case WOOL:
               event.setItemStack(ItemBuilder.fromStack(event.getItemStack()).durability(island.getIslandColor().getWoolId()).build());
               break;
            case HARD_CLAY:
               event.setItemStack(
                  ItemBuilder.fromStack(event.getItemStack()).type(Material.STAINED_CLAY).durability(island.getIslandColor().getWoolId()).build()
               );
               break;
            case SHEARS:
               if (gamer.isShears()) {
                  event.setCancelled(true);
                  player.sendMessage(Language.getLanguage(player.getUniqueId()).t("bedwars.you-already-have-shears"));
               } else {
                  gamer.setShears(true);
               }
               break;
            case DIAMOND_AXE:
            case GOLD_AXE:
            case IRON_AXE:
            case STONE_AXE:
            case WOOD_AXE:
               if (gamer.getAxeLevel() == gamer.getAxeLevel().getNext()) {
                  event.setCancelled(true);
                  player.sendMessage("");
                  return;
               }

               gamer.setAxeLevel(gamer.getAxeLevel().getNext());
               int slot = -1;

               for(int i = 0; i < player.getInventory().getContents().length; ++i) {
                  ItemStack itemStack = player.getInventory().getContents()[i];
                  if (itemStack != null && itemStack.getType() == gamer.getAxeLevel().getPrevious().getItemStack().getType()) {
                     player.getInventory().removeItem(new ItemStack[]{itemStack});
                     slot = i;
                     break;
                  }
               }

               if (slot == -1) {
                  player.getInventory().addItem(new ItemStack[]{gamer.getAxeLevel().getItemStack()});
               } else {
                  player.getInventory().setItem(slot, gamer.getAxeLevel().getItemStack());
               }

               event.setItemStack(null);
               break;
            case DIAMOND_PICKAXE:
            case GOLD_PICKAXE:
            case IRON_PICKAXE:
            case STONE_PICKAXE:
            case WOOD_PICKAXE:
               if (gamer.getPickaxeLevel() == gamer.getPickaxeLevel().getNext()) {
                  event.setCancelled(true);
                  return;
               }

               gamer.setPickaxeLevel(gamer.getPickaxeLevel().getNext());
               if (gamer.getPickaxeLevel().ordinal() == 1) {
                  player.getInventory().addItem(new ItemStack[]{gamer.getPickaxeLevel().getItemStack()});
               } else {
                  int i1 = -1;

                  for(int i = 0; i < player.getInventory().getContents().length; ++i) {
                     ItemStack itemStack = player.getInventory().getContents()[i];
                     if (itemStack != null && itemStack.getType() == gamer.getPickaxeLevel().getPrevious().getItemStack().getType()) {
                        player.getInventory().removeItem(new ItemStack[]{itemStack});
                        i1 = i;
                        break;
                     }
                  }

                  if (i1 == -1) {
                     player.getInventory().addItem(new ItemStack[]{gamer.getPickaxeLevel().getItemStack()});
                  } else {
                     player.getInventory().setItem(i1, gamer.getPickaxeLevel().getItemStack());
                  }
               }

               event.setItemStack(null);
               break;
            case STONE_SWORD:
            case IRON_SWORD:
            case DIAMOND_SWORD:
               String swordType = event.getItemStack().getType().name().split("_")[0];
               Gamer.SwordLevel swordLevel = Gamer.SwordLevel.valueOf(swordType);
               if (gamer.getSwordLevel() != swordLevel && gamer.getSwordLevel().ordinal() <= swordLevel.ordinal()) {
                  if (gamer.getSwordLevel() == Gamer.SwordLevel.WOOD) {
                     int slot1 = -1;

                     for(int i = 0; i < player.getInventory().getContents().length; ++i) {
                        ItemStack itemStack = player.getInventory().getContents()[i];
                        if (itemStack != null && itemStack.getType() == Material.valueOf(gamer.getSwordLevel().name() + "_SWORD")) {
                           player.getInventory().removeItem(new ItemStack[]{itemStack});
                           slot1 = i;
                           break;
                        }
                     }

                     if (slot1 != -1) {
                        player.getInventory()
                           .setItem(
                              slot1,
                              ItemBuilder.fromStack(event.getItemStack())
                                 .enchantment(Enchantment.DAMAGE_ALL, island.getUpgradeLevel(IslandUpgrade.SHARPNESS))
                                 .build()
                           );
                     }
                  } else {
                     player.getInventory()
                        .addItem(
                           new ItemStack[]{
                              new ItemBuilder()
                                 .type(Material.valueOf(swordLevel.name() + "_SWORD"))
                                 .enchantment(Enchantment.DAMAGE_ALL, island.getUpgradeLevel(IslandUpgrade.SHARPNESS))
                                 .build()
                           }
                        );
                  }

                  gamer.setSwordLevel(swordLevel);
               } else {
                  player.getInventory()
                     .addItem(
                        new ItemStack[]{
                           ItemBuilder.fromStack(event.getItemStack())
                              .enchantment(Enchantment.DAMAGE_ALL, island.getUpgradeLevel(IslandUpgrade.SHARPNESS))
                              .build()
                        }
                     );
               }

               event.setItemStack(null);
               break;
            case DIAMOND_BOOTS:
            case IRON_BOOTS:
            case CHAINMAIL_BOOTS:
               String start = event.getItemStack().getType().name().split("_")[0];
               Gamer.ArmorLevel armorLevel = Gamer.ArmorLevel.valueOf(start);
               if (gamer.getArmorLevel() == armorLevel) {
                  event.setCancelled(true);
                  player.sendMessage("§cVocê já está usando essa armadura.");
                  return;
               }

               if (armorLevel.ordinal() < gamer.getArmorLevel().ordinal()) {
                  event.setCancelled(true);
                  player.sendMessage("§cVocê não pode comprar essa armadura enquanto estiver usando uma de nível superior.");
                  return;
               }

               player.getInventory()
                  .setLeggings(
                     new ItemBuilder()
                        .type(Material.valueOf(start + "_LEGGINGS"))
                        .enchantment(Enchantment.PROTECTION_ENVIRONMENTAL, island.getUpgradeLevel(IslandUpgrade.ARMOR_REINFORCEMENT))
                        .build()
                  );
               player.getInventory()
                  .setBoots(
                     new ItemBuilder()
                        .type(Material.valueOf(start + "_BOOTS"))
                        .enchantment(Enchantment.PROTECTION_ENVIRONMENTAL, island.getUpgradeLevel(IslandUpgrade.ARMOR_REINFORCEMENT))
                        .build()
                  );
               gamer.setArmorLevel(armorLevel);
               event.setItemStack(null);
         }
      }
   }
}

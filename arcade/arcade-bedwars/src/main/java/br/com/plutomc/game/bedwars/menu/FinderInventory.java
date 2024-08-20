package br.com.plutomc.game.bedwars.menu;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import br.com.plutomc.game.bedwars.GameMain;
import br.com.plutomc.game.bedwars.gamer.Gamer;
import br.com.plutomc.game.engine.ArcadeCommon;
import br.com.plutomc.game.bedwars.island.Island;
import br.com.plutomc.core.bukkit.utils.item.ItemBuilder;
import br.com.plutomc.core.bukkit.utils.menu.MenuInventory;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class FinderInventory {
   public FinderInventory(Player player) {
      Island playerIsland = GameMain.getInstance().getIslandManager().getIsland(player.getUniqueId());
      if (playerIsland != null) {
         Gamer gamer = ArcadeCommon.getInstance().getGamerManager().getGamer(player.getUniqueId(), Gamer.class);
         if (gamer.isAlive()) {
            List<Island> islands = GameMain.getInstance()
               .getIslandManager()
               .getIslands()
               .stream()
               .filter(islandx -> islandx.getIslandColor() != playerIsland.getIslandColor() && islandx.getIslandStatus() != Island.IslandStatus.LOSER)
               .collect(Collectors.toList());
            if (islands.stream().filter(islandx -> islandx.getIslandStatus() == Island.IslandStatus.ALIVE).count() > 0L) {
               player.sendMessage("§cVocê só poderá usar a bússola quando todas as camas foram quebradas.");
            } else {
               MenuInventory menuInventory = new MenuInventory("§7Rastreador", 4);
               int slot = 10;

               for(Island island : islands) {
                  if (playerIsland.getIslandColor() != island.getIslandColor() && island.getIslandStatus() != Island.IslandStatus.LOSER) {
                     menuInventory.setItem(
                        slot,
                        new ItemBuilder()
                           .name(island.getIslandColor().getColor() + "§%" + island.getIslandColor().name().toLowerCase() + "-name%§")
                           .type(Material.WOOL)
                           .durability(island.getIslandColor().getWoolId())
                           .lore(
                              "\n§7Clique para ativar o rastreador no time "
                                 + island.getIslandColor().getColor()
                                 + "§%"
                                 + island.getIslandColor().name().toLowerCase()
                                 + "-name%§"
                           )
                           .build(),
                        (p, inv, type, stack, s) -> {
                           int amount = (int)Arrays.asList(player.getInventory().getContents())
                              .stream()
                              .filter(
                                 itemStackx -> itemStackx != null
                                       && itemStackx.getType() == Material.COMPASS
                                       && itemStackx.getEnchantmentLevel(Enchantment.DURABILITY) == 1
                              )
                              .count();
                           if (amount >= 1) {
                              player.sendMessage("§cCompre outro rastreador para poder marcar outro jogador.");
                           } else {
                              Player nearPlayer = island.stream(false)
                                 .sorted((o1, o2) -> (int)(o1.getLocation().distance(p.getLocation()) - o2.getLocation().distance(p.getLocation())))
                                 .findFirst()
                                 .orElse(null);
                              if (nearPlayer == null) {
                                 p.sendMessage("§cNenhum jogador deste time para rastrear no momento.");
                              } else {
                                 for(int i = 0; i < p.getInventory().getContents().length; ++i) {
                                    ItemStack itemStack = p.getInventory().getContents()[i];
                                    if (itemStack != null && itemStack.getType() == Material.COMPASS) {
                                       p.getInventory()
                                          .setItem(
                                             i,
                                             ItemBuilder.fromStack(itemStack)
                                                .name(island.getIslandColor().getColor() + "Time §%" + island.getIslandColor().name().toLowerCase() + "-name%§")
                                                .enchantment(Enchantment.DURABILITY, 1)
                                                .build()
                                          );
                                       break;
                                    }
                                 }
   
                                 p.setMetadata("player-target", GameMain.getInstance().createMeta(nearPlayer.getUniqueId().toString()));
                              }
                           }
                        }
                     );
                     if (slot % 9 == 7) {
                        slot += 3;
                     } else {
                        ++slot;
                     }
                  }
               }

               menuInventory.open(player);
            }
         }
      }
   }
}

package br.com.plutomc.game.bedwars.menu;

import java.util.stream.Collectors;

import br.com.plutomc.game.bedwars.GameMain;
import br.com.plutomc.game.bedwars.gamer.Gamer;
import br.com.plutomc.core.common.CommonConst;
import br.com.plutomc.game.bedwars.island.Island;
import br.com.plutomc.core.bukkit.utils.item.ItemBuilder;
import br.com.plutomc.core.bukkit.utils.menu.MenuInventory;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class SpectatorInventory {
   public SpectatorInventory(Player player) {
      MenuInventory menuInventory = new MenuInventory("§7Espectadores", 5);
      int w = 10;

      for(Gamer gamer : GameMain.getInstance().getGamerManager().getGamers(Gamer.class).stream().collect(Collectors.toList())) {
         Island island = GameMain.getInstance().getIslandManager().getIsland(gamer.getUniqueId());
         if (island != null && island.getIslandStatus() != Island.IslandStatus.LOSER) {
            Player playerGamer = gamer.getPlayer();
            menuInventory.setItem(
               w,
               new ItemBuilder()
                  .name((gamer.isOnline() && gamer.isAlive() ? "§a" : "§e") + gamer.getPlayerName())
                  .type(Material.SKULL_ITEM)
                  .durability(3)
                  .lore(
                     "§fVida: §7"
                        + (!gamer.isOnline() ? "0" : CommonConst.DECIMAL_FORMAT.format(playerGamer.getHealth() / playerGamer.getMaxHealth() * 100.0))
                        + "%",
                     "§fTime: " + island.getIslandColor().getColor() + "§%" + island.getIslandColor().name().toLowerCase() + "-name%§",
                     "",
                     "§eClique para teletransportar."
                  )
                  .build(),
               (p, inv, type, stack, slot) -> {
                  p.teleport(playerGamer);
                  p.closeInventory();
               }
            );
            if (w % 9 == 7) {
               w += 3;
            } else {
               ++w;
            }
         }
      }

      menuInventory.open(player);
   }
}

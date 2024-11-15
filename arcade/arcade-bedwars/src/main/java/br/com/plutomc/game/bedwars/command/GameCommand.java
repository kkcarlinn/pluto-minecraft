package br.com.plutomc.game.bedwars.command;

import br.com.plutomc.game.bedwars.GameMain;
import br.com.plutomc.game.bedwars.store.ShopCategory;
import br.com.plutomc.game.bedwars.utils.GamerHelper;
import com.google.common.base.Joiner;
import br.com.plutomc.game.engine.ArcadeCommon;
import br.com.plutomc.game.bedwars.island.Island;
import br.com.plutomc.core.bukkit.account.BukkitAccount;
import br.com.plutomc.core.bukkit.utils.item.ItemBuilder;
import br.com.plutomc.core.bukkit.utils.menu.MenuInventory;
import br.com.plutomc.core.common.command.CommandArgs;
import br.com.plutomc.core.common.command.CommandClass;
import br.com.plutomc.core.common.command.CommandFramework;
import br.com.plutomc.core.common.account.status.Status;
import br.com.plutomc.core.common.account.status.StatusType;
import br.com.plutomc.core.common.account.status.types.BedwarsCategory;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class GameCommand implements CommandClass {
   @CommandFramework.Command(
      name = "global",
      aliases = {"shout", "g"},
      console = false
   )
   public void globalCommand(CommandArgs cmdArgs) {
      Player sender = cmdArgs.getSenderAsMember(BukkitAccount.class).getPlayer();
      if (GameMain.getInstance().getPlayersPerTeam() == 1) {
         sender.sendMessage("§cO comando está desativado nessa sala.");
      } else {
         String[] args = cmdArgs.getArgs();
         if (!ArcadeCommon.getInstance().getState().isGametime()) {
            sender.sendMessage("§cO jogo ainda não começou.");
         } else if (args.length == 0) {
            sender.sendMessage("§eUse §b/" + cmdArgs.getLabel() + " <message>§e para mandar uma mensagem no servidor.");
         } else {
            Island island = GameMain.getInstance().getIslandManager().getIsland(sender.getUniqueId());
            if (island == null) {
               sender.sendMessage("§cSomente jogadores com uma ilha podem utilizar esse comando.");
            } else {
               if (island.getIslandStatus() == Island.IslandStatus.LOSER) {
                  sender.sendMessage("§cVocê não pode mais falar no chat.");
               } else {
                  Status status = ArcadeCommon.getInstance().getPlugin().getStatusManager().loadStatus(sender.getUniqueId(), StatusType.BEDWARS);
                  int level = status.getInteger(BedwarsCategory.BEDWARS_LEVEL);
                  String message = GameMain.getInstance().createMessage(sender, Joiner.on(' ').join(args), island, true, true, level);
                  Bukkit.getOnlinePlayers().forEach(ps -> ps.sendMessage(message));
               }
            }
         }
      }
   }

   @CommandFramework.Command(
      name = "rastreador",
      aliases = {"bussola", "compass"},
      console = false
   )
   public void rastreadorCommand(CommandArgs cmdArgs) {
      MenuInventory menuInventory = new MenuInventory("§7Rastreador", 3);
      menuInventory.setItem(
         13,
         new ItemBuilder().name("§aRastreador").lore("§22 esmeraldas").type(Material.COMPASS).build(),
         (player, inv, type, stack, slot) -> GamerHelper.buyItem(player, new ShopCategory.ShopItem(stack, new ShopCategory.ShopPrice(Material.EMERALD, 2)))
      );
      menuInventory.open(cmdArgs.getSenderAsMember(BukkitAccount.class).getPlayer());
   }
}

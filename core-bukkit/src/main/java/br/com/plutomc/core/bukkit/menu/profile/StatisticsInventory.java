package br.com.plutomc.core.bukkit.menu.profile;

import br.com.plutomc.core.bukkit.BukkitCommon;
import br.com.plutomc.core.bukkit.utils.item.ItemBuilder;
import br.com.plutomc.core.bukkit.utils.menu.MenuInventory;
import br.com.plutomc.core.common.CommonPlugin;
import br.com.plutomc.core.common.account.status.Status;
import br.com.plutomc.core.common.account.status.StatusType;
import br.com.plutomc.core.common.account.status.types.*;
import br.com.plutomc.core.common.server.ServerType;
import br.com.plutomc.core.common.utils.string.StringFormat;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class StatisticsInventory {
   public StatisticsInventory(Player player, StatusType statusType) {
      MenuInventory menuInventory = new MenuInventory("§7Suas estatísticas", 3);
      if (statusType == null) {
         menuInventory.setItem(
            10,
            new ItemBuilder().type(Material.BED).name("§aBedwars").lore("§7Suas estatísticas no Bedwars.").build(),
            (p, inv, t, stack, s) -> new StatisticsInventory(player, StatusType.BEDWARS)
         );
         menuInventory.setItem(
            11,
            new ItemBuilder().type(Material.IRON_CHESTPLATE).name("§aPvP").lore("§7Suas estatísticas no PvP.").build(),
            (p, inv, t, stack, s) -> new StatisticsInventory(player, StatusType.PVP)
         );
         menuInventory.setItem(
                 12,
                 new ItemBuilder().type(Material.DIAMOND_SWORD).name("§aDuels").lore("§7Suas estatísticas do Duels.").build(),
                 (p, inv, t, stack, s) -> new StatisticsInventory(player, StatusType.DUEL)
         );
         menuInventory.setItem(
            22,
            new ItemBuilder().type(Material.ARROW).name("§a§%back%§").lore("§7Para Suas estatísticas").build(),
            (p, inv, type, stack, slot) -> new ProfileInventory(player)
         );
      } else {
         Status status = CommonPlugin.getInstance().getStatusManager().loadStatus(player.getUniqueId(), statusType);
         switch(statusType) {
            case BEDWARS:
               menuInventory.setRows(5);
               menuInventory.setItem(
                  4,
                  new ItemBuilder()
                     .name("§aBedwars Geral")
                     .type(Material.PAPER)
                     .lore(
                        "§fNível: §7" + BukkitCommon.getInstance().getColorByLevel(status.getInteger(BedwarsCategory.BEDWARS_LEVEL)),
                        "",
                        "§fPartidas: §7" + status.getInteger(BedwarsCategory.BEDWARS_MATCH),
                        "",
                        "§fCamas quebradas: §7" + status.getInteger(BedwarsCategory.BEDWARS_BED_BREAK),
                        "§fCamas perdidas: §7" + status.getInteger(BedwarsCategory.BEDWARS_BED_BROKEN),
                        "",
                        "§fKills: §7" + status.getInteger(BedwarsCategory.BEDWARS_KILLS),
                        "§fKills finais: §7" + status.getInteger(BedwarsCategory.BEDWARS_FINAL_KILLS),
                        "§fMortes: §7" + status.getInteger(BedwarsCategory.BEDWARS_DEATHS),
                        "§fMortes finais: §7" + status.getInteger(BedwarsCategory.BEDWARS_FINAL_DEATHS),
                        "",
                        "§fWins: §7" + status.getInteger(BedwarsCategory.BEDWARS_WINS),
                        "§fWinstreak: §7" + status.getInteger(BedwarsCategory.BEDWARS_WINSTREAK),
                        "§fDerrotas: §7" + status.getInteger(BedwarsCategory.BEDWARS_LOSES)
                     )
                     .build()
               );
               int w = 10;

               for(ServerType serverType : Arrays.asList(ServerType.values())) {
                  if (serverType.name().contains("BW") && !serverType.isLobby()) {
                     menuInventory.setItem(
                        w,
                        new ItemBuilder()
                           .type(Material.BED)
                           .name("§aBedwars " + StringFormat.formatString(serverType.name().split("_")[1]))
                           .lore(
                              "§fPartidas: §7" + status.getInteger(BedwarsCategory.BEDWARS_MATCH.getSpecialServer(serverType)),
                              "",
                              "§fCamas quebradas: §7" + status.getInteger(BedwarsCategory.BEDWARS_BED_BREAK.getSpecialServer(serverType)),
                              "§fCamas perdidas: §7" + status.getInteger(BedwarsCategory.BEDWARS_BED_BROKEN.getSpecialServer(serverType)),
                              "",
                              "§fKills: §7" + status.getInteger(BedwarsCategory.BEDWARS_KILLS.getSpecialServer(serverType)),
                              "§fKills finais: §7" + status.getInteger(BedwarsCategory.BEDWARS_FINAL_KILLS.getSpecialServer(serverType)),
                              "§fMortes: §7" + status.getInteger(BedwarsCategory.BEDWARS_DEATHS.getSpecialServer(serverType)),
                              "§fMortes finais: §7" + status.getInteger(BedwarsCategory.BEDWARS_FINAL_DEATHS.getSpecialServer(serverType)),
                              "",
                              "§fWins: §7" + status.getInteger(BedwarsCategory.BEDWARS_WINS.getSpecialServer(serverType)),
                              "§fWinstreak: §7" + status.getInteger(BedwarsCategory.BEDWARS_WINSTREAK.getSpecialServer(serverType)),
                              "§fDerrotas: §7" + status.getInteger(BedwarsCategory.BEDWARS_LOSES.getSpecialServer(serverType))
                           )
                           .build()
                     );
                     if (w % 9 == 7) {
                        w += 12;
                     } else {
                        w += 2;
                     }
                  }
               }
               break;
            case PVP:
               menuInventory.setItem(
                  10,
                  new ItemBuilder()
                     .type(Material.PAPER)
                     .name("§aGeral")
                     .lore(
                        "§fKills: §7" + status.getInteger("kills", 0),
                        "§fDeaths: §7" + status.getInteger("deaths", 0),
                        "§fKillstreak: §7" + status.getInteger("killstreak", 0),
                        "§fKillstreak máximo: §7" + status.getInteger("killstreak-max", 0)
                     )
                     .build()
               );
               menuInventory.setItem(
                  11,
                  new ItemBuilder()
                     .type(Material.IRON_CHESTPLATE)
                     .name("§aArena")
                     .lore("§fKills: §70", "§fDeaths: §70", "§fKillstreak: §70", "§fKillstreak máximo: §70", "")
                     .build()
               );
               menuInventory.setItem(
                  12,
                  new ItemBuilder()
                     .type(Material.GLASS)
                     .name("§aFps")
                     .lore(
                        "§fKills: §7" + status.getInteger("fps-kills", 0),
                        "§fDeaths: §7" + status.getInteger("fps-deaths", 0),
                        "§fKillstreak: §7" + status.getInteger("fps-killstreak", 0),
                        "§fKillstreak máximo: §7" + status.getInteger("fps-killstreak-max", 0)
                     )
                     .build()
               );
               menuInventory.setItem(13, new ItemBuilder().type(Material.LAVA_BUCKET).name("§aLava").lore("").build());
               break;

            case DUEL:
               menuInventory.setItem(
                       10,
                       new ItemBuilder()
                               .type(Material.PAPER)
                               .name("§aGapple")
                               .lore(
                                       "§fKills: §7" + status.getInteger(GappleCategory.GAPPLE_KILLS),
                                       "§fDeaths: §7" + status.getInteger(GappleCategory.GAPPLE_DEATHS),
                                       "",
                                       "§fVitórias: §7" + status.getInteger(GappleCategory.GAPPLE_WINS),
                                       "§fDerrotas: §7" + status.getInteger(GappleCategory.GAPPLE_LOSSES),
                                       "§fWinstreak: §7" + + status.getInteger(GappleCategory.GAPPLE_WINSTREAK)
                               )
                               .build()
               );
               menuInventory.setItem(
                       12,
                       new ItemBuilder()
                               .type(Material.PAPER)
                               .name("§aBoxing")
                               .lore(
                                       "§fKills: §7" + status.getInteger(BoxingCategory.BOXING_KILLS),
                                       "§fDeaths: §7" + status.getInteger(BoxingCategory.BOXING_DEATHS),
                                       "",
                                       "§fVitórias: §7" + status.getInteger(BoxingCategory.BOXING_WINS),
                                       "§fDerrotas: §7" + status.getInteger(BoxingCategory.BOXING_LOSSES),
                                       "§fWinstreak: §7" + + status.getInteger(BoxingCategory.BOXING_WINSTREAK)
                               )
                               .build()
               );

               menuInventory.setItem(
                       14,
                       new ItemBuilder()
                               .type(Material.PAPER)
                               .name("§aNoDebuff")
                               .lore(
                                       "§fKills: §7" + status.getInteger(NodebuffCategory.NODEBUFF_KILLS),
                                       "§fDeaths: §7" + status.getInteger(NodebuffCategory.NODEBUFF_DEATHS),
                                       "",
                                       "§fVitórias: §7" + status.getInteger(NodebuffCategory.NODEBUFF_WINS),
                                       "§fDerrotas: §7" + status.getInteger(NodebuffCategory.NODEBUFF_LOSSES),
                                       "§fWinstreak: §7" + + status.getInteger(NodebuffCategory.NODEBUFF_WINSTREAK)
                               )
                               .build()
               );

               menuInventory.setItem(
                       16,
                       new ItemBuilder()
                               .type(Material.PAPER)
                               .name("§aScrim")
                               .lore(
                                       "§fKills: §7" + status.getInteger(ScrimCategory.SCRIM_KILLS),
                                       "§fDeaths: §7" + status.getInteger(ScrimCategory.SCRIM_DEATHS),
                                       "",
                                       "§fVitórias: §7" + status.getInteger(ScrimCategory.SCRIM_WINS),
                                       "§fDerrotas: §7" + status.getInteger(ScrimCategory.SCRIM_LOSSES),
                                       "§fWinstreak: §7" + + status.getInteger(ScrimCategory.SCRIM_WINSTREAK)
                               )
                               .build()
               );
         }

         menuInventory.setItem(
            (menuInventory.getRows() - 1) * 9 + 4,
            new ItemBuilder().type(Material.ARROW).name("§a§%back%§").lore("§7Para Suas estatísticas").build(),
            (p, inv, type, stack, slot) -> new StatisticsInventory(player, null)
         );
      }

      menuInventory.open(player);
   }
}

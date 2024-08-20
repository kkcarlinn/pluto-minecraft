package br.com.plutomc.game.bedwars.listener;

import java.util.UUID;

import br.com.plutomc.game.bedwars.event.IslandWinEvent;
import br.com.plutomc.game.bedwars.event.PlayerKillPlayerEvent;
import br.com.plutomc.game.bedwars.event.island.IslandBedBreakEvent;
import br.com.plutomc.game.bedwars.gamer.Gamer;
import br.com.plutomc.game.bedwars.utils.GamerHelper;
import br.com.plutomc.core.common.CommonPlugin;
import br.com.plutomc.game.engine.ArcadeCommon;
import br.com.plutomc.game.bedwars.island.Island;
import br.com.plutomc.core.common.member.status.Status;
import br.com.plutomc.core.common.member.status.StatusType;
import br.com.plutomc.core.common.member.status.types.BedwarsCategory;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class StatusListener implements Listener {
   @EventHandler
   public void onIslandWin(IslandWinEvent event) {
      Island island = event.getIsland();

      for(UUID id : island.getTeam().getPlayerSet()) {
         Gamer gamer = ArcadeCommon.getInstance().getGamerManager().getGamer(id, Gamer.class);
         Status status = CommonPlugin.getInstance().getStatusManager().loadStatus(id, StatusType.BEDWARS);
         status.addInteger(BedwarsCategory.BEDWARS_POINTS, 50);
         status.addInteger(BedwarsCategory.BEDWARS_WINS, 1);
         status.addInteger(BedwarsCategory.BEDWARS_WINS.getSpecialServer(), 1);
         status.addInteger(BedwarsCategory.BEDWARS_WINSTREAK, 1);
         status.addInteger(BedwarsCategory.BEDWARS_WINSTREAK.getSpecialServer(), 1);
         status.save();
         gamer.checkLevel();
      }
   }

   @EventHandler
   public void onIslandBedBroken(IslandBedBreakEvent event) {
      Player player = event.getPlayer();
      if (player != null) {
         Status status = CommonPlugin.getInstance().getStatusManager().loadStatus(player.getUniqueId(), StatusType.BEDWARS);
         status.setInteger(BedwarsCategory.BEDWARS_POINTS, status.getInteger(BedwarsCategory.BEDWARS_POINTS) + 20);
         status.addInteger(BedwarsCategory.BEDWARS_BED_BREAK, 1);
         status.addInteger(BedwarsCategory.BEDWARS_BED_BREAK.getSpecialServer(), 1);
         status.save();
      }

      for(UUID playerId : event.getIsland().getTeam().getPlayerSet()) {
         Status status = CommonPlugin.getInstance().getStatusManager().loadStatus(playerId, StatusType.BEDWARS);
         status.addInteger(BedwarsCategory.BEDWARS_BED_BROKEN, 1);
         status.addInteger(BedwarsCategory.BEDWARS_BED_BROKEN.getSpecialServer(), 1);
      }
   }

   @EventHandler
   public void onPlayerDeath(PlayerDeathEvent event) {
      Player player = event.getEntity();
      Status status = CommonPlugin.getInstance().getStatusManager().loadStatus(player.getUniqueId(), StatusType.BEDWARS);
      status.setInteger(BedwarsCategory.BEDWARS_KILLSTREAK, 0);
      status.setInteger(BedwarsCategory.BEDWARS_KILLSTREAK.getSpecialServer(), 0);
      status.addInteger(BedwarsCategory.BEDWARS_DEATHS, 1);
      status.addInteger(BedwarsCategory.BEDWARS_DEATHS.getSpecialServer(), 1);
      status.save();
   }

   @EventHandler
   public void onPlayerKillPlayer(PlayerKillPlayerEvent event) {
      Player killer = event.getKiller();
      Status status = CommonPlugin.getInstance().getStatusManager().loadStatus(killer.getUniqueId(), StatusType.BEDWARS);
      status.addInteger(BedwarsCategory.BEDWARS_POINTS, 5);
      status.addInteger(BedwarsCategory.BEDWARS_KILLS, 1);
      status.addInteger(BedwarsCategory.BEDWARS_KILLS.getSpecialServer(), 1);
      status.addInteger(BedwarsCategory.BEDWARS_KILLSTREAK, 1);
      status.addInteger(BedwarsCategory.BEDWARS_KILLSTREAK.getSpecialServer(), 1);
      status.save();
      GamerHelper.updatePoints(killer, status);
      Gamer killerGamer = ArcadeCommon.getInstance().getGamerManager().getGamer(killer.getUniqueId(), Gamer.class);
      killerGamer.addKills(event.isFinalKill());
      killerGamer.checkLevel();
      if (event.isFinalKill()) {
         status.addInteger(BedwarsCategory.BEDWARS_POINTS, 10);
         status.addInteger(BedwarsCategory.BEDWARS_FINAL_KILLS, 1);
         status.addInteger(BedwarsCategory.BEDWARS_FINAL_KILLS.getSpecialServer(), 1);
         status = CommonPlugin.getInstance().getStatusManager().loadStatus(event.getPlayer().getUniqueId(), StatusType.BEDWARS);
         status.addInteger(BedwarsCategory.BEDWARS_FINAL_DEATHS, 1);
         status.addInteger(BedwarsCategory.BEDWARS_FINAL_DEATHS.getSpecialServer(), 1);
      }
   }
}

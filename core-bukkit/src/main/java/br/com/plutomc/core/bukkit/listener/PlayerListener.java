package br.com.plutomc.core.bukkit.listener;

import java.io.File;
import java.util.UUID;

import br.com.plutomc.core.bukkit.BukkitCommon;
import br.com.plutomc.core.bukkit.event.member.PlayerGroupChangeEvent;
import br.com.plutomc.core.bukkit.event.server.ServerPacketReceiveEvent;
import br.com.plutomc.core.bukkit.utils.player.PlayerHelper;
import br.com.plutomc.core.common.CommonPlugin;
import br.com.plutomc.core.bukkit.utils.scoreboard.ScoreHelper;
import br.com.plutomc.core.common.packet.types.ActionBar;
import br.com.plutomc.core.common.permission.Group;
import org.bukkit.Achievement;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class PlayerListener implements Listener {
   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void onPlayerJoin(PlayerJoinEvent event) {
      event.setJoinMessage(null);
      event.getPlayer().awardAchievement(Achievement.OPEN_INVENTORY);
      event.getPlayer().setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
   }

   @EventHandler
   public void onServerPacket(ServerPacketReceiveEvent event) {
      if (event.getPacket() instanceof ActionBar) {
         ActionBar actionBar = (ActionBar)event.getPacket();
         Player player = Bukkit.getPlayer(actionBar.getUniqueId());
         if (player != null) {
            PlayerHelper.actionbar(player, actionBar.getText());
         }
      }
   }

   @EventHandler
   public void onPlayerGroupChange(PlayerGroupChangeEvent event) {
      Group group = event.getGroup();
      if (group == null) {
            event.getPlayer().sendMessage("§aSeu grupo foi atualizado.");
      } else {
         String strippedColor = CommonPlugin.getInstance().getPluginInfo().getTagByGroup(group).getStrippedColor();
         switch(event.getAction()) {
            case ADD:
               event.getPlayer().sendMessage("§aO grupo " + group.getGroupName() + " foi adicionado a sua conta.");
               break;
            case REMOVE:
               event.getPlayer().sendMessage("§aO grupo " + group.getGroupName() + " foi removido de sua conta.");
               break;
            case SET:
               event.getPlayer().sendMessage("§aVocê se tornou " + group.getGroupName());
               break;
            default:
               event.getPlayer().sendMessage("§aSeu grupo foi atualizado para " + group.getGroupName());
         }
      }
   }

   @EventHandler(
      priority = EventPriority.HIGHEST
   )
   public void onPlayerQuitListener(PlayerQuitEvent event) {
      event.setQuitMessage(null);
      ScoreHelper.getInstance().removeScoreboard(event.getPlayer());
      Scoreboard board = event.getPlayer().getScoreboard();
      if (board != null) {
         for(Team t : board.getTeams()) {
            t.unregister();
         }

         for(Objective ob : board.getObjectives()) {
            ob.unregister();
         }
      }

      event.getPlayer().setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
      this.removePlayerFile(event.getPlayer().getUniqueId());
   }

   private void removePlayerFile(UUID uuid) {
      if (BukkitCommon.getInstance().isRemovePlayerDat()) {
         World world = Bukkit.getWorlds().get(0);
         File folder = new File(world.getWorldFolder(), "playerdata");
         if (folder.exists() && folder.isDirectory()) {
            File file = new File(folder, uuid.toString() + ".dat");
            Bukkit.getScheduler().runTaskLaterAsynchronously(BukkitCommon.getInstance(), () -> {
               if (file.exists() && !file.delete()) {
                  this.removePlayerFile(uuid);
               }
            }, 2L);
         }
      }
   }
}

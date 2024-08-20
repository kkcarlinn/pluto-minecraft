package br.com.plutomc.lobby.main.listener;

import br.com.plutomc.core.bukkit.event.UpdateEvent;
import br.com.plutomc.core.common.utils.string.WaveAnimation;
import br.com.plutomc.lobby.main.LobbyMain;
import br.com.plutomc.core.common.CommonPlugin;
import br.com.plutomc.core.bukkit.BukkitCommon;
import br.com.plutomc.core.bukkit.event.member.PlayerGroupChangeEvent;
import br.com.plutomc.core.bukkit.event.member.PlayerLanguageChangeEvent;
import br.com.plutomc.core.bukkit.event.server.PlayerChangeEvent;
import br.com.plutomc.core.bukkit.utils.scoreboard.ScoreHelper;
import br.com.plutomc.core.bukkit.utils.scoreboard.Scoreboard;
import br.com.plutomc.core.common.member.Member;
import br.com.plutomc.core.common.permission.Tag;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class ScoreboardListener implements Listener {

   private static final WaveAnimation animation = new WaveAnimation("PLUTO", "§b§l", "§6§l", "§e§l");

   @EventHandler(
      priority = EventPriority.HIGHEST
   )
   public void onPlayerJoin(PlayerJoinEvent event) {
      this.handleScoreboard(event.getPlayer());
      this.updateScoreboard(event.getPlayer());
      this.updatePlayers();
   }

   @EventHandler
   public void onUpdate(UpdateEvent event) {
      if (event.getCurrentTick() % 2 == 0) {
         String next = animation.next();
         for(Scoreboard score : ScoreHelper.getInstance().scoreMap.values()) {
            score.setDisplayName(next);
         }
      }
   }

   @EventHandler(
      priority = EventPriority.HIGHEST
   )
   public void onPlayerQuit(PlayerQuitEvent event) {
      this.updatePlayers();
   }

   @EventHandler(
      priority = EventPriority.HIGHEST
   )
   public void onPlayerChange(PlayerChangeEvent event) {
      this.updatePlayers();
   }

   @EventHandler
   public void onPlayerLanguageChange(PlayerLanguageChangeEvent event) {
      ScoreHelper.getInstance().removeScoreboard(event.getPlayer());
      this.handleScoreboard(event.getPlayer());
      this.updateScoreboard(event.getPlayer());
   }

   @EventHandler
   public void onPlayerGroupChange(PlayerGroupChangeEvent event) {
      this.updateScoreboard(event.getPlayer());
   }

   private void handleScoreboard(Player player) {
      Member member = CommonPlugin.getInstance().getMemberManager().getMember(player.getUniqueId());
      Scoreboard scoreboard = new Scoreboard(player, "§b§lPLUTO");
      scoreboard.add(8, "§a");
      scoreboard.add(7, "§f§%scoreboard-group%§: " + CommonPlugin.getInstance().getPluginInfo().getTagByGroup(member.getServerGroup()).getScore());
      scoreboard.add(5, "");
      scoreboard.add(4, "§fLobby: §a#" + LobbyMain.getInstance().getLobbyId());
      scoreboard.add(3, "§f§%scoreboard-players%§: §b" + BukkitCommon.getInstance().getServerManager().getTotalMembers());
      scoreboard.add(2, "§a");
      scoreboard.add(1, "§awww." + CommonPlugin.getInstance().getPluginInfo().getWebsite());
      ScoreHelper.getInstance().setScoreboard(player, scoreboard);
   }

   private void updateScoreboard(Player player) {
      Member member = CommonPlugin.getInstance().getMemberManager().getMember(player.getUniqueId());
      Tag tag = CommonPlugin.getInstance().getPluginInfo().getTagByGroup(member.getServerGroup());
      ScoreHelper.getInstance().updateScoreboard(player, 7, "§f§%scoreboard-group%§: " + tag.getScore());
   }

   private void updatePlayers() {
      ScoreHelper.getInstance().updateScoreboard(3, "§f§%scoreboard-players%§: §b" + BukkitCommon.getInstance().getServerManager().getTotalMembers());
   }
}

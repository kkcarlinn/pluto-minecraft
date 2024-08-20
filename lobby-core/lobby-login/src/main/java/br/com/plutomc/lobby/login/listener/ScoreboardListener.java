package br.com.plutomc.lobby.login.listener;

import br.com.plutomc.core.bukkit.event.UpdateEvent;
import br.com.plutomc.core.common.CommonPlugin;
import br.com.plutomc.core.bukkit.BukkitCommon;
import br.com.plutomc.core.bukkit.event.member.PlayerLanguageChangeEvent;
import br.com.plutomc.core.bukkit.event.server.PlayerChangeEvent;
import br.com.plutomc.core.bukkit.utils.scoreboard.ScoreHelper;
import br.com.plutomc.core.bukkit.utils.scoreboard.Scoreboard;
import br.com.plutomc.core.common.utils.string.WaveAnimation;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class ScoreboardListener implements Listener {

   private static final WaveAnimation animation = new WaveAnimation("LOGIN", "§b§l", "§6§l", "§e§l");
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
   public void onPlayerJoin(PlayerJoinEvent event) {
      this.handleScoreboard(event.getPlayer());
      this.updateScoreboard(event.getPlayer());
      this.updatePlayers();
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

   private void handleScoreboard(Player player) {
      Scoreboard scoreboard = new Scoreboard(player, "§b§lLOGIN");
      scoreboard.add(7, "§e");
      scoreboard.add(6, "§7Aguardando você");
      scoreboard.add(5, "§7se autenticar.");
      scoreboard.add(4, "§e");
      scoreboard.add(3, "§f§%scoreboard-players%§: §a" + BukkitCommon.getInstance().getServerManager().getTotalMembers());
      scoreboard.add(2, "§e");
      scoreboard.add(1, "§awww." + CommonPlugin.getInstance().getPluginInfo().getWebsite());
      ScoreHelper.getInstance().setScoreboard(player, scoreboard);
   }

   private void updateScoreboard(Player player) {
   }

   private void updatePlayers() {
      ScoreHelper.getInstance().updateScoreboard(3, "§f§%scoreboard-players%§: §a" + BukkitCommon.getInstance().getServerManager().getTotalMembers());
   }
}

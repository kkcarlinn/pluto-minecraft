package br.com.plutomc.core.bukkit.utils.scoreboard;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class ScoreHelper {
   private static ScoreHelper scoreHelper = new ScoreHelper();
   public Map<UUID, Scoreboard> scoreMap = new HashMap<>();

   public void setScoreboard(Player player, Scoreboard scoreboard) {
      player.setScoreboard(scoreboard.getScoreboard());
      this.scoreMap.put(player.getUniqueId(), scoreboard);
   }

   public void setScoreboardName(Player player, String name) {
      if (this.scoreMap.containsKey(player.getUniqueId())) {
         this.scoreMap.get(player.getUniqueId()).setDisplayName(name);
      }
   }

   public void removeScoreboard(int index) {
      for(Player player : Bukkit.getOnlinePlayers()) {
         if (this.scoreMap.containsKey(player.getUniqueId())) {
            this.scoreMap.get(player.getUniqueId()).remove(index);
         }
      }
   }

   public void removeScoreboard(Player player) {
      this.scoreMap.remove(player.getUniqueId());
   }

   public void removeScoreboard(Player player, int index) {
      if (this.scoreMap.containsKey(player.getUniqueId())) {
         this.scoreMap.get(player.getUniqueId()).remove(index);
      }
   }

   public void addScoreboard(Player player, int index, String value) {
      if (this.scoreMap.containsKey(player.getUniqueId())) {
         this.scoreMap.get(player.getUniqueId()).add(index, value);
      }
   }

   public void updateScoreboard(int index, String value) {
      for(Player player : Bukkit.getOnlinePlayers()) {
         this.addScoreboard(player, index, value);
      }
   }

   public void updateScoreboard(Player player, int index, String value) {
      this.addScoreboard(player, index, value);
   }

   public static ScoreHelper getInstance() {
      return scoreHelper;
   }
}

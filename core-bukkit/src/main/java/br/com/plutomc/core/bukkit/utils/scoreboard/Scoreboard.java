package br.com.plutomc.core.bukkit.utils.scoreboard;

import br.com.plutomc.core.bukkit.utils.player.PlayerHelper;
import br.com.plutomc.core.common.member.Member;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Team;

public class Scoreboard extends Objective {
   private Player player;
   private int index = 15;

   public Scoreboard(Player player, String title) {
      super(player.getScoreboard(), DisplaySlot.SIDEBAR);
      this.player = player;
      this.setDisplayName(title);
   }

   public void add(String text) {
      this.add(this.index--, text);
   }

   public static void main(String[] args) {
      String text = "§f§lB §fBranco §c✖";
      String prefix = "";
      String suffix = "";
      if (text.length() <= 16) {
         prefix = text;

         for(int i = text.length(); i > 0 && prefix.substring(0, i).endsWith("§"); --i) {
            prefix = prefix.substring(0, i - 1);
         }
      } else {
         prefix = text.substring(0, 16);

         for(int i = prefix.length(); i > 0 && prefix.substring(0, i).endsWith("§"); --i) {
            prefix = prefix.substring(0, i - 1);
         }

         String color = ChatColor.getLastColors(prefix);
         if (color.startsWith("§f")) {
            color = color.substring(2);
         }

         suffix = color + text.substring(16);
         if (!suffix.startsWith("§")) {
            ChatColor byChar = ChatColor.getByChar(suffix.charAt(0));
            if (byChar != null) {
               suffix = byChar + suffix.substring(1);
            }
         }

         if (suffix.length() > 16) {
            suffix = suffix.substring(0, 16);
         }
      }
   }

   public void add(int index, String text) {
      text = PlayerHelper.translate(Member.getLanguage(this.player.getUniqueId()), text);
      Team team = this.getScoreboard().getTeam("score-" + index);
      String prefix = "";
      String suffix = "";
      if (team == null) {
         team = this.getScoreboard().registerNewTeam("score-" + index);
         String score = ChatColor.values()[index - 1].toString();
         this.getObjective().getScore(score).setScore(index);
         if (!team.hasEntry(score)) {
            team.addEntry(score);
         }
      }

      if (text.length() <= 16) {
         prefix = text;

         for(int i = text.length(); i > 0 && prefix.substring(0, i).endsWith("§"); --i) {
            prefix = prefix.substring(0, i - 1);
         }
      } else {
         prefix = text.substring(0, 16);

         for(int i = prefix.length(); i > 0 && prefix.substring(0, i).endsWith("§"); --i) {
            prefix = prefix.substring(0, i - 1);
         }

         String color = ChatColor.getLastColors(prefix);
         if (color.startsWith("§f")) {
            color = color.substring(2);
         }

         suffix = color + text.substring(16);
         if (!suffix.startsWith("§")) {
            ChatColor byChar = ChatColor.getByChar(suffix.charAt(0));
            if (byChar != null) {
               suffix = byChar + suffix.substring(1);
            }
         }

         if (suffix.length() > 16) {
            suffix = suffix.substring(0, 16);
         }
      }

      team.setPrefix(prefix);
      team.setSuffix(suffix);
   }

   public void remove(int index) {
      Team team = this.getScoreboard().getTeam("score-" + index);
      if (team != null) {
         String score = ChatColor.values()[index - 1].toString();
         if (!team.hasEntry(score)) {
            team.addEntry(score);
         }

         team.unregister();
         this.getScoreboard().resetScores(score);
      }
   }

   public Player getPlayer() {
      return this.player;
   }
}

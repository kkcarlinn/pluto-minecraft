package br.com.plutomc.game.bedwars.island;

import org.bukkit.ChatColor;
import org.bukkit.Color;

public enum IslandColor {
   WHITE(ChatColor.WHITE, 0),
   RED(ChatColor.RED, 14),
   PINK(ChatColor.LIGHT_PURPLE, 6),
   CYAN(ChatColor.AQUA, 3),
   YELLOW(ChatColor.YELLOW, 4),
   GREEN(ChatColor.GREEN, 5),
   BLUE(ChatColor.BLUE, 11),
   GRAY(ChatColor.DARK_GRAY, 8),
   ORANGE(ChatColor.GOLD, 1);

   private ChatColor color;
   private int woolId;

   public Color getColorEquivalent() {
      switch(this.color) {
         case AQUA:
            return Color.AQUA;
         case BLACK:
            return Color.BLACK;
         case BLUE:
            return Color.BLUE;
         case DARK_AQUA:
            return Color.BLUE;
         case DARK_BLUE:
            return Color.BLUE;
         case DARK_GRAY:
            return Color.GRAY;
         case DARK_GREEN:
            return Color.GREEN;
         case DARK_PURPLE:
            return Color.PURPLE;
         case DARK_RED:
            return Color.RED;
         case GOLD:
            return Color.YELLOW;
         case GRAY:
            return Color.GRAY;
         case GREEN:
            return Color.GREEN;
         case LIGHT_PURPLE:
            return Color.PURPLE;
         case RED:
            return Color.RED;
         case WHITE:
            return Color.WHITE;
         case YELLOW:
            return Color.YELLOW;
         default:
            return null;
      }
   }

   public ChatColor getColor() {
      return this.color;
   }

   public int getWoolId() {
      return this.woolId;
   }

   private IslandColor(ChatColor color, int woolId) {
      this.color = color;
      this.woolId = woolId;
   }
}

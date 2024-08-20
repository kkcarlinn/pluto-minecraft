package br.com.plutomc.game.bedwars.generator;

import org.bukkit.ChatColor;

public enum GeneratorType {
   EMERALD(60, ChatColor.DARK_GREEN),
   DIAMOND(30, ChatColor.AQUA),
   NORMAL;

   private int timer = 1;
   private ChatColor color;

   public GeneratorType getNextUpgrade() {
      return this.ordinal() - 1 < 0 ? values()[values().length - 2] : values()[this.ordinal() - 1];
   }

   public String getConfigFieldName() {
      return this.name().toLowerCase() + "Generator";
   }

   public int getTimer() {
      return this.timer;
   }

   public ChatColor getColor() {
      return this.color;
   }

   private GeneratorType(int timer, ChatColor color) {
      this.timer = timer;
      this.color = color;
   }

   private GeneratorType() {
   }

   public void setTimer(int timer) {
      this.timer = timer;
   }
}

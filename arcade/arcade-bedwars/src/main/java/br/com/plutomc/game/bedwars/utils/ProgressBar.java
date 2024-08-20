package br.com.plutomc.game.bedwars.utils;

import com.google.common.base.Strings;
import org.bukkit.ChatColor;

public class ProgressBar {
   public static String getProgressBar(double current, double max, int totalBars, char symbol, ChatColor completedColor, ChatColor notCompletedColor) {
      float percent = (float)((double)((float)current) / max);
      int progressBars = (int)((float)totalBars * percent);
      return Strings.repeat("" + completedColor + symbol, progressBars) + Strings.repeat("" + notCompletedColor + symbol, totalBars - progressBars);
   }
}

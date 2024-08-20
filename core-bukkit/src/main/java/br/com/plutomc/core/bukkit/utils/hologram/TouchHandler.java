package br.com.plutomc.core.bukkit.utils.hologram;

import org.bukkit.entity.Player;

public interface TouchHandler {
   void onTouch(Hologram var1, Player var2, TouchType var3);

   public static enum TouchType {
      LEFT,
      RIGHT;
   }
}

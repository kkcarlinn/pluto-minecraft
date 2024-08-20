package br.com.plutomc.core.bukkit.listener;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.weather.WeatherChangeEvent;

public class WorldListener implements Listener {
   @EventHandler
   public void onWheater(WeatherChangeEvent event) {
      for(World w : Bukkit.getWorlds()) {
         w.setWeatherDuration(0);
      }

      event.setCancelled(true);
   }
}

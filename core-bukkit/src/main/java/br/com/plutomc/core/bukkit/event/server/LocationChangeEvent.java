package br.com.plutomc.core.bukkit.event.server;

import br.com.plutomc.core.bukkit.event.NormalEvent;
import org.bukkit.Location;

public class LocationChangeEvent extends NormalEvent {
   private String configName;
   private Location newLocation;
   private Location oldLocation;

   public String getConfigName() {
      return this.configName;
   }

   public Location getNewLocation() {
      return this.newLocation;
   }

   public Location getOldLocation() {
      return this.oldLocation;
   }

   public LocationChangeEvent(String configName, Location newLocation, Location oldLocation) {
      this.configName = configName;
      this.newLocation = newLocation;
      this.oldLocation = oldLocation;
   }
}

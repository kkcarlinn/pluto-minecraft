package br.com.plutomc.core.bukkit.manager;

import java.util.HashMap;
import java.util.Map;

import br.com.plutomc.core.bukkit.BukkitCommon;
import br.com.plutomc.core.bukkit.event.server.LocationChangeEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;

public class LocationManager {
   private Map<String, Location> locationMap = new HashMap<>();

   public boolean hasLocation(String locationName) {
      return this.locationMap.containsKey(locationName.toLowerCase());
   }

   public void loadLocation(String locationName, Location location) {
      this.locationMap.put(locationName.toLowerCase(), location);
   }

   public void saveLocation(String locationName, Location location) {
      BukkitCommon.getInstance().getConfig().set("location." + locationName + ".world", location.getWorld().getName());
      BukkitCommon.getInstance().getConfig().set("location." + locationName + ".x", Double.valueOf(location.getX()));
      BukkitCommon.getInstance().getConfig().set("location." + locationName + ".y", Double.valueOf(location.getY()));
      BukkitCommon.getInstance().getConfig().set("location." + locationName + ".z", Double.valueOf(location.getZ()));
      BukkitCommon.getInstance().getConfig().set("location." + locationName + ".pitch", Float.valueOf(location.getPitch()));
      BukkitCommon.getInstance().getConfig().set("location." + locationName + ".yaw", Float.valueOf(location.getYaw()));
      BukkitCommon.getInstance().saveConfig();
      Bukkit.getPluginManager().callEvent(new LocationChangeEvent(locationName, location, null));
   }

   public void saveAndLoadLocation(String locationName, Location location) {
      this.locationMap.put(locationName.toLowerCase(), location);
      this.saveLocation(locationName, location);
   }

   public Location getLocation(String locationName) {
      return this.locationMap.computeIfAbsent(locationName.toLowerCase(), v -> this.getLocationFromConfig(locationName));
   }

   public Location getLocationFromConfig(String locationName) {
      if (!BukkitCommon.getInstance().getConfig().contains("location." + locationName)) {
         return new Location(Bukkit.getWorlds().stream().findFirst().orElse(null), 0.0, 120.0, 0.0);
      } else {
         World world = Bukkit.getWorld(BukkitCommon.getInstance().getConfig().getString("location." + locationName + ".world").toLowerCase());
         if (world == null) {
            world = Bukkit.createWorld(new WorldCreator(BukkitCommon.getInstance().getConfig().getString("location." + locationName + ".world").toLowerCase()));
         }

         return new Location(
            world,
            BukkitCommon.getInstance().getConfig().getDouble("location." + locationName + ".x"),
            BukkitCommon.getInstance().getConfig().getDouble("location." + locationName + ".y"),
            BukkitCommon.getInstance().getConfig().getDouble("location." + locationName + ".z"),
            (float)BukkitCommon.getInstance().getConfig().getDouble("location." + locationName + ".yaw"),
            (float)BukkitCommon.getInstance().getConfig().getDouble("location." + locationName + ".pitch")
         );
      }
   }

   public void removeLocationInConfig(String locationName) {
      this.locationMap.remove(locationName.toLowerCase());
      BukkitCommon.getInstance().getConfig().set("location." + locationName, null);
      BukkitCommon.getInstance().saveConfig();
   }

   public String[] getLocations() {
      return this.locationMap.keySet().stream().toArray(x$0 -> new String[x$0]);
   }

   public Map<String, Location> getLocationMap() {
      return this.locationMap;
   }
}

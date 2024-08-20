package br.com.plutomc.core.bukkit.utils;

import java.util.OptionalInt;
import br.com.plutomc.core.common.CommonConst;
import org.bukkit.Bukkit;
import org.bukkit.World;

public class Location {
   private String worldName;
   private double x;
   private double y;
   private double z;
   private float yaw;
   private float pitch;

   public Location() {
      this(Bukkit.getWorlds().stream().findFirst().orElse(null).getName(), 0.0, 0.0, 0.0, 0.0F, 0.0F);
   }

   public Location(String worldName) {
      this(worldName, 0.0, 0.0, 0.0, 0.0F, 0.0F);
   }

   public Location(String worldName, double x, double y, double z) {
      this(worldName, x, y, z, 0.0F, 0.0F);
   }

   public World getWorld() {
      return Bukkit.getWorld(this.worldName);
   }

   public void set(org.bukkit.Location location) {
      this.x = location.getX();
      this.y = location.getY();
      this.z = location.getZ();
      this.yaw = location.getYaw();
      this.pitch = location.getPitch();
   }

   public org.bukkit.Location getAsLocation() {
      return new org.bukkit.Location(this.getWorld(), this.x, this.y, this.z, this.yaw, this.pitch);
   }

   public static Location fromLocation(org.bukkit.Location location) {
      return new Location(location.getWorld().getName(), location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
   }

   @Override
   public String toString() {
      return CommonConst.GSON.toJson(this);
   }

   public static Location valueOf(String value) {
      if (value.startsWith("{") && value.endsWith("}")) {
         return CommonConst.GSON.fromJson(value, Location.class);
      } else if (value.contains(",")) {
         boolean space = value.contains(", ");
         String[] split = value.split(space ? ", " : ",");
         String worldName = split[0];
         OptionalInt optionalX = OptionalInt.of(Integer.valueOf(split[1]));
         OptionalInt optionalY = OptionalInt.of(Integer.valueOf(split[2]));
         OptionalInt optionalZ = OptionalInt.of(Integer.valueOf(split[3]));
         return new Location(worldName, (double)optionalX.getAsInt(), (double)optionalY.getAsInt(), (double)optionalZ.getAsInt(), 0.0F, 0.0F);
      } else {
         return null;
      }
   }

   public String getWorldName() {
      return this.worldName;
   }

   public double getX() {
      return this.x;
   }

   public double getY() {
      return this.y;
   }

   public double getZ() {
      return this.z;
   }

   public float getYaw() {
      return this.yaw;
   }

   public float getPitch() {
      return this.pitch;
   }

   public void setWorldName(String worldName) {
      this.worldName = worldName;
   }

   public void setX(double x) {
      this.x = x;
   }

   public void setY(double y) {
      this.y = y;
   }

   public void setZ(double z) {
      this.z = z;
   }

   public void setYaw(float yaw) {
      this.yaw = yaw;
   }

   public void setPitch(float pitch) {
      this.pitch = pitch;
   }

   public Location(String worldName, double x, double y, double z, float yaw, float pitch) {
      this.worldName = worldName;
      this.x = x;
      this.y = y;
      this.z = z;
      this.yaw = yaw;
      this.pitch = pitch;
   }
}

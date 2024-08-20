package br.com.plutomc.core.bukkit.utils.hologram.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import br.com.plutomc.core.bukkit.utils.hologram.TouchHandler;
import br.com.plutomc.core.bukkit.utils.hologram.Hologram;
import br.com.plutomc.core.bukkit.utils.hologram.ViewHandler;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public class SimpleHologram implements Hologram {
   private String displayName;
   private Location location;
   private TouchHandler touchHandler;
   private ViewHandler viewHandler;
   private ArmorStand armorStand;
   private List<Hologram> hologramLines;
   private List<Player> invisibleTo;
   private boolean spawned;

   public SimpleHologram(String displayName, Location location, TouchHandler touchHandler, ViewHandler viewHandler) {
      this.displayName = displayName;
      this.location = location;
      this.touchHandler = touchHandler;
      this.viewHandler = viewHandler;
      this.hologramLines = new ArrayList<>();
      this.invisibleTo = new ArrayList<>();
   }

   public SimpleHologram(String displayName, Location location) {
      this(displayName, location, null, null);
   }

   @Override
   public void spawn() {
      if (!this.isSpawned()) {
         this.spawned = true;
         if (!this.location.getChunk().isLoaded()) {
            this.location.getChunk().load();
         }

         try {
            this.armorStand = this.createArmorStand();
         } catch (Exception var2) {
            var2.printStackTrace();
         }

         if (!this.hologramLines.isEmpty()) {
            this.hologramLines.forEach(Hologram::spawn);
         }
      }
   }

   @Override
   public void remove() {
      this.spawned = false;
      if (this.armorStand != null) {
         this.armorStand.remove();
      }

      if (!this.hologramLines.isEmpty()) {
         this.hologramLines.forEach(Hologram::remove);
      }
   }

   @Override
   public boolean isSpawned() {
      return this.spawned;
   }

   @Override
   public Hologram setDisplayName(String displayName) {
      this.displayName = displayName;
      if (this.isSpawned()) {
         this.armorStand.setCustomName(displayName);
         this.armorStand.setCustomNameVisible(this.isCustomNameVisible());
      }

      return this;
   }

   @Override
   public boolean hasDisplayName() {
      return this.isCustomNameVisible();
   }

   @Override
   public boolean isCustomNameVisible() {
      return this.displayName != null && !this.displayName.isEmpty();
   }

   @Override
   public String getDisplayName() {
      return this.displayName;
   }

   @Override
   public Hologram line(String line) {
      SimpleHologram hologram = new SimpleHologram(line, this.getLocation().clone().subtract(0.0, (double)(this.getLines().size() + 1) * 0.25, 0.0));
      hologram.setTouchHandler(this.getTouchHandler());
      if (this.isSpawned()) {
         hologram.spawn();
      }

      this.hologramLines.add(hologram);
      return hologram;
   }

   @Override
   public Hologram line(Hologram hologram) {
      if (this.isSpawned()) {
         hologram.spawn();
      }

      if (!hologram.hasTouchHandler()) {
         hologram.setTouchHandler(this.getTouchHandler());
      }

      this.hologramLines.add(hologram);
      return hologram;
   }

   @Override
   public List<Hologram> getLines() {
      return this.hologramLines;
   }

   @Override
   public void teleport(Location location) {
      this.location = location;
      if (this.armorStand != null) {
         this.armorStand.teleport(location);
         this.hologramLines.forEach(hologram -> hologram.teleport(location));
      }
   }

   @Override
   public Location getLocation() {
      return this.location;
   }

   @Override
   public void show(Player player) {
      if (this.invisibleTo.contains(player)) {
         this.invisibleTo.remove(player);
      }
   }

   @Override
   public void hide(Player player) {
      if (!this.invisibleTo.contains(player)) {
         this.invisibleTo.add(player);
      }
   }

   @Override
   public boolean isVisibleTo(Player player) {
      return !this.invisibleTo.contains(player);
   }

   @Override
   public Collection<? extends Player> getViewers() {
      Collection<? extends Player> clone = Bukkit.getOnlinePlayers();
      clone.removeAll(this.invisibleTo);
      return clone;
   }

   private ArmorStand createArmorStand() throws Exception {
      if (!this.location.getChunk().isLoaded() && !this.location.getChunk().load(true)) {
         throw new Exception("Could not load the chunk " + this.location.getX() + ", " + this.location.getY() + ", " + this.location.getZ());
      } else {
         for(Entity entity : this.location.getWorld().getNearbyEntities(this.location, 0.3, 0.3, 0.3)) {
            if (entity instanceof ArmorStand && entity.isCustomNameVisible() == this.isCustomNameVisible() && entity.getCustomName().equals(this.displayName)) {
               return (ArmorStand)entity;
            }
         }

         ArmorStand armorStand = (ArmorStand)this.location.getWorld().spawnEntity(this.location, EntityType.ARMOR_STAND);
         armorStand.setVisible(false);
         armorStand.setGravity(false);
         armorStand.setCustomName(this.displayName);
         armorStand.setCustomNameVisible(this.isCustomNameVisible());
         armorStand.setCanPickupItems(false);
         return armorStand;
      }
   }

   @Override
   public boolean hasTouchHandler() {
      return this.touchHandler != null;
   }

   @Override
   public boolean hasViewHandler() {
      return this.viewHandler != null;
   }

   @Override
   public boolean compareEntity(Entity rightClicked) {
      return rightClicked == this.armorStand;
   }

   public int getEntityId() {
      return this.armorStand.getEntityId();
   }

   @Override
   public boolean isEntityOrLine(int entityId) {
      if (this.armorStand == null) {
         return false;
      } else {
         return this.armorStand.getEntityId() == entityId
            ? true
            : this.hologramLines.stream().filter(hologram -> this.getEntityId() == entityId).findFirst().isPresent();
      }
   }

   @Override
   public TouchHandler getTouchHandler() {
      return this.touchHandler;
   }

   @Override
   public ViewHandler getViewHandler() {
      return this.viewHandler;
   }

   public ArmorStand getArmorStand() {
      return this.armorStand;
   }

   public List<Hologram> getHologramLines() {
      return this.hologramLines;
   }

   public List<Player> getInvisibleTo() {
      return this.invisibleTo;
   }

   @Override
   public void setTouchHandler(TouchHandler touchHandler) {
      this.touchHandler = touchHandler;
   }

   @Override
   public void setViewHandler(ViewHandler viewHandler) {
      this.viewHandler = viewHandler;
   }
}

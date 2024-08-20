package br.com.plutomc.core.bukkit.utils.hologram;

import java.lang.reflect.InvocationTargetException;

import br.com.plutomc.core.bukkit.utils.hologram.impl.SimpleHologram;
import br.com.plutomc.core.bukkit.manager.HologramManager;
import org.bukkit.Location;

public class HologramBuilder {
   private String displayName;
   private Location location;
   private TouchHandler touchHandler;
   private ViewHandler viewHandler;
   private Class<? extends Hologram> clazz;
   private HologramManager hologramController;
   private boolean register;
   private boolean spawn;

   public HologramBuilder(String displayName) {
      this.displayName = displayName;
   }

   public HologramBuilder(String displayName, Location location) {
      this.displayName = displayName;
      this.location = location;
   }

   public HologramBuilder setDisplayName(String displayName) {
      this.displayName = displayName;
      return this;
   }

   public HologramBuilder setLocation(Location location) {
      this.location = location;
      return this;
   }

   public HologramBuilder setTouchHandler(TouchHandler touchHandler) {
      this.touchHandler = touchHandler;
      return this;
   }

   public HologramBuilder setTouchHandler(ViewHandler viewHandler) {
      this.viewHandler = viewHandler;
      return this;
   }

   public HologramBuilder setHologramClass(Class<? extends Hologram> clazz) {
      this.clazz = clazz;
      return this;
   }

   public HologramBuilder setHologramController(HologramManager hologramController) {
      this.hologramController = hologramController;
      return this;
   }

   public HologramBuilder setRegister(boolean register) {
      this.register = register;
      return this;
   }

   public HologramBuilder setSpawn(boolean spawn) {
      this.spawn = spawn;
      return this;
   }

   public Hologram build() {
      Hologram hologram = null;
      if (this.location == null) {
         throw new NullPointerException("Location cannot be null!");
      } else if (this.displayName == null) {
         throw new NullPointerException("DisplayName cannot be null!");
      } else {
         if (this.clazz == null) {
            this.clazz = SimpleHologram.class;
         }

         try {
            hologram = this.clazz.getConstructor(String.class, Location.class).newInstance(this.displayName, this.location);
         } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException | InstantiationException var3) {
            var3.printStackTrace();
         }

         if (this.touchHandler != null) {
            hologram.setTouchHandler(this.touchHandler);
         }

         if (this.register) {
            if (this.hologramController == null) {
               throw new IllegalStateException("");
            }

            this.hologramController.registerHologram(hologram);
         }

         if (this.spawn) {
            hologram.spawn();
         }

         return hologram;
      }
   }

   public HologramBuilder() {
   }

   public HologramBuilder(
      String displayName,
      Location location,
      TouchHandler touchHandler,
      ViewHandler viewHandler,
      Class<? extends Hologram> clazz,
      HologramManager hologramController,
      boolean register,
      boolean spawn
   ) {
      this.displayName = displayName;
      this.location = location;
      this.touchHandler = touchHandler;
      this.viewHandler = viewHandler;
      this.clazz = clazz;
      this.hologramController = hologramController;
      this.register = register;
      this.spawn = spawn;
   }

   public String getDisplayName() {
      return this.displayName;
   }

   public Location getLocation() {
      return this.location;
   }

   public TouchHandler getTouchHandler() {
      return this.touchHandler;
   }

   public ViewHandler getViewHandler() {
      return this.viewHandler;
   }

   public Class<? extends Hologram> getClazz() {
      return this.clazz;
   }

   public HologramManager getHologramController() {
      return this.hologramController;
   }

   public boolean isRegister() {
      return this.register;
   }

   public boolean isSpawn() {
      return this.spawn;
   }
}

package br.com.plutomc.core.bukkit.utils.hologram;

import java.util.Collection;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public interface Hologram {
   void spawn();

   void remove();

   boolean isSpawned();

   Hologram setDisplayName(String var1);

   boolean hasDisplayName();

   boolean isCustomNameVisible();

   String getDisplayName();

   Hologram line(String var1);

   Hologram line(Hologram var1);

   List<Hologram> getLines();

   void teleport(Location var1);

   default void teleport(World world, int x, int y, int z) {
      this.teleport(new Location(world, (double)x, (double)y, (double)z));
   }

   default void teleport(int x, int y, int z) {
      this.teleport(new Location(this.getLocation().getWorld(), (double)x, (double)y, (double)z));
   }

   Location getLocation();

   void show(Player var1);

   void hide(Player var1);

   Collection<? extends Player> getViewers();

   boolean isVisibleTo(Player var1);

   void setTouchHandler(TouchHandler var1);

   boolean hasTouchHandler();

   TouchHandler getTouchHandler();

   void setViewHandler(ViewHandler var1);

   boolean hasViewHandler();

   ViewHandler getViewHandler();

   boolean compareEntity(Entity var1);

   boolean isEntityOrLine(int var1);
}

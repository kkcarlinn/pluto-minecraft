package br.com.plutomc.core.bukkit.utils.worldedit;

import br.com.plutomc.core.bukkit.BukkitCommon;
import org.bukkit.Location;
import org.bukkit.Material;

public class FutureBlock {
   private Location location;
   private Material type;
   private byte data;
   private boolean async;

   public FutureBlock(Location location, Material type, byte data) {
      this.location = location;
      this.type = type;
      this.data = data;
   }

   public byte getData() {
      return this.data;
   }

   public Location getLocation() {
      return this.location;
   }

   public Material getType() {
      return this.type;
   }

   public FutureBlock async() {
      this.async = true;
      return this;
   }

   public void place() {
      BukkitCommon.getInstance()
         .getBlockManager()
         .setBlockFast(this.location.getWorld(), (int)this.location.getX(), (int)this.location.getY(), (int)this.location.getZ(), this.type.getId(), this.data);
   }

   public boolean isAsync() {
      return this.async;
   }
}

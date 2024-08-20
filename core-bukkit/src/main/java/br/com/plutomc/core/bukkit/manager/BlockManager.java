package br.com.plutomc.core.bukkit.manager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;

import br.com.plutomc.core.bukkit.BukkitCommon;
import br.com.plutomc.core.bukkit.event.UpdateEvent;
import br.com.plutomc.core.bukkit.utils.item.ActionItemStack;
import br.com.plutomc.core.bukkit.utils.item.ItemBuilder;
import br.com.plutomc.core.bukkit.utils.worldedit.FutureBlock;
import br.com.plutomc.core.bukkit.utils.worldedit.schematic.Schematic;
import br.com.plutomc.core.common.CommonPlugin;
import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.Chunk;
import net.minecraft.server.v1_8_R3.IBlockData;
import net.minecraft.server.v1_8_R3.WorldServer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

public class BlockManager {
   private Map<UUID, Position> positionMap = new HashMap<>();
   private ActionItemStack wand = new ActionItemStack(new ItemBuilder().name("§dWand").type(Material.WOOD_AXE).build(), new ActionItemStack.Interact() {
      @Override
      public boolean onInteract(Player player, Entity entity, Block block, ItemStack item, ActionItemStack.ActionType action) {
         if (block != null) {
            if (action == ActionItemStack.ActionType.LEFT) {
               BlockManager.this.setFirstPosition(player, block.getLocation());
               player.sendMessage("§dO local da primeira posição é " + block.getX() + ", " + block.getY() + ", " + block.getZ());
            } else {
               BlockManager.this.setSecondPosition(player, block.getLocation());
               player.sendMessage("§dO local da segunda posição é " + block.getX() + ", " + block.getY() + ", " + block.getZ());
            }
         }

         return true;
      }
   });
   private Map<Location, Place> blocksForUpdate = new HashMap<>();

   public BlockManager() {
      Bukkit.getPluginManager().registerEvents(new Listener() {
         @EventHandler
         public void onUpdate(UpdateEvent event) {
            if (!BlockManager.this.blocksForUpdate.isEmpty()) {
               for(Entry<Location, Place> entry : BlockManager.this.blocksForUpdate.entrySet()) {
                  Location location = entry.getKey();
                  WorldServer worldServer = ((CraftWorld)location.getWorld()).getHandle();
                  worldServer.notify(new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ()));
                  if (entry.getValue() != null) {
                     entry.getValue().place(location);
                  }
               }

               BlockManager.this.blocksForUpdate.clear();
            }
         }
      }, BukkitCommon.getInstance());
   }

   public void setBlockFast(World world, int x, int y, int z, int blockId, byte data) {
      WorldServer worldServer = ((CraftWorld)world).getHandle();
      Chunk chunk = worldServer.getChunkAt(x >> 4, z >> 4);
      BlockPosition bp = new BlockPosition(x, y, z);
      int i = blockId + (data << 12);
      IBlockData ibd = net.minecraft.server.v1_8_R3.Block.getByCombinedId(i);
      chunk.a(bp, ibd);
      this.addBlockUpdate(new Location(world, (double)x, (double)y, (double)z));
   }

   public void setBlockFast(World world, int x, int y, int z, int blockId, byte data, Place place) {
      WorldServer worldServer = ((CraftWorld)world).getHandle();
      Chunk chunk = worldServer.getChunkAt(x >> 4, z >> 4);
      BlockPosition bp = new BlockPosition(x, y, z);
      int i = blockId + (data << 12);
      IBlockData ibd = net.minecraft.server.v1_8_R3.Block.getByCombinedId(i);
      chunk.a(bp, ibd);
      this.addBlockUpdate(new Location(world, (double)x, (double)y, (double)z), place);
   }

   public void setBlockFast(Location location, int blockId) {
      this.setBlockFast(location.getWorld(), location, blockId, (byte)0);
   }

   public void setBlockFast(Location location, Material material) {
      this.setBlockFast(location.getWorld(), location, material.getId(), (byte)0);
   }

   public void setBlockFast(Location location, Material material, byte data) {
      this.setBlockFast(location.getWorld(), location, material.getId(), data);
   }

   public void setBlockFast(Location location, Material material, byte data, Place place) {
      this.setBlockFast(location.getWorld(), location, material.getId(), data, place);
   }

   public void setBlockFast(Location location, int blockId, byte data) {
      this.setBlockFast(location.getWorld(), location, blockId, data);
   }

   public void setBlockFast(World world, Location location, int blockId) {
      this.setBlockFast(world, location, blockId, (byte)0);
   }

   public void setBlockFast(World world, Location location, int blockId, byte data) {
      this.setBlockFast(world, location.getBlockX(), location.getBlockY(), location.getBlockZ(), blockId, data);
   }

   public void setBlockFast(Location location, int blockId, Place place) {
      this.setBlockFast(location.getWorld(), location, blockId, (byte)0, place);
   }

   public void setBlockFast(Location location, Material material, Place place) {
      this.setBlockFast(location.getWorld(), location, material.getId(), (byte)0, place);
   }

   public void setBlockFast(Location location, int blockId, byte data, Place place) {
      this.setBlockFast(location.getWorld(), location, blockId, data, place);
   }

   public void setBlockFast(World world, Location location, int blockId, Place place) {
      this.setBlockFast(world, location, blockId, (byte)0, place);
   }

   public void setBlockFast(World world, Location location, int blockId, byte data, Place place) {
      this.setBlockFast(world, location.getBlockX(), location.getBlockY(), location.getBlockZ(), blockId, data, place);
   }

   public void addBlockUpdate(Location location) {
      this.addBlockUpdate(location, null);
   }

   public void addBlockUpdate(Location location, Place place) {
      this.blocksForUpdate.put(location, place);
   }

   public void addUndo(Player player, Map<Location, BlockState> map) {
      this.positionMap.computeIfAbsent(player.getUniqueId(), v -> new Position()).addUndo(map);
   }

   public void removeUndo(Player player, Map<Location, BlockState> map) {
      this.positionMap.computeIfAbsent(player.getUniqueId(), v -> new Position()).removeUndo(map);
   }

   public void giveWand(Player player) {
      player.getInventory().addItem(new ItemStack[]{this.wand.getItemStack()});
   }

   public void setFirstPosition(Player player, Location location) {
      this.positionMap.computeIfAbsent(player.getUniqueId(), v -> new Position()).setFirstLocation(location);
   }

   public void setSecondPosition(Player player, Location location) {
      this.positionMap.computeIfAbsent(player.getUniqueId(), v -> new Position()).setSecondLocation(location);
   }

   public boolean hasFirstPosition(Player player) {
      return this.positionMap.computeIfAbsent(player.getUniqueId(), v -> new Position()).hasFirstLocation();
   }

   public boolean hasSecondPosition(Player player) {
      return this.positionMap.computeIfAbsent(player.getUniqueId(), v -> new Position()).hasSecondLocation();
   }

   public boolean hasUndoList(Player player) {
      return !this.positionMap.computeIfAbsent(player.getUniqueId(), v -> new Position()).getUndoList().isEmpty();
   }

   public Location getFirstPosition(Player player) {
      return this.positionMap.computeIfAbsent(player.getUniqueId(), v -> new Position()).getFirstLocation();
   }

   public Location getSecondPosition(Player player) {
      return this.positionMap.computeIfAbsent(player.getUniqueId(), v -> new Position()).getSecondLocation();
   }

   public List<Map<Location, BlockState>> getUndoList(Player player) {
      return this.positionMap.computeIfAbsent(player.getUniqueId(), v -> new Position()).getUndoList();
   }

   public List<Location> getLocationsFromTwoPoints(Location location1, Location location2) {
      List<Location> locations = new ArrayList<>();
      int topBlockX = location1.getBlockX() < location2.getBlockX() ? location2.getBlockX() : location1.getBlockX();
      int bottomBlockX = location1.getBlockX() > location2.getBlockX() ? location2.getBlockX() : location1.getBlockX();
      int topBlockY = location1.getBlockY() < location2.getBlockY() ? location2.getBlockY() : location1.getBlockY();
      int bottomBlockY = location1.getBlockY() > location2.getBlockY() ? location2.getBlockY() : location1.getBlockY();
      int topBlockZ = location1.getBlockZ() < location2.getBlockZ() ? location2.getBlockZ() : location1.getBlockZ();
      int bottomBlockZ = location1.getBlockZ() > location2.getBlockZ() ? location2.getBlockZ() : location1.getBlockZ();

      for(int x = bottomBlockX; x <= topBlockX; ++x) {
         for(int z = bottomBlockZ; z <= topBlockZ; ++z) {
            for(int y = bottomBlockY; y <= topBlockY; ++y) {
               locations.add(new Location(location1.getWorld(), (double)x, (double)y, (double)z));
            }
         }
      }

      return locations;
   }

   public List<FutureBlock> load(Location location, File file) {
      List<FutureBlock> blocks = new ArrayList<>();

      try {
         BufferedReader reader = new BufferedReader(new FileReader(file));
         String line = null;

         while((line = reader.readLine()) != null) {
            if (line.contains(",") && line.contains(":")) {
               String[] parts = line.split(":");
               String[] coordinates = parts[0].split(",");
               String[] blockData = parts[1].split("\\.");
               blocks.add(
                  new FutureBlock(
                     location.clone()
                        .add(
                           (double)Integer.valueOf(coordinates[0]).intValue(),
                           (double)Integer.valueOf(coordinates[2]).intValue(),
                           (double)Integer.valueOf(coordinates[1]).intValue()
                        ),
                     Material.values()[Integer.valueOf(blockData[0])],
                     blockData.length > 1 ? Byte.valueOf(blockData[1]) : 0
                  )
               );
            }
         }

         reader.close();
      } catch (Exception var9) {
         CommonPlugin.getInstance().debug("Error to load the bo2file " + file.getName() + " in the location " + location.toString());
      }

      return blocks;
   }

   public List<FutureBlock> spawn(Location location, File file, Place place) {
      List<FutureBlock> load = this.load(location, file);

      for(FutureBlock futureBlock : load) {
         BukkitCommon.getInstance().getBlockManager().setBlockFast(futureBlock.getLocation(), futureBlock.getType(), futureBlock.getData(), place);
      }

      return load;
   }

   public List<FutureBlock> spawn(Location location, File file) {
      return this.spawn(location, file, null);
   }

   public List<FutureBlock> spawn(Location location, Schematic schematic) {
      return this.spawn(location, schematic, false, null);
   }

   public List<FutureBlock> spawn(Location location, Schematic schematic, boolean air, Place place) {
      List<FutureBlock> list = new ArrayList<>();
      short length = schematic.getLenght();
      short width = schematic.getWidth();
      short height = schematic.getHeight();

      for(int x = 0; x < width; ++x) {
         for(int y = 0; y < height; ++y) {
            for(int z = 0; z < length; ++z) {
               int index = y * width * length + z * width + x;
               FutureBlock futureBlock = new FutureBlock(
                  new Location(location.getWorld(), (double)x + location.getX(), (double)y + location.getY(), (double)z + location.getZ()),
                  Material.getMaterial(schematic.getBlocks()[index]),
                  schematic.getData()[index]
               );
               if (air || futureBlock.getType() != Material.AIR) {
                  list.add(
                     new FutureBlock(
                        new Location(location.getWorld(), (double)x + location.getX(), (double)y + location.getY(), (double)z + location.getZ()),
                        Material.getMaterial(schematic.getBlocks()[index]),
                        schematic.getData()[index]
                     )
                  );
               }
            }
         }
      }

      for(FutureBlock futureBlock : list) {
         BukkitCommon.getInstance().getBlockManager().setBlockFast(futureBlock.getLocation(), futureBlock.getType(), futureBlock.getData(), place);
      }

      return list;
   }

   public interface Place {
      void place(Location var1);
   }

   public class Position {
      private Location firstLocation;
      private Location secondLocation;
      private List<Map<Location, BlockState>> undoList = new ArrayList<>();

      public void addUndo(Map<Location, BlockState> map) {
         this.undoList.add(map);
      }

      public void removeUndo(Map<Location, BlockState> map) {
         this.undoList.remove(map);
      }

      public boolean hasFirstLocation() {
         return this.firstLocation != null;
      }

      public boolean hasSecondLocation() {
         return this.firstLocation != null;
      }

      public Location getFirstLocation() {
         return this.firstLocation;
      }

      public Location getSecondLocation() {
         return this.secondLocation;
      }

      public List<Map<Location, BlockState>> getUndoList() {
         return this.undoList;
      }

      public void setFirstLocation(Location firstLocation) {
         this.firstLocation = firstLocation;
      }

      public void setSecondLocation(Location secondLocation) {
         this.secondLocation = secondLocation;
      }
   }
}

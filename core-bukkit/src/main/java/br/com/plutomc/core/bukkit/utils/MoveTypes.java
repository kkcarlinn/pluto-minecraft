package br.com.plutomc.core.bukkit.utils;

import java.util.ArrayList;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.player.PlayerMoveEvent;

public class MoveTypes {
   public static boolean isSwimming(org.bukkit.Location location) {
      String type = location.getBlock().getType().name();
      return type.contains("WATER") || type.contains("LAVA");
   }

   public static boolean isFalling(PlayerMoveEvent event) {
      return event.getFrom().getY() - event.getTo().getY() > 0.0;
   }

   public static boolean isGoingUp(PlayerMoveEvent event) {
      return event.getTo().getBlockY() > event.getFrom().getBlockY();
   }

   public static boolean isClimbing(org.bukkit.Location location) {
      Block block = location.getBlock();
      Block north = block.getRelative(BlockFace.NORTH);
      Block south = block.getRelative(BlockFace.SOUTH);
      Block west = block.getRelative(BlockFace.WEST);
      Block east = block.getRelative(BlockFace.EAST);
      return location.getBlock().getType().equals(Material.VINE)
         || location.getBlock().getType().equals(Material.LADDER)
         || north.getType().equals(Material.VINE)
         || north.getType().equals(Material.LADDER)
         || south.getType().equals(Material.VINE)
         || south.getType().equals(Material.LADDER)
         || east.getType().equals(Material.VINE)
         || east.getType().equals(Material.LADDER)
         || west.getType().equals(Material.VINE)
         || west.getType().equals(Material.LADDER);
   }

   public static boolean hasSurrondingBlocks(Block block) {
      boolean hasNearBlocks = false;
      ArrayList<Block> blocks = new ArrayList<>();
      Block relative = block.getRelative(BlockFace.DOWN);
      Block relativeNorth = relative.getRelative(BlockFace.NORTH);
      Block relativeSouth = relative.getRelative(BlockFace.SOUTH);
      Block relativeEast = relative.getRelative(BlockFace.EAST);
      Block relativeWest = relative.getRelative(BlockFace.WEST);
      Block relativeNorthEast = relative.getRelative(BlockFace.NORTH_EAST);
      Block relativeNorthWest = relative.getRelative(BlockFace.NORTH_WEST);
      Block relativeSouthEast = relative.getRelative(BlockFace.SOUTH_EAST);
      Block relativeSouthWest = relative.getRelative(BlockFace.SOUTH_WEST);
      blocks.add(relative);
      blocks.add(relativeNorth);
      blocks.add(relativeSouth);
      blocks.add(relativeEast);
      blocks.add(relativeWest);
      blocks.add(relativeNorthEast);
      blocks.add(relativeNorthWest);
      blocks.add(relativeSouthEast);
      blocks.add(relativeSouthWest);

      for(Block blockLoop : blocks) {
         if (blockLoop.getType() != Material.AIR) {
            hasNearBlocks = true;
            break;
         }
      }

      blocks.clear();
      return hasNearBlocks;
   }
}

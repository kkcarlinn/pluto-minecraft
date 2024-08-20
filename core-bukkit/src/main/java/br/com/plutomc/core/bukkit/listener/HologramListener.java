package br.com.plutomc.core.bukkit.listener;

import java.util.stream.Collectors;

import br.com.plutomc.core.bukkit.BukkitCommon;
import br.com.plutomc.core.bukkit.utils.hologram.Hologram;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.WorldLoadEvent;

public class HologramListener implements Listener {
   public HologramListener() {
      for(World world : Bukkit.getWorlds()) {
         for(Entity armorStand : world.getEntities().stream().filter(entity -> entity.getType() == EntityType.ARMOR_STAND).collect(Collectors.toList())) {
            armorStand.remove();
         }
      }
   }

   @EventHandler
   public void onPlayerArmorStandManipulate(PlayerArmorStandManipulateEvent e) {
      ArmorStand armorStand = e.getRightClicked();
      if (!armorStand.isVisible()) {
         e.setCancelled(true);
      }
   }

   @EventHandler
   public void onWorldLoad(WorldLoadEvent event) {
      for(Entity armorStand : event.getWorld().getEntities().stream().filter(entity -> entity.getType() == EntityType.ARMOR_STAND).collect(Collectors.toList())) {
         armorStand.remove();
      }
   }

   @EventHandler
   public void onChunkLoad(ChunkLoadEvent event) {
      for(Hologram hologram : BukkitCommon.getInstance().getHologramManager().getHologramList()) {
         if (hologram.getLocation().getChunk().equals(event.getChunk()) && !hologram.isSpawned()) {
            hologram.spawn();
         }
      }
   }

   @EventHandler
   public void onChunkUnload(ChunkUnloadEvent event) {
      for(Hologram hologram : BukkitCommon.getInstance().getHologramManager().getHologramList()) {
         if (hologram.isSpawned() && hologram.getLocation().getChunk().equals(event.getChunk())) {
            event.setCancelled(true);
         }
      }
   }

   @EventHandler
   public void onPluginDisable(PluginDisableEvent event) {
      for(Hologram hologram : BukkitCommon.getInstance().getHologramManager().getHologramList()) {
         hologram.remove();
      }
   }
}

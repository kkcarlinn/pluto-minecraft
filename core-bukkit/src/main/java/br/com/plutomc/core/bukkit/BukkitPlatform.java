package br.com.plutomc.core.bukkit;

import java.util.UUID;
import java.util.logging.Logger;
import br.com.plutomc.core.common.PluginPlatform;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class BukkitPlatform implements PluginPlatform {
   @Override
   public UUID getUniqueId(String playerName) {
      Player player = Bukkit.getPlayerExact(playerName);
      return player == null ? null : player.getUniqueId();
   }

   @Override
   public String getName(UUID uuid) {
      Player player = Bukkit.getPlayer(uuid);
      return player == null ? null : player.getName();
   }

   @Override
   public void runAsync(Runnable runnable) {
      Bukkit.getScheduler().runTaskAsynchronously(BukkitCommon.getPlugin(BukkitCommon.class), runnable);
   }

   @Override
   public void runAsync(Runnable runnable, long delay) {
      Bukkit.getScheduler().runTaskLaterAsynchronously(BukkitCommon.getPlugin(BukkitCommon.class), runnable, delay);
   }

   @Override
   public void runAsync(Runnable runnable, long delay, long repeat) {
      Bukkit.getScheduler().runTaskTimerAsynchronously(BukkitCommon.getPlugin(BukkitCommon.class), runnable, delay, repeat);
   }

   @Override
   public void run(Runnable runnable, long delay) {
      Bukkit.getScheduler().runTaskLater(BukkitCommon.getPlugin(BukkitCommon.class), runnable, delay);
   }

   @Override
   public void run(Runnable runnable, long delay, long repeat) {
      Bukkit.getScheduler().runTaskTimer(BukkitCommon.getPlugin(BukkitCommon.class), runnable, delay, repeat);
   }

   @Override
   public void shutdown(String message) {
      Bukkit.getConsoleSender().sendMessage("§4" + message);

      for(Player player : Bukkit.getOnlinePlayers()) {
         player.kickPlayer("§cO servidor foi fechado!");
      }

      Bukkit.shutdown();
   }

   @Override
   public Logger getLogger() {
      return Bukkit.getLogger();
   }

   @Override
   public void dispatchCommand(String command) {
      Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
   }

   @Override
   public void broadcast(String string) {
      Bukkit.broadcastMessage(string);
   }

   @Override
   public void broadcast(String string, String permission) {
      Bukkit.broadcast(string, permission);
   }
}

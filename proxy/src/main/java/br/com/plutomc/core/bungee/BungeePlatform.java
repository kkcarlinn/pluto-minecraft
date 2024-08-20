package br.com.plutomc.core.bungee;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import br.com.plutomc.core.common.PluginPlatform;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class BungeePlatform implements PluginPlatform {
   @Override
   public UUID getUniqueId(String playerName) {
      ProxiedPlayer proxiedPlayer = ProxyServer.getInstance().getPlayer(playerName);
      return proxiedPlayer == null ? null : proxiedPlayer.getUniqueId();
   }

   @Override
   public String getName(UUID uuid) {
      ProxiedPlayer proxiedPlayer = ProxyServer.getInstance().getPlayer(uuid);
      return proxiedPlayer == null ? null : proxiedPlayer.getName();
   }

   @Override
   public void runAsync(Runnable runnable) {
      ProxyServer.getInstance().getScheduler().runAsync(BungeeMain.getInstance(), runnable);
   }

   @Override
   public void runAsync(Runnable runnable, long delay) {
      throw new UnsupportedOperationException("This operation is not implemented in BungeeCord");
   }

   @Override
   public void runAsync(Runnable runnable, long delay, long repeat) {
      throw new UnsupportedOperationException("This operation is not implemented in BungeeCord");
   }

   @Override
   public void run(Runnable runnable, long delay) {
      ProxyServer.getInstance().getScheduler().schedule(BungeeMain.getInstance(), runnable, delay / 20L, TimeUnit.SECONDS);
   }

   @Override
   public void run(Runnable runnable, long delay, long repeat) {
      ProxyServer.getInstance().getScheduler().schedule(BungeeMain.getInstance(), runnable, delay / 20L, repeat / 20L, TimeUnit.SECONDS);
   }

   @Override
   public void shutdown(String message) {
      System.out.println("§4" + message);
      ProxyServer.getInstance().getPlayers().forEach(player -> player.disconnect("§c" + message));
      ProxyServer.getInstance().stop(message);
   }

   @Override
   public Logger getLogger() {
      return BungeeMain.getInstance().getLogger();
   }

   @Override
   public void dispatchCommand(String command) {
   }

   @Override
   public void broadcast(String string) {
      ProxyServer.getInstance().broadcast(string);
   }

   @Override
   public void broadcast(String string, String permission) {
      ProxyServer.getInstance().broadcast(string);
   }
}

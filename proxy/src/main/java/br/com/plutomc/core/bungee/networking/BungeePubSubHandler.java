package br.com.plutomc.core.bungee.networking;

import br.com.plutomc.core.bungee.event.RedisMessageEvent;
import net.md_5.bungee.api.ProxyServer;
import redis.clients.jedis.JedisPubSub;

public class BungeePubSubHandler extends JedisPubSub {
   @Override
   public void onMessage(String channel, String message) {
      ProxyServer.getInstance().getPluginManager().callEvent(new RedisMessageEvent(channel, message));
   }
}

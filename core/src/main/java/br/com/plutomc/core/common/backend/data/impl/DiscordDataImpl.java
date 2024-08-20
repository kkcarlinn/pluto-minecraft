package br.com.plutomc.core.common.backend.data.impl;

import java.util.Iterator;
import java.util.Set;

import br.com.plutomc.core.common.utils.string.CodeCreator;
import br.com.plutomc.core.common.backend.data.DiscordData;
import br.com.plutomc.core.common.backend.redis.RedisConnection;
import redis.clients.jedis.Jedis;

public class DiscordDataImpl implements DiscordData {
   private RedisConnection redisConnection;

   @Override
   public String getNameByCode(String code, boolean delete) {
      String var9;
      try (Jedis jedis = this.redisConnection.getPool().getResource()) {
         Set<String> list = jedis.keys("discord-sync:*");
         Iterator var6 = list.iterator();

         String possible;
         do {
            if (!var6.hasNext()) {
               return null;
            }

            possible = (String)var6.next();
         } while(!jedis.get(possible).equals(code));

         String name = possible.replace("discord-sync:", "");
         if (delete) {
            jedis.del(possible);
         }

         var9 = name;
      }

      return var9;
   }

   @Override
   public String getCodeOrCreate(String playerName, String code) {
      String var6;
      try (Jedis jedis = this.redisConnection.getPool().getResource()) {
         boolean exists = jedis.ttl("discord-sync:" + playerName.toLowerCase()) >= 0L;
         if (!exists) {
            code = CodeCreator.DEFAULT_CREATOR_LETTERS_ONLY.random(6);
            jedis.setex("discord-sync:" + playerName.toLowerCase(), 120, code);
            return code;
         }

         var6 = jedis.get("discord-sync:" + playerName.toLowerCase());
      }

      return var6;
   }

   public DiscordDataImpl(RedisConnection redisConnection) {
      this.redisConnection = redisConnection;
   }
}

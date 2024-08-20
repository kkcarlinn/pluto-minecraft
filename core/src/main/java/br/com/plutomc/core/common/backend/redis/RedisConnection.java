package br.com.plutomc.core.common.backend.redis;

import java.util.logging.Level;
import lombok.NonNull;
import br.com.plutomc.core.common.CommonPlugin;
import br.com.plutomc.core.common.backend.Credentials;
import br.com.plutomc.core.common.backend.Database;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;

public class RedisConnection implements Database {
   @NonNull
   private final String hostname;
   @NonNull
   private final String password;
   private final int port;
   private JedisPool pool;

   public RedisConnection() {
      this("localhost", "", 6379);
   }

   public RedisConnection(Credentials credentials) {
      this(credentials.getHostName(), credentials.getPassWord(), credentials.getPort());
   }

   @Override
   public void connect() {
      JedisPoolConfig config = new JedisPoolConfig();
      config.setMaxTotal(128);
      if (!this.password.isEmpty()) {
         this.pool = new JedisPool(config, this.hostname, this.port, 0, this.password);
      } else {
         this.pool = new JedisPool(config, this.hostname, this.port, 0);
      }
   }

   @Override
   public boolean isConnected() {
      return !this.pool.isClosed();
   }

   @Override
   public void close() {
      if (this.pool != null) {
         this.pool.destroy();
      }
   }

   public RedisConnection(@NonNull String hostname, @NonNull String password, int port) {
      if (hostname == null) {
         throw new NullPointerException("hostname is marked non-null but is null");
      } else if (password == null) {
         throw new NullPointerException("password is marked non-null but is null");
      } else {
         this.hostname = hostname;
         this.password = password;
         this.port = port;
      }
   }

   public JedisPool getPool() {
      return this.pool;
   }

   public static class PubSubListener implements Runnable {
      private RedisConnection redis;
      private JedisPubSub jpsh;
      private final String[] channels;

      public PubSubListener(RedisConnection redis, JedisPubSub s, String... channels) {
         this.redis = redis;
         this.jpsh = s;
         this.channels = channels;
      }

      @Override
      public void run() {
         CommonPlugin.getInstance().getLogger().log(Level.INFO, "Loading jedis!");

         try (Jedis jedis = this.redis.getPool().getResource()) {
            try {
               jedis.subscribe(this.jpsh, this.channels);
            } catch (Exception var15) {
               CommonPlugin.getInstance().getLogger().log(Level.INFO, "PubSub error, attempting to recover.", (Throwable)var15);

               try {
                  this.jpsh.unsubscribe();
               } catch (Exception var14) {
               }

               this.run();
            }
         }
      }

      public void addChannel(String... channel) {
         this.jpsh.subscribe(channel);
      }

      public void removeChannel(String... channel) {
         this.jpsh.unsubscribe(channel);
      }

      public void poison() {
         this.jpsh.unsubscribe();
      }
   }
}

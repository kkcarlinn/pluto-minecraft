package br.com.plutomc.core.common.backend.data.impl;

import br.com.plutomc.core.common.packet.Packet;
import com.google.gson.JsonElement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import br.com.plutomc.core.common.CommonConst;
import br.com.plutomc.core.common.CommonPlugin;
import br.com.plutomc.core.common.backend.data.DataServerMessage;
import br.com.plutomc.core.common.backend.data.ServerData;
import br.com.plutomc.core.common.backend.mongodb.MongoQuery;
import br.com.plutomc.core.common.backend.redis.RedisConnection;
import br.com.plutomc.core.common.server.ServerType;
import br.com.plutomc.core.common.server.loadbalancer.server.MinigameState;
import br.com.plutomc.core.common.server.loadbalancer.server.ProxiedServer;
import br.com.plutomc.core.common.utils.json.JsonBuilder;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;

public class ServerDataImpl implements ServerData {
   private RedisConnection redisDatabase;

   public ServerDataImpl(RedisConnection redisConnection) {
      this.redisDatabase = redisConnection;
   }

   @Override
   public int getTime(String serverId) {
      try (Jedis jedis = this.redisDatabase.getPool().getResource()) {
         Map<String, String> m = jedis.hgetAll("server:" + serverId);
         if (m.containsKey("time")) {
            return Integer.valueOf(m.get("time"));
         }
      } catch (Exception var18) {
         var18.printStackTrace();
      }

      return -1;
   }

   @Override
   public long getStartTime(String serverId) {
      try (Jedis jedis = this.redisDatabase.getPool().getResource()) {
         Map<String, String> m = jedis.hgetAll("server:" + serverId);
         if (m.containsKey("starttime")) {
            return (long)Integer.valueOf(m.get("starttime")).intValue();
         }
      } catch (Exception var19) {
         var19.printStackTrace();
      }

      return -1L;
   }

   @Override
   public MinigameState getState(String serverId) {
      try (Jedis jedis = this.redisDatabase.getPool().getResource()) {
         Map<String, String> m = jedis.hgetAll("server:" + serverId);
         if (m.containsKey("state")) {
            return MinigameState.valueOf(m.get("state").toUpperCase());
         }
      } catch (Exception var18) {
         var18.printStackTrace();
      }

      return MinigameState.NONE;
   }

   @Override
   public String getMap(String serverId) {
      try (Jedis jedis = this.redisDatabase.getPool().getResource()) {
         Map<String, String> m = jedis.hgetAll("server:" + serverId);
         if (m.containsKey("map")) {
            return m.get("map");
         }
      } catch (Exception var18) {
         var18.printStackTrace();
      }

      return "Unknown";
   }

   @Override
   public Map<String, Map<String, String>> loadServers() {
      Map<String, Map<String, String>> map = new HashMap<>();

      try (Jedis jedis = this.redisDatabase.getPool().getResource()) {
         String[] str = new String[ServerType.values().length];

         for(int i = 0; i < ServerType.values().length; ++i) {
            str[i] = "server:type:" + ServerType.values()[i].toString().toLowerCase();
         }

         for(String server : jedis.sunion(str)) {
            Map<String, String> m = jedis.hgetAll("server:" + server);
            map.put(server, m);
         }

         return map;
      } catch (Exception var18) {
         var18.printStackTrace();
         return new HashMap<>();
      }
   }

   @Override
   public Set<UUID> getPlayers(String serverId) {
      Set<UUID> players = new HashSet<>();

      try (Jedis jedis = this.redisDatabase.getPool().getResource()) {
         for(String uuid : jedis.smembers("server:" + serverId + ":players")) {
            UUID uniqueId = UUID.fromString(uuid);
            players.add(uniqueId);
         }
      } catch (Exception var18) {
         var18.printStackTrace();
      }

      return players;
   }

   @Override
   public void startServer(int maxPlayers) {
      try (Jedis jedis = this.redisDatabase.getPool().getResource()) {
         Pipeline pipe = jedis.pipelined();
         pipe.sadd("server:type:" + CommonPlugin.getInstance().getServerType().toString().toLowerCase(), new String[]{CommonPlugin.getInstance().getServerId()});
         Map<String, String> map = new HashMap<>();
         map.put("type", CommonPlugin.getInstance().getServerType().toString().toLowerCase());
         map.put("maxplayers", maxPlayers + "");
         map.put("joinenabled", CommonPlugin.getInstance().isJoinEnabled() + "");
         map.put("address", CommonPlugin.getInstance().getServerAddress());
         map.put("map", CommonPlugin.getInstance().getMap());
         map.put("time", CommonPlugin.getInstance().getServerTime() + "");
         map.put("state", CommonPlugin.getInstance().getMinigameState().toString().toLowerCase());
         map.put("starttime", System.currentTimeMillis() + "");
         pipe.hmset("server:" + CommonPlugin.getInstance().getServerId(), map);
         pipe.del("server:" + CommonPlugin.getInstance().getServerId() + ":players");
         ProxiedServer server = new ProxiedServer(
            CommonPlugin.getInstance().getServerId(),
            CommonPlugin.getInstance().getServerType(),
            new HashSet<>(),
            maxPlayers,
            CommonPlugin.getInstance().isJoinEnabled()
         );
         pipe.publish(
            "server_info",
            CommonConst.GSON
               .toJson(
                  new DataServerMessage<>(
                     CommonPlugin.getInstance().getServerId(),
                     CommonPlugin.getInstance().getServerType(),
                     DataServerMessage.Action.START,
                     new DataServerMessage.StartPayload(CommonPlugin.getInstance().getServerAddress(), server, System.currentTimeMillis())
                  )
               )
         );
         pipe.sync();
      }
   }

   @Override
   public void updateStatus() {
      this.updateStatus(CommonPlugin.getInstance().getMinigameState(), CommonPlugin.getInstance().getMap(), CommonPlugin.getInstance().getServerTime());
   }

   @Override
   public void updateStatus(MinigameState state, int time) {
      this.updateStatus(state, CommonPlugin.getInstance().getMap(), time);
   }

   @Override
   public void updateStatus(MinigameState state, String map, int time) {
      this.updateStatus(CommonPlugin.getInstance().getServerId(), state, map, time);
   }

   @Override
   public void updateStatus(String serverId, MinigameState state, String map, int time) {
      try (Jedis jedis = this.redisDatabase.getPool().getResource()) {
         Pipeline pipe = jedis.pipelined();
         pipe.hset("server:" + serverId, "map", map);
         pipe.hset("server:" + serverId, "time", Integer.toString(time));
         pipe.hset("server:" + serverId, "state", state.toString().toLowerCase());
         pipe.publish(
            "server_info",
            CommonConst.GSON
               .toJson(
                  new DataServerMessage<>(
                     CommonPlugin.getInstance().getServerId(),
                     CommonPlugin.getInstance().getServerType(),
                     DataServerMessage.Action.UPDATE,
                     new DataServerMessage.UpdatePayload(time, map, state)
                  )
               )
         );
         pipe.sync();
      }
   }

   @Override
   public void setJoinEnabled(String serverId, boolean bol) {
      try (Jedis jedis = this.redisDatabase.getPool().getResource()) {
         Pipeline pipe = jedis.pipelined();
         pipe.hset("server:" + serverId, "joinenabled", Boolean.toString(bol));
         pipe.publish(
            "server_info",
            CommonConst.GSON
               .toJson(
                  new DataServerMessage<>(
                     CommonPlugin.getInstance().getServerId(),
                     CommonPlugin.getInstance().getServerType(),
                     DataServerMessage.Action.JOIN_ENABLE,
                     new DataServerMessage.JoinEnablePayload(bol)
                  )
               )
         );
         pipe.sync();
      }
   }

   @Override
   public void setJoinEnabled(boolean bol) {
      this.setJoinEnabled(CommonPlugin.getInstance().getServerId(), bol);
   }

   @Override
   public void stopServer() {
      try (Jedis jedis = this.redisDatabase.getPool().getResource()) {
         Pipeline pipe = jedis.pipelined();
         pipe.srem("server:type:" + CommonPlugin.getInstance().getServerType().toString().toLowerCase(), new String[]{CommonPlugin.getInstance().getServerId()});
         pipe.del("server:" + CommonPlugin.getInstance().getServerId());
         pipe.del("server:" + CommonPlugin.getInstance().getServerId() + ":players");
         pipe.publish(
            "server_info",
            CommonConst.GSON
               .toJson(
                  new DataServerMessage<>(
                     CommonPlugin.getInstance().getServerId(),
                     CommonPlugin.getInstance().getServerType(),
                     DataServerMessage.Action.STOP,
                     new DataServerMessage.StopPayload(CommonPlugin.getInstance().getServerId())
                  )
               )
         );
         pipe.sync();
      }
   }

   @Override
   public void setTotalPlayers(int totalMembers) {
      try (Jedis jedis = this.redisDatabase.getPool().getResource()) {
         Pipeline pipe = jedis.pipelined();
         pipe.publish("server_members", CommonConst.GSON.toJson((JsonElement)new JsonBuilder().addProperty("totalMembers", totalMembers).build()));
         pipe.sync();
      }
   }

   @Override
   public void joinPlayer(UUID uuid, int maxPlayers) {
      try (Jedis jedis = this.redisDatabase.getPool().getResource()) {
         Pipeline pipe = jedis.pipelined();
         pipe.sadd("server:" + CommonPlugin.getInstance().getServerId() + ":players", new String[]{uuid.toString()});
         pipe.hset("server:" + CommonPlugin.getInstance().getServerId(), "maxplayers", Integer.toString(maxPlayers));
         pipe.publish(
            "server_info",
            CommonConst.GSON
               .toJson(
                  new DataServerMessage<>(
                     CommonPlugin.getInstance().getServerId(),
                     CommonPlugin.getInstance().getServerType(),
                     DataServerMessage.Action.JOIN,
                     new DataServerMessage.JoinPayload(uuid, maxPlayers)
                  )
               )
         );
         pipe.sync();
      }
   }

   @Override
   public void leavePlayer(UUID uuid, int maxPlayers) {
      try (Jedis jedis = this.redisDatabase.getPool().getResource()) {
         Pipeline pipe = jedis.pipelined();
         pipe.srem("server:" + CommonPlugin.getInstance().getServerId() + ":players", new String[]{uuid.toString()});
         pipe.hset("server:" + CommonPlugin.getInstance().getServerId(), "maxplayers", Integer.toString(maxPlayers));
         pipe.publish(
            "server_info",
            CommonConst.GSON
               .toJson(
                  new DataServerMessage<>(
                     CommonPlugin.getInstance().getServerId(),
                     CommonPlugin.getInstance().getServerType(),
                     DataServerMessage.Action.LEAVE,
                     new DataServerMessage.LeavePayload(uuid, maxPlayers)
                  )
               )
         );
         pipe.sync();
      }
   }

   @Override
   public void sendPacket(Packet packet) {
      try (Jedis jedis = this.redisDatabase.getPool().getResource()) {
         Pipeline pipe = jedis.pipelined();
         pipe.publish("server_packet", CommonConst.GSON.toJson(packet));
         pipe.sync();
      }
   }

   @Override
   public void closeConnection() {
      this.redisDatabase.close();
   }

   public MongoQuery getQuery() {
      return null;
   }
}

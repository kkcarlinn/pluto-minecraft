package br.com.plutomc.core.bukkit.networking;

import br.com.plutomc.core.bukkit.BukkitCommon;
import br.com.plutomc.core.bukkit.event.member.PlayerUpdateFieldEvent;
import br.com.plutomc.core.bukkit.event.member.PlayerUpdatedFieldEvent;
import br.com.plutomc.core.bukkit.event.server.PlayerChangeEvent;
import br.com.plutomc.core.bukkit.event.server.ServerEvent;
import br.com.plutomc.core.bukkit.event.server.ServerPacketReceiveEvent;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Field;
import java.util.UUID;
import java.util.logging.Level;
import br.com.plutomc.core.common.CommonConst;
import br.com.plutomc.core.common.CommonPlugin;
import br.com.plutomc.core.common.backend.data.DataServerMessage;
import br.com.plutomc.core.bukkit.member.BukkitMember;
import br.com.plutomc.core.common.member.Member;
import br.com.plutomc.core.common.packet.Packet;
import br.com.plutomc.core.common.packet.PacketType;
import br.com.plutomc.core.common.server.ServerType;
import br.com.plutomc.core.common.server.loadbalancer.server.MinigameServer;
import br.com.plutomc.core.common.server.loadbalancer.server.ProxiedServer;
import org.bukkit.Bukkit;
import redis.clients.jedis.JedisPubSub;

public class BukkitPubSubHandler extends JedisPubSub {
   @Override
   public void onMessage(String channel, String message) {
      try {
         JsonObject jsonObject = (JsonObject)JsonParser.parseString(message);
         if (CommonPlugin.getInstance().getPluginInfo().isRedisDebugEnabled()) {
            CommonPlugin.getInstance().debug("Redis message from channel " + channel + ": " + jsonObject);
         }

         switch(channel) {
            case "server_packet":
               PacketType packetType = PacketType.valueOf(jsonObject.get("packetType").getAsString());
               Packet packet = CommonConst.GSON.fromJson(jsonObject, packetType.getClassType());
               if (!packet.isExclusiveServers() || packet.isExclusiveServers() && packet.getServerList().contains(CommonPlugin.getInstance().getServerId())) {
                  packet.receive();
                  Bukkit.getPluginManager().callEvent(new ServerPacketReceiveEvent(packetType, packet));
               }
               break;
            case "server_members":
               BukkitCommon.getInstance().getServerManager().setTotalMembers(jsonObject.get("totalMembers").getAsInt());
               Bukkit.getPluginManager().callEvent(new PlayerChangeEvent(jsonObject.get("totalMembers").getAsInt()));
               break;
            case "member_field":
               if (jsonObject.has("source") && !jsonObject.get("source").getAsString().equals(CommonPlugin.getInstance().getServerId())) {
                  UUID uuid = UUID.fromString(jsonObject.get("uniqueId").getAsString());
                  BukkitMember player = CommonPlugin.getInstance().getMemberManager().getMember(uuid, BukkitMember.class);
                  boolean pass = false;
                  if (player != null) {
                     try {
                        Field field = this.getField(Member.class, jsonObject.get("field").getAsString());
                        Object oldObject = field.get(player);
                        Object object = CommonConst.GSON.fromJson(jsonObject.get("value"), field.getGenericType());
                        PlayerUpdateFieldEvent event = new PlayerUpdateFieldEvent(
                           Bukkit.getServer().getPlayer(uuid), player, field.getName(), oldObject, object
                        );
                        Bukkit.getPluginManager().callEvent(event);
                        if (!event.isCancelled()) {
                           field.set(player, event.getObject());
                           Bukkit.getPluginManager()
                              .callEvent(new PlayerUpdatedFieldEvent(Bukkit.getServer().getPlayer(uuid), player, field.getName(), oldObject, object));
                           pass = true;
                        }
                     } catch (Exception var14) {
                        var14.printStackTrace();
                     }

                     if (pass && jsonObject.get("field").getAsString().toLowerCase().contains("configuration")) {
                        try {
                           Field field = Member.class.getDeclaredField(jsonObject.get("field").getAsString());
                           field.setAccessible(true);
                           Object object = field.get(player);
                           Field memberField = object.getClass().getDeclaredField("member");
                           memberField.setAccessible(true);
                           memberField.set(field.get(player), player);
                        } catch (Exception var13) {
                           var13.printStackTrace();
                        }
                     }
                  }
               }
               break;
            case "server_info":
               if (!BukkitCommon.getInstance().isServerLog()) {
                  return;
               }

               ServerType sourceType = ServerType.valueOf(jsonObject.get("serverType").getAsString());
               if (sourceType == ServerType.BUNGEECORD) {
                  return;
               }

               String source = jsonObject.get("source").getAsString();
               DataServerMessage.Action action = DataServerMessage.Action.valueOf(jsonObject.get("action").getAsString());
               switch(action) {
                  case JOIN: {
                     DataServerMessage<DataServerMessage.JoinPayload> payload = CommonConst.GSON
                        .fromJson(jsonObject, (new TypeToken<DataServerMessage<DataServerMessage.JoinPayload>>() {
                        }).getType());
                     ProxiedServer server = BukkitCommon.getInstance().getServerManager().getServer(source);
                     if (server == null) {
                        return;
                     }

                     server.joinPlayer(payload.getPayload().getUniqueId());
                     Bukkit.getPluginManager().callEvent(new ServerEvent(source, sourceType, server, payload, action));
                     break;
                  }
                  case LEAVE: {
                     DataServerMessage<DataServerMessage.LeavePayload> payload = CommonConst.GSON
                        .fromJson(jsonObject, (new TypeToken<DataServerMessage<DataServerMessage.LeavePayload>>() {
                        }).getType());
                     ProxiedServer server = BukkitCommon.getInstance().getServerManager().getServer(source);
                     if (server == null) {
                        return;
                     }

                     server.leavePlayer(payload.getPayload().getUniqueId());
                     Bukkit.getPluginManager().callEvent(new ServerEvent(source, sourceType, server, payload, action));
                     break;
                  }
                  case JOIN_ENABLE: {
                     DataServerMessage<DataServerMessage.JoinEnablePayload> payload = CommonConst.GSON
                        .fromJson(jsonObject, (new TypeToken<DataServerMessage<DataServerMessage.JoinEnablePayload>>() {
                        }).getType());
                     ProxiedServer server = BukkitCommon.getInstance().getServerManager().getServer(source);
                     if (server == null) {
                        return;
                     }

                     server.setJoinEnabled(payload.getPayload().isEnable());
                     break;
                  }
                  case START: {
                     DataServerMessage<DataServerMessage.StartPayload> payload = CommonConst.GSON
                        .fromJson(jsonObject, (new TypeToken<DataServerMessage<DataServerMessage.StartPayload>>() {
                        }).getType());
                     Bukkit.getPluginManager()
                        .callEvent(
                           new ServerEvent(
                              source,
                              sourceType,
                              BukkitCommon.getInstance()
                                 .getServerManager()
                                 .addActiveServer(
                                    payload.getPayload().getServerAddress(),
                                    payload.getPayload().getServer().getServerId(),
                                    sourceType,
                                    payload.getPayload().getServer().getMaxPlayers(),
                                    payload.getPayload().getStartTime()
                                 ),
                              payload,
                              action
                           )
                        );
                     break;
                  }
                  case STOP: {
                     DataServerMessage<DataServerMessage.StopPayload> payload = CommonConst.GSON
                        .fromJson(jsonObject, (new TypeToken<DataServerMessage<DataServerMessage.StopPayload>>() {
                        }).getType());
                     BukkitCommon.getInstance().getServerManager().removeActiveServer(payload.getPayload().getServerId());
                     Bukkit.getPluginManager().callEvent(new ServerEvent(source, sourceType, null, payload, action));
                     break;
                  }
                  case UPDATE: {
                     DataServerMessage<DataServerMessage.UpdatePayload> payload = CommonConst.GSON
                        .fromJson(jsonObject, (new TypeToken<DataServerMessage<DataServerMessage.UpdatePayload>>() {
                        }).getType());
                     ProxiedServer server = BukkitCommon.getInstance().getServerManager().getServer(source);
                     if (server == null) {
                        return;
                     }

                     if (server instanceof MinigameServer) {
                        ((MinigameServer)server).setState(payload.getPayload().getState());
                        ((MinigameServer)server).setTime(payload.getPayload().getTime());
                        ((MinigameServer)server).setMap(payload.getPayload().getMap());
                        Bukkit.getPluginManager().callEvent(new ServerEvent(source, sourceType, server, payload, action));
                     }
                  }
               }
         }
      } catch (Exception var15) {
         CommonPlugin.getInstance()
            .getLogger()
            .log(Level.WARNING, "An error occured when reading json packet in redis " + channel + "!\n" + message, (Throwable)var15);
         System.out.println(var15.getLocalizedMessage());
      }
   }

   private Field getField(Class<?> clazz, String fieldName) {
      while(clazz != null && clazz != Object.class) {
         try {
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field;
         } catch (NoSuchFieldException var4) {
            clazz = clazz.getSuperclass();
         }
      }

      return null;
   }
}

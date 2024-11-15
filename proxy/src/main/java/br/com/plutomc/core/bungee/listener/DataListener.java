package br.com.plutomc.core.bungee.listener;

import br.com.plutomc.core.bungee.BungeeMain;
import br.com.plutomc.core.bungee.event.RedisMessageEvent;
import br.com.plutomc.core.bungee.event.ServerUpdateEvent;
import br.com.plutomc.core.bungee.event.packet.PacketReceiveEvent;
import br.com.plutomc.core.bungee.event.player.PlayerFieldUpdateEvent;
import br.com.plutomc.core.bungee.event.player.PlayerPunishEvent;
import br.com.plutomc.core.bungee.account.BungeeAccount;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Field;
import java.util.UUID;
import java.util.logging.Level;
import br.com.plutomc.core.common.CommonConst;
import br.com.plutomc.core.common.CommonPlugin;
import br.com.plutomc.core.common.backend.data.DataServerMessage;
import br.com.plutomc.core.bungee.command.BungeeCommandSender;
import br.com.plutomc.core.common.command.CommandSender;
import br.com.plutomc.core.common.account.Account;
import br.com.plutomc.core.common.packet.Packet;
import br.com.plutomc.core.common.packet.PacketType;
import br.com.plutomc.core.common.packet.types.PunishPlayerPacket;
import br.com.plutomc.core.common.packet.types.skin.SkinChange;
import br.com.plutomc.core.common.packet.types.staff.TeleportToTarget;
import br.com.plutomc.core.common.punish.Punish;
import br.com.plutomc.core.common.server.ServerType;
import br.com.plutomc.core.common.server.loadbalancer.server.MinigameServer;
import br.com.plutomc.core.common.server.loadbalancer.server.MinigameState;
import br.com.plutomc.core.common.server.loadbalancer.server.ProxiedServer;
import br.com.plutomc.core.common.utils.reflection.Reflection;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class DataListener implements Listener {
   @EventHandler(
           priority = -128
   )
   public void onPluginMessage(PluginMessageEvent event) {
      if (event.getTag().equals("BungeeCord") && event.getSender() instanceof Server && event.getReceiver() instanceof ProxiedPlayer) {
         ProxiedPlayer proxiedPlayer = (ProxiedPlayer)event.getReceiver();
         Account player = CommonPlugin.getInstance().getAccountManager().getAccount(proxiedPlayer.getUniqueId());
         ByteArrayDataInput in = ByteStreams.newDataInput(event.getData());
         String subChannel = in.readUTF();
         switch(subChannel) {
            case "BungeeCommand":
               ProxyServer.getInstance().getPluginManager().dispatchCommand(proxiedPlayer, in.readUTF());
               break;
            case "PlayerConnect":
               String serverId = in.readUTF();
               boolean silent = in.readBoolean();
               ProxiedServer server = BungeeMain.getInstance().getServerManager().getServer(serverId);
               if (server != null && server.getServerInfo() != null) {
                  proxiedPlayer.connect(server.getServerInfo());
                  break;
               }

               if (!silent) {
                  player.sendMessage(player.getLanguage().t("server-not-found", "%server%", serverId));
               }

               return;
            case "SearchServer":
               String serverStr = in.readUTF();
               silent = in.readBoolean();

               for(String s : serverStr.contains("-") ? serverStr.split("-") : new String[]{serverStr}) {
                  try {
                     ServerType serverType = ServerType.valueOf(s);
                     if (this.searchServer(player, proxiedPlayer, serverType, silent)) {
                        return;
                     }
                  } catch (Exception var15) {
                  }
               }

               if (!silent) {
                  player.sendMessage(player.getLanguage().t("server.search-server.not-found"));
               }
         }
      }
   }

   public boolean searchServer(Account player, ProxiedPlayer proxiedPlayer, ServerType serverType, boolean silent) {
      ProxiedServer server = BungeeMain.getInstance().getServerManager().getBalancer(serverType).next();
      if (server == null || server.getServerInfo() == null) {
         return false;
      } else if (server.isFull() && !player.hasPermission("server.full")) {
         if (!silent) {
            proxiedPlayer.sendMessage(player.getLanguage().t("server-is-full"));
         }

         return true;
      } else if (!server.canBeSelected() && !player.hasPermission("command.admin")) {
         if (!silent) {
            proxiedPlayer.sendMessage(player.getLanguage().t("server-not-available"));
         }

         return true;
      } else {
         proxiedPlayer.connect(server.getServerInfo());
         return true;
      }
   }

   @EventHandler
   public void onPostLogin(PostLoginEvent event) {
      CommonPlugin.getInstance().getServerData().setTotalPlayers(BungeeCord.getInstance().getOnlineCount());
      BungeeMain.getInstance().setPlayersRecord(Math.max(BungeeCord.getInstance().getOnlineCount(), BungeeMain.getInstance().getPlayersRecord()));
   }

   @EventHandler
   public void onPlayerDisconnect(PlayerDisconnectEvent event) {
      CommonPlugin.getInstance().getServerData().setTotalPlayers(BungeeCord.getInstance().getOnlineCount() - 1);
   }

   @EventHandler
   public void onRedisMessage(RedisMessageEvent event) {
      String message = event.getMessage();

      try {
         if (message.startsWith("{") && message.endsWith("}")) {
            this.handleMessageGson(event.getChannel(), JsonParser.parseString(message).getAsJsonObject());
            return;
         }
      } catch (Exception var4) {
         CommonPlugin.getInstance().getLogger().log(Level.WARNING, "An error occured when reading json packet in redis!\n" + message, (Throwable)var4);
         System.out.println(var4.getLocalizedMessage());
      }
   }

   @EventHandler
   public void onPacketReceive(PacketReceiveEvent event) {
      switch(event.getPacketType()) {
         case TELEPORT_TO_TARGET:
            TeleportToTarget teleportToTarget = (TeleportToTarget)event.getPacket();
            ProxiedPlayer player = ProxyServer.getInstance().getPlayer(teleportToTarget.getPlayerId());
            if (player == null) {
               return;
            }

            ProxiedPlayer target = ProxyServer.getInstance().getPlayer(teleportToTarget.getTargetId());
            if (target == null) {
               player.sendMessage("§cO jogador " + teleportToTarget.getTargetName() + " não foi encontrado.");
               return;
            }

            BungeeMain.getInstance().teleport(player, target);
            break;
         case SKIN_CHANGE:
            SkinChange skinChange = (SkinChange)event.getPacket();
            player = ProxyServer.getInstance().getPlayer(skinChange.getPlayerId());
            if (player == null) {
               return;
            }

            BungeeMain.getInstance().loadTexture(player.getPendingConnection(), skinChange.getSkin());
            break;
         case PUNISH_PLAYER:
            PunishPlayerPacket punishPlayer = (PunishPlayerPacket)event.getPacket();
            Account account = CommonPlugin.getInstance().getAccountManager().getAccount(punishPlayer.getPlayerId());
            Punish punish = punishPlayer.getPunish();
            if (account == null) {
               account = CommonPlugin.getInstance().getAccountData().loadAccount(punishPlayer.getPlayerId());
            }

            ProxyServer.getInstance()
                    .getPluginManager()
                    .callEvent(
                            new PlayerPunishEvent(
                                    account,
                                    punishPlayer.getPunish(),
                                    (CommandSender)(CommonConst.CONSOLE_ID.equals(punish.getPunisherId())
                                            ? new BungeeCommandSender(ProxyServer.getInstance().getConsole())
                                            : CommonPlugin.getInstance().getAccountManager().getAccount(punish.getPunisherId()))
                            )
                    );
      }
   }

   void handleMessageGson(String channel, JsonObject jsonObject) {
      if (CommonPlugin.getInstance().getPluginInfo().isRedisDebugEnabled()) {
         CommonPlugin.getInstance().debug("Redis message from channel " + channel + ": " + jsonObject);
      }

      switch(channel) {
         case "server_packet":
            PacketType packetType = PacketType.valueOf(jsonObject.get("packetType").getAsString());
            Packet packet = CommonConst.GSON.fromJson(jsonObject, packetType.getClassType());
            if (!packet.isExclusiveServers() || packet.isExclusiveServers() && packet.getServerList().contains(CommonPlugin.getInstance().getServerId())) {
               packet.receive();
               ProxyServer.getInstance().getPluginManager().callEvent(new PacketReceiveEvent(packet));
            }
            break;
         case "member_field":
            UUID uuid = UUID.fromString(jsonObject.getAsJsonPrimitive("uniqueId").getAsString());
            ProxiedPlayer p = ProxyServer.getInstance().getPlayer(uuid);
            if (p == null) {
               return;
            }

            Account player = CommonPlugin.getInstance().getAccountManager().getAccount(uuid);
            if (player == null) {
               return;
            }

            try {
               Field f = Reflection.getField(Account.class, jsonObject.get("field").getAsString());
               Object object = CommonConst.GSON.fromJson(jsonObject.get("value"), f.getGenericType());
               f.setAccessible(true);
               f.set(player, object);
               ProxyServer.getInstance().getPluginManager().callEvent(new PlayerFieldUpdateEvent((BungeeAccount)player, jsonObject.get("field").getAsString()));
            } catch (IllegalArgumentException | IllegalAccessException | SecurityException var13) {
               var13.printStackTrace();
            }

            if (jsonObject.get("field").getAsString().toLowerCase().contains("configuration")) {
               try {
                  Field field = Account.class.getDeclaredField(jsonObject.get("field").getAsString());
                  field.setAccessible(true);
                  Object object = field.get(player);
                  Field memberField = object.getClass().getDeclaredField("member");
                  memberField.setAccessible(true);
                  memberField.set(field.get(player), player);
               } catch (Exception var12) {
                  var12.printStackTrace();
               }
            }
            break;
         case "server_info":
            String source = jsonObject.get("source").getAsString();
            ServerType sourceType = ServerType.valueOf(jsonObject.get("serverType").getAsString());
            DataServerMessage.Action action = DataServerMessage.Action.valueOf(jsonObject.get("action").getAsString());
            if (sourceType != ServerType.BUNGEECORD) {
               switch(action) {
                  case JOIN: {
                     DataServerMessage<DataServerMessage.JoinPayload> payload = CommonConst.GSON
                             .fromJson(jsonObject, (new TypeToken<DataServerMessage<DataServerMessage.JoinPayload>>() {
                             }).getType());
                     ProxiedServer server = BungeeMain.getInstance().getServerManager().getServer(source);
                     if (server == null) {
                        return;
                     }

                     server.joinPlayer(payload.getPayload().getUniqueId());
                     server.setMaxPlayers(payload.getPayload().getMaxPlayers());
                     break;
                  }
                  case LEAVE: {
                     DataServerMessage<DataServerMessage.LeavePayload> payload = CommonConst.GSON
                             .fromJson(jsonObject, (new TypeToken<DataServerMessage<DataServerMessage.LeavePayload>>() {
                             }).getType());
                     ProxiedServer server = BungeeMain.getInstance().getServerManager().getServer(source);
                     if (server == null) {
                        return;
                     }

                     server.leavePlayer(payload.getPayload().getUniqueId());
                     server.setMaxPlayers(payload.getPayload().getMaxPlayers());
                     break;
                  }
                  case JOIN_ENABLE: {
                     DataServerMessage<DataServerMessage.JoinEnablePayload> payload = CommonConst.GSON
                             .fromJson(jsonObject, (new TypeToken<DataServerMessage<DataServerMessage.JoinEnablePayload>>() {
                             }).getType());
                     ProxiedServer server = BungeeMain.getInstance().getServerManager().getServer(source);
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
                     BungeeMain.getInstance()
                             .getServerManager()
                             .addActiveServer(
                                     payload.getPayload().getServerAddress(),
                                     payload.getPayload().getServer().getServerId(),
                                     sourceType,
                                     payload.getPayload().getServer().getMaxPlayers(),
                                     payload.getPayload().getStartTime()
                             );
                     break;
                  }
                  case STOP: {
                     DataServerMessage<DataServerMessage.StopPayload> payload = CommonConst.GSON
                             .fromJson(jsonObject, (new TypeToken<DataServerMessage<DataServerMessage.StopPayload>>() {
                             }).getType());
                     if (sourceType != ServerType.BUNGEECORD) {
                        BungeeMain.getInstance().getServerManager().removeActiveServer(payload.getPayload().getServerId());
                     }
                     break;
                  }
                  case UPDATE: {
                     DataServerMessage<DataServerMessage.UpdatePayload> payload = CommonConst.GSON
                             .fromJson(jsonObject, (new TypeToken<DataServerMessage<DataServerMessage.UpdatePayload>>() {
                             }).getType());
                     ProxiedServer server = BungeeMain.getInstance().getServerManager().getServer(source);
                     if (server == null) {
                        return;
                     }

                     if (server instanceof MinigameServer) {
                        MinigameServer minigame = (MinigameServer)server;
                        MinigameState lastState = minigame.getState();
                        minigame.setState(payload.getPayload().getState());
                        minigame.setTime(payload.getPayload().getTime());
                        minigame.setMap(payload.getPayload().getMap());
                        ProxyServer.getInstance()
                                .getPluginManager()
                                .callEvent(
                                        new ServerUpdateEvent(
                                                minigame, payload.getPayload().getMap(), payload.getPayload().getTime(), lastState, payload.getPayload().getState()
                                        )
                                );
                     }
                  }
               }
            }
      }
   }
}
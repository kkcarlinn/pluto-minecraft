package br.com.plutomc.core.common.backend.data;

import java.util.UUID;

import br.com.plutomc.core.common.server.ServerType;
import br.com.plutomc.core.common.server.loadbalancer.server.MinigameState;
import br.com.plutomc.core.common.server.loadbalancer.server.ProxiedServer;

public class DataServerMessage<T> {
   private final String source;
   private final ServerType serverType;
   private final Action action;
   private final T payload;

   public String getSource() {
      return this.source;
   }

   public ServerType getServerType() {
      return this.serverType;
   }

   public Action getAction() {
      return this.action;
   }

   public T getPayload() {
      return this.payload;
   }

   public DataServerMessage(String source, ServerType serverType, Action action, T payload) {
      this.source = source;
      this.serverType = serverType;
      this.action = action;
      this.payload = payload;
   }

   public static enum Action {
      START,
      STOP,
      UPDATE,
      JOIN_ENABLE,
      JOIN,
      LEAVE;
   }

   public static class JoinEnablePayload {
      private final boolean enable;

      public boolean isEnable() {
         return this.enable;
      }

      public JoinEnablePayload(boolean enable) {
         this.enable = enable;
      }
   }

   public static class JoinPayload {
      private final UUID uniqueId;
      private final int maxPlayers;

      public UUID getUniqueId() {
         return this.uniqueId;
      }

      public int getMaxPlayers() {
         return this.maxPlayers;
      }

      public JoinPayload(UUID uniqueId, int maxPlayers) {
         this.uniqueId = uniqueId;
         this.maxPlayers = maxPlayers;
      }
   }

   public static class LeavePayload {
      private final UUID uniqueId;
      private final int maxPlayers;

      public UUID getUniqueId() {
         return this.uniqueId;
      }

      public int getMaxPlayers() {
         return this.maxPlayers;
      }

      public LeavePayload(UUID uniqueId, int maxPlayers) {
         this.uniqueId = uniqueId;
         this.maxPlayers = maxPlayers;
      }
   }

   public static class StartPayload {
      private final String serverAddress;
      private final ProxiedServer server;
      private final long startTime;

      public String getServerAddress() {
         return this.serverAddress;
      }

      public ProxiedServer getServer() {
         return this.server;
      }

      public long getStartTime() {
         return this.startTime;
      }

      public StartPayload(String serverAddress, ProxiedServer server, long startTime) {
         this.serverAddress = serverAddress;
         this.server = server;
         this.startTime = startTime;
      }
   }

   public static class StopPayload {
      private final String serverId;

      public String getServerId() {
         return this.serverId;
      }

      public StopPayload(String serverId) {
         this.serverId = serverId;
      }
   }

   public static class UpdatePayload {
      private final int time;
      private final String map;
      private final MinigameState state;

      public int getTime() {
         return this.time;
      }

      public String getMap() {
         return this.map;
      }

      public MinigameState getState() {
         return this.state;
      }

      public UpdatePayload(int time, String map, MinigameState state) {
         this.time = time;
         this.map = map;
         this.state = state;
      }
   }
}

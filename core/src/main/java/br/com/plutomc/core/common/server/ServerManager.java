package br.com.plutomc.core.common.server;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import br.com.plutomc.core.common.server.loadbalancer.BaseBalancer;
import br.com.plutomc.core.common.server.loadbalancer.server.*;
import br.com.plutomc.core.common.server.loadbalancer.type.LeastConnection;
import br.com.plutomc.core.common.server.loadbalancer.type.MostConnection;

public class ServerManager {
   private Map<String, ProxiedServer> activeServers;
   private Map<ServerType, BaseBalancer<ProxiedServer>> balancers = new HashMap<>();
   private int totalMembers;

   public ServerManager() {
      this.activeServers = new HashMap<>();

      for(ServerType serverType : ServerType.values()) {
         if (serverType != ServerType.BUNGEECORD) {
            this.balancers
               .put(serverType, serverType.name().contains("LOBBY") ? new LeastConnection<>() : new MostConnection<>());
         }
      }
   }

   public BaseBalancer<ProxiedServer> getBalancer(ServerType type) {
      return this.balancers.get(type);
   }

   public void putBalancer(ServerType type, BaseBalancer<ProxiedServer> balancer) {
      this.balancers.put(type, balancer);
   }

   public ProxiedServer addActiveServer(String serverAddress, String serverIp, ServerType type, int maxPlayers, long startTime) {
      return this.updateActiveServer(serverIp, type, new HashSet<>(), maxPlayers, true, startTime);
   }

   public ProxiedServer updateActiveServer(String serverId, ServerType type, Set<UUID> onlinePlayers, int maxPlayers, boolean canJoin, long startTime) {
      return this.updateActiveServer(serverId, type, onlinePlayers, maxPlayers, canJoin, 0, "Unknown", null, startTime);
   }

   public ProxiedServer updateActiveServer(
           String serverId, ServerType type, Set<UUID> onlinePlayers, int maxPlayers, boolean canJoin, int tempo, String map, MinigameState state, long startTime
   ) {
      ProxiedServer server = this.activeServers.get(serverId);
      if (server == null) {
         if (type.isLobby()) {
            server = new ProxiedServer(serverId, type, onlinePlayers, maxPlayers, true);
         } else if (type.isHG()) {
            server = new HungerGamesServer(serverId, type, onlinePlayers, maxPlayers, true);
         } else if (type.name().startsWith("SW")) {
            server = new SkywarsServer(serverId, type, onlinePlayers, maxPlayers, true);
         } else if (type.name().startsWith("BW")) {
            server = new BedwarsServer(serverId, type, onlinePlayers, maxPlayers, true);
         } else if(type.name().startsWith("DUELS")) {
            server = new DuelsServer(serverId, type, onlinePlayers, maxPlayers, true);
         }else {
            server = new ProxiedServer(serverId, type, onlinePlayers, maxPlayers, true);
         }

         this.activeServers.put(serverId.toLowerCase(), server);
      }

      server.setOnlinePlayers(onlinePlayers);
      server.setJoinEnabled(canJoin);
      server.setStartTime(startTime);
      if (state != null && server instanceof MinigameServer) {
         ((MinigameServer)server).setState(state);
         ((MinigameServer)server).setTime(tempo);
         ((MinigameServer)server).setMap(map);
      }

      this.addToBalancers(serverId, server);
      return server;
   }

   public ProxiedServer getServer(String serverName) {
      return this.activeServers.get(serverName.toLowerCase());
   }

   public ProxiedServer getServerByName(String serverName) {
      for(ProxiedServer proxiedServer : this.activeServers.values()) {
         if (proxiedServer.getServerId().toLowerCase().startsWith(serverName.toLowerCase())) {
            return proxiedServer;
         }
      }

      return this.activeServers.get(serverName.toLowerCase());
   }

   public Collection<ProxiedServer> getServers() {
      return this.activeServers.values();
   }

   public void removeActiveServer(String str) {
      if (this.getServer(str) != null) {
         this.removeFromBalancers(this.getServer(str));
      }

      this.activeServers.remove(str.toLowerCase());
   }

   public void addToBalancers(String serverId, ProxiedServer server) {
      BaseBalancer<ProxiedServer> balancer = this.getBalancer(server.getServerType());
      if (balancer != null) {
         balancer.add(serverId.toLowerCase(), server);
      }
   }

   public void removeFromBalancers(ProxiedServer serverId) {
      BaseBalancer<ProxiedServer> balancer = this.getBalancer(serverId.getServerType());
      if (balancer != null) {
         balancer.remove(serverId.getServerId().toLowerCase());
      }
   }

   public void setTotalMembers(int totalMembers) {
      this.totalMembers = totalMembers;
   }

   public int getTotalNumber() {
      return this.totalMembers;
   }

   public int getTotalNumber(ServerType... serverTypes) {
      int number = 0;

      for(ServerType serverType : serverTypes) {
         number += this.getBalancer(serverType).getTotalNumber();
      }

      return number;
   }

   public int getTotalNumber(List<ServerType> types) {
      int players = 0;

      for(ServerType type : types) {
         players += this.getBalancer(type).getTotalNumber();
      }

      return players;
   }

   public Map<String, ProxiedServer> getActiveServers() {
      return this.activeServers;
   }

   public Map<ServerType, BaseBalancer<ProxiedServer>> getBalancers() {
      return this.balancers;
   }

   public int getTotalMembers() {
      return this.totalMembers;
   }
}

package br.com.plutomc.lobby.lobbyhost.server;

import java.util.HashSet;
import java.util.Set;
import br.com.plutomc.core.common.backend.data.DataServerMessage;
import br.com.plutomc.core.common.server.ServerType;
import br.com.plutomc.core.common.server.loadbalancer.server.ProxiedServer;

public abstract class ServerWatcher {
   private Set<String> serverIds = new HashSet<>();
   private Set<ServerType> serverTypes = new HashSet<>();

   public ServerWatcher server(ServerType serverType) {
      this.serverTypes.add(serverType);
      return this;
   }

   public ServerWatcher server(String serverId) {
      this.serverIds.add(serverId.toLowerCase());
      return this;
   }

   public void pulse(ProxiedServer server, DataServerMessage<?> data) {
      if (this.serverIds.contains(data.getSource()) || this.serverTypes.contains(data.getServerType())) {
         this.onServerUpdate(server, data);
      }
   }

   public abstract void onServerUpdate(ProxiedServer var1, DataServerMessage<?> var2);

   public Set<String> getServerIds() {
      return this.serverIds;
   }

   public Set<ServerType> getServerTypes() {
      return this.serverTypes;
   }
}

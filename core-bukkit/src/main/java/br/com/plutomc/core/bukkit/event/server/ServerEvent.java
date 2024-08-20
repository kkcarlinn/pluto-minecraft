package br.com.plutomc.core.bukkit.event.server;

import br.com.plutomc.core.bukkit.event.NormalEvent;
import br.com.plutomc.core.common.backend.data.DataServerMessage;
import br.com.plutomc.core.common.server.ServerType;
import br.com.plutomc.core.common.server.loadbalancer.server.ProxiedServer;

public class ServerEvent extends NormalEvent {
   private String serverId;
   private ServerType serverType;
   private ProxiedServer proxiedServer;
   private DataServerMessage<?> data;
   private DataServerMessage.Action action;

   public ServerEvent(String serverId, ServerType serverType, ProxiedServer proxiedServer, DataServerMessage<?> data, DataServerMessage.Action action) {
      this.serverId = serverId;
      this.serverType = serverType;
      this.proxiedServer = proxiedServer;
      this.data = data;
      this.action = action;
   }

   public String getServerId() {
      return this.serverId;
   }

   public ServerType getServerType() {
      return this.serverType;
   }

   public ProxiedServer getProxiedServer() {
      return this.proxiedServer;
   }

   public DataServerMessage<?> getData() {
      return this.data;
   }

   public DataServerMessage.Action getAction() {
      return this.action;
   }
}

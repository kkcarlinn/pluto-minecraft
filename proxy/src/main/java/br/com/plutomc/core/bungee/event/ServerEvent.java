package br.com.plutomc.core.bungee.event;

import br.com.plutomc.core.common.server.loadbalancer.server.ProxiedServer;
import net.md_5.bungee.api.plugin.Event;

public class ServerEvent extends Event {
   private ProxiedServer proxiedServer;

   public ProxiedServer getProxiedServer() {
      return this.proxiedServer;
   }

   public ServerEvent(ProxiedServer proxiedServer) {
      this.proxiedServer = proxiedServer;
   }
}

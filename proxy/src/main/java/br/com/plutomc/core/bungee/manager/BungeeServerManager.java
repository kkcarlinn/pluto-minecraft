package br.com.plutomc.core.bungee.manager;

import java.net.InetSocketAddress;
import br.com.plutomc.core.common.server.ServerManager;
import br.com.plutomc.core.common.server.ServerType;
import br.com.plutomc.core.common.server.loadbalancer.server.ProxiedServer;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;

public class BungeeServerManager extends ServerManager {
   @Override
   public ProxiedServer addActiveServer(String serverAddress, String serverIp, ServerType type, int maxPlayers, long startTime) {
      ProxiedServer server = super.addActiveServer(serverAddress, serverIp, type, maxPlayers, startTime);
      if (!ProxyServer.getInstance().getServers().containsKey(serverIp.toLowerCase())) {
         String ipAddress = serverAddress.split(":")[0];
         int port = Integer.valueOf(serverAddress.split(":")[1]);
         ServerInfo localServerInfo = ProxyServer.getInstance()
            .constructServerInfo(serverIp.toLowerCase(), new InetSocketAddress(ipAddress, port), "Restarting", false);
         ProxyServer.getInstance().getServers().put(serverIp.toLowerCase(), localServerInfo);
      }

      return server;
   }

   @Override
   public void removeActiveServer(String str) {
      super.removeActiveServer(str);
      if (ProxyServer.getInstance().getServers().containsKey(str.toLowerCase())) {
         ProxyServer.getInstance().getServers().remove(str.toLowerCase());
      }
   }
}

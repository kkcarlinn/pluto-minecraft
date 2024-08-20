package br.com.plutomc.lobby.lobbyhost.listener;

import br.com.plutomc.core.bukkit.event.server.ServerEvent;
import br.com.plutomc.lobby.lobbyhost.LobbyHost;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class ServerListener implements Listener {
   @EventHandler
   public void onServer(ServerEvent event) {
      LobbyHost.getInstance().getServerWatcherManager().pulse(event.getProxiedServer(), event.getData());
   }
}

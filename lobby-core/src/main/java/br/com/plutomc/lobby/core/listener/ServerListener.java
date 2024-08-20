package br.com.plutomc.lobby.core.listener;

import br.com.plutomc.core.bukkit.event.server.ServerEvent;
import br.com.plutomc.lobby.core.CoreMain;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class ServerListener implements Listener {
   @EventHandler
   public void onServer(ServerEvent event) {
      CoreMain.getInstance().getServerWatcherManager().pulse(event.getProxiedServer(), event.getData());
   }
}

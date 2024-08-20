package br.com.plutomc.lobby.lobbyhost.manager;

import com.google.common.collect.ImmutableSet;
import java.util.HashSet;
import java.util.Set;
import br.com.plutomc.core.common.backend.data.DataServerMessage;
import br.com.plutomc.lobby.lobbyhost.server.ServerWatcher;
import br.com.plutomc.core.common.server.loadbalancer.server.ProxiedServer;

public class ServerWatcherManager {
   private Set<ServerWatcher> serverWatcherSet = new HashSet<>();

   public void watch(ServerWatcher serverWatcher) {
      this.serverWatcherSet.add(serverWatcher);
   }

   public void pulse(ProxiedServer server, DataServerMessage<?> data) {
      ImmutableSet.copyOf(this.serverWatcherSet).forEach(serverWatcher -> serverWatcher.pulse(server, data));
   }
}

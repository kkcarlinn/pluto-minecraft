package br.com.plutomc.core.common.server.loadbalancer.server;

import java.util.Set;
import java.util.UUID;
import br.com.plutomc.core.common.server.ServerType;

public class BedwarsServer extends MinigameServer {
   public BedwarsServer(String serverId, ServerType type, Set<UUID> players, int maxPlayers, boolean joinEnabled) {
      super(serverId, type, players, maxPlayers, joinEnabled);
      this.setState(MinigameState.WAITING);
   }

   @Override
   public boolean canBeSelected() {
      return super.canBeSelected() && !this.isInProgress() && (this.getState() == MinigameState.WAITING || this.getState() == MinigameState.STARTING);
   }

   @Override
   public boolean isInProgress() {
      return this.getState() == MinigameState.PREGAME || this.getState() == MinigameState.GAMETIME || this.getState() == MinigameState.INVINCIBILITY;
   }
}

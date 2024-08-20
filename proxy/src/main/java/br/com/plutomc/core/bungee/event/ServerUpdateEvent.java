package br.com.plutomc.core.bungee.event;

import br.com.plutomc.core.common.server.loadbalancer.server.MinigameState;
import br.com.plutomc.core.common.server.loadbalancer.server.ProxiedServer;

public class ServerUpdateEvent extends ServerEvent {
   private String map;
   private int time;
   private MinigameState lastState;
   private MinigameState state;

   public ServerUpdateEvent(ProxiedServer proxiedServer, String map, int time, MinigameState lastState, MinigameState state) {
      super(proxiedServer);
      this.map = map;
      this.time = time;
      this.lastState = lastState;
      this.state = state;
   }

   public String getMap() {
      return this.map;
   }

   public int getTime() {
      return this.time;
   }

   public MinigameState getLastState() {
      return this.lastState;
   }

   public MinigameState getState() {
      return this.state;
   }
}

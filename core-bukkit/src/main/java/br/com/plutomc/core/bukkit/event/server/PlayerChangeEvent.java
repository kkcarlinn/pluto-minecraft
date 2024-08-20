package br.com.plutomc.core.bukkit.event.server;

import br.com.plutomc.core.bukkit.event.NormalEvent;

public class PlayerChangeEvent extends NormalEvent {
   private int totalMembers;

   public int getTotalMembers() {
      return this.totalMembers;
   }

   public PlayerChangeEvent(int totalMembers) {
      this.totalMembers = totalMembers;
   }
}

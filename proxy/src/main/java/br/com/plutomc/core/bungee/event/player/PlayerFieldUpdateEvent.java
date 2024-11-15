package br.com.plutomc.core.bungee.event.player;

import br.com.plutomc.core.bungee.account.BungeeAccount;
import net.md_5.bungee.api.plugin.Event;

public class PlayerFieldUpdateEvent extends Event {
   private final BungeeAccount player;
   private String fieldName;

   public PlayerFieldUpdateEvent(BungeeAccount player, String fieldName) {
      this.player = player;
      this.fieldName = fieldName;
   }

   public BungeeAccount getPlayer() {
      return this.player;
   }

   public String getFieldName() {
      return this.fieldName;
   }
}

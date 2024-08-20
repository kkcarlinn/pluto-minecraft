package br.com.plutomc.core.bungee.event.player;

import br.com.plutomc.core.bungee.member.BungeeMember;
import net.md_5.bungee.api.plugin.Event;

public class PlayerFieldUpdateEvent extends Event {
   private final BungeeMember player;
   private String fieldName;

   public PlayerFieldUpdateEvent(BungeeMember player, String fieldName) {
      this.player = player;
      this.fieldName = fieldName;
   }

   public BungeeMember getPlayer() {
      return this.player;
   }

   public String getFieldName() {
      return this.fieldName;
   }
}

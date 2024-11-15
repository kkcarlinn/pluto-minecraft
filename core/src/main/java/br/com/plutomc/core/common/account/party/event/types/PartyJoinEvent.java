package br.com.plutomc.core.common.account.party.event.types;

import java.util.UUID;
import br.com.plutomc.core.common.account.party.event.PartyEvent;

public class PartyJoinEvent extends PartyEvent {
   private UUID userId;

   public UUID getUserId() {
      return this.userId;
   }

   public PartyJoinEvent(UUID userId) {
      this.userId = userId;
   }
}

package br.com.plutomc.core.common.account.party.event.types;

import java.util.UUID;
import br.com.plutomc.core.common.account.party.event.PartyEvent;

public class PartyLeaveEvent extends PartyEvent {
   private UUID userId;

   public UUID getUserId() {
      return this.userId;
   }

   public PartyLeaveEvent(UUID memberId) {
      this.userId = memberId;
   }
}

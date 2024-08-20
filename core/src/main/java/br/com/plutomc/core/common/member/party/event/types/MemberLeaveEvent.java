package br.com.plutomc.core.common.member.party.event.types;

import java.util.UUID;
import br.com.plutomc.core.common.member.party.event.PartyEvent;

public class MemberLeaveEvent extends PartyEvent {
   private UUID memberId;

   public UUID getMemberId() {
      return this.memberId;
   }

   public MemberLeaveEvent(UUID memberId) {
      this.memberId = memberId;
   }
}

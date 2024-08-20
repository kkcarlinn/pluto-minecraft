package br.com.plutomc.core.common.member.party.event.types;

import java.util.UUID;
import br.com.plutomc.core.common.member.party.event.PartyEvent;

public class MemberJoinEvent extends PartyEvent {
   private UUID memberId;

   public UUID getMemberId() {
      return this.memberId;
   }

   public MemberJoinEvent(UUID memberId) {
      this.memberId = memberId;
   }
}

package br.com.plutomc.core.bungee.member;

import java.util.UUID;
import br.com.plutomc.core.common.member.Member;
import br.com.plutomc.core.common.member.party.Party;

public class BungeeParty extends Party {
   public BungeeParty(UUID partyId, Member member) {
      super(partyId, member);
   }
}

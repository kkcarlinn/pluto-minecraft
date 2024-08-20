package br.com.plutomc.core.bukkit.member.party;

import java.util.UUID;
import br.com.plutomc.core.common.member.Member;
import br.com.plutomc.core.common.member.party.Party;

public class BukkitParty extends Party {
   public BukkitParty(UUID partyId, Member member) {
      super(partyId, member);
   }
}

package br.com.plutomc.core.common.packet.types.party;

import java.util.UUID;

import br.com.plutomc.core.common.CommonPlugin;
import br.com.plutomc.core.common.account.party.Party;
import br.com.plutomc.core.common.packet.Packet;
import br.com.plutomc.core.common.packet.PacketType;

public class PartyDelete extends Packet {
   private UUID partyId;

   public PartyDelete(UUID partyId) {
      super(PacketType.PARTY_DELETE);
      this.partyId = partyId;
   }

   @Override
   public void receive() {
      Party party = CommonPlugin.getInstance().getPartyManager().getPartyById(this.partyId);
      if (party != null) {
         CommonPlugin.getInstance().getPartyManager().unloadParty(this.partyId);
      }
   }

   public UUID getPartyId() {
      return this.partyId;
   }
}

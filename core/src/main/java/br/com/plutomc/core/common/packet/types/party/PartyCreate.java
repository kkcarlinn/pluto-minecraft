package br.com.plutomc.core.common.packet.types.party;

import br.com.plutomc.core.common.CommonConst;
import br.com.plutomc.core.common.CommonPlugin;
import br.com.plutomc.core.common.member.party.Party;
import br.com.plutomc.core.common.packet.Packet;
import br.com.plutomc.core.common.packet.PacketType;
import com.google.gson.JsonObject;

public class PartyCreate extends Packet {
   private JsonObject jsonObject;

   public PartyCreate(Party party) {
      super(PacketType.PARTY_CREATE);
      this.jsonObject = CommonConst.GSON.toJsonTree(party).getAsJsonObject();
   }

   @Override
   public void receive() {
      Party party = CommonConst.GSON.fromJson(this.jsonObject, CommonPlugin.getInstance().getPartyClass());
      if (party != null) {
         CommonPlugin.getInstance().getPartyManager().loadParty(party);
         System.out.println("pacote recebido");
      }
   }

   public JsonObject getJsonObject() {
      return this.jsonObject;
   }
}

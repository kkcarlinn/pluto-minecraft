package br.com.plutomc.core.common.packet.types.party;

import br.com.plutomc.core.common.CommonConst;
import br.com.plutomc.core.common.CommonPlugin;
import br.com.plutomc.core.common.member.party.Party;
import br.com.plutomc.core.common.packet.Packet;
import br.com.plutomc.core.common.packet.PacketType;
import br.com.plutomc.core.common.utils.json.JsonBuilder;
import br.com.plutomc.core.common.utils.json.JsonUtils;
import br.com.plutomc.core.common.utils.reflection.Reflection;
import com.google.gson.JsonObject;
import java.lang.reflect.Field;
import java.util.UUID;

public class PartyField extends Packet {
   private UUID reportId;
   private JsonObject jsonObject;

   public PartyField(Party report, String fieldName) {
      super(PacketType.PARTY_FIELD);
      JsonObject tree = JsonUtils.jsonTree(report);
      this.reportId = report.getPartyId();
      this.jsonObject = new JsonBuilder().addProperty("fieldName", fieldName).add("value", tree.get(fieldName)).build();
   }

   @Override
   public void receive() {
      Party party = CommonPlugin.getInstance().getPartyManager().getPartyById(this.reportId);
      if (party != null) {
         try {
            Field f = Reflection.getField(Party.class, this.jsonObject.get("fieldName").getAsString());
            Object object = CommonConst.GSON.fromJson(this.jsonObject.get("value"), f.getGenericType());
            f.setAccessible(true);
            f.set(party, object);
         } catch (SecurityException | IllegalAccessException | IllegalArgumentException var4) {
            var4.printStackTrace();
         }
      }
   }
}

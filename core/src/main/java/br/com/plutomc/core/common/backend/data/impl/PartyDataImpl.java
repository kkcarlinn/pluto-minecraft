package br.com.plutomc.core.common.backend.data.impl;

import br.com.plutomc.core.common.CommonConst;
import br.com.plutomc.core.common.CommonPlugin;
import br.com.plutomc.core.common.backend.data.PartyData;
import br.com.plutomc.core.common.backend.mongodb.MongoConnection;
import br.com.plutomc.core.common.backend.mongodb.MongoQuery;
import br.com.plutomc.core.common.member.party.Party;
import br.com.plutomc.core.common.packet.types.party.PartyCreate;
import br.com.plutomc.core.common.packet.types.party.PartyDelete;
import br.com.plutomc.core.common.packet.types.party.PartyField;
import br.com.plutomc.core.common.utils.json.JsonBuilder;
import br.com.plutomc.core.common.utils.json.JsonUtils;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.UUID;

public class PartyDataImpl implements PartyData {
   private MongoQuery query;

   public PartyDataImpl(MongoConnection mongoConnection) {
      this.query = MongoQuery.createDefault(mongoConnection, mongoConnection.getDataBase(), "party");
   }

   @Override
   public <T extends Party> T loadParty(UUID partyId, Class<T> clazz) {
      JsonElement jsonElement = this.query.findOne("partyId", partyId.toString());
      return jsonElement == null ? null : CommonConst.GSON.fromJson(jsonElement, clazz);
   }

   @Override
   public void createParty(Party party) {
      JsonElement jsonElement = this.query.findOne("partyId", party.getPartyId().toString());
      if (jsonElement == null) {
         this.query.create(new String[]{CommonConst.GSON.toJson(party)});
      }

      CommonPlugin.getInstance()
         .getPluginPlatform()
         .runAsync(
            () -> CommonPlugin.getInstance()
                  .getServerData()
                  .sendPacket(
                     new PartyCreate(party)
                        .server(
                           party.getMembers()
                              .stream()
                              .map(id -> CommonPlugin.getInstance().getMemberManager().getMember(id).getActualServerId())
                              .toArray(x$0 -> new String[x$0])
                        )
                  )
         );
   }

   @Override
   public void deleteParty(Party party) {
      JsonElement jsonElement = this.query.findOne("partyId", party.getPartyId().toString());
      if (jsonElement != null) {
         this.query.deleteOne("partyId", party.getPartyId().toString());
      }

      CommonPlugin.getInstance()
         .getPluginPlatform()
         .runAsync(
            () -> CommonPlugin.getInstance()
                  .getServerData()
                  .sendPacket(
                     new PartyDelete(party.getPartyId())
                        .server(
                           party.getMembers()
                              .stream()
                              .filter(id -> CommonPlugin.getInstance().getMemberManager().getMember(id) != null)
                              .map(id -> CommonPlugin.getInstance().getMemberManager().getMember(id).getActualServerId())
                              .distinct()
                              .toArray(x$0 -> new String[x$0])
                        )
                  )
         );
   }

   @Override
   public void updateParty(final Party party, final String fieldName) {
      CommonPlugin.getInstance()
         .getPluginPlatform()
         .runAsync(
            new Runnable() {
               @Override
               public void run() {
                  JsonObject tree = JsonUtils.jsonTree(party);
                  CommonPlugin.getInstance()
                     .getServerData()
                     .sendPacket(
                        new PartyField(party, fieldName)
                           .server(
                              party.getMembers()
                                 .stream()
                                 .filter(id -> CommonPlugin.getInstance().getMemberManager().getMember(id) != null)
                                 .map(id -> CommonPlugin.getInstance().getMemberManager().getMember(id).getActualServerId())
                                 .distinct()
                                 .toArray(x$0 -> new String[x$0])
                           )
                     );
                  PartyDataImpl.this.query
                     .updateOne(
                        "partyId",
                        party.getPartyId().toString(),
                        new JsonBuilder().addProperty("fieldName", fieldName).add("value", tree.get(fieldName)).build()
                     );
               }
            }
         );
   }

   @Override
   public UUID getPartyId() {
      UUID id;
      do {
         id = UUID.randomUUID();
      } while(this.query.findOne("partyId", id.toString()) != null);

      return id;
   }

   public MongoQuery getQuery() {
      return this.query;
   }
}

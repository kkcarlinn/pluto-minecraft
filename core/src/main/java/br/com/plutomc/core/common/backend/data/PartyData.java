package br.com.plutomc.core.common.backend.data;

import java.util.UUID;
import br.com.plutomc.core.common.backend.mongodb.MongoQuery;
import br.com.plutomc.core.common.account.party.Party;

public interface PartyData extends Data<MongoQuery> {
   <T extends Party> T loadParty(UUID var1, Class<T> var2);

   void createParty(Party var1);

   void deleteParty(Party var1);

   void updateParty(Party var1, String var2);

   UUID getPartyId();
}

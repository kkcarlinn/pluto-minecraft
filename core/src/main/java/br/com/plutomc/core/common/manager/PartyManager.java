package br.com.plutomc.core.common.manager;

import br.com.plutomc.core.common.account.party.Party;
import br.com.plutomc.core.common.account.party.PartyRole;
import com.google.common.collect.ImmutableList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import br.com.plutomc.core.common.CommonPlugin;

public class PartyManager {
   private Map<UUID, Party> partyMap = new HashMap<>();
   private Map<UUID, Map<UUID, InviteInfo>> partyInvitesMap = new HashMap<>();

   public void loadParty(Party party) {
      this.partyMap.put(party.getPartyId(), party);
      CommonPlugin.getInstance().debug("The party " + party.getPartyId() + " has been loaded.");
   }

   public Party getPartyById(UUID uniqueId) {
      return this.partyMap.get(uniqueId);
   }

   public Party getPartyByOwner(UUID uniqueId) {
      return this.partyMap
         .values()
         .stream()
         .filter(party -> party.getMembersMap().containsKey(uniqueId) && party.getMembersMap().get(uniqueId) == PartyRole.OWNER)
         .findFirst()
         .orElse(null);
   }

   public Party getPlayerParty(UUID uniqueId) {
      return this.partyMap.values().stream().filter(party -> party.getMembersMap().containsKey(uniqueId)).findFirst().orElse(null);
   }

   public void unloadParty(UUID partyId) {
      this.partyMap.remove(partyId);
      CommonPlugin.getInstance().debug("The party " + partyId + " has been unloaded.");
   }

   public void invite(UUID sender, UUID member, Party party) {
      this.partyInvitesMap.computeIfAbsent(member, v -> new HashMap()).put(sender, new InviteInfo(party.getPartyId()));
   }

   public List<Party> loadParties() {
      return ImmutableList.copyOf(this.partyMap.values());
   }

   public Map<UUID, Map<UUID, InviteInfo>> getPartyInvitesMap() {
      return this.partyInvitesMap;
   }

   public class InviteInfo {
      private final UUID partyId;
      private long createdAt = System.currentTimeMillis();

      public UUID getPartyId() {
         return this.partyId;
      }

      public long getCreatedAt() {
         return this.createdAt;
      }

      public InviteInfo(UUID partyId) {
         this.partyId = partyId;
      }
   }
}

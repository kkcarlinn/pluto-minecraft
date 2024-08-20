package br.com.plutomc.core.common.member.party;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import br.com.plutomc.core.common.member.party.event.PartyEvent;
import br.com.plutomc.core.common.member.party.event.types.MemberJoinEvent;
import br.com.plutomc.core.common.member.party.event.types.MemberLeaveEvent;
import br.com.plutomc.core.common.CommonPlugin;
import br.com.plutomc.core.common.command.CommandSender;
import br.com.plutomc.core.common.member.Member;
import br.com.plutomc.core.common.member.Profile;
import net.md_5.bungee.api.chat.BaseComponent;

public abstract class Party {
   private UUID partyId;
   private PartyPrivacy partyPrivacy;
   private Map<UUID, PartyRole> membersMap;
   private int maxPlayers;

   public Party(UUID partyId, Member member) {
      this.partyId = partyId;
      this.partyPrivacy = PartyPrivacy.PRIVATE;
      this.membersMap = new HashMap<>();
      this.maxPlayers = 12;
      this.membersMap.put(member.getUniqueId(), PartyRole.OWNER);
   }

   public boolean openParty(int maxPlayers) {
      if (this.partyPrivacy == PartyPrivacy.PRIVATE && maxPlayers < this.membersMap.size()) {
         this.partyPrivacy = PartyPrivacy.PUBLIC;
         this.maxPlayers = maxPlayers;
         this.save("partyPrivacy");
         this.sendMessage("§aA party foi aberta para no máximo " + maxPlayers + " membros.");
         return true;
      } else {
         return false;
      }
   }

   public boolean closeParty() {
      if (this.partyPrivacy == PartyPrivacy.PUBLIC) {
         this.partyPrivacy = PartyPrivacy.PRIVATE;
         this.save("partyPrivacy");
         this.sendMessage("§aA party foi fechada.");
         return true;
      } else {
         return false;
      }
   }

   public Collection<UUID> getMembers() {
      return this.membersMap.keySet();
   }

   public boolean hasRole(UUID playerId, PartyRole partyRole) {
      return this.membersMap.containsKey(playerId) ? this.membersMap.get(playerId).ordinal() >= partyRole.ordinal() : false;
   }

   public int size() {
      return this.membersMap.size();
   }

   public void disband() {
      this.sendMessage("§cA party foi desfeita.");
      CommonPlugin.getInstance().getPartyData().deleteParty(this);
      this.membersMap.keySet().stream().map(id -> CommonPlugin.getInstance().getMemberManager().getMember(id)).forEach(member -> {
         if (member != null) {
            member.setPartyId(null);
         }
      });
      this.membersMap.clear();
      CommonPlugin.getInstance().getPartyManager().unloadParty(this.partyId);
   }

   public boolean addMember(Profile profile) {
      if (this.membersMap.size() >= this.maxPlayers) {
         return false;
      } else {
         this.membersMap.put(profile.getUniqueId(), PartyRole.MEMBER);
         this.save("membersMap");
         this.onPartyEvent(new MemberJoinEvent(profile.getUniqueId()));
         this.sendMessage("§a" + profile.getPlayerName() + " entrou na party.");
         return true;
      }
   }

   public boolean removeMember(Profile profile) {
      if (this.membersMap.containsKey(profile.getUniqueId())) {
         this.membersMap.remove(profile.getUniqueId());
         this.save("membersMap");
         this.onPartyEvent(new MemberLeaveEvent(profile.getUniqueId()));
         this.sendMessage("§c" + profile.getPlayerName() + " saiu da party.");
         return true;
      } else {
         return false;
      }
   }

   public boolean kickMember(CommandSender sender, Member member) {
      if (this.membersMap.containsKey(member.getUniqueId())) {
         this.sendMessage("§cO " + sender.getName() + " expulsou o " + member.getPlayerName() + " da party.");
         this.removeMember(Profile.from(member));
         return true;
      } else {
         return false;
      }
   }

   public void sendMessage(String message) {
      this.forEach(member -> member.sendMessage("§d[PARTY] §f" + message));
   }

   public void sendMessage(BaseComponent baseComponent) {
      this.forEach(member -> member.sendMessage(baseComponent));
   }

   public void sendMessage(BaseComponent[] baseComponents) {
      this.forEach(member -> member.sendMessage(baseComponents));
   }

   public void chat(CommandSender sender, String message) {
      this.forEach(member -> member.sendMessage("§d[PARTY] §7" + sender.getName() + ": §f" + message));
   }

   public void onPartyEvent(PartyEvent partyEvent) {
      if (partyEvent instanceof MemberLeaveEvent) {
         this.onMemberLeave((MemberLeaveEvent)partyEvent);
      } else if (partyEvent instanceof MemberJoinEvent) {
         this.onMemberJoin((MemberJoinEvent)partyEvent);
      }
   }

   private void onMemberJoin(MemberJoinEvent partyEvent) {
   }

   public void onMemberLeave(MemberLeaveEvent partyEvent) {
   }

   public void forEach(Consumer<Member> consumer) {
      this.membersMap
         .keySet()
         .stream()
         .filter(id -> CommonPlugin.getInstance().getMemberManager().getMember(id) != null)
         .map(id -> CommonPlugin.getInstance().getMemberManager().getMember(id))
         .forEach(consumer);
   }

   public void save(String... fields) {
      for(String fieldName : fields) {
         CommonPlugin.getInstance().getPartyData().updateParty(this, fieldName);
      }
   }

   public UUID getPartyId() {
      return this.partyId;
   }

   public PartyPrivacy getPartyPrivacy() {
      return this.partyPrivacy;
   }

   public Map<UUID, PartyRole> getMembersMap() {
      return this.membersMap;
   }

   public int getMaxPlayers() {
      return this.maxPlayers;
   }

   public void setPartyId(UUID partyId) {
      this.partyId = partyId;
   }

   public void setPartyPrivacy(PartyPrivacy partyPrivacy) {
      this.partyPrivacy = partyPrivacy;
   }

   public void setMembersMap(Map<UUID, PartyRole> membersMap) {
      this.membersMap = membersMap;
   }

   public void setMaxPlayers(int maxPlayers) {
      this.maxPlayers = maxPlayers;
   }

   public static enum PartyPrivacy {
      PRIVATE,
      PUBLIC;
   }
}

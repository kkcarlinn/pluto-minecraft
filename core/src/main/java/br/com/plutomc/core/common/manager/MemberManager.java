package br.com.plutomc.core.common.manager;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import br.com.plutomc.core.common.CommonPlugin;
import br.com.plutomc.core.common.member.Member;
import net.md_5.bungee.api.chat.TextComponent;

public class MemberManager {
   private Map<UUID, Member> memberMap = new HashMap<>();

   public void loadMember(Member member) {
      this.memberMap.put(member.getUniqueId(), member);
      CommonPlugin.getInstance().debug("The member " + member.getPlayerName() + "(" + member.getUniqueId() + ") has been loaded.");
   }

   public <T extends Member> T getMember(UUID uniqueId, Class<T> clazz) {
      return this.memberMap.containsKey(uniqueId) ? clazz.cast(this.memberMap.get(uniqueId)) : null;
   }

   public Member getMember(UUID uniqueId) {
      return this.memberMap.get(uniqueId);
   }

   public Member getMemberByName(String playerName) {
      return this.getMembers().stream().filter(member -> member.getName().equalsIgnoreCase(playerName)).findFirst().orElse(null);
   }

   public <T extends Member> T getMemberByName(String playerName, Class<T> clazz) {
      Member orElse = this.getMembers().stream().filter(member -> member.getName().equalsIgnoreCase(playerName)).findFirst().orElse(null);
      return orElse == null ? null : clazz.cast(orElse);
   }

   public void unloadMember(UUID uniqueId) {
      this.unloadMember(this.getMember(uniqueId));
   }

   public void unloadMember(Member member) {
      if (member != null) {
         this.memberMap.remove(member.getUniqueId());
         CommonPlugin.getInstance().debug("The member " + member.getPlayerName() + "(" + member.getUniqueId() + ") has been unloaded.");
      }
   }

   public Collection<Member> getMembers() {
      return ImmutableList.copyOf(this.memberMap.values());
   }

   public <T extends Member> Collection<T> getMembers(Class<T> clazz) {
      return this.getMembers()
         .stream()
         .filter(member -> clazz.isAssignableFrom(member.getClass()))
         .map(member -> clazz.cast(member))
         .collect(Collectors.toList());
   }

   public void broadcast(String message, String permission) {
      this.getMembers().stream().filter(member -> member.hasPermission(permission)).forEach(member -> member.sendMessage(message));
      System.out.println(message);
   }

   public void staffLog(String message, boolean format) {
      this.getMembers()
         .stream()
         .filter(member -> member.hasPermission("staff.log") && member.getMemberConfiguration().isSeeingLogs())
         .forEach(member -> member.sendMessage(format ? "§7[" + message + "§7]" : message));
      System.out.println(message);
   }

   public void staffLog(TextComponent textComponent) {
      this.getMembers()
         .stream()
         .filter(member -> member.hasPermission("staff.log") && member.getMemberConfiguration().isSeeingLogs())
         .forEach(member -> member.sendMessage(textComponent));
      System.out.println(textComponent.toPlainText());
   }

   public void staffLog(String message) {
      this.staffLog(message, true);
   }

   public void actionbar(String message, String permission) {
      this.getMembers().stream().filter(member -> member.hasPermission(permission)).forEach(member -> member.sendActionBar(message));
   }

   public void title(String title, String subTitle, String permission) {
      this.getMembers().stream().filter(member -> member.hasPermission(permission)).forEach(member -> member.sendTitle(title, subTitle));
   }
}

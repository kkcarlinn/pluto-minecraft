package br.com.plutomc.core.common.manager;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import br.com.plutomc.core.common.CommonPlugin;
import br.com.plutomc.core.common.account.Account;
import net.md_5.bungee.api.chat.TextComponent;

public class AccountManager {
   private Map<UUID, Account> accountMap = new HashMap<>();

   public void loadAccount(Account account) {
      this.accountMap.put(account.getUniqueId(), account);
      CommonPlugin.getInstance().debug("The member " + account.getPlayerName() + "(" + account.getUniqueId() + ") has been loaded.");
   }

   public <T extends Account> T getAccount(UUID uniqueId, Class<T> clazz) {
      return this.accountMap.containsKey(uniqueId) ? clazz.cast(this.accountMap.get(uniqueId)) : null;
   }

   public Account getAccount(UUID uniqueId) {
      return this.accountMap.get(uniqueId);
   }

   public Account getAccountByName(String playerName) {
      return this.getAccounts().stream().filter(member -> member.getName().equalsIgnoreCase(playerName)).findFirst().orElse(null);
   }

   public <T extends Account> T getAccountByName(String playerName, Class<T> clazz) {
      Account orElse = this.getAccounts().stream().filter(member -> member.getName().equalsIgnoreCase(playerName)).findFirst().orElse(null);
      return orElse == null ? null : clazz.cast(orElse);
   }

   public void unloadAccount(UUID uniqueId) {
      this.unloadAccount(this.getAccount(uniqueId));
   }

   public void unloadAccount(Account account) {
      if (account != null) {
         this.accountMap.remove(account.getUniqueId());
         CommonPlugin.getInstance().debug("The member " + account.getPlayerName() + "(" + account.getUniqueId() + ") has been unloaded.");
      }
   }

   public Collection<Account> getAccounts() {
      return ImmutableList.copyOf(this.accountMap.values());
   }

   public <T extends Account> Collection<T> getAccounts(Class<T> clazz) {
      return this.getAccounts()
         .stream()
         .filter(member -> clazz.isAssignableFrom(member.getClass()))
         .map(member -> clazz.cast(member))
         .collect(Collectors.toList());
   }

   public void broadcast(String message, String permission) {
      this.getAccounts().stream().filter(member -> member.hasPermission(permission)).forEach(member -> member.sendMessage(message));
      System.out.println(message);
   }

   public void staffLog(String message, boolean format) {
      this.getAccounts()
         .stream()
         .filter(member -> member.hasPermission("staff.log") && member.getAccountConfiguration().isSeeingLogs())
         .forEach(member -> member.sendMessage(format ? "§7[" + message + "§7]" : message));
      System.out.println(message);
   }

   public void staffLog(TextComponent textComponent) {
      this.getAccounts()
         .stream()
         .filter(member -> member.hasPermission("staff.log") && member.getAccountConfiguration().isSeeingLogs())
         .forEach(member -> member.sendMessage(textComponent));
      System.out.println(textComponent.toPlainText());
   }

   public void staffLog(String message) {
      this.staffLog(message, true);
   }

   public void actionbar(String message, String permission) {
      this.getAccounts().stream().filter(member -> member.hasPermission(permission)).forEach(member -> member.sendActionBar(message));
   }

   public void title(String title, String subTitle, String permission) {
      this.getAccounts().stream().filter(member -> member.hasPermission(permission)).forEach(member -> member.sendTitle(title, subTitle));
   }
}

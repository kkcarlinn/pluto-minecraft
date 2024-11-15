package br.com.plutomc.core.common.account.configuration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import br.com.plutomc.core.common.punish.Punish;
import br.com.plutomc.core.common.punish.PunishType;
import br.com.plutomc.core.common.command.CommandSender;
import br.com.plutomc.core.common.account.Account;

public class PunishConfiguration {
   private transient Account account;
   private Map<PunishType, List<Punish>> punishMap;

   public PunishConfiguration(Account account) {
      this.account = account;
      this.punishMap = new HashMap<>();
   }

   public Punish getActualPunish(PunishType punishType) {
      return this.punishMap
         .computeIfAbsent(punishType, v -> new ArrayList<Punish>())
         .stream()
         .filter(punish -> !punish.isUnpunished() && !punish.hasExpired())
         .findFirst()
         .orElse(null);
   }

   public Punish getPunishById(String id, PunishType punishType) {
      return this.punishMap.computeIfAbsent(punishType, v -> new ArrayList<>()).stream().filter(punish -> punish.getId().equals(id)).findFirst().orElse(null);
   }

   public Collection<Punish> getPunish(PunishType punishType) {
      return this.punishMap.containsKey(punishType) ? this.punishMap.get(punishType) : new ArrayList<>();
   }

   public Collection<Punish> getPunishById(UUID punisherId, PunishType punishType) {
      return this.punishMap
         .computeIfAbsent(punishType, v -> new ArrayList<>())
         .stream()
         .filter(punish -> punish.getPunisherId() == punisherId)
         .collect(Collectors.toList());
   }

   public Collection<Punish> getPunishByName(String punisherName, PunishType punishType) {
      return this.punishMap
         .computeIfAbsent(punishType, v -> new ArrayList<>())
         .stream()
         .filter(punish -> punish.getPunisherName().equals(punisherName))
         .collect(Collectors.toList());
   }

   public boolean pardon(Punish punish, CommandSender sender) {
      List<Punish> list = this.punishMap.computeIfAbsent(punish.getPunishType(), v -> new ArrayList());
      if (list.stream().filter(p -> p.getId().equals(punish.getId())).findFirst().isPresent()) {
         punish.unpunish(sender);
         return true;
      } else {
         return false;
      }
   }

   public void punish(Punish punish) {
      this.punishMap.computeIfAbsent(punish.getPunishType(), v -> new ArrayList()).add(punish);
   }

   public void loadConfiguration(Account account) {
      this.account = account;
   }

   public Account getAccount() {
      return this.account;
   }

   public Map<PunishType, List<Punish>> getPunishMap() {
      return this.punishMap;
   }
}

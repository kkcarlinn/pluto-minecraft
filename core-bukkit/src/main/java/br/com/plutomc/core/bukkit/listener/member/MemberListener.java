package br.com.plutomc.core.bukkit.listener.member;

import java.util.Map;
import java.util.UUID;

import br.com.plutomc.core.bukkit.BukkitCommon;
import br.com.plutomc.core.bukkit.event.member.PlayerGroupChangeEvent;
import br.com.plutomc.core.bukkit.event.member.PlayerUpdatedFieldEvent;
import br.com.plutomc.core.bukkit.event.player.PlayerAdminEvent;
import br.com.plutomc.core.bukkit.utils.player.PlayerAPI;
import br.com.plutomc.core.common.CommonPlugin;
import br.com.plutomc.core.bukkit.account.BukkitAccount;
import br.com.plutomc.core.bukkit.account.party.BukkitParty;
import br.com.plutomc.core.common.account.Account;
import br.com.plutomc.core.common.account.party.Party;
import br.com.plutomc.core.common.account.status.StatusType;
import br.com.plutomc.core.common.permission.Group;
import br.com.plutomc.core.common.permission.GroupInfo;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;
import org.bukkit.scheduler.BukkitRunnable;

public class MemberListener implements Listener {
   @EventHandler(
      priority = EventPriority.LOW
   )
   public void onAsyncPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
      if (event.getLoginResult() == Result.ALLOWED) {
         UUID uniqueId = event.getUniqueId();
         BukkitAccount member = CommonPlugin.getInstance().getAccountData().loadAccount(uniqueId, BukkitAccount.class);
         if (member == null) {
            event.disallow(Result.KICK_OTHER, CommonPlugin.getInstance().getPluginInfo().translate("account-not-exists"));
         } else {
            Party party = member.getParty();
            if (party == null && member.getPartyId() != null) {
               party = CommonPlugin.getInstance().getPartyData().loadParty(member.getPartyId(), BukkitParty.class);
               if (party == null) {
                  CommonPlugin.getInstance().debug("The party " + member.getPartyId() + " didnt load.");
               } else {
                  CommonPlugin.getInstance().getPartyManager().loadParty(party);
               }
            }

            member.connect();
            CommonPlugin.getInstance().getAccountManager().loadAccount(member);

            for(StatusType types : BukkitCommon.getInstance().getPreloadedStatus()) {
               CommonPlugin.getInstance().getStatusManager().loadStatus(uniqueId, types);
            }
         }
      }
   }

   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void onPlayerLoginLW(PlayerLoginEvent event) {
      if (CommonPlugin.getInstance().getAccountManager().getAccount(event.getPlayer().getUniqueId()) == null) {
         event.disallow(PlayerLoginEvent.Result.KICK_OTHER, CommonPlugin.getInstance().getPluginInfo().translate("account-not-loaded"));
      } else {
         BukkitAccount member = CommonPlugin.getInstance().getAccountManager().getAccount(event.getPlayer().getUniqueId(), BukkitAccount.class);
         Player player = event.getPlayer();
         member.setPlayer(player);
      }
   }

   @EventHandler(
      priority = EventPriority.HIGH
   )
   public void onPlayerLogin(final PlayerLoginEvent event) {
      if (CommonPlugin.getInstance().getAccountManager().getAccount(event.getPlayer().getUniqueId()) == null) {
         event.disallow(PlayerLoginEvent.Result.KICK_OTHER, CommonPlugin.getInstance().getPluginInfo().translate("account-not-loaded"));
      } else {
         final BukkitAccount member = CommonPlugin.getInstance().getAccountManager().getAccount(event.getPlayer().getUniqueId(), BukkitAccount.class);
         int admins = BukkitCommon.getInstance().getVanishManager().getPlayersInAdmin().size();
         if (event.getResult() == PlayerLoginEvent.Result.KICK_FULL
            && Bukkit.getOnlinePlayers().size() - admins >= Bukkit.getMaxPlayers()
            && !member.hasPermission("server.full")) {
            event.setKickMessage("§cO servidor está cheio.");
         } else if ((event.getResult() == PlayerLoginEvent.Result.KICK_WHITELIST || !CommonPlugin.getInstance().isJoinEnabled())
            && !member.hasPermission("command.admin")) {
            event.setKickMessage("§cSomente membros da equipe podem entrar no momento.");
         } else {
            event.allow();
            if (member.isUsingFake()) {
               PlayerAPI.changePlayerName(event.getPlayer(), member.getFakeName(), false);
            }

            if (member.hasCustomSkin()) {
               (new BukkitRunnable() {
                  @Override
                  public void run() {
                     PlayerAPI.changePlayerSkin(event.getPlayer(), member.getSkin().getValue(), member.getSkin().getSignature(), false);
                  }
               }).runTask(BukkitCommon.getInstance());
            }
         }
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onPlayerLoginM(PlayerLoginEvent event) {
      if (event.getResult() != PlayerLoginEvent.Result.ALLOWED) {
         CommonPlugin.getInstance().getAccountManager().unloadAccount(event.getPlayer().getUniqueId());
         CommonPlugin.getInstance().getStatusManager().unloadStatus(event.getPlayer().getUniqueId());
      }
   }

   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void onPlayerJoin(PlayerJoinEvent event) {
      if (CommonPlugin.getInstance().getAccountManager().getAccount(event.getPlayer().getUniqueId()) == null) {
         event.getPlayer().kickPlayer(CommonPlugin.getInstance().getPluginInfo().translate("account-not-loaded"));
      } else {
         CommonPlugin.getInstance()
            .getPluginPlatform()
            .runAsync(() -> CommonPlugin.getInstance().getServerData().joinPlayer(event.getPlayer().getUniqueId(), Bukkit.getMaxPlayers()));
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onPlayerAdmin(PlayerAdminEvent event) {
      if (event.getAdminMode() == PlayerAdminEvent.AdminMode.ADMIN) {
         CommonPlugin.getInstance()
            .getPluginPlatform()
            .runAsync(() -> CommonPlugin.getInstance().getServerData().leavePlayer(event.getPlayer().getUniqueId(), Bukkit.getMaxPlayers()));
      } else {
         CommonPlugin.getInstance()
            .getPluginPlatform()
            .runAsync(() -> CommonPlugin.getInstance().getServerData().joinPlayer(event.getPlayer().getUniqueId(), Bukkit.getMaxPlayers()));
      }
   }

   @EventHandler
   public void onPlayerUpdatedField(PlayerUpdatedFieldEvent event) {
      Player player = event.getPlayer();
      BukkitAccount member = event.getBukkitMember();
      String var4 = event.getField().toLowerCase();
      byte var5 = -1;
      switch(var4.hashCode()) {
         case -1237460524:
            if (var4.equals("groups")) {
               var5 = 0;
            }
         default:
            switch(var5) {
               case 0:
                  Map<String, GroupInfo> oldObject = (Map)event.getOldObject();
                  Map<String, GroupInfo> newObject = (Map)event.getObject();
                  if (newObject.isEmpty()) {
                     Group highGroup = CommonPlugin.getInstance()
                        .getPluginInfo()
                        .getGroupMap()
                        .values()
                        .stream()
                        .filter(group -> oldObject.containsKey(group.getGroupName().toLowerCase()))
                        .sorted((o1, o2) -> Integer.compare(o1.getId(), o2.getId()))
                        .findFirst()
                        .orElse(null);
                     if (highGroup == null) {
                        Bukkit.getPluginManager()
                           .callEvent(new PlayerGroupChangeEvent(player, member, (String)null, 0L, PlayerGroupChangeEvent.Action.UNKNOWN));
                     } else {
                        Bukkit.getPluginManager().callEvent(new PlayerGroupChangeEvent(player, member, highGroup, 0L, PlayerGroupChangeEvent.Action.REMOVE));
                     }
                  } else if (oldObject.isEmpty()) {
                     Group highGroup = CommonPlugin.getInstance()
                        .getPluginInfo()
                        .getGroupMap()
                        .values()
                        .stream()
                        .filter(group -> newObject.containsKey(group.getGroupName().toLowerCase()))
                        .sorted((o1, o2) -> Integer.compare(o1.getId(), o2.getId()))
                        .findFirst()
                        .orElse(null);
                     if (highGroup == null) {
                        Bukkit.getPluginManager().callEvent(new PlayerGroupChangeEvent(player, member, (String)null, 0L, PlayerGroupChangeEvent.Action.SET));
                     } else {
                        Bukkit.getPluginManager().callEvent(new PlayerGroupChangeEvent(player, member, highGroup, 0L, PlayerGroupChangeEvent.Action.SET));
                     }
                  } else if (newObject.size() == 1) {
                     String groupName = newObject.keySet().stream().findFirst().orElse(null);
                     if (groupName == null) {
                        Bukkit.getPluginManager().callEvent(new PlayerGroupChangeEvent(player, member, (String)null, 0L, PlayerGroupChangeEvent.Action.SET));
                     } else {
                        Bukkit.getPluginManager().callEvent(new PlayerGroupChangeEvent(player, member, groupName, 0L, PlayerGroupChangeEvent.Action.SET));
                     }
                  } else if (oldObject.size() < newObject.size()) {
                     String groupName = oldObject.keySet().stream().filter(group -> newObject.containsKey(group)).findFirst().orElse(null);
                     if (groupName == null) {
                        Bukkit.getPluginManager().callEvent(new PlayerGroupChangeEvent(player, member, (String)null, 0L, PlayerGroupChangeEvent.Action.ADD));
                     } else {
                        Bukkit.getPluginManager().callEvent(new PlayerGroupChangeEvent(player, member, groupName, 0L, PlayerGroupChangeEvent.Action.ADD));
                     }
                  } else if (oldObject.size() > newObject.size()) {
                     String groupName = newObject.keySet().stream().filter(group -> !oldObject.containsKey(group)).findFirst().orElse(null);
                     if (groupName == null) {
                        Bukkit.getPluginManager().callEvent(new PlayerGroupChangeEvent(player, member, (String)null, 0L, PlayerGroupChangeEvent.Action.REMOVE));
                     } else {
                        Bukkit.getPluginManager().callEvent(new PlayerGroupChangeEvent(player, member, groupName, 0L, PlayerGroupChangeEvent.Action.REMOVE));
                     }
                  } else {
                     Bukkit.getPluginManager()
                        .callEvent(
                           new PlayerGroupChangeEvent(
                              player, member, newObject.keySet().stream().findFirst().orElse(null), 0L, PlayerGroupChangeEvent.Action.UNKNOWN
                           )
                        );
                  }

                  member.updateGroup();
            }
      }
   }

   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void onPlayerQuit(PlayerQuitEvent event) {
      Account account = CommonPlugin.getInstance().getAccountManager().getAccount(event.getPlayer().getUniqueId());
      if (account != null) {
         CommonPlugin.getInstance()
            .getPluginPlatform()
            .runAsync(
               () -> {
                  CommonPlugin.getInstance().getAccountManager().unloadAccount(account);
                  CommonPlugin.getInstance().getServerData().leavePlayer(event.getPlayer().getUniqueId(), Bukkit.getMaxPlayers());
                  CommonPlugin.getInstance().getStatusManager().unloadStatus(account.getUniqueId());
                  Party party = account.getParty();
                  if (party != null
                     && !party.getMembers().stream().filter(id -> CommonPlugin.getInstance().getAccountManager().getAccount(id) != null).findFirst().isPresent()) {
                     CommonPlugin.getInstance().getPartyData().deleteParty(party);
                     CommonPlugin.getInstance().getPartyManager().unloadParty(party.getPartyId());
                  }
               }
            );
      }
   }
}

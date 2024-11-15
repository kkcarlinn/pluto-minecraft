package br.com.plutomc.core.bungee.listener;

import br.com.plutomc.core.bungee.BungeeConst;
import br.com.plutomc.core.bungee.BungeeMain;
import br.com.plutomc.core.bungee.event.player.PlayerFieldUpdateEvent;
import br.com.plutomc.core.bungee.event.player.PlayerPardonedEvent;
import br.com.plutomc.core.bungee.event.player.PlayerPunishEvent;
import br.com.plutomc.core.bungee.account.BungeeAccount;
import br.com.plutomc.core.common.CommonConst;
import br.com.plutomc.core.common.CommonPlugin;
import br.com.plutomc.core.common.PluginInfo;
import br.com.plutomc.core.common.account.Account;
import br.com.plutomc.core.common.account.configuration.LoginConfiguration;
import br.com.plutomc.core.common.account.party.Party;
import br.com.plutomc.core.common.permission.Group;
import br.com.plutomc.core.common.punish.Punish;
import br.com.plutomc.core.common.punish.PunishType;
import br.com.plutomc.core.common.report.Report;
import br.com.plutomc.core.common.utils.DateUtils;
import br.com.plutomc.core.common.utils.skin.Skin;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.collect.ImmutableList;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.PermissionCheckEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.connection.InitialHandler;
import net.md_5.bungee.connection.LoginResult;
import net.md_5.bungee.connection.LoginResult.Property;
import net.md_5.bungee.event.EventHandler;

import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class MemberListener implements Listener {
   private Cache<String, Punish> banCache = CacheBuilder.newBuilder()
           .expireAfterWrite(30L, TimeUnit.MINUTES)
           .expireAfterAccess(30L, TimeUnit.MINUTES)
           .build(new CacheLoader<String, Punish>() {
              public Punish load(String name) throws Exception {
                 return null;
              }
           });

   @EventHandler
   public void onLogin(LoginEvent event) {
      event.registerIntent(BungeeMain.getInstance());
      CommonPlugin.getInstance().getPluginPlatform().runAsync(() -> {
         this.handleMemberLoad(event);
         event.completeIntent(BungeeMain.getInstance());
      });
   }

   private void handleMemberLoad(LoginEvent event) {
      String playerName = event.getConnection().getName();
      UUID uniqueId = event.getConnection().getUniqueId();
      BungeeAccount member = CommonPlugin.getInstance().getAccountData().loadAccount(uniqueId, BungeeAccount.class);
      if (member == null) {
         LoginConfiguration.AccountType accountType = event.getConnection().isOnlineMode()
                 ? LoginConfiguration.AccountType.PREMIUM
                 : LoginConfiguration.AccountType.CRACKED;
         BungeeAccount memberByName = CommonPlugin.getInstance().getAccountData().loadAccount(playerName, true, BungeeAccount.class);
         if (accountType == LoginConfiguration.AccountType.PREMIUM
                 && memberByName != null
                 && memberByName.getLoginConfiguration().getAccountType() == LoginConfiguration.AccountType.CRACKED) {
            try {
               InitialHandler initialHandler = (InitialHandler)event.getConnection();
               Field field = InitialHandler.class.getDeclaredField("uniqueId");
               field.setAccessible(true);
               field.set(initialHandler, memberByName.getUniqueId());
            } catch (Exception var12) {
               CommonPlugin.getInstance().getLogger().log(Level.SEVERE, "Failed to set unique id", (Throwable)var12);
               event.setCancelled(true);
               event.setCancelReason(
                       "§cSua conta não foi carregada.\nDetectamos que você está usando uma conta premium, mas já está registrado no servidor como cracked.\nFizemos as alterações necessárias mas está ocorrendo um erro.\nEntre em contato com um administrador."
               );
               return;
            }

            event.getConnection().setOnlineMode(false);
         } else {
            if (memberByName != null) {
               if (!memberByName.getPlayerName().equals(playerName)) {
                  event.setCancelled(true);
                  event.setCancelReason("§cSua conta já está registrada no servidor com outro nick.");
                  return;
               }

               if (accountType != memberByName.getLoginConfiguration().getAccountType()) {
                  event.setCancelled(true);
                  event.setCancelReason("§cSua conta já está registrada no servidor como " + memberByName.getLoginConfiguration().getAccountType().name() + ".");
               }
            }

            member = new BungeeAccount(uniqueId, playerName, accountType);
            CommonPlugin.getInstance().getAccountData().createMember(member);
            CommonPlugin.getInstance().debug("The member " + member.getPlayerName() + "(" + member.getUniqueId() + ") has been created.");
         }
      } else if (member.getLoginConfiguration().getAccountType() == LoginConfiguration.AccountType.NONE) {
         member.getLoginConfiguration()
                 .setAccountType(event.getConnection().isOnlineMode() ? LoginConfiguration.AccountType.PREMIUM : LoginConfiguration.AccountType.CRACKED);
         member.saveConfig();
      }

      if (member.getSkin() == null || !member.isCustomSkin() && member.getSkin().getCreatedAt() + 604800000L > System.currentTimeMillis()) {
         try {
            InitialHandler initialHandler = (InitialHandler)event.getConnection();
            LoginResult loginProfile = initialHandler.getLoginProfile();
            Skin skin = null;
            if (loginProfile != null && loginProfile.getProperties() != null) {
               for(Property property : loginProfile.getProperties()) {
                  if (property.getName().equals("textures")) {
                     skin = new Skin(member.getName(), property.getValue(), property.getSignature());
                     break;
                  }
               }
            }

            if (skin == null) {
               member.setSkin(CommonConst.DEFAULT_SKIN);
            } else {
               member.setSkin(skin);
            }
         } catch (Exception var13) {
            CommonPlugin.getInstance().getLogger().log(Level.WARNING, "Failed to load skin for " + member.getName(), (Throwable)var13);
         }
      }

      if (!this.handleLogin(member, event)) {
         if (!this.handlePunish(member, event)) {
            if (!this.handleTimeout(member, event)) {
               if (!this.handleWhiteList(member, event)) {
                  if (!event.isCancelled()) {
                     CommonPlugin.getInstance().getAccountManager().loadAccount(member);
                     this.handleParty(member, event);
                     if (!CommonPlugin.getInstance().getAccountData().checkCache(member.getUniqueId())) {
                        CommonPlugin.getInstance().getAccountData().saveRedisMember(member);
                     }

                     Report report = CommonPlugin.getInstance().getReportManager().getReportById(member.getUniqueId());
                     if (report != null) {
                        report.setOnline(true);
                     }

                     BungeeMain.getInstance().loadTexture(event.getConnection(), member.getSkin());
                  }
               }
            }
         }
      }
   }

   @EventHandler
   public void onPostLogin(PostLoginEvent event) {
      BungeeAccount member = CommonPlugin.getInstance().getAccountManager().getAccount(event.getPlayer().getUniqueId(), BungeeAccount.class);
      if (member == null) {
         event.getPlayer().disconnect(CommonPlugin.getInstance().getPluginInfo().translate("account-not-loaded"));
      } else {
         member.setProxiedPlayer(event.getPlayer());
         this.handlePermissions(member);
      }
   }

   @EventHandler
   public void onPlayerPunish(PlayerPunishEvent event) {
      if (event.getPunish().getPunishType() == PunishType.BAN && !event.getPunish().getPlayerName().equals("CONSOLE") && event.getPunish().isPermanent()) {
         Account account = event.getPunished();
         this.banIp(account.getLastIpAddress(), event.getPunish());
      }
   }

   @EventHandler
   public void onPlayerPardoned(PlayerPardonedEvent event) {
      if (event.getPunish().getPunishType() == PunishType.BAN && event.getPunish().isPermanent() && this.isIpBanned(event.getPunished().getLastIpAddress())) {
         this.banCache.invalidate(event.getPunished().getLastIpAddress());
      }
   }

   public void handlePermissions(BungeeAccount member) {
      ProxiedPlayer player = member.getProxiedPlayer();

      for(String permission : ImmutableList.copyOf(player.getPermissions())) {
         player.setPermission(permission, false);
      }

      for(String string : member.getPermissions()) {
         player.setPermission(string.toLowerCase(), true);
      }

      for(Group group : member.getGroups()
              .keySet()
              .stream()
              .map(name -> CommonPlugin.getInstance().getPluginInfo().getGroupByName(name))
              .toArray(x$0 -> new Group[x$0])) {
         for(String string : group.getPermissions()) {
            player.setPermission(string.toLowerCase(), true);
         }
      }
   }

   @EventHandler
   public void onPlayerFieldUpdate(PlayerFieldUpdateEvent event) {
      if (event.getFieldName().toLowerCase().contains("group")) {
         this.handlePermissions(event.getPlayer());
      }
   }

   @EventHandler
   public void onPermissionCheck(PermissionCheckEvent event) {
      if (event.getSender() instanceof ProxiedPlayer && !event.hasPermission()) {
         CommandSender sender = event.getSender();
         Account account = CommonPlugin.getInstance().getAccountManager().getAccount(((ProxiedPlayer)sender).getUniqueId());
         if (account != null) {
            String permission = sender.getPermissions().stream().filter(string -> string.equals("*")).findFirst().orElse(null);
            if (permission == null) {
               event.setHasPermission(account.hasSilentPermission(event.getPermission()));
            } else {
               event.setHasPermission(true);
            }
         }
      }
   }

   @EventHandler
   public void onPlayerDisconnect(PlayerDisconnectEvent event) {
      BungeeAccount member = CommonPlugin.getInstance().getAccountManager().getAccount(event.getPlayer().getUniqueId(), BungeeAccount.class);
      if (member != null) {
         member.getLoginConfiguration().logOut();
         member.logOut();
         CommonPlugin.getInstance().getAccountManager().unloadAccount(member);
         CommonPlugin.getInstance()
                 .getAccountData()
                 .cacheConnection(
                         event.getPlayer().getPendingConnection().getName(), member.getLoginConfiguration().getAccountType() == LoginConfiguration.AccountType.PREMIUM
                 );
         CommonPlugin.getInstance().getAccountData().cacheMember(member.getUniqueId());
         Party party = member.getParty();
         if (party != null
                 && !party.getMembers().stream().filter(id -> CommonPlugin.getInstance().getAccountManager().getAccount(id) != null).findFirst().isPresent()) {
            member.setPartyId(null);
            CommonPlugin.getInstance().getPartyData().deleteParty(party);
            CommonPlugin.getInstance().getPartyManager().unloadParty(party.getPartyId());
         }

         Report report = CommonPlugin.getInstance().getReportManager().getReportById(member.getUniqueId());
         if (report != null) {
            report.setOnline(false);
         }

         CommonPlugin.getInstance().getPartyManager().getPartyInvitesMap().remove(member.getUniqueId());
      }
   }

   private void banIp(String ipAddress, Punish punish) {
      this.banCache.put(ipAddress, punish);
   }

   private boolean isIpBanned(String ipAddress) {
      return this.banCache.asMap().containsKey(ipAddress);
   }

   private boolean handleParty(BungeeAccount member, LoginEvent event) {
      Party party = member.getParty();
      if (party != null) {
         return true;
      } else {
         if (member.getPartyId() != null) {
            member.setPartyId(null);
         }

         return true;
      }
   }

   private boolean handleTimeout(BungeeAccount member, LoginEvent event) {
      if (member.getLoginConfiguration().isTimeouted()) {
         event.setCancelled(true);
         event.setCancelReason(
                 PluginInfo.t(
                         member,
                         "command.login.timeouted",
                         "%time%",
                         DateUtils.getTime(member.getLanguage(), member.getLoginConfiguration().getTimeoutTime()),
                         "%website%",
                         CommonPlugin.getInstance().getPluginInfo().getWebsite()
                 )
         );
         return true;
      } else {
         return false;
      }
   }

   private boolean handlePunish(BungeeAccount member, LoginEvent event) {
      Punish punish = member.getPunishConfiguration().getActualPunish(PunishType.BAN);
      if (punish != null) {
         event.setCancelled(true);
         event.setCancelReason(
                 PluginInfo.t(
                         member,
                         "ban-" + (punish.isPermanent() ? "permanent" : "temporary") + "-kick-message",
                         "%reason%",
                         punish.getPunishReason(),
                         "%expireAt%",
                         DateUtils.getTime(member.getLanguage(), punish.getExpireAt()),
                         "%punisher%",
                         punish.getPunisherName(),
                         "%website%",
                         CommonPlugin.getInstance().getPluginInfo().getWebsite(),
                         "%store%",
                         CommonPlugin.getInstance().getPluginInfo().getStore(),
                         "%discord%",
                         CommonPlugin.getInstance().getPluginInfo().getDiscord(),
                         "%id%",
                         punish.getId()
                 )
         );
         return true;
      } else if (this.isIpBanned(member.getLastIpAddress())) {
         punish = new Punish(member, BungeeConst.CONSOLE_SENDER, "Conta alternativa", -1L, PunishType.BAN);
         member.getPunishConfiguration().punish(punish);
         member.saveConfig();
         ProxyServer.getInstance().getPluginManager().callEvent(new PlayerPunishEvent(member, punish, BungeeConst.CONSOLE_SENDER));
         event.setCancelled(true);
         event.setCancelReason(
                 PluginInfo.t(
                         member,
                         "ban-" + (punish.isPermanent() ? "permanent" : "temporary") + "-kick-message",
                         "%reason%",
                         punish.getPunishReason(),
                         "%expireAt%",
                         DateUtils.getTime(member.getLanguage(), punish.getExpireAt()),
                         "%punisher%",
                         punish.getPunisherName(),
                         "%website%",
                         CommonPlugin.getInstance().getPluginInfo().getWebsite(),
                         "%store%",
                         CommonPlugin.getInstance().getPluginInfo().getStore(),
                         "%discord%",
                         CommonPlugin.getInstance().getPluginInfo().getDiscord(),
                         "%id%",
                         punish.getId()
                 )
         );
         return true;
      } else {
         return false;
      }
   }

   private boolean handleLogin(BungeeAccount member, LoginEvent event) {
      SocketAddress socket = event.getConnection().getSocketAddress();
      if (!(socket instanceof InetSocketAddress)) {
         event.setCancelled(true);
         event.setCancelReason("§cWe cannot load your ip address.");
         return true;
      } else {
         String playerName = event.getConnection().getName();
         InetSocketAddress inetSocketAddress = (InetSocketAddress)socket;
         String ipAddress = inetSocketAddress.getHostString();

         try {
            member.logIn(playerName, ipAddress);
            return false;
         } catch (NullPointerException var8) {
            event.setCancelled(true);
            event.setCancelReason("§cWe cannot load your ip address.");
            var8.printStackTrace();
            return true;
         }
      }
   }

   private boolean handleWhiteList(BungeeAccount bungeeMember, LoginEvent loginEvent) {
      if (BungeeMain.getInstance().isMaintenance()
              && !bungeeMember.hasPermission("command.admin")
              && !BungeeMain.getInstance().isMemberInWhiteList(bungeeMember.getPlayerName())) {
         long maintenanceTime = BungeeMain.getInstance().getWhitelistExpires();
         loginEvent.setCancelled(true);
         loginEvent.setCancelReason(
                 "§c§lPLUTOMC§4§cO servidor entrou em manutenção!§cPara melhorar sua jogabilidade estamos atualizando o servidor"
                         + (
                         maintenanceTime == 0L
                                 ? ", espere para entrar novamente!"
                                 : ".\n§cO servidor volta em " + DateUtils.getTime(CommonPlugin.getInstance().getPluginInfo().getDefaultLanguage(), maintenanceTime)
                 )
                         + "\n§f\n§ePara mais informações §b"
                         + CommonPlugin.getInstance().getPluginInfo().getDiscord()
         );
         return true;
      } else {
         return false;
      }
   }
}
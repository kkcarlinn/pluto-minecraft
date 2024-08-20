package br.com.plutomc.core.bungee.listener;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import br.com.plutomc.core.bungee.BungeeMain;
import br.com.plutomc.core.bungee.member.BungeeMember;
import br.com.plutomc.core.common.CommonPlugin;
import br.com.plutomc.core.common.language.Language;
import br.com.plutomc.core.common.member.Member;
import br.com.plutomc.core.common.member.party.Party;
import br.com.plutomc.core.common.member.party.PartyRole;
import br.com.plutomc.core.common.server.ServerType;
import br.com.plutomc.core.common.server.loadbalancer.server.ProxiedServer;
import br.com.plutomc.core.common.utils.string.StringFormat;
import net.md_5.bungee.api.Callback;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.ServerPing.PlayerInfo;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ProxyPingEvent;
import net.md_5.bungee.api.event.SearchServerEvent;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.event.ServerKickEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class ServerListener implements Listener {
   private static final String MOTD_HEADER = StringFormat.centerString("§b§lPLUTO §7» §aloja.plutomc.com.br", 127);
   private static final String MOTD_FOOTER = StringFormat.centerString("§6§lVENHA JOGAR §B§LBEDWARS", 127);
   private static final String MAINTENANCE_FOOTER = StringFormat.centerString("§cO servidor está em manutenção.", 127);
   private static final String SERVER_NOT_FOUND = StringFormat.centerString("§cServidor inserido não existe.");
   private Set<UUID> playerUpdateSet = new HashSet<>();

   @EventHandler
   public void onSearchServer(SearchServerEvent event) {
      BungeeMember member = CommonPlugin.getInstance().getMemberManager().getMember(event.getPlayer().getUniqueId(), BungeeMember.class);
      member.setProxiedPlayer(event.getPlayer());
      member.loadConfiguration();
      boolean logged = member.getLoginConfiguration().isLogged();
      boolean refreshLogin = logged ? false : member.getLoginConfiguration().reloadSession();
      logged = logged || refreshLogin;
      ProxiedServer server = BungeeMain.getInstance().getServerManager().getServer(this.getServerIp(event.getPlayer().getPendingConnection()));
      if (logged && server != null && server.getServerInfo() != null) {
         event.setServer(server.getServerInfo());
      } else {
         if (logged
            && System.currentTimeMillis() - member.getLastLogin() <= 15000L
            && member.getActualServerId() != null
            && !member.getActualServerType().name().contains("LOBBY")) {
            server = BungeeMain.getInstance().getServerManager().getServer(member.getActualServerId());
            if (server != null && server.getServerInfo() != null) {
               event.setServer(server.getServerInfo());
               return;
            }
         }

         server = BungeeMain.getInstance()
            .getServerManager()
            .getBalancer(logged ? ServerType.LOBBY : ServerType.LOGIN)
            .getList()
            .stream()
            .findFirst()
            .orElse(null);
         if (server != null && server.getServerInfo() != null) {
            if (refreshLogin) {
               this.playerUpdateSet.add(member.getUniqueId());
            }

            event.setServer(server.getServerInfo());
         } else {
            event.setCancelled(true);
            event.setCancelMessage(
               Language.getLanguage(event.getPlayer().getUniqueId()).t(logged ? "server-fallback-not-found" : "login.kick.login-not-available")
            );
         }
      }
   }

   @EventHandler
   public void onServerConnect(ServerConnectEvent event) {
      BungeeMember member = CommonPlugin.getInstance().getMemberManager().getMember(event.getPlayer().getUniqueId(), BungeeMember.class);
      if (member == null) {
         event.setCancelled(true);
         event.getPlayer().disconnect("§cHouve um problema com a sua conta!");
      } else {
         boolean logged = member.getLoginConfiguration().isLogged();
         if (!logged && BungeeMain.getInstance().getServerManager().getServer(event.getTarget().getName()).getServerType() != ServerType.LOGIN) {
            boolean disconnect = event.getPlayer().getServer() == null || event.getPlayer().getServer().getInfo() == null;
            String message = member.getLanguage()
               .t(disconnect ? "login.kick.not-logged" : "login.message.not-logged", "%website%", CommonPlugin.getInstance().getPluginInfo().getWebsite());
            if (disconnect) {
               event.getPlayer().disconnect(message);
            } else {
               event.setCancelled(true);
               event.getPlayer().sendMessage(message);
            }
         }
      }
   }

   @EventHandler
   public void onServerConnected(ServerConnectedEvent event) {
      BungeeMember member = CommonPlugin.getInstance().getMemberManager().getMember(event.getPlayer().getUniqueId(), BungeeMember.class);
      if (member != null) {
         if (this.playerUpdateSet.contains(member.getUniqueId())) {
            ProxyServer.getInstance().getScheduler().schedule(BungeeMain.getInstance(), () -> member.saveConfig(), 3L, TimeUnit.SECONDS);
         }

         ProxiedServer server = BungeeMain.getInstance().getServerManager().getServer(event.getServer().getInfo().getName());
         if (!server.getServerType().isLobby()) {
            Party party = member.getParty();
            if (party != null && party.hasRole(member.getUniqueId(), PartyRole.OWNER)) {
               if (server.getOnlinePlayers() + party.getMembers().size() > server.getMaxPlayers()) {
                  member.sendMessage("§eO servidor não suporta todos os membros da sua party, talvez alguns dos jogadores não sejam teletransportados.");
               }

               party.getMembers().stream().map(id -> CommonPlugin.getInstance().getMemberManager().getMember(id, BungeeMember.class)).forEach(partyMember -> {
                  if (partyMember != null && !partyMember.getActualServerId().equals(event.getServer().getInfo().getName())) {
                     partyMember.getProxiedPlayer().connect(event.getServer().getInfo());
                  }
               });
            }

            member.handleCheckGroup();
         }
      }
   }

   @EventHandler
   public void onServerKick(ServerKickEvent event) {
      if (!event.getKickReason().toLowerCase().contains("kick") && !event.getKickReason().toLowerCase().contains("expulso")) {
         Member member = CommonPlugin.getInstance().getMemberManager().getMember(event.getPlayer().getUniqueId());
         if (member == null || member.getLoginConfiguration().isLogged()) {
            ProxiedPlayer player = event.getPlayer();
            ProxiedServer kickedFrom = BungeeMain.getInstance().getServerManager().getServer(event.getKickedFrom().getName());
            ProxiedServer server = kickedFrom == null
               ? BungeeMain.getInstance().getServerManager().getBalancer(ServerType.LOBBY).next()
               : this.getFirstIfNull(
                  BungeeMain.getInstance().getServerManager().getBalancer(kickedFrom.getServerType().getServerLobby()).next(),
                  BungeeMain.getInstance().getServerManager().getBalancer(ServerType.LOBBY).next()
               );
            if (server != null && server.getServerInfo() != null && server != kickedFrom) {
               event.setCancelled(true);
               event.setCancelServer(server.getServerInfo());
               player.sendMessage(event.getKickReasonComponent());
            } else {
               player.disconnect(event.getKickReasonComponent());
            }
         }
      }
   }

   @EventHandler(
      priority = 127
   )
   public void onProxyPing(final ProxyPingEvent event) {
      final ServerPing serverPing = event.getResponse();
      String serverIp = this.getServerIp(event.getConnection());
      ProxiedServer server = BungeeMain.getInstance().getServerManager().getServer(serverIp);
      serverPing.getPlayers().setMax(ProxyServer.getInstance().getOnlineCount() + 1);
      serverPing.getPlayers().setOnline(ProxyServer.getInstance().getOnlineCount());
      if (server == null) {
         serverPing.getPlayers().setSample(new PlayerInfo[]{new PlayerInfo("§e" + CommonPlugin.getInstance().getPluginInfo().getWebsite(), UUID.randomUUID())});
         serverPing.setDescription(MOTD_HEADER + "\n" + (BungeeMain.getInstance().isMaintenance() ? MAINTENANCE_FOOTER : MOTD_FOOTER));
      } else {
         event.registerIntent(BungeeMain.getInstance());
         server.getServerInfo()
            .ping(
               new Callback<ServerPing>() {
                  public void done(ServerPing realPing, Throwable throwable) {
                     if (throwable == null) {
                        serverPing.getPlayers().setMax(realPing.getPlayers().getMax());
                        serverPing.getPlayers().setOnline(realPing.getPlayers().getOnline());
                        serverPing.setDescription(realPing.getDescription());
                     } else {
                        serverPing.getPlayers()
                           .setSample(new PlayerInfo[]{new PlayerInfo("§e" + CommonPlugin.getInstance().getPluginInfo().getWebsite(), UUID.randomUUID())});
                        serverPing.setDescription(ServerListener.MOTD_HEADER + "\n" + ServerListener.SERVER_NOT_FOUND);
                     }
      
                     event.completeIntent(BungeeMain.getInstance());
                  }
               }
            );
      }
   }

   private String getServerIp(PendingConnection con) {
      return con != null && con.getVirtualHost() != null ? con.getVirtualHost().getHostName().toLowerCase() : "";
   }

   public <T> T getFirstIfNull(T first, T second) {
      return (T)(second == null ? first : second);
   }
}

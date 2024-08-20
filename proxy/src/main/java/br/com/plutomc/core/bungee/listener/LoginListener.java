package br.com.plutomc.core.bungee.listener;

import br.com.plutomc.core.bungee.BungeeMain;
import com.google.common.base.Charsets;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import br.com.plutomc.core.common.CommonConst;
import br.com.plutomc.core.common.CommonPlugin;
import net.md_5.bungee.api.event.ClientConnectEvent;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class LoginListener implements Listener {
   private static final int MAX_CONNECTIONS_PER_IP = 10;
   private static final int MAX_CONNECTIONS = 25;
   private Cache<String, Integer> loginCache = CacheBuilder.newBuilder().expireAfterWrite(5L, TimeUnit.SECONDS).build(new CacheLoader<String, Integer>() {
      public Integer load(String name) throws Exception {
         return 1;
      }
   });
   private LoadingCache<String, Integer> throttleCache = CacheBuilder.newBuilder()
      .expireAfterWrite(5L, TimeUnit.SECONDS)
      .build(new CacheLoader<String, Integer>() {
         public Integer load(String name) throws Exception {
            return 0;
         }
      });
   private Map<String, Long> blockMap = new HashMap<>();

   @EventHandler
   public void onClient(ClientConnectEvent event) {
      SocketAddress socket = event.getSocketAddress();
      if (!(socket instanceof InetSocketAddress)) {
         event.setCancelled(true);
      } else {
         InetSocketAddress inetSocketAddress = (InetSocketAddress)socket;
         String ipAddress = inetSocketAddress.getHostString();
         if (this.isIpBlocked(ipAddress)) {
            event.setCancelled(true);
         } else {
            int throttle = this.throttleCache.getUnchecked(ipAddress);
            if (throttle >= 10) {
               event.setCancelled(true);
            }
         }
      }
   }

   @EventHandler
   public void onPreLogin(PreLoginEvent event) {
      SocketAddress socket = event.getConnection().getSocketAddress();
      if (!(socket instanceof InetSocketAddress)) {
         event.setCancelled(true);
         event.setCancelReason("§cWe cannot load your ip address.");
      } else {
         String playerName = event.getConnection().getName();
         String ipAddress = ((InetSocketAddress)socket).getHostString();
         int throttle = this.throttleCache.getUnchecked(ipAddress);
         if (throttle >= 3) {
            event.setCancelled(true);
            event.setCancelReason("§cAguarde para tentar uma nova conexão.");
         } else {
            this.throttleCache.put(ipAddress, Integer.valueOf(throttle + 1));
            int connections = this.throttleCache.asMap().size();
            if (connections >= 25) {
               CommonPlugin.getInstance()
                  .debug(
                     "The connection of player "
                        + playerName
                        + " ("
                        + ipAddress
                        + ") have been forced to pass as premium because the server reach the maximum connetions per attemps ("
                        + connections
                        + " connections current)."
                  );
               event.getConnection().setOnlineMode(true);
            } else if (playerName.toLowerCase().contains("mcstorm")) {
               event.setCancelled(true);
               event.setCancelReason("§cSua conta não foi carregada. [BungeeCord: 02]");
               this.block(ipAddress, "the name of player contains \"mcstorm\"");
            } else if (!CommonConst.NAME_PATTERN.matcher(playerName).matches()) {
               event.setCancelReason("§cSeu nome no jogo é inválido.\n§cPara entrar no servidor utilize um nome com até 16 caracteres, números ou \"_\".");
               event.setCancelled(true);
            } else {
               if (this.loginCache.asMap().containsKey(ipAddress)) {
                  if (this.loginCache.asMap().get(ipAddress) >= 10) {
                     event.setCancelReason("§cSua conta foi bloqueada por múltiplas conexões simultâneas.");
                     event.setCancelled(true);
                     this.loginCache.invalidate(ipAddress);
                     this.throttleCache.invalidate(ipAddress);
                     this.block(ipAddress, "multiple connections while verifing in mojang");
                     return;
                  }

                  this.loginCache.put(ipAddress, this.loginCache.asMap().get(ipAddress) + 1);
               }

               if (CommonPlugin.getInstance().getPluginInfo().isPiratePlayersEnabled()) {
                  event.registerIntent(BungeeMain.getInstance());
                  CommonPlugin.getInstance()
                     .getPluginPlatform()
                     .runAsync(
                        () -> {
                           boolean onlineMode = true;
                           if (CommonPlugin.getInstance().getMemberData().isRedisCached(playerName)) {
                              onlineMode = CommonPlugin.getInstance().getMemberData().isConnectionPremium(playerName);
                              CommonPlugin.getInstance()
                                 .debug("The player " + event.getConnection().getName() + " is " + (onlineMode ? "premium" : "cracked") + " (cached)");
                           } else {
                              boolean save = true;
                              this.loginCache.put(ipAddress, 1);
                              UUID uniqueId = CommonPlugin.getInstance().getUuidFetcher().getUniqueId(playerName);
                              if (uniqueId == null) {
                                 onlineMode = false;
                              } else {
                                 CommonPlugin.getInstance().debug("The player " + playerName + " have the UUID " + uniqueId);
                              }
      
                              if (save) {
                                 CommonPlugin.getInstance()
                                    .getMemberData()
                                    .setConnectionStatus(
                                       playerName,
                                       uniqueId == null ? UUID.nameUUIDFromBytes(("OfflinePlayer:" + playerName).getBytes(Charsets.UTF_8)) : uniqueId,
                                       onlineMode
                                    );
                              }
      
                              CommonPlugin.getInstance()
                                 .debug("The player " + event.getConnection().getName() + " is " + (onlineMode ? "premium" : "cracked") + " (not cached)");
                           }
      
                           CommonPlugin.getInstance().debug("The number of " + connections + " connections currently.");
                           event.getConnection().setOnlineMode(onlineMode);
                           event.completeIntent(BungeeMain.getInstance());
                           this.throttleCache.invalidate(ipAddress);
                           this.loginCache.invalidate(ipAddress);
                        }
                     );
               } else {
                  event.getConnection().setOnlineMode(true);
               }
            }
         }
      }
   }

   private boolean isIpBlocked(String hostString) {
      if (this.blockMap.containsKey(hostString)) {
         if (this.blockMap.get(hostString) > System.currentTimeMillis()) {
            return true;
         }

         this.blockMap.remove(hostString);
      }

      return false;
   }

   private void block(String ipAddress, String reason) {
      this.blockMap.put(ipAddress, System.currentTimeMillis() + 1800000L);
      CommonPlugin.getInstance().debug("The ip " + ipAddress + " has been blocked because " + reason);
   }
}

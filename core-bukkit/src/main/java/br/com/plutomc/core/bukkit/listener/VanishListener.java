package br.com.plutomc.core.bukkit.listener;

import br.com.plutomc.core.bukkit.BukkitCommon;
import br.com.plutomc.core.bukkit.event.player.PlayerAdminEvent;
import br.com.plutomc.core.bukkit.event.player.PlayerShowToPlayerEvent;
import br.com.plutomc.core.common.CommonPlugin;
import br.com.plutomc.core.common.account.Account;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class VanishListener implements Listener {
   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void onPlayerJoinL(PlayerJoinEvent event) {
      Player player = event.getPlayer();
      BukkitCommon.getInstance().getVanishManager().updateVanishToPlayer(player);
      Bukkit.getOnlinePlayers().forEach(online -> {
         if (!online.getUniqueId().equals(player.getUniqueId())) {
            PlayerShowToPlayerEvent eventCall = new PlayerShowToPlayerEvent(player, online);
            Bukkit.getPluginManager().callEvent(eventCall);
            if (eventCall.isCancelled()) {
               if (online.canSee(player)) {
                  online.hidePlayer(player);
               }
            } else if (!online.canSee(player)) {
               online.showPlayer(player);
            }
         }
      });
   }

   @EventHandler(
      priority = EventPriority.HIGH
   )
   public void onPlayerJoin(PlayerJoinEvent event) {
      Player player = event.getPlayer();
      Account account = CommonPlugin.getInstance().getAccountManager().getAccount(player.getUniqueId());
      if (player.hasPermission("command.admin") && account.getAccountConfiguration().isAdminOnJoin()) {
         BukkitCommon.getInstance().getVanishManager().setPlayerInAdmin(player);
      }
   }

   @EventHandler(
      priority = EventPriority.HIGHEST
   )
   public void onPlayerPickupItem(PlayerPickupItemEvent event) {
      if (this.isPlayerInAdmin(event.getPlayer())) {
         event.setCancelled(true);
      }
   }

   @EventHandler
   public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
      if (event.getRightClicked() instanceof Player && this.isPlayerInAdmin(event.getPlayer())) {
         event.getPlayer().performCommand("invsee " + event.getRightClicked().getName());
      }
   }

   @EventHandler
   public void onPlayerAdmin(PlayerAdminEvent event) {
      CommonPlugin.getInstance()
         .getAccountManager()
         .getAccount(event.getPlayer().getUniqueId())
         .getAccountConfiguration()
         .setAdminMode(event.getAdminMode() == PlayerAdminEvent.AdminMode.ADMIN);
   }

   @EventHandler
   public void onPlayerQuit(PlayerQuitEvent event) {
      BukkitCommon.getInstance().getVanishManager().resetPlayer(event.getPlayer());
   }

   private boolean isPlayerInAdmin(Player player) {
      return BukkitCommon.getInstance().getVanishManager().isPlayerInAdmin(player);
   }
}

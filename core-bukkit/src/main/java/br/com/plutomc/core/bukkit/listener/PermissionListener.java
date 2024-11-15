package br.com.plutomc.core.bukkit.listener;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import br.com.plutomc.core.bukkit.BukkitCommon;
import br.com.plutomc.core.bukkit.event.member.PlayerGroupChangeEvent;
import br.com.plutomc.core.bukkit.utils.permission.PermissionManager;
import br.com.plutomc.core.common.CommonPlugin;
import br.com.plutomc.core.common.account.Account;
import br.com.plutomc.core.common.permission.Group;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.scheduler.BukkitRunnable;

public class PermissionListener implements Listener {
   private final Map<UUID, PermissionAttachment> attachments = new HashMap<>();
   private PermissionManager manager = BukkitCommon.getInstance().getPermissionManager();

   public PermissionListener() {
      (new BukkitRunnable() {
         @Override
         public void run() {
            for(Player player : PermissionListener.this.manager.getServer().getOnlinePlayers()) {
               PermissionListener.this.updateAttachment(player);
            }
         }
      }).runTaskLater(this.manager.getPlugin(), 10L);
   }

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void onPlayerLogin(PlayerLoginEvent event) {
      this.updateAttachment(event.getPlayer());
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onPlayerLoginMonitor(PlayerLoginEvent event) {
      if (event.getResult() != Result.ALLOWED) {
         this.removeAttachment(event.getPlayer());
      }
   }

   @EventHandler
   public void onPlayerGroupChange(PlayerGroupChangeEvent event) {
      Player player = event.getPlayer();
      Group group = event.getGroup();
      if (group == null) {
         CommonPlugin.getInstance().debug("The server couldnt load group " + event.getGroupName() + " to change the permissions of " + player.getName());
      } else {
         this.removeAttachment(player);
         this.updateAttachment(player);
         player.recalculatePermissions();
      }
   }

   public void updateAttachment(Player player) {
      Account account = CommonPlugin.getInstance().getAccountManager().getAccount(player.getUniqueId());
      if (account != null) {
         PermissionAttachment attach = this.attachments.get(player.getUniqueId());
         Permission playerPerm = this.getCreateWrapper(player, player.getUniqueId().toString());
         if (attach == null) {
            attach = player.addAttachment(this.manager.getPlugin());
            this.attachments.put(player.getUniqueId(), attach);
            attach.setPermission(playerPerm, true);
         } else {
            attach.getPermissions().clear();
            attach.setPermission(playerPerm, true);
         }

         playerPerm.getChildren().clear();

         for(Group group : account.getGroups()
            .keySet()
            .stream()
            .map(groupName -> CommonPlugin.getInstance().getPluginInfo().getGroupByName(groupName))
            .filter(group -> group != null)
            .collect(Collectors.toList())) {
            for(String perm : group.getPermissions()) {
               if (!playerPerm.getChildren().containsKey(perm)) {
                  playerPerm.getChildren().put(perm, true);
               }
            }
         }

         for(String perm : account.getPermissions()) {
            if (!playerPerm.getChildren().containsKey(perm)) {
               playerPerm.getChildren().put(perm, true);
            }
         }

         player.recalculatePermissions();
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onQuit(PlayerQuitEvent event) {
      this.removeAttachment(event.getPlayer());
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onKick(PlayerKickEvent event) {
      this.removeAttachment(event.getPlayer());
   }

   protected void removeAttachment(Player player) {
      PermissionAttachment attach = this.attachments.remove(player.getUniqueId());
      if (attach != null) {
         attach.remove();
      }

      this.manager.getServer().getPluginManager().removePermission(player.getUniqueId().toString());
   }

   public void onDisable() {
      for(PermissionAttachment attach : this.attachments.values()) {
         attach.remove();
      }

      this.attachments.clear();
   }

   private Permission getCreateWrapper(Player player, String name) {
      Permission perm = this.manager.getServer().getPluginManager().getPermission(name);
      if (perm == null) {
         perm = new Permission(name, "Interal Permission", PermissionDefault.FALSE);
         this.manager.getServer().getPluginManager().addPermission(perm);
      }

      return perm;
   }
}

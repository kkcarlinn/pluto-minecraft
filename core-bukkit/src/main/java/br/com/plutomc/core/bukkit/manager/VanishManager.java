package br.com.plutomc.core.bukkit.manager;

import br.com.plutomc.core.bukkit.event.player.PlayerAdminEvent;
import br.com.plutomc.core.bukkit.event.player.PlayerHideToPlayerEvent;
import br.com.plutomc.core.bukkit.event.player.PlayerShowToPlayerEvent;
import com.google.common.collect.ImmutableSet;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import br.com.plutomc.core.common.CommonPlugin;
import br.com.plutomc.core.common.language.Language;
import br.com.plutomc.core.common.account.Account;
import br.com.plutomc.core.common.permission.Group;
import br.com.plutomc.core.common.utils.string.StringFormat;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class VanishManager {
   private Map<UUID, Group> vanishMap = new HashMap<>();
   private Set<UUID> adminSet = new HashSet<>();
   private Map<UUID, PlayerState> playerStateMap = new HashMap<>();

   public boolean setPlayerInAdmin(Player player) {
      if (this.adminSet.contains(player.getUniqueId())) {
         return false;
      } else {
         PlayerAdminEvent playerAdminEvent = new PlayerAdminEvent(player, PlayerAdminEvent.AdminMode.ADMIN, GameMode.CREATIVE);
         Bukkit.getPluginManager().callEvent(playerAdminEvent);
         if (playerAdminEvent.isCancelled()) {
            return false;
         } else {
            if (CommonPlugin.getInstance().getAccountManager().getAccount(player.getUniqueId()).getAccountConfiguration().isAdminRemoveItems()) {
               ItemStack[] contents = new ItemStack[player.getInventory().getContents().length + 4];

               for(int i = 0; i < player.getInventory().getContents().length; ++i) {
                  contents[i] = player.getInventory().getContents()[i];
               }

               for(int i = 0; i < player.getInventory().getArmorContents().length; ++i) {
                  contents[player.getInventory().getContents().length + i] = player.getInventory().getArmorContents()[i];
               }

               this.playerStateMap.put(player.getUniqueId(), new PlayerState(contents, player.getGameMode()));
               player.getInventory().clear();
               player.getInventory().setArmorContents(new ItemStack[4]);
            }

            this.adminSet.add(player.getUniqueId());
            Group group = this.hidePlayer(player);
            player.sendMessage("§dVocê entrou no modo admin.");
            player.sendMessage(
               Language.getLanguage(player.getUniqueId()).t("vanish.player-group-hided", "%group%", StringFormat.formatString(group.getGroupName()))
            );
            player.setGameMode(playerAdminEvent.getGameMode());
            if (playerAdminEvent.getGameMode() == GameMode.CREATIVE) {
               player.setFlying(true);
            }

            return true;
         }
      }
   }

   public boolean setPlayer(Player player) {
      if (this.adminSet.contains(player.getUniqueId())) {
         PlayerAdminEvent playerAdminEvent = new PlayerAdminEvent(
            player,
            PlayerAdminEvent.AdminMode.PLAYER,
            this.playerStateMap.containsKey(player.getUniqueId()) ? this.playerStateMap.get(player.getUniqueId()).getGameMode() : GameMode.SURVIVAL
         );
         Bukkit.getPluginManager().callEvent(playerAdminEvent);
         if (playerAdminEvent.isCancelled()) {
            return false;
         } else {
            this.adminSet.remove(player.getUniqueId());
            this.showPlayer(player);
            player.sendMessage("§dVocê saiu no modo admin.");
            player.sendMessage("§dVocê está visível para todos os jogadores.");
            player.setGameMode(playerAdminEvent.getGameMode());
            if (this.playerStateMap.containsKey(player.getUniqueId())) {
               ItemStack[] contents = this.playerStateMap.get(player.getUniqueId()).getContents();
               player.getInventory().setContents(Arrays.copyOfRange(contents, 0, player.getInventory().getContents().length));
               player.getInventory().setArmorContents(Arrays.copyOfRange(contents, player.getInventory().getContents().length, contents.length));
               this.playerStateMap.remove(player.getUniqueId());
            }

            return false;
         }
      } else {
         return true;
      }
   }

   public void resetPlayer(Player player) {
      this.adminSet.remove(player.getUniqueId());
      this.vanishMap.remove(player.getUniqueId());
      this.playerStateMap.remove(player.getUniqueId());
   }

   public void showPlayer(Player player) {
      this.setPlayerVanishToGroup(player, null);
   }

   public Group setPlayerVanishToGroup(Player player, Group group) {
      if (group == null) {
         this.vanishMap.remove(player.getUniqueId());
      } else {
         this.vanishMap.put(player.getUniqueId(), group);
      }

      for(Player online : Bukkit.getOnlinePlayers()) {
         if (!online.getUniqueId().equals(player.getUniqueId())) {
            Account onlineP = CommonPlugin.getInstance().getAccountManager().getAccount(online.getUniqueId());
            if (onlineP != null) {
               if (group != null && (onlineP.getServerGroup().getId() <= group.getId() || !onlineP.getAccountConfiguration().isSpectatorsEnabled())) {
                  PlayerHideToPlayerEvent event = new PlayerHideToPlayerEvent(player, online);
                  Bukkit.getPluginManager().callEvent(event);
                  if (event.isCancelled()) {
                     if (!online.canSee(player)) {
                        online.showPlayer(player);
                     }
                  } else if (online.canSee(player)) {
                     online.hidePlayer(player);
                  }
               } else {
                  PlayerShowToPlayerEvent event = new PlayerShowToPlayerEvent(player, online);
                  Bukkit.getPluginManager().callEvent(event);
                  if (event.isCancelled()) {
                     if (online.canSee(player)) {
                        online.hidePlayer(player);
                     }
                  } else if (!online.canSee(player)) {
                     online.showPlayer(player);
                  }
               }
            }
         }
      }

      return group;
   }

   public void updateVanishToPlayer(Player player) {
      Account account = CommonPlugin.getInstance().getAccountManager().getAccount(player.getUniqueId());

      for(Player online : Bukkit.getOnlinePlayers()) {
         if (!online.getUniqueId().equals(player.getUniqueId())) {
            Group group = this.vanishMap.get(online.getUniqueId());
            if (group != null && (account.getServerGroup().getId() <= group.getId() || !account.getAccountConfiguration().isSpectatorsEnabled())) {
               PlayerHideToPlayerEvent event = new PlayerHideToPlayerEvent(online, player);
               Bukkit.getPluginManager().callEvent(event);
               if (event.isCancelled()) {
                  if (!player.canSee(online)) {
                     player.showPlayer(online);
                  }
               } else if (player.canSee(online)) {
                  player.hidePlayer(online);
               }
            } else {
               PlayerShowToPlayerEvent event = new PlayerShowToPlayerEvent(online, player);
               Bukkit.getPluginManager().callEvent(event);
               if (event.isCancelled()) {
                  if (player.canSee(online)) {
                     player.hidePlayer(online);
                  }
               } else if (!player.canSee(online)) {
                  player.showPlayer(online);
               }
            }
         }
      }
   }

   public Group hidePlayer(Player player) {
      Account account = CommonPlugin.getInstance().getAccountManager().getAccount(player.getUniqueId());
      Group serverGroup = account.getServerGroup();
      Group group = serverGroup.getId() - 1 >= 0
         ? CommonPlugin.getInstance()
            .getPluginInfo()
            .filterGroup(
               g -> g.getId() < serverGroup.getId() && g.isStaff(), CommonPlugin.getInstance().getPluginInfo().getFirstLowerGroup(serverGroup.getId())
            )
         : serverGroup;
      return this.setPlayerVanishToGroup(player, group);
   }

   public Group getVanishedToGroup(Player player) {
      return this.vanishMap.get(player.getUniqueId());
   }

   public boolean isPlayerInAdmin(UUID playerId) {
      return this.adminSet.contains(playerId);
   }

   public boolean isPlayerInAdmin(Player player) {
      return this.adminSet.contains(player.getUniqueId());
   }

   public Set<UUID> getPlayersInAdmin() {
      return ImmutableSet.copyOf(this.adminSet);
   }

   public boolean isPlayerVanished(UUID uniqueId) {
      return this.vanishMap.containsKey(uniqueId);
   }

   public class PlayerState {
      private ItemStack[] contents;
      private GameMode gameMode;

      public PlayerState(ItemStack[] contents, GameMode gameMode) {
         this.contents = contents;
         this.gameMode = gameMode;
      }

      public ItemStack[] getContents() {
         return this.contents;
      }

      public GameMode getGameMode() {
         return this.gameMode;
      }
   }
}

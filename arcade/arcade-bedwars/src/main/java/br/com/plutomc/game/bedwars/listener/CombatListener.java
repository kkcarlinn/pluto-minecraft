package br.com.plutomc.game.bedwars.listener;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import br.com.plutomc.game.bedwars.utils.GamerHelper;
import br.com.plutomc.core.bukkit.event.player.PlayerDamagePlayerEvent;
import br.com.plutomc.game.engine.ArcadeCommon;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.metadata.MetadataValue;

public class CombatListener implements Listener {
   private Map<UUID, Combat> playerMap = new HashMap<>();

   @EventHandler(
      priority = EventPriority.HIGHEST
   )
   public void onPlayerDamagePlayer(PlayerDamagePlayerEvent event) {
      Player player = event.getPlayer();
      if (GamerHelper.isPlayerProtection(player)) {
         event.setCancelled(true);
      } else if (player.hasMetadata("invencibility")) {
         MetadataValue metadataValue = player.getMetadata("invencibility").stream().findFirst().orElse(null);
         if (metadataValue.asLong() > System.currentTimeMillis()) {
            event.setCancelled(true);
         } else {
            player.removeMetadata("invencibility", ArcadeCommon.getInstance());
         }
      } else {
         Player damager = event.getDamager();
         if (GamerHelper.isPlayerProtection(damager)) {
            GamerHelper.removePlayerProtection(damager);
         }

         if (damager.hasMetadata("invencibility")) {
            damager.removeMetadata("invencibility", ArcadeCommon.getInstance());
         }

         if (damager.getItemInHand() != null && damager.getItemInHand().getType().name().contains("SWORD")) {
            damager.getItemInHand().setDurability((short)0);
            damager.updateInventory();
         }

         this.playerMap.put(player.getUniqueId(), new Combat(damager.getUniqueId(), System.currentTimeMillis()));
         this.playerMap.put(damager.getUniqueId(), new Combat(player.getUniqueId(), System.currentTimeMillis()));
         event.setDamage(event.getDamage() * 1.2);
      }
   }

   @EventHandler
   public void onPlayerQuit(PlayerQuitEvent event) {
      if (this.playerMap.containsKey(event.getPlayer().getUniqueId())) {
         this.playerMap.remove(event.getPlayer().getUniqueId());
      }
   }

   public Map<UUID, Combat> getPlayerMap() {
      return this.playerMap;
   }

   public class Combat {
      private UUID lastPlayer;
      private long createdAt;

      public UUID getLastPlayer() {
         return this.lastPlayer;
      }

      public long getCreatedAt() {
         return this.createdAt;
      }

      public Combat(UUID lastPlayer, long createdAt) {
         this.lastPlayer = lastPlayer;
         this.createdAt = createdAt;
      }
   }
}

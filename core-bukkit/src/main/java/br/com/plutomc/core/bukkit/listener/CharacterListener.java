package br.com.plutomc.core.bukkit.listener;

import br.com.plutomc.core.bukkit.BukkitCommon;
import br.com.plutomc.core.bukkit.event.player.PlayerMoveUpdateEvent;
import br.com.plutomc.core.bukkit.utils.character.Character;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.PacketType.Play.Client;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers.EntityUseAction;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class CharacterListener implements Listener {
   private static final double MAX_DISTANCE = 128.0;

   public CharacterListener() {
      ProtocolLibrary.getProtocolManager()
         .addPacketListener(
            new PacketAdapter(BukkitCommon.getInstance(), Client.USE_ENTITY) {
               public void onPacketReceiving(PacketEvent event) {
                  if (!event.isCancelled()) {
                     Player player = event.getPlayer();
                     if (event.getPacket().getEntityUseActions().read(0) == EntityUseAction.INTERACT
                        || event.getPacket().getEntityUseActions().read(0) == EntityUseAction.ATTACK) {
                        int entityId = event.getPacket().getIntegers().read(0);
                        Character character = Character.getCharacter(entityId);
                        if (character != null) {
                           character.getInteractHandler().onInteract(player, event.getPacket().getEntityUseActions().read(0) == EntityUseAction.INTERACT);
                        }
                     }
                  }
               }
            }
         );
   }

   @EventHandler
   public void onPlayerJoin(PlayerJoinEvent event) {
      Character.getCharacters()
         .forEach(
            character -> {
               if (character.getNpc().getLocation().getWorld() == event.getPlayer().getLocation().getWorld()
                  && character.getNpc().getLocation().distance(event.getPlayer().getLocation()) < 128.0) {
                  character.show(event.getPlayer());
               }
            }
         );
   }

   @EventHandler(
      priority = EventPriority.HIGHEST
   )
   public void onPlayerRespawn(final PlayerRespawnEvent event) {
      (new BukkitRunnable() {
            @Override
            public void run() {
               Character.getCharacters()
                  .forEach(
                     character -> {
                        if (character.getNpc().getLocation().getWorld() == event.getPlayer().getLocation().getWorld()
                           && character.isShowing(event.getPlayer().getUniqueId())
                           && character.getNpc().getLocation().distance(event.getPlayer().getLocation()) < 128.0) {
                           character.hide(event.getPlayer());
                           character.show(event.getPlayer());
                        }
                     }
                  );
            }
         })
         .runTaskLater(BukkitCommon.getInstance(), 5L);
   }

   @EventHandler
   public void onPlayerTeleport(PlayerTeleportEvent event) {
      Character.getCharacters()
         .forEach(
            character -> {
               if (character.isShowing(event.getPlayer().getUniqueId())) {
                  if (character.getNpc().getLocation().getWorld() != event.getPlayer().getLocation().getWorld()) {
                     character.hide(event.getPlayer());
                  } else if (character.getNpc().getLocation().distance(event.getPlayer().getLocation()) > 128.0) {
                     character.hide(event.getPlayer());
                  }
               } else if (character.getNpc().getLocation().getWorld() == event.getPlayer().getLocation().getWorld()
                  && character.getNpc().getLocation().distance(event.getPlayer().getLocation()) < 128.0) {
                  character.show(event.getPlayer());
               }
            }
         );
   }

   @EventHandler
   public void onPlayerMoveUpdate(PlayerMoveUpdateEvent event) {
      Character.getCharacters()
         .forEach(
            character -> {
               if (character.isShowing(event.getPlayer().getUniqueId())) {
                  if (character.getNpc().getLocation().getWorld() != event.getPlayer().getLocation().getWorld()) {
                     character.hide(event.getPlayer());
                  } else if (character.getNpc().getLocation().distance(event.getPlayer().getLocation()) > 128.0) {
                     character.hide(event.getPlayer());
                  }
               } else if (character.getNpc().getLocation().getWorld() == event.getPlayer().getLocation().getWorld()
                  && character.getNpc().getLocation().distance(event.getPlayer().getLocation()) < 128.0) {
                  character.show(event.getPlayer());
               }
            }
         );
   }

   @EventHandler
   public void onPlayerJoin(PlayerQuitEvent event) {
      Character.getCharacters().forEach(character -> character.hide(event.getPlayer()));
   }
}

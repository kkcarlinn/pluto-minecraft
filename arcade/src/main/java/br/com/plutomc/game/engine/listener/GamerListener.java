package br.com.plutomc.game.engine.listener;

import java.lang.reflect.InvocationTargetException;
import java.util.Optional;
import java.util.UUID;

import br.com.plutomc.game.engine.ArcadeCommon;
import br.com.plutomc.game.engine.event.GamerLoadEvent;
import br.com.plutomc.game.engine.gamer.Gamer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;

public class GamerListener implements Listener {
   @EventHandler(
      priority = EventPriority.HIGHEST
   )
   public void onPlayerLogin(PlayerLoginEvent event) {
      if (event.getResult() == Result.ALLOWED) {
         Player player = event.getPlayer();
         Gamer gamer = ArcadeCommon.getInstance().getGamerManager().getGamer(player.getUniqueId());
         if (gamer == null) {
            Optional<Gamer> optional = ArcadeCommon.getInstance().getGamerData().loadGamer(player.getUniqueId());
            if (optional.isPresent()) {
               gamer = optional.get();
            } else {
               try {
                  gamer = ArcadeCommon.getInstance().getGamerClass().getConstructor(String.class, UUID.class).newInstance(player.getName(), player.getUniqueId());
                  ArcadeCommon.getInstance().getGamerData().createGamer(gamer);
               } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException | InstantiationException var6) {
                  event.setResult(Result.KICK_OTHER);
                  event.setKickMessage("§c§%gamer-not-loaded%§");
                  var6.printStackTrace();
                  return;
               }
            }

            gamer.setPlayer(event.getPlayer());
            gamer.loadGamer();
            GamerLoadEvent gamerLoadEvent = new GamerLoadEvent(player, gamer);
            Bukkit.getPluginManager().callEvent(gamerLoadEvent);
            if (gamerLoadEvent.isCancelled()) {
               event.setResult(Result.KICK_OTHER);
               event.setKickMessage(gamerLoadEvent.getReason());
            } else {
               ArcadeCommon.getInstance().getGamerManager().loadGamer(gamer);
               ArcadeCommon.getInstance().debug("The gamer " + player.getName() + "(" + player.getUniqueId() + ") has been loaded.");
            }
         } else {
            gamer.setPlayer(event.getPlayer());
            ArcadeCommon.getInstance().debug("The gamer " + player.getName() + "(" + player.getUniqueId() + ") has already been loaded.");
         }

         gamer.setOnline(true);
      }
   }

   @EventHandler(
      priority = EventPriority.HIGH
   )
   public void onPlayerQuit(PlayerQuitEvent event) {
      Player player = event.getPlayer();
      Gamer gamer = ArcadeCommon.getInstance().getGamerManager().getGamer(player.getUniqueId());
      if (gamer != null) {
         gamer.setPlayer(null);
         gamer.setOnline(false);
         if (ArcadeCommon.getInstance().isUnloadGamer()) {
            ArcadeCommon.getInstance().getGamerManager().unloadGamer(gamer.getUniqueId());
            ArcadeCommon.getInstance().debug("The gamer " + player.getName() + "(" + player.getUniqueId() + ") has been unloaded.");
         }
      }
   }
}

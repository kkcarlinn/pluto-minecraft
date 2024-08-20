package br.com.plutomc.core.bukkit.anticheat.hack.verify;

import br.com.plutomc.core.bukkit.anticheat.gamer.UserData;
import br.com.plutomc.core.bukkit.anticheat.hack.HackType;
import br.com.plutomc.core.bukkit.anticheat.hack.Verify;
import net.minecraft.server.v1_8_R3.MinecraftServer;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class ReachCheck implements Verify {
   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
      if (event.getDamager() instanceof Player) {
         if (!(MinecraftServer.getServer().recentTps[0] <= 19.98)) {
            Player player = (Player)event.getDamager();
            UserData userData = this.getUserData(player);
            if (player.getGameMode() != GameMode.CREATIVE && player.getGameMode() != GameMode.SPECTATOR) {
               if (!player.getAllowFlight()) {
                  double distance = Math.pow(player.getLocation().getX() - event.getDamager().getLocation().getX(), 2.0)
                     + Math.pow(player.getLocation().getZ() - event.getDamager().getLocation().getZ(), 2.0)
                     - 0.55;
                  double maxDistance = 4.5;
                  if (player.isSprinting()) {
                     maxDistance += 0.35;
                  }

                  if (((Player)event.getDamager()).isBlocking()) {
                     maxDistance -= 0.15;
                  }

                  if (((Player)event.getDamager()).isSneaking()) {
                     maxDistance -= 0.25;
                  }

                  int ping = userData.getPing();
                  if (ping >= 25) {
                     maxDistance += (double)(ping / 500);
                  }

                  if (distance >= maxDistance * maxDistance) {
                     this.alert(player, "distance: " + Math.sqrt(distance) + ", max: " + maxDistance + ", ping: " + ping);
                  }
               }
            }
         }
      }
   }

   @Override
   public HackType getHackType() {
      return HackType.REACH;
   }
}

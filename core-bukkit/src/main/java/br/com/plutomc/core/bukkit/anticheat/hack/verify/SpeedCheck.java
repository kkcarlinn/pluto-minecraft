package br.com.plutomc.core.bukkit.anticheat.hack.verify;

import br.com.plutomc.core.bukkit.anticheat.gamer.UserData;
import br.com.plutomc.core.bukkit.anticheat.hack.HackType;
import br.com.plutomc.core.bukkit.anticheat.hack.Verify;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffect;

public class SpeedCheck implements Verify {
   @EventHandler(
      priority = EventPriority.LOW
   )
   public void onPlayerMove(PlayerMoveEvent event) {
      Player player = event.getPlayer();
      if (!this.isIgnore(player)) {
         UserData userData = this.getUserData(event.getPlayer());
         Location lastLocation = userData.getLastLocation() == null ? event.getFrom() : userData.getLastLocation();
         double distance = Math.pow(event.getFrom().getX() - lastLocation.getX(), 2.0) + Math.pow(event.getFrom().getZ() - lastLocation.getZ(), 2.0);
         if (!player.getAllowFlight() && userData.getPing() <= 150 && !(distance < Math.pow(2.5, 2.0))) {
            float maxWalkSpeed = player.getWalkSpeed() * 1.2F;
            PotionEffect potion = player.getActivePotionEffects()
               .stream()
               .filter(potionEffect -> potionEffect.getType().getName().equals("SPEED"))
               .findFirst()
               .orElse(null);
            if (potion == null) {
               if (distance > Math.pow((double)maxWalkSpeed, 2.0)) {
                  this.alert(player, "(distance " + distance + ", maxSpeed: " + maxWalkSpeed + ")");
                  this.ignore(player, 0.5);
               }
            }
         }
      }
   }

   @Override
   public HackType getHackType() {
      return HackType.SPEED;
   }
}

package br.com.plutomc.lobby.login.captcha.impl;

import br.com.plutomc.lobby.login.captcha.Captcha;
import br.com.plutomc.core.bukkit.BukkitCommon;
import br.com.plutomc.core.common.utils.Callback;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class MoveCaptcha implements Captcha {
   @Override
   public void verify(final Player player, final Callback<Boolean> callback) {
      final Location lastLocation = player.getLocation().clone();
      player.teleport(player.getLocation().add(0.0, 5.0, 0.0));
      final Location actualLocation = player.getLocation().clone();
      (new BukkitRunnable() {
         @Override
         public void run() {
            callback.callback(Math.abs(actualLocation.getY() - player.getLocation().getY()) > 2.0);
            player.teleport(lastLocation);
         }
      }).runTaskLater(BukkitCommon.getInstance(), 100L);
   }
}

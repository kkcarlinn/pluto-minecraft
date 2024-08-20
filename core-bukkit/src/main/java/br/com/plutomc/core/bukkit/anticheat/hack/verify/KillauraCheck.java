package br.com.plutomc.core.bukkit.anticheat.hack.verify;

import br.com.plutomc.core.bukkit.anticheat.utils.ForcefieldManager;
import br.com.plutomc.core.bukkit.event.UpdateEvent;
import br.com.plutomc.core.bukkit.anticheat.StormCore;
import br.com.plutomc.core.bukkit.anticheat.hack.HackType;
import br.com.plutomc.core.bukkit.anticheat.hack.Verify;
import br.com.plutomc.core.common.utils.supertype.FutureCallback;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;

public class KillauraCheck implements Verify {
   private long lastCheck = System.currentTimeMillis();

   @EventHandler
   public void onUpdate(UpdateEvent event) {
      if (this.lastCheck + 300000L < System.currentTimeMillis()) {
         Bukkit.getOnlinePlayers()
            .forEach(player -> StormCore.getInstance().getForcefieldManager().check(player, 8, new FutureCallback<ForcefieldManager.Result>() {
                  public void result(ForcefieldManager.Result result, Throwable error) {
                     if (result.getHits() >= 5) {
                        KillauraCheck.this.alert(player, "hits: " + result.getHits() + ", entityCount: " + result.getEntityCount());
                     }
                  }
               }));
         this.lastCheck = System.currentTimeMillis();
      }
   }

   @Override
   public HackType getHackType() {
      return HackType.KILLAURA;
   }
}

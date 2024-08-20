package br.com.plutomc.core.bukkit.anticheat.hack.verify;

import br.com.plutomc.core.bukkit.event.UpdateEvent;
import br.com.plutomc.core.bukkit.anticheat.hack.Clicks;
import br.com.plutomc.core.bukkit.anticheat.hack.HackType;
import br.com.plutomc.core.bukkit.anticheat.hack.Verify;
import com.google.common.collect.ImmutableList;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;

public class MacroCheck implements Verify {
   private Map<Player, Clicks> clicksPerSecond = new HashMap<>();

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   private void onInventoryClick(InventoryClickEvent event) {
      Player player = (Player)event.getWhoClicked();
      if (player.getGameMode() != GameMode.CREATIVE && player.getGameMode() != GameMode.ADVENTURE) {
         if (!event.isShiftClick() || event.getAction() != InventoryAction.MOVE_TO_OTHER_INVENTORY) {
            Clicks click = this.clicksPerSecond.computeIfAbsent(player, v -> new Clicks());
            if (click.getExpireTime() < System.currentTimeMillis()) {
               if (click.getClicks() >= 25) {
                  this.alert(player);
               }

               this.clicksPerSecond.remove(player);
            } else {
               click.addClick();
            }
         }
      }
   }

   @EventHandler
   public void onUpdate(UpdateEvent event) {
      ImmutableList.copyOf(this.clicksPerSecond.entrySet())
         .stream()
         .filter(entry -> entry.getValue().getExpireTime() < System.currentTimeMillis())
         .forEach(entry -> {
            if (entry.getValue().getClicks() >= 20) {
               this.alert(entry.getKey(), "" + entry.getValue().getClicks() + " cps, max:20");
            }
   
            this.clicksPerSecond.remove(entry.getKey());
         });
   }

   @Override
   public HackType getHackType() {
      return HackType.MACRO;
   }
}

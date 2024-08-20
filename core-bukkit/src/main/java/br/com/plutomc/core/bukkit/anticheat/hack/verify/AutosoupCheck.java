package br.com.plutomc.core.bukkit.anticheat.hack.verify;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import br.com.plutomc.core.bukkit.anticheat.hack.HackType;
import br.com.plutomc.core.bukkit.anticheat.hack.Verify;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class AutosoupCheck implements Verify {
   private Map<UUID, Long> time = new HashMap<>();

   @EventHandler
   private void onClick(InventoryClickEvent event) {
      if (event.getAction().equals(InventoryAction.MOVE_TO_OTHER_INVENTORY)
         && event.getCurrentItem() != null
         && event.getCurrentItem().getType().equals(Material.MUSHROOM_SOUP)) {
         this.time.put(event.getWhoClicked().getUniqueId(), System.currentTimeMillis());
      }
   }

   @EventHandler(
      priority = EventPriority.LOWEST
   )
   private void onPlayerInteract(PlayerInteractEvent event) {
      if (event.hasItem() && event.getItem().getType().equals(Material.MUSHROOM_SOUP)) {
         Player player = event.getPlayer();
         if (this.time.containsKey(player.getUniqueId())) {
            Long spentTime = System.currentTimeMillis() - this.time.get(player.getUniqueId());
            if (spentTime <= 10L) {
               this.alert(player);
            }

            this.time.remove(player.getUniqueId());
         }
      }
   }

   @Override
   public HackType getHackType() {
      return HackType.AUTOSOUP;
   }
}

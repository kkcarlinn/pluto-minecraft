package br.com.plutomc.core.bukkit.listener;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import br.com.plutomc.core.bukkit.BukkitCommon;
import br.com.plutomc.core.bukkit.event.UpdateEvent;
import br.com.plutomc.core.bukkit.event.player.PlayerOpenInventoryEvent;
import br.com.plutomc.core.bukkit.utils.menu.MenuHolder;
import br.com.plutomc.core.bukkit.utils.menu.MenuInventory;
import br.com.plutomc.core.bukkit.utils.menu.MenuItem;
import br.com.plutomc.core.bukkit.utils.menu.click.ClickType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.scheduler.BukkitRunnable;

public class MenuListener implements Listener {
   private Map<Player, InventoryHolder> playerMap = new HashMap<>();

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onInventoryClickListener(InventoryClickEvent event) {
      if (event.getInventory() != null) {
         Inventory inv = event.getInventory();
         if (inv.getHolder() != null && inv.getHolder() instanceof MenuHolder) {
            event.setCancelled(true);
            if (event.getClickedInventory() == inv && event.getWhoClicked() instanceof Player && event.getSlot() >= 0) {
               MenuHolder holder = (MenuHolder)inv.getHolder();
               MenuInventory menu = holder.getMenu();
               if (menu.hasItem(event.getSlot())) {
                  Player p = (Player)event.getWhoClicked();
                  MenuItem item = menu.getItem(event.getSlot());
                  item.getHandler()
                     .onClick(
                        p,
                        inv,
                        event.getAction() == InventoryAction.PICKUP_HALF
                           ? ClickType.RIGHT
                           : (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY ? ClickType.SHIFT : ClickType.LEFT),
                        event.getCurrentItem(),
                        event.getSlot()
                     );
               } else {
                  event.setCancelled(true);
               }
            }
         }
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onMenuOpen(PlayerOpenInventoryEvent event) {
      if (event.getInventory() != null) {
         Inventory inventory = event.getInventory();
         if (inventory.getHolder() instanceof MenuHolder) {
            MenuInventory menu = ((MenuHolder)inventory.getHolder()).getMenu();
            if (menu.getUpdateHandler() != null) {
               this.playerMap.put(event.getPlayer(), inventory.getHolder());
            }
         }
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onInventoryClose(final InventoryCloseEvent event) {
      if (event.getInventory() != null) {
         Inventory inventory = event.getInventory();
         if (inventory.getHolder() instanceof MenuHolder) {
            final MenuHolder menuHolder = (MenuHolder)inventory.getHolder();
            final Player player = (Player)event.getPlayer();
            if (this.playerMap.containsKey(player)) {
               this.playerMap.remove(player);
            }

            if (menuHolder.getMenu().isReopenInventory()) {
               (new BukkitRunnable() {
                  @Override
                  public void run() {
                     if (player.isOnline()) {
                        menuHolder.getMenu().open((Player)event.getPlayer());
                     }
                  }
               }).runTaskLater(BukkitCommon.getInstance(), 10L);
            } else {
               menuHolder.onClose(player);
            }
         }
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onPlayerQuit(PlayerQuitEvent event) {
      this.playerMap.remove(event.getPlayer());
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onUpdate(UpdateEvent event) {
      if (event.getType() == UpdateEvent.UpdateType.SECOND) {
         for(Entry<Player, InventoryHolder> entry : this.playerMap.entrySet()) {
            MenuInventory menu = ((MenuHolder)entry.getValue()).getMenu();
            if (menu.getUpdateHandler() != null) {
               menu.getUpdateHandler().onUpdate(entry.getKey(), menu);
            }
         }
      }
   }
}

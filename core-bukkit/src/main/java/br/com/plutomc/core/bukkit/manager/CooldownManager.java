package br.com.plutomc.core.bukkit.manager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import br.com.plutomc.core.bukkit.BukkitCommon;
import br.com.plutomc.core.bukkit.event.UpdateEvent;
import br.com.plutomc.core.bukkit.event.cooldown.CooldownFinishEvent;
import br.com.plutomc.core.bukkit.event.cooldown.CooldownStartEvent;
import br.com.plutomc.core.bukkit.event.cooldown.CooldownStopEvent;
import br.com.plutomc.core.bukkit.utils.cooldown.Cooldown;
import br.com.plutomc.core.bukkit.utils.cooldown.ItemCooldown;
import br.com.plutomc.core.bukkit.utils.player.PlayerHelper;
import br.com.plutomc.core.common.utils.string.StringFormat;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

public class CooldownManager implements Listener {
   private static final char CHAR = '|';
   private Map<UUID, List<Cooldown>> map = new ConcurrentHashMap<>();
   private Listener listener = new CooldownListener();

   public void addCooldown(Player player, Cooldown cooldown) {
      CooldownStartEvent event = new CooldownStartEvent(player, cooldown);
      Bukkit.getServer().getPluginManager().callEvent(event);
      if (!event.isCancelled()) {
         List<Cooldown> list = this.map.computeIfAbsent(player.getUniqueId(), v -> new ArrayList());
         boolean add = true;

         for(Cooldown cool : list) {
            if (cool.getName().equals(cooldown.getName())) {
               cool.update(cooldown.getDuration(), cooldown.getStartTime());
               add = false;
            }
         }

         if (add) {
            list.add(cooldown);
         }

         if (!this.map.isEmpty()) {
            this.registerListener();
         }
      }
   }

   public void addCooldown(UUID uuid, String name, long duration) {
      Player player = Bukkit.getPlayer(uuid);
      if (player != null) {
         Cooldown cooldown = new Cooldown(name, duration);
         CooldownStartEvent event = new CooldownStartEvent(player, cooldown);
         Bukkit.getServer().getPluginManager().callEvent(event);
         if (!event.isCancelled()) {
            List<Cooldown> list = this.map.computeIfAbsent(player.getUniqueId(), v -> new ArrayList());
            boolean add = true;

            for(Cooldown cool : list) {
               if (cool.getName().equals(cooldown.getName())) {
                  cool.update(cooldown.getDuration(), cooldown.getStartTime());
                  add = false;
               }
            }

            if (add) {
               list.add(cooldown);
            }

            if (!this.map.isEmpty()) {
               this.registerListener();
            }
         }
      }
   }

   private void registerListener() {
      if (this.listener == null) {
         this.listener = new CooldownListener();
         Bukkit.getPluginManager().registerEvents(this.listener, BukkitCommon.getInstance());
      }
   }

   public boolean removeCooldown(Player player, String name) {
      if (this.map.containsKey(player.getUniqueId())) {
         List<Cooldown> list = this.map.get(player.getUniqueId());
         Iterator<Cooldown> it = list.iterator();

         while(it.hasNext()) {
            Cooldown cooldown = it.next();
            if (cooldown.getName().equals(name)) {
               it.remove();
               Bukkit.getPluginManager().callEvent(new CooldownStopEvent(player, cooldown));
               return true;
            }
         }
      }

      return false;
   }

   public boolean hasCooldown(Player player, String name) {
      if (this.map.containsKey(player.getUniqueId())) {
         for(Cooldown cooldown : this.map.get(player.getUniqueId())) {
            if (cooldown.getName().equals(name)) {
               return true;
            }
         }
      }

      return false;
   }

   public boolean hasCooldown(UUID uniqueId, String name) {
      if (this.map.containsKey(uniqueId)) {
         for(Cooldown cooldown : this.map.get(uniqueId)) {
            if (cooldown.getName().equals(name)) {
               return true;
            }
         }
      }

      return false;
   }

   public Cooldown getCooldown(UUID uniqueId, String name) {
      if (this.map.containsKey(uniqueId)) {
         for(Cooldown cooldown : this.map.get(uniqueId)) {
            if (cooldown.getName().equals(name)) {
               return cooldown;
            }
         }
      }

      return null;
   }

   public void clearCooldown(Player player) {
      if (this.map.containsKey(player.getUniqueId())) {
         this.map.remove(player.getUniqueId());
      }
   }

   public class CooldownListener implements Listener {
      @EventHandler
      public void onUpdate(UpdateEvent event) {
         if (event.getType() == UpdateEvent.UpdateType.TICK) {
            if (event.getCurrentTick() % 5L != 0L) {
               for(UUID uuid : CooldownManager.this.map.keySet()) {
                  Player player = Bukkit.getPlayer(uuid);
                  if (player != null) {
                     List<Cooldown> list = CooldownManager.this.map.get(uuid);
                     Iterator<Cooldown> it = list.iterator();
                     Cooldown found = null;

                     while(it.hasNext()) {
                        Cooldown cooldown = it.next();
                        if (!cooldown.expired()) {
                           if (cooldown instanceof ItemCooldown) {
                              ItemStack hand = player.getItemInHand();
                              if (hand != null && hand.getType() != Material.AIR) {
                                 ItemCooldown item = (ItemCooldown)cooldown;
                                 if (hand.equals(item.getItem())) {
                                    item.setSelected(true);
                                    found = item;
                                    break;
                                 }
                              }
                           } else {
                              found = cooldown;
                           }
                        } else {
                           it.remove();
                           CooldownFinishEvent e = new CooldownFinishEvent(player, cooldown);
                           Bukkit.getServer().getPluginManager().callEvent(e);
                        }
                     }

                     if (found != null) {
                        this.display(player, found);
                     } else if (list.isEmpty()) {
                        PlayerHelper.actionbar(player, " ");
                        CooldownManager.this.map.remove(uuid);
                     } else {
                        Cooldown cooldown = list.get(0);
                        if (cooldown instanceof ItemCooldown) {
                           ItemCooldown item = (ItemCooldown)cooldown;
                           if (item.isSelected()) {
                              item.setSelected(false);
                              PlayerHelper.actionbar(player, " ");
                           }
                        }
                     }
                  }
               }
            }
         }
      }

      @EventHandler
      public void onCooldown(CooldownStopEvent event) {
         if (CooldownManager.this.map.isEmpty()) {
            HandlerList.unregisterAll(CooldownManager.this.listener);
            CooldownManager.this.listener = null;
         }
      }

      private void display(Player player, Cooldown cooldown) {
         StringBuilder bar = new StringBuilder();
         double percentage = cooldown.getPercentage();
         double count = 20.0 - Math.max(percentage > 0.0 ? 1.0 : 0.0, percentage / 5.0);

         for(int a = 0; (double)a < count; ++a) {
            bar.append("§a|");
         }

         for(int a = 0; (double)a < 20.0 - count; ++a) {
            bar.append("§c|");
         }

         PlayerHelper.actionbar(
            player,
            "§f" + cooldown.getName() + " " + bar.toString() + " §f" + StringFormat.formatTime((int)cooldown.getRemaining(), StringFormat.TimeFormat.NORMAL)
         );
      }
   }
}

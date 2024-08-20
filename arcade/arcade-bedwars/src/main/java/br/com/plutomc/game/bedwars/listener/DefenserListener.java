package br.com.plutomc.game.bedwars.listener;

import br.com.plutomc.game.bedwars.GameMain;
import br.com.plutomc.game.bedwars.gamer.Gamer;
import br.com.plutomc.game.bedwars.utils.ProgressBar;
import com.google.common.collect.ImmutableList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import br.com.plutomc.core.bukkit.event.UpdateEvent;
import br.com.plutomc.game.bedwars.island.Island;
import br.com.plutomc.game.bedwars.island.IslandColor;
import br.com.plutomc.core.bukkit.utils.item.ItemBuilder;
import br.com.plutomc.core.common.utils.string.StringFormat;
import net.minecraft.server.v1_8_R3.AttributeInstance;
import net.minecraft.server.v1_8_R3.AttributeModifier;
import net.minecraft.server.v1_8_R3.EntityLiving;
import net.minecraft.server.v1_8_R3.GenericAttributes;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Silverfish;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class DefenserListener implements Listener {
   private static final long IRONGOLEM_TIME = 240000L;
   private static final long SILVERFISH_TIME = 30000L;
   private Map<Entity, Defenser> defenserMap = new HashMap<>();

   @EventHandler
   public void onProjectileHit(ProjectileHitEvent event) {
      if (event.getEntity() instanceof Snowball) {
         Snowball snowball = (Snowball)event.getEntity();
         if (snowball.getShooter() instanceof Player) {
            Player player = (Player)snowball.getShooter();
            Island island = GameMain.getInstance().getIslandManager().getIsland(player.getUniqueId());
            if (island == null) {
               return;
            }

            Location location = event.getEntity().getLocation();
            Silverfish silverfish = location.getWorld().spawn(location, Silverfish.class);
            long time = System.currentTimeMillis() + 30000L;
            int leftTime = (int)(time % System.currentTimeMillis()) / 1000;
            silverfish.setCustomName(
               "§7["
                  + ProgressBar.getProgressBar(silverfish.getHealth(), silverfish.getMaxHealth(), 5, '▮', ChatColor.AQUA, ChatColor.GRAY)
                  + "§7] §b"
                  + StringFormat.formatTime(leftTime, StringFormat.TimeFormat.SHORT)
            );
            silverfish.setCustomNameVisible(true);
            EntityLiving en = (EntityLiving)((CraftEntity)silverfish).getHandle();
            AttributeInstance speed = en.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED);
            AttributeModifier speedModifier = new AttributeModifier(silverfish.getUniqueId(), "SpeedIncreaser", 1.4, 1);
            speed.b(speedModifier);
            speed.a(speedModifier);
            this.defenserMap.put(silverfish, new Defenser(island.getIslandColor(), time));
         }
      }
   }

   @EventHandler
   public void onEntityChangeBlock(EntityChangeBlockEvent event) {
      if (event.getEntity().getType().equals(EntityType.SILVERFISH)) {
         event.setCancelled(true);
      }
   }

   @EventHandler
   public void onPlayerInteract(PlayerInteractEvent event) {
      if (event.getItem() != null && event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getItem().getType() == Material.MONSTER_EGG) {
         Player player = event.getPlayer();
         Island island = GameMain.getInstance().getIslandManager().getIsland(player.getUniqueId());
         if (island == null) {
            event.setCancelled(true);
         } else {
            ItemStack itemStack = event.getItem();
            player.setItemInHand(
               ItemBuilder.fromStack(itemStack).type(itemStack.getAmount() > 1 ? itemStack.getType() : Material.AIR).amount(itemStack.getAmount() - 1).build()
            );
            long time = System.currentTimeMillis() + 240000L;
            int leftTime = (int)(time % System.currentTimeMillis()) / 1000;
            EntityType entityType = EntityType.IRON_GOLEM;
            Entity spawnEntity = event.getClickedBlock()
               .getLocation()
               .getWorld()
               .spawnEntity(event.getClickedBlock().getLocation().clone().add(0.0, 1.5, 0.0), entityType);
            this.defenserMap.put(spawnEntity, new Defenser(island.getIslandColor(), System.currentTimeMillis() + 240000L));
            spawnEntity.setCustomName(
               "§7["
                  + ProgressBar.getProgressBar(
                     (double)((int)((Damageable)spawnEntity).getHealth()),
                     (double)((int)((Damageable)spawnEntity).getMaxHealth()),
                     5,
                     '▮',
                     ChatColor.AQUA,
                     ChatColor.GRAY
                  )
                  + "§7] §b"
                  + StringFormat.formatTime(leftTime, StringFormat.TimeFormat.SHORT)
            );
            spawnEntity.setCustomNameVisible(true);
            ((Damageable)spawnEntity).setMaxHealth(20.0);
         }
      }
   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onEntityDamageEntity(EntityDamageByEntityEvent event) {
      if (event.getDamager() instanceof Player || event.getDamager() instanceof Projectile) {
         Entity entity = event.getEntity();
         Player player = event.getDamager() instanceof Player ? (Player)event.getDamager() : (Player)((Projectile)event.getDamager()).getShooter();
         Island island = GameMain.getInstance().getIslandManager().getIsland(player.getUniqueId());
         if (island != null && this.defenserMap.containsKey(entity) && this.defenserMap.get(entity).getIsland() == island.getIslandColor()) {
            event.setCancelled(true);
         }
      }
   }

   @EventHandler
   public void onPlayerDeath(PlayerDeathEvent event) {
      Player player = event.getEntity();
      ImmutableList.copyOf(this.defenserMap.keySet())
         .stream()
         .map(entity -> (Creature)entity)
         .filter(entity -> entity.getTarget().getUniqueId() == player.getUniqueId())
         .forEach(entity -> entity.setTarget(null));
   }

   @EventHandler
   public void onEntityTarget(EntityTargetEvent event) {
      if (event.getTarget() instanceof Player) {
         Player player = (Player)event.getTarget();
         Island island = GameMain.getInstance().getIslandManager().getIsland(player.getUniqueId());
         if (island == null) {
            event.setCancelled(true);
         } else {
            Entity entity = event.getEntity();
            if (this.defenserMap.containsKey(entity)) {
               Defenser defenser = this.defenserMap.get(entity);
               if (defenser.getIsland() == island.getIslandColor()) {
                  event.setCancelled(true);
               }
            }
         }
      }
   }

   @EventHandler
   public void onEntityDeath(EntityDeathEvent event) {
      Entity entity = event.getEntity();
      if (this.defenserMap.containsKey(entity)) {
         this.defenserMap.remove(entity);
      }
   }

   @EventHandler
   public void onUpdate(UpdateEvent event) {
      if (event.getType() == UpdateEvent.UpdateType.SECOND) {
         Iterator<Entry<Entity, Defenser>> iterator = this.defenserMap.entrySet().iterator();

         while(iterator.hasNext()) {
            Entry<Entity, Defenser> entry = iterator.next();
            Entity entity = entry.getKey();
            int leftTime = (int)(entry.getValue().getExpireTime() - System.currentTimeMillis()) / 1000;
            if (leftTime > 0) {
               Creature creature = (Creature)entity;
               if (creature.getTarget() != null && creature.getTarget().getLocation().distance(entity.getLocation()) > 20.0) {
                  creature.setTarget(null);
               }

               if (creature.getTarget() == null) {
                  for(Gamer gamer : GameMain.getInstance().getAlivePlayers()) {
                     if (gamer.isOnline() && gamer.isAlive()) {
                        Player player = gamer.getPlayer();
                        if (GameMain.getInstance().getIslandManager().getIsland(player.getUniqueId()).getIslandColor() != entry.getValue().getIsland()
                           && player.getLocation().distance(entity.getLocation()) <= 10.0) {
                           creature.setTarget(player);
                        }
                     }
                  }
               }

               entity.setCustomName(
                  "§7["
                     + ProgressBar.getProgressBar(
                        (double)((int)((Damageable)entity).getHealth()),
                        (double)((int)((Damageable)entity).getMaxHealth()),
                        5,
                        '▮',
                        ChatColor.AQUA,
                        ChatColor.GRAY
                     )
                     + "§7] §b"
                     + StringFormat.formatTime(leftTime, StringFormat.TimeFormat.SHORT)
               );
            } else {
               entity.remove();
               iterator.remove();
            }
         }
      }
   }

   public class Defenser {
      private final IslandColor island;
      private long expireTime;

      public Defenser(IslandColor island, long expireTime) {
         this.island = island;
         this.expireTime = expireTime;
      }

      public IslandColor getIsland() {
         return this.island;
      }

      public long getExpireTime() {
         return this.expireTime;
      }
   }
}

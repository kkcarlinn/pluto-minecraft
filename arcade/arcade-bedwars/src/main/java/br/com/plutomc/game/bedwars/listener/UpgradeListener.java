package br.com.plutomc.game.bedwars.listener;

import br.com.plutomc.game.bedwars.GameMain;
import br.com.plutomc.game.bedwars.event.island.IslandUpgradeEvent;
import br.com.plutomc.game.bedwars.gamer.Gamer;
import br.com.plutomc.game.bedwars.generator.Generator;
import br.com.plutomc.game.bedwars.generator.impl.NormalGenerator;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Stream;
import br.com.plutomc.core.common.CommonConst;
import br.com.plutomc.core.bukkit.event.UpdateEvent;
import br.com.plutomc.game.engine.ArcadeCommon;
import br.com.plutomc.game.bedwars.island.Island;
import br.com.plutomc.game.bedwars.island.IslandUpgrade;
import br.com.plutomc.core.bukkit.utils.player.PlayerHelper;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class UpgradeListener implements Listener {
   private static final double ISLAND_DISTANCE = 25.0;
   private Map<Island, Integer> regenMap = new HashMap<>();
   private Map<UUID, Long> trapBlockMap = new HashMap<>();
   private List<Island> trapList = new ArrayList<>();

   @EventHandler
   public void onUpdateEvent(UpdateEvent event) {
      if (event.getType() == UpdateEvent.UpdateType.SECOND) {
         this.regenMap
            .entrySet()
            .forEach(
               entry -> {
                  Location location = entry.getKey().getIslandGenerators().stream().findFirst().orElse(null).getLocation();
      
                  for(int i = 0; i <= 40; ++i) {
                     Location particleLocation = new Location(
                        location.getWorld(),
                        location.getX() + Math.random() * 16.666666666666668 * (double)(CommonConst.RANDOM.nextBoolean() ? -1 : 1),
                        location.getY() + Math.random() * 8.333333333333334,
                        location.getZ() + Math.random() * 16.666666666666668 * (double)(CommonConst.RANDOM.nextBoolean() ? -1 : 1)
                     );
                     location.getWorld().playEffect(particleLocation, Effect.HAPPY_VILLAGER, 1);
                  }
               }
            );
         ImmutableList.copyOf(this.regenMap.entrySet())
            .forEach(
               entry -> this.map(entry.getKey())
                     .filter(gamer -> gamer.getPlayer().getLocation().distance(((Island)entry.getKey()).getSpawnLocation().getAsLocation()) <= 25.0)
                     .forEach(gamer -> gamer.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 80, entry.getValue() - 1)))
            );
         ImmutableList.copyOf(this.trapList)
            .forEach(
               island -> Bukkit.getOnlinePlayers()
                     .stream()
                     .filter(
                        player -> !island.getTeam().isTeam(player.getUniqueId())
                              && ArcadeCommon.getInstance().getGamerManager().getGamer(player.getUniqueId(), Gamer.class).isAlive()
                              && player.getLocation().distance(island.getSpawnLocation().getAsLocation()) <= 25.0
                              && (
                                 !this.trapBlockMap.containsKey(player.getUniqueId())
                                    || this.trapBlockMap.get(player.getUniqueId()) < System.currentTimeMillis()
                              )
                     )
                     .forEach(player -> {
                        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 100, 1), true);
                        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 100, 0), true);
                        this.forEach(island, gamer -> PlayerHelper.actionbar(gamer.getPlayer(), "§c§lARMADILHA ACIONADA!"));
                        this.trapList.remove(island);
                        island.removeUpgrade(IslandUpgrade.TRAP);
                     })
            );
      }
   }

   @EventHandler
   public void onPlayerConsume(PlayerItemConsumeEvent event) {
      if (event.getItem().getType() == Material.MILK_BUCKET) {
         event.setCancelled(true);
         final Player player = event.getPlayer();
         (new BukkitRunnable() {
            @Override
            public void run() {
               player.getInventory().remove(Material.MILK_BUCKET);
               player.removePotionEffect(PotionEffectType.BLINDNESS);
               player.removePotionEffect(PotionEffectType.SLOW);
               if (!UpgradeListener.this.trapBlockMap.containsKey(player.getUniqueId())) {
                  UpgradeListener.this.trapBlockMap.put(player.getUniqueId(), System.currentTimeMillis() + 30000L);
               }
            }
         }).runTaskLater(ArcadeCommon.getInstance(), 3L);
      }
   }

   @EventHandler
   public void onIslandUpgrade(IslandUpgradeEvent event) {
      Island island = event.getIsland();
      IslandUpgrade upgrade = event.getUpgrade();
      int level = event.getLevel();
      switch(upgrade) {
         case SHARPNESS:
            this.forEach(island, gamer -> {
               if (gamer.getPlayer() != null) {
                  for(ItemStack itemStackx : gamer.getPlayer().getInventory().getContents()) {
                     if (itemStackx != null && itemStackx.getType().name().contains("SWORD")) {
                        itemStackx.addEnchantment(Enchantment.DAMAGE_ALL, level);
                     }
                  }

                  for(ItemStack itemStack : gamer.getPlayer().getEnderChest().getContents()) {
                     if (itemStack != null && itemStack.getType().name().contains("SWORD")) {
                        itemStack.addEnchantment(Enchantment.DAMAGE_ALL, level);
                     }
                  }
               }
            });

            for(Location location : GameMain.getInstance().getNearestBlocksByMaterial(island.getSpawnLocation().getAsLocation(), Material.CHEST, 10, 5)) {
               Chest chest = (Chest)location.getBlock().getState();

               for(ItemStack itemStack : chest.getInventory().getContents()) {
                  if (itemStack != null && itemStack.getType().name().contains("SWORD")) {
                     itemStack.addEnchantment(Enchantment.DAMAGE_ALL, level);
                  }
               }
            }
            break;
         case ARMOR_REINFORCEMENT:
            this.forEach(island, gamer -> {
               if (gamer.getPlayer() != null) {
                  for(ItemStack itemStackx : gamer.getPlayer().getInventory().getArmorContents()) {
                     if (itemStackx != null) {
                        itemStackx.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, level);
                     }
                  }
               }
            });
            break;
         case HASTE:
            this.forEach(island, gamer -> {
               if (gamer.getPlayer() != null) {
                  gamer.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, 199980, level - 1), true);
               }
            });
            break;
         case TRAP:
            this.trapList.add(island);
            break;
         case REGENERATION:
            this.regenMap.put(island, level);
            break;
         case FORGE:
            if (level == 1) {
               island.getIslandGenerators()
                  .stream()
                  .limit(3L)
                  .filter(generatorx -> generatorx.getItemStack().getType() == Material.IRON_INGOT)
                  .forEach(generatorx -> {
                     generatorx.setGenerateTime(generatorx.getGenerateTime() - 250);
                     generatorx.setLevel(generatorx.getLevel() + 1);
                  });
               island.getIslandGenerators().stream().filter(generatorx -> generatorx.getItemStack().getType() == Material.GOLD_INGOT).forEach(generatorx -> {
                  generatorx.setGenerateTime(generatorx.getGenerateTime() - 250);
                  generatorx.setLevel(generatorx.getLevel() + 1);
               });
            } else if (level == 2) {
               island.getIslandGenerators().stream().forEach(generatorx -> {
                  generatorx.setGenerateTime(generatorx.getGenerateTime() - 250);
                  generatorx.setLevel(generatorx.getLevel() + 1);
               });
            } else if (level == 3) {
               Generator generator = new NormalGenerator(
                  island.getGeneratorMap().get(Material.GOLD_INGOT).stream().findFirst().orElse(null).getAsLocation(), Material.EMERALD
               );
               generator.setGenerateTime(15000L);
               island.getIslandGenerators().add(generator);
            } else if (level == 4) {
               island.getIslandGenerators().stream().forEach(generatorx -> {
                  generatorx.setGenerateTime(Math.max(generatorx.getGenerateTime() - 2L, 0L));
                  generatorx.setLevel(generatorx.getLevel() + 1);
               });
            }
      }
   }

   private Stream<Gamer> map(Island island) {
      return island.getTeam()
         .getPlayerSet()
         .stream()
         .map(id -> ArcadeCommon.getInstance().getGamerManager().getGamer(id, Gamer.class))
         .filter(gamer -> gamer.getPlayer() != null && gamer.isAlive());
   }

   private void forEach(Island island, Consumer<Gamer> consumer) {
      island.getTeam()
         .getPlayerSet()
         .stream()
         .map(id -> ArcadeCommon.getInstance().getGamerManager().getGamer(id, Gamer.class))
         .filter(gamer -> gamer.getPlayer() != null && gamer.isAlive())
         .forEach(consumer);
   }
}

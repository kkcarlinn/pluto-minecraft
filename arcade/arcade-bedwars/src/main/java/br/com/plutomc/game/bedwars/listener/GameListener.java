package br.com.plutomc.game.bedwars.listener;

import br.com.plutomc.game.bedwars.GameMain;
import br.com.plutomc.game.bedwars.event.PlayerKillPlayerEvent;
import br.com.plutomc.game.bedwars.event.island.IslandLoseEvent;
import br.com.plutomc.game.bedwars.gamer.Gamer;
import br.com.plutomc.game.bedwars.generator.Generator;
import br.com.plutomc.game.bedwars.utils.GamerHelper;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.PacketType.Play.Server;
import com.google.common.collect.ImmutableList;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import br.com.plutomc.core.common.CommonConst;
import br.com.plutomc.core.common.CommonPlugin;
import br.com.plutomc.core.bukkit.BukkitCommon;
import br.com.plutomc.core.bukkit.event.UpdateEvent;
import br.com.plutomc.core.bukkit.event.player.PlayerAdminEvent;
import br.com.plutomc.core.bukkit.event.player.PlayerMoveUpdateEvent;
import br.com.plutomc.game.engine.ArcadeCommon;
import br.com.plutomc.game.bedwars.island.Island;
import br.com.plutomc.game.bedwars.island.IslandColor;
import br.com.plutomc.game.bedwars.island.IslandUpgrade;
import br.com.plutomc.game.engine.event.GamerLoadEvent;
import br.com.plutomc.core.bukkit.utils.PacketBuilder;
import br.com.plutomc.core.bukkit.utils.item.ItemBuilder;
import br.com.plutomc.core.bukkit.utils.menu.MenuHolder;
import br.com.plutomc.core.bukkit.utils.player.PlayerHelper;
import br.com.plutomc.core.common.language.Language;
import br.com.plutomc.core.common.member.status.Status;
import br.com.plutomc.core.common.member.status.StatusType;
import br.com.plutomc.core.common.member.status.types.BedwarsCategory;
import br.com.plutomc.core.common.packet.types.staff.Stafflog;
import br.com.plutomc.core.common.server.loadbalancer.server.MinigameState;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Egg;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class GameListener implements Listener {
   private List<Material> dropableItems = Arrays.asList(Material.DIAMOND, Material.EMERALD, Material.GOLD_INGOT, Material.POTION);
   private Map<UUID, Long> playerJoinMap = new HashMap<>();

   @EventHandler
   public void onProjectileLaunch(ProjectileLaunchEvent event) {
      if (event.getEntity() instanceof Egg && event.getEntity().getShooter() instanceof Player) {
         final Egg egg = (Egg)event.getEntity();
         Player player = (Player)egg.getShooter();
         final Island island = GameMain.getInstance().getIslandManager().getIsland(player.getUniqueId());
         if (island != null) {
            final int distance = 40;
            final Location startLocation = egg.getLocation().clone();

            for(Block block : player.getLineOfSight((Set<Material>)null, distance)) {
               Block highestBlockAt = block.getWorld().getHighestBlockAt(block.getLocation());
               if (!this.isPlaceable(highestBlockAt.getLocation())) {
                  player.getInventory().addItem(new ItemStack[]{ItemBuilder.fromStack(player.getItemInHand()).amount(1).build()});
                  player.sendMessage("§cVocê não pode jogar o ovo nessa direção.");
                  event.setCancelled(true);
                  break;
               }
            }

            (new BukkitRunnable() {
                  @Override
                  public void run() {
                     Location eggLocation = egg.getLocation().subtract(0.0, 2.5, 0.0);
                     if (eggLocation.getY() <= 70.0) {
                        egg.remove();
                     }
   
                     if (!egg.isDead() && !GameListener.this.distanceSquared(eggLocation, startLocation, (double)distance)) {
                        if (eggLocation.getBlock().getType() == Material.AIR) {
                           final Location location = eggLocation.getBlock().getLocation().add(0.5, 0.0, 0.5);
                           (new BukkitRunnable() {
                                 @Override
                                 public void run() {
                                    for(int x = -1; x < 1; ++x) {
                                       for(int z = -1; z < 1; ++z) {
                                          Location newLocation = location.clone().add((double)x, 0.0, (double)z);
                                          if (newLocation.getBlock().getType() == Material.AIR) {
                                             GameMain.getInstance()
                                                .getBlockManager()
                                                .setBlockFast(
                                                   newLocation,
                                                   Material.WOOL,
                                                   (byte)island.getIslandColor().getWoolId(),
                                                   place -> GameMain.getInstance().getPlayersBlock().add(place)
                                                );
                                          }
                                       }
                                    }
                                 }
                              })
                              .runTaskLater(ArcadeCommon.getInstance(), 5L);
                        }
                     } else {
                        this.cancel();
                     }
                  }
               })
               .runTaskTimer(ArcadeCommon.getInstance(), 5L, 0L);
         }
      }
   }

   @EventHandler
   public void onGamerLoad(GamerLoadEvent event) {
      Player player = event.getPlayer();
      if (!this.playerJoinMap.containsKey(player.getUniqueId())) {
         Island island = this.getIsland(player.getUniqueId());
         if (island == null && !player.hasPermission("command.admin")) {
            event.setCancelled(true);
            event.setReason("§cO jogo já iniciou!");
         }
      }
   }

   @EventHandler(
      priority = EventPriority.NORMAL,
      ignoreCancelled = true
   )
   public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
      Player player = event.getPlayer();
      Gamer gamer = GameMain.getInstance().getGamerManager().getGamer(player.getUniqueId(), Gamer.class);
      if (gamer != null) {
         Island island = this.getIsland(player.getUniqueId());
         if (island != null) {
            event.setCancelled(true);
            String message;
            if (island.getIslandStatus() != Island.IslandStatus.LOSER) {
               boolean solo = CommonPlugin.getInstance().getServerType().name().contains("SOLO");
               Status status = ArcadeCommon.getInstance().getPlugin().getStatusManager().loadStatus(player.getUniqueId(), StatusType.BEDWARS);
               int level = status.getInteger(BedwarsCategory.BEDWARS_LEVEL);
               message = GameMain.getInstance().createMessage(player, event.getMessage(), island, solo, solo ? !solo : true, level);
               if (!solo) {
                  event.getRecipients().removeIf(p -> !island.getTeam().getPlayerSet().contains(p.getUniqueId()));
               }
            } else {
               event.getRecipients().removeIf(p -> !GameMain.getInstance().hasLose(p.getUniqueId()));
               message = "§7[ESPECTADORES] " + player.getName() + ": " + event.getMessage();
            }

            event.getRecipients().forEach(ps -> ps.sendMessage(message));
         }
      }
   }

   @EventHandler
   public void onInventoryClose(InventoryCloseEvent event) {
      if (!(event.getInventory().getHolder() instanceof MenuHolder)) {
         Player player = (Player)event.getPlayer();
         Gamer gamer = ArcadeCommon.getInstance().getGamerManager().getGamer(player.getUniqueId(), Gamer.class);
         if (gamer != null && gamer.isAlive()) {
            Inventory inventory = event.getInventory();
            PlayerInventory playerInventory = player.getInventory();

            for(ItemStack itemStack : inventory.getContents()) {
               if (itemStack != null
                  && (
                     itemStack.getType() == Material.WOOD_SWORD
                        || itemStack.getType() == Material.SHEARS
                        || itemStack.getType() == gamer.getAxeLevel().getItemStack().getType()
                        || itemStack.getType() == gamer.getPickaxeLevel().getItemStack().getType()
                  )) {
                  inventory.remove(itemStack);
                  playerInventory.addItem(new ItemStack[]{itemStack});
               }
            }

            int swordCount = this.getSwordCount(playerInventory);
            int woodSwordCount = this.getItemCount(player, Material.WOOD_SWORD);
            if (this.getSwordCount(playerInventory) == 0) {
               playerInventory.addItem(
                  new ItemStack[]{
                     new ItemBuilder()
                        .type(Material.WOOD_SWORD)
                        .enchantment(
                           Enchantment.DAMAGE_ALL,
                           GameMain.getInstance().getIslandManager().getIsland(player.getUniqueId()).getUpgradeLevel(IslandUpgrade.SHARPNESS)
                        )
                        .build()
                  }
               );
            }

            if (swordCount != woodSwordCount) {
               for(ItemStack itemStack : playerInventory.getContents()) {
                  if (itemStack != null && itemStack.getType() == Material.WOOD_SWORD) {
                     playerInventory.removeItem(new ItemStack[]{itemStack});
                  }
               }
            }
         }
      }
   }

   @EventHandler
   public void onInventoryClick(InventoryClickEvent event) {
      if (event.getSlotType() == SlotType.CRAFTING || event.getSlotType() == SlotType.ARMOR) {
         event.setCancelled(true);
      }
   }

   @EventHandler(
      priority = EventPriority.HIGH,
      ignoreCancelled = true
   )
   public void onPlayerDropItem(PlayerDropItemEvent event) {
      Player player = event.getPlayer();
      if (player.getAllowFlight()) {
         event.setCancelled(true);
      } else {
         Gamer gamer = ArcadeCommon.getInstance().getGamerManager().getGamer(player.getUniqueId(), Gamer.class);
         ItemStack itemStack = event.getItemDrop().getItemStack();
         if (itemStack.getType().name().contains("SWORD")) {
            if (itemStack.getType() == Material.WOOD_SWORD) {
               event.setCancelled(true);
            } else if (this.getSwordCount(player) == 0) {
               player.getInventory()
                  .setItemInHand(
                     new ItemBuilder()
                        .type(Material.WOOD_SWORD)
                        .enchantment(
                           Enchantment.DAMAGE_ALL,
                           GameMain.getInstance().getIslandManager().getIsland(player.getUniqueId()).getUpgradeLevel(IslandUpgrade.SHARPNESS)
                        )
                        .build()
                  );
               gamer.setSwordLevel(Gamer.SwordLevel.WOOD);
            }
         } else if (!itemStack.getType().name().contains("HELMET")
            && !itemStack.getType().name().contains("CHESTPLATE")
            && !itemStack.getType().name().contains("LEGGINGS")
            && !itemStack.getType().name().contains("BOOTS")
            && !itemStack.getType().name().contains("AXE")
            && !itemStack.getType().name().contains("PICKAXE")
            && !itemStack.getType().name().contains("SHEARS")) {
            if (!gamer.isAlive() || !(player.getFallDistance() > 2.0F) && !(player.getLocation().getY() < 20.0)) {
               if (this.dropableItems.contains(event.getItemDrop().getItemStack().getType())) {
                  for(Block block : player.getLineOfSight((Set<Material>)null, 5)) {
                     Block highestBlockAt = block.getWorld().getHighestBlockAt(block.getLocation());
                     if (highestBlockAt == null || highestBlockAt.getLocation().getY() == 0.0) {
                        event.setCancelled(true);
                        player.sendMessage(Language.getLanguage(player.getUniqueId()).t("bedwars.drop-item.no-block-to-drop"));
                        return;
                     }
                  }
               }
            } else {
               event.setCancelled(true);
               player.sendMessage(Language.getLanguage(player.getUniqueId()).t("bedwars.drop-item.void-drop"));
            }
         } else {
            event.setCancelled(true);
         }
      }
   }

   @EventHandler(
      priority = EventPriority.HIGH,
      ignoreCancelled = true
   )
   public void onPlayerPickupItem(PlayerPickupItemEvent event) {
      Player player = event.getPlayer();
      Gamer gamer = ArcadeCommon.getInstance().getGamerManager().getGamer(player.getUniqueId(), Gamer.class);
      if (!gamer.isAlive()) {
         event.setCancelled(true);
      } else if (event.getItem().getItemStack().getType().toString().contains("SWORD") && this.getItemCount(player, Material.WOOD_SWORD) == 1) {
         for(ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == Material.WOOD_SWORD) {
               player.getInventory().removeItem(new ItemStack[]{item});
               break;
            }
         }

         gamer.setSwordLevel(Gamer.SwordLevel.valueOf(event.getItem().getItemStack().getType().name().replace("_SWORD", "")));
      } else {
         Item item = event.getItem();
         ItemStack itemStack = item.getItemStack();
         Collection<Player> playerList = GameMain.getInstance()
            .getAlivePlayers()
            .stream()
            .filter(g -> g.isAlive() && g.getPlayer() != null && g.getPlayer().getLocation().distance(item.getLocation()) <= 3.0)
            .map(br.com.plutomc.game.engine.gamer.Gamer::getPlayer)
            .collect(Collectors.toList());
         if (playerList.size() > 1 && itemStack.getAmount() > playerList.size()) {
            int itemPerPlayer = itemStack.getAmount() / playerList.size();
            int remeaning = itemStack.getAmount() - itemPerPlayer * playerList.size();

            for(Player target : playerList) {
               target.getInventory().addItem(new ItemStack[]{ItemBuilder.fromStack(itemStack).amount(itemPerPlayer).build()});

               ProtocolLibrary.getProtocolManager()
                       .sendServerPacket(
                               target, new PacketBuilder(Server.COLLECT).writeInteger(0, item.getEntityId()).writeInteger(1, target.getEntityId()).build()
                       );
            }

            item.remove();
            event.setCancelled(true);
            if (remeaning > 0) {
               player.getInventory().addItem(new ItemStack[]{ItemBuilder.fromStack(itemStack).amount(remeaning).build()});
            }
         }
      }
   }

   @EventHandler(
      priority = EventPriority.HIGH,
      ignoreCancelled = true
   )
   public void onEntityDamage(EntityDamageEvent event) {
      if (event.getEntity() instanceof Player) {
         Player player = (Player)event.getEntity();
         Gamer gamer = ArcadeCommon.getInstance().getGamerManager().getGamer(player.getUniqueId(), Gamer.class);
         if (!gamer.isAlive()) {
            event.setCancelled(true);
            return;
         }
      }
   }

   @EventHandler(
      priority = EventPriority.HIGH,
      ignoreCancelled = true
   )
   public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
      if (event.getDamager() instanceof IronGolem) {
         event.setDamage(6.0);
      }

      if (event.getEntity() instanceof Fireball) {
         event.setCancelled(true);
      } else {
         if (event.getEntity() instanceof Player) {
            Player player = (Player)event.getEntity();
            Gamer gamer = ArcadeCommon.getInstance().getGamerManager().getGamer(player.getUniqueId(), Gamer.class);
            if (!gamer.isAlive()) {
               event.setCancelled(true);
               return;
            }

            for(ItemStack itemStack : player.getInventory().getArmorContents()) {
               itemStack.setDurability((short)0);
            }

            Player damager = null;
            if (event.getDamager() instanceof Player) {
               damager = (Player)event.getDamager();
            } else if (event.getDamager() instanceof Projectile) {
               Projectile projectile = (Projectile)event.getDamager();
               if (projectile.getShooter() instanceof Player) {
                  damager = (Player)projectile.getShooter();
               }
            }

            if (!(damager instanceof Player)) {
               return;
            }

            Gamer damagerGamer = ArcadeCommon.getInstance().getGamerManager().getGamer(damager.getUniqueId(), Gamer.class);
            if (!damagerGamer.isAlive()) {
               event.setCancelled(true);
               return;
            }

            Island island = this.getIsland(player.getUniqueId());
            Island islandDamager = this.getIsland(damager.getUniqueId());
            if (island.getIslandColor() == islandDamager.getIslandColor()) {
               event.setCancelled(true);
            }
         }
      }
   }

   @EventHandler
   public void onPlayerMoveUpdate(PlayerMoveUpdateEvent event) {
      Player player = event.getPlayer();
      Gamer gamer = ArcadeCommon.getInstance().getGamerManager().getGamer(player.getUniqueId(), Gamer.class);
      Island island = this.getIsland(player.getUniqueId());
      if (gamer.isAlive() && event.getTo().getY() <= GameMain.getInstance().getMinimunY()) {
         boolean finalKill = island.getIslandStatus() != Island.IslandStatus.ALIVE;
         this.handleDeath(gamer, player);
         if (island.getIslandStatus() == Island.IslandStatus.LOSER) {
            this.spectate(player);
            return;
         }

         if (finalKill) {
            island.checkLose();
            this.spectate(player);
         } else {
            this.respawn(player);
         }
      }

      if (player.hasMetadata("player-target")) {
         if (gamer.isAlive()) {
            MetadataValue orElse = player.getMetadata("player-target").stream().findFirst().orElse(null);
            UUID uuid = UUID.fromString(orElse.asString());
            Player target = Bukkit.getPlayer(uuid);
            if (target == null) {
               return;
            }

            PlayerHelper.actionbar(
               player,
               "§aRastreando: §f"
                  + target.getName()
                  + " §7("
                  + CommonConst.DECIMAL_FORMAT.format(target.getLocation().distance(player.getLocation()))
                  + " blocos)"
            );
            player.setCompassTarget(target.getLocation());
         } else {
            player.removeMetadata("player-target", ArcadeCommon.getInstance());
         }
      }
   }

   @EventHandler
   public void onIslandLose(IslandLoseEvent event) {
      GameMain.getInstance().checkWinner();
   }

   @EventHandler
   public void onPlayerDeath(PlayerDeathEvent event) {
      event.setDeathMessage(null);
      Player player = event.getEntity();
      Gamer gamer = ArcadeCommon.getInstance().getGamerManager().getGamer(player.getUniqueId(), Gamer.class);
      if (!gamer.isSpectator()) {
         if (gamer.isAlive()) {
            boolean finalKill = this.getIsland(player.getUniqueId()).getIslandStatus() == Island.IslandStatus.BED_BROKEN;
            if (player.getKiller() instanceof Player) {
               Player killer = player.getKiller();

               for(ItemStack itemStack : event.getDrops()) {
                  if (this.dropableItems.contains(itemStack.getType())) {
                     killer.getInventory().addItem(new ItemStack[]{itemStack});
                  }
               }

               Bukkit.getPluginManager().callEvent(new PlayerKillPlayerEvent(player, killer, finalKill));
            }

            this.handleDeath(gamer, player);
            event.getDrops().clear();
            event.setDroppedExp(0);
            this.getIsland(player.getUniqueId()).checkLose();
            player.removeMetadata("player-armor", ArcadeCommon.getInstance());
            if (finalKill) {
               this.spectate(player);
            } else {
               this.respawn(player);
            }
         }
      }
   }

   private void handleDeath(Gamer gamer, Player player) {
      boolean finalKill = this.getIsland(player.getUniqueId()).getIslandStatus() == Island.IslandStatus.BED_BROKEN;
      player.setHealth(20.0);
      player.setMaxHealth(20.0);
      player.setLevel(0);
      player.setExp(0.0F);
      player.getInventory().clear();
      player.getInventory().setArmorContents(new ItemStack[4]);
      gamer.setAlive(false);
      gamer.setSwordLevel(Gamer.SwordLevel.WOOD);
      if (player.getKiller() instanceof Player) {
         Bukkit.getPluginManager().callEvent(new PlayerKillPlayerEvent(player, player.getKiller(), finalKill));
      }

      if (gamer.getPickaxeLevel().ordinal() >= Gamer.PickaxeLevel.values()[2].ordinal()) {
         gamer.setPickaxeLevel(gamer.getPickaxeLevel().getPrevious());
      }

      if (gamer.getAxeLevel().ordinal() >= Gamer.AxeLevel.values()[2].ordinal()) {
         gamer.setAxeLevel(gamer.getAxeLevel().getPrevious());
      }

      this.broadcastDeath(player, player.getKiller(), finalKill);
   }

   public void broadcastDeath(Player player, Player killer, boolean finalKill) {
      Island island = this.getIsland(player.getUniqueId());
      StringBuilder stringBuilder = new StringBuilder();
      if (killer == null) {
         stringBuilder.append("§7" + island.getIslandColor().getColor() + player.getName() + " §7foi morto.");
      } else {
         Island killerIsland = this.getIsland(killer.getUniqueId());
         stringBuilder.append(
            "§7"
               + island.getIslandColor().getColor()
               + player.getName()
               + " §7foi morto por "
               + killerIsland.getIslandColor().getColor()
               + killer.getName()
               + "§7."
         );
      }

      if (finalKill) {
         stringBuilder.append(" ").append("§b§lFINAL KILL");
      }

      Bukkit.broadcastMessage(stringBuilder.toString().trim());
   }

   @EventHandler(
      priority = EventPriority.HIGHEST
   )
   public void onPlayerJoin(PlayerJoinEvent event) {
      Player player = event.getPlayer();
      if (this.playerJoinMap.containsKey(player.getUniqueId())) {
         this.playerJoinMap.remove(player.getUniqueId());
      }

      Gamer gamer = ArcadeCommon.getInstance().getGamerManager().getGamer(player.getUniqueId(), Gamer.class);
      Island island = this.getIsland(player.getUniqueId());
      if (island != null && island.getIslandStatus() == Island.IslandStatus.ALIVE && !gamer.isSpectator()) {
         this.respawn(player);
      } else {
         this.spectate(player);
      }
   }

   @EventHandler(
      priority = EventPriority.NORMAL
   )
   public void onPlayerQuit(PlayerQuitEvent event) {
      Player player = event.getPlayer();
      Gamer gamer = GameMain.getInstance().getGamerManager().getGamer(player.getUniqueId(), Gamer.class);
      Island island = this.getIsland(player.getUniqueId());
      if (island != null && ArcadeCommon.getInstance().getState() == MinigameState.GAMETIME) {
         if (gamer.isAlive()) {
            if (island.getIslandStatus() == Island.IslandStatus.BED_BROKEN) {
               this.broadcastDeath(player, null, true);
               gamer.setSpectator(true);
            } else {
               this.playerJoinMap.put(player.getUniqueId(), System.currentTimeMillis());
               Bukkit.broadcastMessage("§7" + island.getIslandColor().getColor() + player.getName() + " §7desconectou.");
            }
         }

         island.checkLose();
      }
   }

   @EventHandler
   public void onUpdate(UpdateEvent event) {
      if (event.getType() == UpdateEvent.UpdateType.SECOND) {
         for(Entry<UUID, Long> next : ImmutableList.copyOf(this.playerJoinMap.entrySet())) {
            if (next.getValue() + 45000L < System.currentTimeMillis()) {
               this.playerJoinMap.remove(next.getKey());
               Gamer gamer = ArcadeCommon.getInstance().getGamerManager().getGamer(next.getKey(), Gamer.class);
               gamer.setAlive(false);
               gamer.setSpectator(true);
               Island island = this.getIsland(gamer.getUniqueId());
               if (island != null) {
                  if (island.getIslandStatus() == Island.IslandStatus.ALIVE
                     && island.getTeam()
                           .getPlayerSet()
                           .stream()
                           .map(id -> ArcadeCommon.getInstance().getGamerManager().getGamer(id, Gamer.class))
                           .filter(g -> g.isAlive())
                           .count()
                        == 0L) {
                     island.handleBreakBed(null);
                  }

                  island.checkLose();
               }

               Bukkit.broadcastMessage("§7" + island.getIslandColor().getColor() + gamer.getPlayerName() + " §7foi morto. §b§lFINAL KILL");
            }
         }
      }
   }

   @EventHandler
   public void onPlayerInteract(PlayerInteractEvent event) {
      Gamer gamer = ArcadeCommon.getInstance().getGamerManager().getGamer(event.getPlayer().getUniqueId(), Gamer.class);
      if (!gamer.isAlive()) {
         event.setCancelled(true);
      } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
         Block block = event.getClickedBlock();
         if (block.getType() == Material.FURNACE
            || block.getType() == Material.ANVIL
            || block.getType() == Material.ENCHANTMENT_TABLE
            || block.getType() == Material.WORKBENCH) {
            event.setCancelled(true);
         } else if (block.getType() == Material.CHEST) {
            Island playerIsland = this.getIsland(event.getPlayer().getUniqueId());
            if (playerIsland == null) {
               event.setCancelled(true);
            } else {
               if (block.hasMetadata("chest-island")) {
                  MetadataValue metadataValue = block.getMetadata("chest-island").stream().findFirst().orElse(null);
                  Island island = this.getIsland((IslandColor)metadataValue.value());
                  if (playerIsland.getIslandColor() != island.getIslandColor() && island.getIslandStatus() == Island.IslandStatus.ALIVE) {
                     event.setCancelled(true);
                     event.getPlayer().sendMessage("§cVocê não pode abrir o baú do inimigo enquanto ele ainda estiver vivo.");
                  }
               }
            }
         }
      }
   }

   @EventHandler
   public void onPlayerBedEnter(PlayerBedEnterEvent event) {
      event.setCancelled(true);
   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onBlockBreak(BlockBreakEvent event) {
      Player player = event.getPlayer();
      if (player.getAllowFlight()) {
         event.setCancelled(true);
      } else {
         Island playerIsland = this.getIsland(player.getUniqueId());
         if (playerIsland == null) {
            event.setCancelled(true);
         } else if (event.getBlock().getType() == Material.BED_BLOCK) {
            double distance = player.getLocation().distance(event.getBlock().getLocation());
            if (distance > 12.0) {
               CommonPlugin.getInstance()
                  .getServerData()
                  .sendPacket(new Stafflog("O jogador " + player.getName() + " quebrou uma cama a " + distance + " blocos de distancia"));
               event.setCancelled(true);
            } else {
               if (event.getBlock().hasMetadata("bed-island")) {
                  MetadataValue metadataValue = event.getBlock().getMetadata("bed-island").stream().findFirst().orElse(null);
                  Island island = GameMain.getInstance().getIslandManager().getIsland((IslandColor)metadataValue.value());
                  if (playerIsland.getIslandColor() != island.getIslandColor()) {
                     island.handleBreakBed(event.getPlayer());

                     for(Location location : GameMain.getInstance().getNearestBlocksByMaterial(event.getBlock().getLocation(), Material.BED_BLOCK, 2)) {
                        location.getBlock().setType(Material.AIR);
                     }

                     ArcadeCommon.getInstance().getGamerManager().getGamer(event.getPlayer().getUniqueId(), Gamer.class).addBedBroken();
                  } else {
                     event.setCancelled(true);
                  }
               }
            }
         } else {
            if (GameMain.getInstance().getPlayersBlock().contains(event.getBlock().getLocation())) {
               GameMain.getInstance().getPlayersBlock().remove(event.getBlock().getLocation());
            } else {
               event.setCancelled(true);
               player.sendMessage(Language.getLanguage(player.getUniqueId()).t("bedwars-cant-break-this-block"));
            }

            if (player.getItemInHand() != null && player.getItemInHand().getType().name().contains("AXE")) {
               player.getItemInHand().setDurability((short)0);
               player.updateInventory();
            }
         }
      }
   }

   @EventHandler(
      priority = EventPriority.HIGH,
      ignoreCancelled = true
   )
   public void onBlockPlace(final BlockPlaceEvent event) {
      if (!this.isPlaceable(event.getBlock().getLocation())) {
         event.setCancelled(true);
         event.getPlayer().sendMessage(Language.getLanguage(event.getPlayer().getUniqueId()).t("bedwars.block-place.can-not-place-here"));
      } else if (event.getBlock().getType() == Material.SPONGE) {
         (new BukkitRunnable() {
            @Override
            public void run() {
               event.getBlock().setType(Material.AIR);
            }
         }).runTaskLater(ArcadeCommon.getInstance(), 3L);
      } else if (event.getBlock().getType() == Material.CHEST) {
         event.setCancelled(true);
         int amount = Math.max(event.getItemInHand().getAmount() - 1, 0);
         if (amount == 0) {
            event.getPlayer().setItemInHand(null);
         } else {
            event.getItemInHand().setAmount(amount);
         }

         if (GameMain.getInstance().getTowerSchematic() == null) {
            event.getPlayer().sendMessage("§cNão foi possível spawnar a construção.");
         } else {
            (new BukkitRunnable() {
               @Override
               public void run() {
                  BukkitCommon.getInstance().getBlockManager().spawn(event.getBlock().getLocation(), GameMain.getInstance().getTowerSchematic());
               }
            }).runTaskLater(ArcadeCommon.getInstance(), 3L);
         }
      } else {
         GameMain.getInstance().getPlayersBlock().add(event.getBlock().getLocation());
      }
   }

   public boolean distanceSquared(Location locatioTo, Location locationFrom, double radius) {
      double distX = locatioTo.getX() - locationFrom.getX();
      double distZ = locatioTo.getZ() - locationFrom.getZ();
      double distance = distX * distX + distZ * distZ;
      return distance > radius * radius;
   }

   @EventHandler
   public void onBlockPhysics(BlockPhysicsEvent event) {
      if (event.getBlock().getType() == Material.BED_BLOCK) {
         event.setCancelled(true);
      }
   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onPlayerAdmin(PlayerAdminEvent event) {
      Island island = this.getIsland(event.getPlayer().getUniqueId());
      if (island != null) {
         island.checkLose();
      }

      GameMain.getInstance().checkWinner();
   }

   @EventHandler
   public void onEntityDeath(EntityDeathEvent event) {
      if (!(event.getEntity() instanceof Player)) {
         event.setDroppedExp(0);
         event.getDrops().clear();
      }
   }

   private void spectate(Player player) {
      if (player.getLocation().getY() <= 20.0) {
         player.teleport(BukkitCommon.getInstance().getLocationManager().getLocation("central"));
      }

      GamerHelper.handleSpectate(player);
      ArcadeCommon.getInstance().getGamerManager().getGamer(player.getUniqueId(), Gamer.class).setSpectator(true);
      ArcadeCommon.getInstance().getVanishManager().updateVanishToPlayer(player);
      player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20, 254));
   }

   private void respawn(final Player player) {
      final Language language = Language.getLanguage(player.getUniqueId());
      final Gamer gamer = ArcadeCommon.getInstance().getGamerManager().getGamer(player.getUniqueId(), Gamer.class);
      final Island island = this.getIsland(player.getUniqueId());
      if (player.getLocation().getY() < 20.0) {
         player.teleport(ArcadeCommon.getInstance().getLocationManager().getLocation("central"));
      } else {
         player.teleport(player.getLocation().add(0.0, 2.5, 0.0));
      }

      player.getInventory().clear();
      player.getInventory().setArmorContents(new ItemStack[4]);
      player.setAllowFlight(true);
      player.setFlying(true);
      player.setMetadata("invencibility", ArcadeCommon.getInstance().createMeta(Long.valueOf(System.currentTimeMillis() + 5000L)));
      player.getActivePotionEffects().forEach(potion -> player.removePotionEffect(potion.getType()));
      player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 140, 2));
      player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20, 254));
      GamerHelper.hidePlayer(player);
      (new BukkitRunnable() {
            int time = 5;
   
            @Override
            public void run() {
               if (player != null && player.isOnline()) {
                  if (this.time == 0) {
                     GameMain.getInstance().getVanishManager().showPlayer(player);
                     player.setFallDistance(-1.0F);
                     player.teleport(island.getSpawnLocation().getAsLocation());
                     gamer.setAlive(true);
                     GamerHelper.handlePlayerToGame(player);
                     PlayerHelper.title(player, language.t("bedwars-title-respawn"), language.t("bedwars-subtitle-respawn"), 10, 20, 10);
                     player.sendMessage(language.t("bedwars.message.respawn"));
                     GamerHelper.setPlayerProtection(player, 5);
                     this.cancel();
                  } else {
                     PlayerHelper.title(
                        player, language.t("bedwars-title-you-are-dead"), language.t("bedwars-subtitle-you-are-dead", "%time%", "" + this.time), 0, 20, 20
                     );
                     player.sendMessage(language.t("bedwars.message.respawning", "%time%", "" + this.time));
                  }
   
                  --this.time;
               } else {
                  this.cancel();
               }
            }
         })
         .runTaskTimer(ArcadeCommon.getInstance(), 20L, 20L);
   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onPlayerBucketEmpty(final PlayerBucketEmptyEvent event) {
      (new BukkitRunnable() {
         @Override
         public void run() {
            event.getPlayer().getInventory().remove(Material.BUCKET);
         }
      }).runTaskLater(ArcadeCommon.getInstance(), 3L);
   }

   @EventHandler(
      priority = EventPriority.HIGHEST
   )
   public void onPlayerBucketEmpty(PlayerBucketFillEvent event) {
      event.setCancelled(true);
   }

   private int getSwordCount(Player player) {
      return this.getSwordCount(player.getInventory());
   }

   private int getSwordCount(Inventory playerInventory) {
      int count = 0;

      for(int slot = 0; slot < playerInventory.getSize(); ++slot) {
         ItemStack itemStack = playerInventory.getContents()[slot];
         if (itemStack != null && itemStack.getType().toString().contains("SWORD")) {
            ++count;
         }
      }

      return count;
   }

   private int getItemCount(Player player, Material material) {
      int count = 0;

      for(int slot = 0; slot < player.getInventory().getSize(); ++slot) {
         ItemStack itemStack = player.getInventory().getContents()[slot];
         if (itemStack != null && itemStack.getType() == material) {
            ++count;
         }
      }

      return count;
   }

   public boolean isPlaceable(Location location) {
      if (location.getY() >= GameMain.getInstance().getMaxHeight()) {
         return false;
      } else if (GameMain.getInstance()
         .getGeneratorManager()
         .getGenerators()
         .stream()
         .filter(generator -> !this.distanceSquared(generator.getLocation(), location, 3.0) && location.getY() < generator.getLocation().getY() + 6.0)
         .findFirst()
         .isPresent()) {
         return false;
      } else {
         Optional<Generator> optionalGenerator = GameMain.getInstance().getIslandManager().getClosestGenerator(location);
         return !(location.distance(optionalGenerator.get().getLocation()) <= GameMain.getInstance().getMinimunDistanceToPlaceBlocks());
      }
   }

   public Island getIsland(IslandColor islandColor) {
      return GameMain.getInstance().getIslandManager().getIsland(islandColor);
   }

   public Island getIsland(UUID playerId) {
      return GameMain.getInstance().getIslandManager().getIsland(playerId);
   }
}

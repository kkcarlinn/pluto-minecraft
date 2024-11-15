package br.com.plutomc.game.bedwars.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import br.com.plutomc.core.common.CommonPlugin;
import br.com.plutomc.game.engine.ArcadeCommon;
import br.com.plutomc.game.bedwars.GameMain;
import br.com.plutomc.game.bedwars.event.PlayerBoughtItemEvent;
import br.com.plutomc.game.bedwars.event.PlayerSpectateEvent;
import br.com.plutomc.game.bedwars.gamer.Gamer;
import br.com.plutomc.game.bedwars.island.Island;
import br.com.plutomc.game.bedwars.island.IslandUpgrade;
import br.com.plutomc.game.bedwars.menu.SpectatorInventory;
import br.com.plutomc.game.bedwars.store.ShopCategory;
import br.com.plutomc.core.bukkit.utils.item.ActionItemStack;
import br.com.plutomc.core.bukkit.utils.item.ItemBuilder;
import br.com.plutomc.core.common.account.status.Status;
import br.com.plutomc.core.common.account.status.StatusType;
import br.com.plutomc.core.common.account.status.types.BedwarsCategory;
import br.com.plutomc.core.common.server.ServerType;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Criterias;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

public class GamerHelper {
   public static final ActionItemStack PLAYERS = new ActionItemStack(
      new ItemBuilder().name("§aTeleportador").type(Material.COMPASS).build(), new ActionItemStack.Interact() {
         @Override
         public boolean onInteract(Player player, Entity entity, Block block, ItemStack item, ActionItemStack.ActionType action) {
            new SpectatorInventory(player);
            return false;
         }
      }
   );
   public static final ActionItemStack PLAY_AGAIN = new ActionItemStack(
      new ItemBuilder().name("§aJogar novamente").type(Material.PAPER).build(), new ActionItemStack.Interact() {
         @Override
         public boolean onInteract(Player player, Entity entity, Block block, ItemStack item, ActionItemStack.ActionType action) {
            ArcadeCommon.getInstance().sendPlayerToServer(player, new ServerType[]{CommonPlugin.getInstance().getServerType()});
            return false;
         }
      }
   );
   public static final ActionItemStack LOBBY = new ActionItemStack(
      new ItemBuilder().name("§aVoltar ao lobby").type(Material.BED).build(), new ActionItemStack.Interact() {
         @Override
         public boolean onInteract(Player player, Entity entity, Block block, ItemStack item, ActionItemStack.ActionType action) {
            ArcadeCommon.getInstance().sendPlayerToServer(player, new ServerType[]{CommonPlugin.getInstance().getServerType().getServerLobby()});
            return false;
         }
      }
   );

   public static void handlePlayerToSpawn(Player player) {
      handlePlayer(player);
      player.getInventory().setItem(8, LOBBY.getItemStack());
   }

   public static boolean isPlayerProtection(Player player) {
      if (player.hasMetadata("bed-island")) {
         MetadataValue metadataValue = player.getMetadata("bed-island").stream().findFirst().orElse(null);
         if (metadataValue.asLong() > System.currentTimeMillis()) {
            return true;
         }

         metadataValue.invalidate();
      }

      return false;
   }

   public static void setPlayerProtection(Player player, int seconds) {
      player.setMetadata("bed-island", ArcadeCommon.getInstance().createMeta(Long.valueOf(System.currentTimeMillis() + (long)(seconds * 1000))));
   }

   public static void removePlayerProtection(Player player) {
      player.removeMetadata("bed-island", ArcadeCommon.getInstance());
   }

   public static void handlePlayerToGame(Player player) {
      Island island = GameMain.getInstance().getIslandManager().getIsland(player.getUniqueId());
      if (island != null) {
         Gamer gamer = ArcadeCommon.getInstance().getGamerManager().getGamer(player.getUniqueId(), Gamer.class);
         handlePlayer(player);
         if (island.hasUpgrade(IslandUpgrade.HASTE)) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, 199980, island.getUpgradeLevel(IslandUpgrade.HASTE) - 1));
         }

         player.getInventory()
            .setItem(
               0,
               new ItemBuilder()
                  .type(Material.valueOf(gamer.getSwordLevel().name() + "_SWORD"))
                  .enchantment(Enchantment.DAMAGE_ALL, island.getUpgradeLevel(IslandUpgrade.SHARPNESS))
                  .build()
            );
         if (gamer.getAxeLevel() != Gamer.AxeLevel.NONE) {
            player.getInventory().addItem(new ItemStack[]{gamer.getAxeLevel().getItemStack()});
         }

         if (gamer.getPickaxeLevel() != Gamer.PickaxeLevel.NONE) {
            player.getInventory().addItem(new ItemStack[]{gamer.getPickaxeLevel().getItemStack()});
         }

         if (gamer.isShears()) {
            player.getInventory().addItem(new ItemStack[]{new ItemBuilder().type(Material.SHEARS).build()});
         }

         handleArmor(player);
         handleHeart(player);
         Status status = CommonPlugin.getInstance().getStatusManager().loadStatus(player.getUniqueId(), StatusType.BEDWARS);
         if (player != null) {
            updatePoints(player, status);
         }
      }
   }

   private static void handleHeart(Player player) {
      Scoreboard scoreboard = player.getScoreboard();
      if (scoreboard.getObjective("showhealth") == null) {
         Objective objective = scoreboard.registerNewObjective("showhealth", Criterias.HEALTH);
         objective.setDisplaySlot(DisplaySlot.BELOW_NAME);
         objective.setDisplayName(ChatColor.DARK_RED + "♥");
      }
   }

   public static void updatePoints(Player player, Status status) {
      player.setExp(0.0F);
      player.setTotalExperience(0);
      player.setLevel(status.getInteger(BedwarsCategory.BEDWARS_LEVEL));
      player.setExp((float)(status.getInteger(BedwarsCategory.BEDWARS_POINTS) / GameMain.getInstance().getMaxPoints(player.getLevel())));
   }

   public static void handleSpectate(final Player player) {
      Bukkit.getPluginManager().callEvent(new PlayerSpectateEvent(player));
      (new BukkitRunnable() {
         @Override
         public void run() {
            GamerHelper.handlePlayer(player);
            player.getInventory().setItem(0, GamerHelper.PLAYERS.getItemStack());
            player.getInventory().setItem(7, GamerHelper.PLAY_AGAIN.getItemStack());
            player.getInventory().setItem(8, GamerHelper.LOBBY.getItemStack());
            player.setGameMode(GameMode.ADVENTURE);
            player.setAllowFlight(true);
            player.setFlying(true);
            player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 199980, 2));
            GamerHelper.hidePlayer(player);
         }
      }).runTaskLater(ArcadeCommon.getInstance(), 3L);
   }

   public static void handlePlayer(Player player) {
      player.setFallDistance(-1.0F);
      player.setHealth(20.0);
      player.setMaxHealth(20.0);
      player.setFoodLevel(20);
      player.setLevel(0);
      player.setExp(0.0F);
      player.setAllowFlight(false);
      player.setFlying(false);
      player.setGameMode(GameMode.SURVIVAL);
      player.getInventory().clear();
      player.getInventory().setArmorContents(new ItemStack[4]);
      player.getActivePotionEffects().forEach(potion -> player.removePotionEffect(potion.getType()));
   }

   private static String getYaw(Location location) {
      float yaw = location.getYaw();
      if (yaw < 0.0F) {
         yaw += 360.0F;
      }

      if (yaw >= 315.0F || yaw < 45.0F) {
         return "S";
      } else if (yaw < 135.0F) {
         return "W";
      } else if (yaw < 225.0F) {
         return "N";
      } else {
         return yaw < 315.0F ? "E" : "N";
      }
   }

   public static Location forwardLocationByPlayerRotation(Location locationToRotate, Location location, int blocks) {
      String var3 = getYaw(locationToRotate);
      switch(var3) {
         case "N":
            return location.clone().add(0.0, 0.0, (double)(-blocks));
         case "W":
            return location.clone().add((double)(-blocks), 0.0, 0.0);
         case "S":
            return location.clone().add(0.0, 0.0, (double)blocks);
         case "E":
            return location.clone().add((double)blocks, 0.0, 0.0);
         default:
            return location.clone();
      }
   }

   public static List<Player> getPlayersNear(Location location, double radius) {
      return Bukkit.getOnlinePlayers().stream().filter(player -> location.distance(player.getLocation()) <= radius).collect(Collectors.toList());
   }

   public static Location forwardLocationByPlayerRotation(Player player, Location location, int blocks) {
      return forwardLocationByPlayerRotation(player.getLocation(), location, blocks);
   }

   public static List<Block> getNearbyBlocks(Location location, int radius) {
      List<Block> blocks = new ArrayList<>();

      for(int x = location.getBlockX() - radius; x <= location.getBlockX() + radius; ++x) {
         for(int y = location.getBlockY() - radius; y <= location.getBlockY() + radius; ++y) {
            for(int z = location.getBlockZ() - radius; z <= location.getBlockZ() + radius; ++z) {
               blocks.add(location.getWorld().getBlockAt(x, y, z));
            }
         }
      }

      return blocks;
   }

   public static Player getMoreNearbyPlayers(Location location, double radius) {
      Player nearby = null;

      for(Player player : getPlayersNear(location, radius)) {
         if (nearby == null) {
            nearby = player;
         } else if (nearby.getLocation().distance(location) > player.getLocation().distance(location)) {
            nearby = player;
         }
      }

      return nearby;
   }

   public static void handleRemoveArmor(Player player) {
      player.getInventory().setHelmet(null);
      player.getInventory().setChestplate(null);
      player.getInventory().setLeggings(null);
      player.getInventory().setBoots(null);
   }

   public static void handleArmor(Player player) {
      Island island = GameMain.getInstance().getIslandManager().getIsland(player.getUniqueId());
      Gamer gamer = GameMain.getInstance().getGamerManager().getGamer(player.getUniqueId(), Gamer.class);
      if (island != null) {
         player.getInventory()
            .setHelmet(
               new ItemBuilder()
                  .type(Material.LEATHER_HELMET)
                  .color(island.getIslandColor().getColorEquivalent())
                  .enchantment(Enchantment.PROTECTION_ENVIRONMENTAL, island.getUpgradeLevel(IslandUpgrade.ARMOR_REINFORCEMENT))
                  .build()
            );
         player.getInventory()
            .setChestplate(
               new ItemBuilder()
                  .type(Material.LEATHER_CHESTPLATE)
                  .color(island.getIslandColor().getColorEquivalent())
                  .enchantment(Enchantment.PROTECTION_ENVIRONMENTAL, island.getUpgradeLevel(IslandUpgrade.ARMOR_REINFORCEMENT))
                  .build()
            );
         player.getInventory()
            .setLeggings(
               new ItemBuilder()
                  .type(Material.valueOf(gamer.getArmorLevel().name() + "_LEGGINGS"))
                  .color(island.getIslandColor().getColorEquivalent())
                  .enchantment(Enchantment.PROTECTION_ENVIRONMENTAL, island.getUpgradeLevel(IslandUpgrade.ARMOR_REINFORCEMENT))
                  .build()
            );
         player.getInventory()
            .setBoots(
               new ItemBuilder()
                  .type(Material.valueOf(gamer.getArmorLevel().name() + "_BOOTS"))
                  .color(island.getIslandColor().getColorEquivalent())
                  .enchantment(Enchantment.PROTECTION_ENVIRONMENTAL, island.getUpgradeLevel(IslandUpgrade.ARMOR_REINFORCEMENT))
                  .build()
            );
      }
   }

   public static void hidePlayer(Player player) {
      GameMain.getInstance().getVanishManager().setPlayerVanishToGroup(player, CommonPlugin.getInstance().getPluginInfo().getHighGroup());
   }

   public static void buyItem(Player player, ShopCategory.ShopItem shopItem) {
      PlayerBoughtItemEvent playerBoughtItemEvent = new PlayerBoughtItemEvent(
         player,
         new ItemBuilder()
            .type(shopItem.getStack().getType())
            .durability(shopItem.getStack().getDurability())
            .amount(shopItem.getStack().getAmount())
            .potion(ItemBuilder.fromStack(shopItem.getStack()).getPotions())
            .enchantment(shopItem.getStack().getEnchantments())
            .build()
      );
      Bukkit.getPluginManager().callEvent(playerBoughtItemEvent);
      if (!playerBoughtItemEvent.isCancelled()) {
         player.getInventory()
            .removeItem(new ItemStack[]{new ItemBuilder().type(shopItem.getPrice().getMaterial()).amount(shopItem.getPrice().getAmount()).build()});
         if (playerBoughtItemEvent.getItemStack() != null) {
            player.getInventory().addItem(new ItemStack[]{playerBoughtItemEvent.getItemStack()});
         }

         player.playSound(player.getLocation(), Sound.NOTE_PLING, 1.0F, 1.0F);
      }
   }
}

package br.com.plutomc.game.bedwars.scheduler;

import br.com.plutomc.game.bedwars.GameMain;
import br.com.plutomc.core.common.CommonPlugin;
import br.com.plutomc.core.bukkit.event.player.PlayerAdminEvent;
import br.com.plutomc.core.bukkit.event.player.PlayerMoveUpdateEvent;
import br.com.plutomc.game.engine.ArcadeCommon;
import br.com.plutomc.game.bedwars.gamer.Gamer;
import br.com.plutomc.game.engine.scheduler.Scheduler;
import br.com.plutomc.core.bukkit.account.BukkitAccount;
import br.com.plutomc.core.bukkit.utils.item.ActionItemStack;
import br.com.plutomc.core.bukkit.utils.item.ItemBuilder;
import br.com.plutomc.core.bukkit.utils.player.PlayerHelper;
import br.com.plutomc.core.common.language.Language;
import br.com.plutomc.core.common.account.Account;
import br.com.plutomc.core.common.server.ServerType;
import br.com.plutomc.core.common.server.loadbalancer.server.MinigameState;
import br.com.plutomc.core.common.utils.string.StringFormat;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

public class WaitingScheduler implements Scheduler, Listener {
   private static final ActionItemStack ACTION_ITEM_STACK = new ActionItemStack(
      new ItemBuilder().name("§aRetornar ao Lobby").type(Material.BED).build(), new ActionItemStack.Interact() {
         @Override
         public boolean onInteract(Player player, Entity entity, Block block, ItemStack item, ActionItemStack.ActionType action) {
            ArcadeCommon.getInstance().sendPlayerToServer(player, new ServerType[]{CommonPlugin.getInstance().getServerType().getServerLobby(), ServerType.LOBBY});
            return false;
         }
      }
   );
   private boolean timeWasReduced;

   @EventHandler
   public void onPlayerJoin(PlayerJoinEvent event) {
      Player player = event.getPlayer();
      Gamer gamer = ArcadeCommon.getInstance().getGamerManager().getGamer(player.getUniqueId(), Gamer.class);
      Account account = CommonPlugin.getInstance().getAccountManager().getAccount(player.getUniqueId());
      player.teleport(ArcadeCommon.getInstance().getLocationManager().getLocation("spawn"));
      player.setHealth(20.0);
      player.setMaxHealth(20.0);
      player.setFoodLevel(20);
      player.setLevel(0);
      player.setExp(0.0F);
      player.setAllowFlight(false);
      player.setFlying(false);
      player.setGameMode(GameMode.ADVENTURE);
      player.getInventory().clear();
      player.getInventory().setArmorContents(new ItemStack[4]);
      player.getActivePotionEffects().forEach(potion -> player.removePotionEffect(potion.getType()));
      player.getInventory().setItem(8, ACTION_ITEM_STACK.getItemStack());
      gamer.setAlive(true);
      gamer.setSpectator(false);
      if (player.hasPermission("command.admin") && account.getAccountConfiguration().isAdminOnJoin()) {
         player.setMetadata("admin", new FixedMetadataValue(ArcadeCommon.getInstance(), true));
      } else {
         this.broadcast(account.getTag().getRealPrefix(), event.getPlayer().getName(), false);
      }
   }

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void onPlayerQuit(final PlayerQuitEvent event) {
      final Account account = CommonPlugin.getInstance().getAccountManager().getAccount(event.getPlayer().getUniqueId());
      if (!ArcadeCommon.getInstance().getVanishManager().isPlayerInAdmin(event.getPlayer())) {
         (new BukkitRunnable() {
            @Override
            public void run() {
               WaitingScheduler.this.broadcast(account.getTag().getRealPrefix(), event.getPlayer().getName(), true);
            }
         }).runTaskLater(ArcadeCommon.getInstance(), 7L);
      }

      if (event.getPlayer().hasMetadata("admin")) {
         event.getPlayer().removeMetadata("admin", ArcadeCommon.getInstance());
      }
   }

   @EventHandler(
      priority = EventPriority.HIGHEST
   )
   public void onPlayerAdmin(PlayerAdminEvent event) {
      Player player = event.getPlayer();
      Account account = CommonPlugin.getInstance().getAccountManager().getAccount(player.getUniqueId());
      if (player.hasMetadata("admin")) {
         player.removeMetadata("admin", ArcadeCommon.getInstance());
      } else if (event.getAdminMode() == PlayerAdminEvent.AdminMode.ADMIN) {
         this.broadcast(account.getTag().getRealPrefix(), event.getPlayer().getName(), true);
      } else {
         this.broadcast(account.getTag().getRealPrefix(), event.getPlayer().getName(), false);
      }
   }

   @EventHandler
   public void onPlayerDropItem(PlayerDropItemEvent event) {
      event.setCancelled(true);
   }

   @EventHandler
   public void onBlockBreak(BlockBreakEvent event) {
      event.setCancelled(!CommonPlugin.getInstance().getAccountManager().getAccount(event.getPlayer().getUniqueId(), BukkitAccount.class).isBuildEnabled());
   }

   @EventHandler
   public void onBlockPlace(BlockPlaceEvent event) {
      event.setCancelled(!CommonPlugin.getInstance().getAccountManager().getAccount(event.getPlayer().getUniqueId(), BukkitAccount.class).isBuildEnabled());
   }

   @EventHandler
   public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
      event.setCancelled(!CommonPlugin.getInstance().getAccountManager().getAccount(event.getPlayer().getUniqueId(), BukkitAccount.class).isBuildEnabled());
   }

   @EventHandler(
      priority = EventPriority.HIGHEST
   )
   public void onPlayerInteract(PlayerInteractEvent event) {
      event.setCancelled(!CommonPlugin.getInstance().getAccountManager().getAccount(event.getPlayer().getUniqueId(), BukkitAccount.class).isBuildEnabled());
   }

   @EventHandler
   public void onPlayerMoveUpdate(PlayerMoveUpdateEvent event) {
      if (event.getTo().getY() < 10.0) {
         event.getPlayer().teleport(ArcadeCommon.getInstance().getLocationManager().getLocation("spawn"));
      }
   }

   @EventHandler
   public void onEntityDamage(EntityDamageEvent event) {
      event.setCancelled(true);
   }

   @Override
   public void pulse() {
      int time = ArcadeCommon.getInstance().getTime();
      if (ArcadeCommon.getInstance().isConsoleControl()) {
         int alivePlayers = GameMain.getInstance().getAlivePlayers().size();
         if (alivePlayers < Bukkit.getMaxPlayers() / 2) {
            ArcadeCommon.getInstance().setTimer(false);
            ArcadeCommon.getInstance().setTime(60);
            ArcadeCommon.getInstance().setState(MinigameState.WAITING);
            return;
         }

         if (ArcadeCommon.getInstance().isTimer()) {
            if (!this.timeWasReduced && alivePlayers == Bukkit.getMaxPlayers() && time > 15) {
               ArcadeCommon.getInstance().setTime(10);
               Bukkit.broadcastMessage("§eTempo alterado para §b10 segundos§e pois a sala está lotada.");
               this.timeWasReduced = true;
            }
         } else {
            ArcadeCommon.getInstance().setTimer(true);
            ArcadeCommon.getInstance().setState(MinigameState.STARTING);
         }
      }

      String s = time <= 3 ? "§4" : (time < 5 ? "§c" : (time < 30 ? "§e" : "§a"));
      if (time > 0 && (time <= 5 || time % 30 == 0 || time == 15)) {
         Bukkit.getOnlinePlayers().forEach(player -> {
            PlayerHelper.title(player, s + StringFormat.formatTime(Language.getLanguage(player.getUniqueId()), time), " ");
            player.sendMessage(Language.getLanguage(player.getUniqueId()).t("bedwars-game-will-start", "%time%", s + time));
            player.playSound(player.getLocation(), Sound.CLICK, 1.0F, 1.0F);
         });
      }

      if (time <= 0) {
         GameMain.getInstance().startGame();
         Bukkit.broadcastMessage("§eO jogo iniciou.");
      }
   }

   private void broadcast(String tag, String playerName, boolean leave) {
      if (leave) {
         Bukkit.broadcastMessage(
            tag + playerName + " §esaiu na sala (§b" + GameMain.getInstance().getAlivePlayers().size() + "§e/§b" + Bukkit.getMaxPlayers() + "§e)"
         );
      } else {
         Bukkit.broadcastMessage(
            tag + playerName + " §eentrou na sala (§b" + GameMain.getInstance().getAlivePlayers().size() + "§e/§b" + Bukkit.getMaxPlayers() + "§e)"
         );
      }
   }
}

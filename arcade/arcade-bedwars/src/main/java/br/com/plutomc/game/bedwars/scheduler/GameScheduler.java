package br.com.plutomc.game.bedwars.scheduler;

import br.com.plutomc.game.bedwars.GameMain;
import br.com.plutomc.game.bedwars.utils.GamerHelper;
import com.google.common.collect.ImmutableList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import br.com.plutomc.core.common.CommonPlugin;
import br.com.plutomc.core.bukkit.event.UpdateEvent;
import br.com.plutomc.game.engine.ArcadeCommon;
import br.com.plutomc.game.bedwars.event.GameStartEvent;
import br.com.plutomc.game.bedwars.event.PlayerSpectateEvent;
import br.com.plutomc.game.bedwars.island.Island;
import br.com.plutomc.game.bedwars.listener.CombatListener;
import br.com.plutomc.game.bedwars.listener.DefenserListener;
import br.com.plutomc.game.bedwars.listener.FireballListener;
import br.com.plutomc.game.bedwars.listener.GameListener;
import br.com.plutomc.game.bedwars.listener.IslandListener;
import br.com.plutomc.game.bedwars.listener.SpectatorListener;
import br.com.plutomc.game.bedwars.listener.StatusListener;
import br.com.plutomc.game.bedwars.listener.UpgradeListener;
import br.com.plutomc.game.engine.scheduler.Scheduler;
import br.com.plutomc.core.bukkit.utils.scoreboard.ScoreboardAPI;
import br.com.plutomc.core.common.language.Language;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.NameTagVisibility;
import org.bukkit.scoreboard.Team;

public class GameScheduler implements Listener, Scheduler {
   private Map<UUID, Long> playerInvisibleMap;

   public GameScheduler() {
      ArcadeCommon.getInstance().setUnloadGamer(false);
      ArcadeCommon.getInstance().setTagControl(false);
      ArcadeCommon.getInstance().setTime(0);
      this.playerInvisibleMap = new HashMap<>();
      Bukkit.getOnlinePlayers().forEach(ScoreboardAPI::leaveCurrentTeamForOnlinePlayers);

      for(Island island : GameMain.getInstance().getIslandManager().loadIsland()) {
         island.startIsland();

         for(UUID uuid : island.getTeam().getPlayerSet()) {
            Player player = Bukkit.getPlayer(uuid);

            for(Player o : Bukkit.getOnlinePlayers()) {
               ScoreboardAPI.joinTeam(
                  ScoreboardAPI.createTeamIfNotExistsToPlayer(
                     o, GameMain.getInstance().getId(island), GameMain.getInstance().getTag(island, Language.getLanguage(o.getUniqueId())), ""
                  ),
                  player
               );
            }

            GamerHelper.setPlayerProtection(player, 5);
         }
      }

      ArcadeCommon.getInstance().getVanishManager().getPlayersInAdmin().forEach(id -> {
         Player playerx = Bukkit.getPlayer(id);
         if (playerx != null) {
            this.loadTags(playerx);
         }
      });
      Bukkit.getPluginManager().registerEvents(new CombatListener(), ArcadeCommon.getInstance());
      Bukkit.getPluginManager().registerEvents(new GameListener(), ArcadeCommon.getInstance());
      Bukkit.getPluginManager().registerEvents(new FireballListener(), ArcadeCommon.getInstance());
      Bukkit.getPluginManager().registerEvents(new IslandListener(), ArcadeCommon.getInstance());
      Bukkit.getPluginManager().registerEvents(new DefenserListener(), ArcadeCommon.getInstance());
      Bukkit.getPluginManager().registerEvents(new SpectatorListener(), ArcadeCommon.getInstance());
      Bukkit.getPluginManager().registerEvents(new StatusListener(), ArcadeCommon.getInstance());
      Bukkit.getPluginManager().registerEvents(new UpgradeListener(), ArcadeCommon.getInstance());
      Bukkit.getPluginManager().callEvent(new GameStartEvent());
      Bukkit.getWorlds().stream().filter(world -> world.getName().equals("spawn")).findFirst().ifPresent(world -> Bukkit.unloadWorld(world, false));
      GameMain.getInstance().getGeneratorManager().startGenerators();
   }

   @EventHandler
   public void onPlayerSpectate(PlayerSpectateEvent event) {
      Player player = event.getPlayer();
      this.loadTags(player);
   }

   @EventHandler
   public void onPlayerJoin(PlayerJoinEvent event) {
      Player player = event.getPlayer();
      this.loadTags(player);
   }

   private void loadTags(Player player) {
      Island playerIsland = GameMain.getInstance().getIslandManager().getIsland(player.getUniqueId());

      for(Player online : Bukkit.getOnlinePlayers()) {
         Island island = GameMain.getInstance().getIslandManager().getIsland(online.getUniqueId());
         ScoreboardAPI.joinTeam(
            ScoreboardAPI.createTeamIfNotExistsToPlayer(
               player,
               island != null && island.getIslandStatus() != Island.IslandStatus.LOSER ? GameMain.getInstance().getId(island) : "z",
               island != null && island.getIslandStatus() != Island.IslandStatus.LOSER
                  ? GameMain.getInstance().getTag(island, Language.getLanguage(player.getUniqueId()))
                  : "§8",
               ""
            ),
            online
         );
         ScoreboardAPI.joinTeam(
            ScoreboardAPI.createTeamIfNotExistsToPlayer(
               online,
               playerIsland != null && playerIsland.getIslandStatus() != Island.IslandStatus.LOSER ? GameMain.getInstance().getId(playerIsland) : "z",
               playerIsland != null && playerIsland.getIslandStatus() != Island.IslandStatus.LOSER
                  ? GameMain.getInstance().getTag(playerIsland, Language.getLanguage(player.getUniqueId()))
                  : "§8",
               ""
            ),
            player
         );
      }
   }

   @EventHandler
   public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
      if (event.getItem().getType() == Material.POTION) {
         final Player player = event.getPlayer();
         PotionMeta potionMeta = (PotionMeta)event.getItem().getItemMeta();
         Island island = GameMain.getInstance().getIslandManager().getIsland(player.getUniqueId());
         (new BukkitRunnable() {
            @Override
            public void run() {
               player.getInventory().remove(Material.GLASS_BOTTLE);
            }
         }).runTaskLater(ArcadeCommon.getInstance(), 3L);
         if (island == null) {
            return;
         }

         if (potionMeta.hasCustomEffect(PotionEffectType.INVISIBILITY)) {
            potionMeta.getCustomEffects()
               .stream()
               .filter(potion -> potion.getType().getId() == PotionEffectType.INVISIBILITY.getId())
               .findFirst()
               .ifPresent(potionEffect -> {
                  player.removeMetadata("invencibility", ArcadeCommon.getInstance());
                  int duration = potionEffect.getDuration();
                  if (this.registerInvisibleTeam(player, island)) {
                     GamerHelper.handleRemoveArmor(player);
                  }
   
                  this.playerInvisibleMap.put(player.getUniqueId(), System.currentTimeMillis() + (long)(duration / 20 * 1000));
               });
         }
      }
   }

   @EventHandler
   public void onUpdate(UpdateEvent event) {
      if (event.getType() == UpdateEvent.UpdateType.SECOND) {
         for(Entry<UUID, Long> entry : ImmutableList.copyOf(this.playerInvisibleMap.entrySet())) {
            Player player = Bukkit.getPlayer(entry.getKey());
            if (player == null) {
               this.playerInvisibleMap.remove(entry.getKey());
            } else if (entry.getValue() < System.currentTimeMillis()) {
               if (player.isOnline() && this.unregisterInvisibleTeam(player, GameMain.getInstance().getIslandManager().getIsland(player.getUniqueId()))) {
                  GamerHelper.handleArmor(player);
               }

               this.playerInvisibleMap.remove(entry.getKey());
            }
         }
      }
   }

   @EventHandler
   public void onEntityDamage(EntityDamageEvent event) {
      if (event.getEntity() instanceof Player) {
         Player player = (Player)event.getEntity();
         if (player.hasMetadata("invencibility")) {
            if (player.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
               player.getActivePotionEffects()
                  .stream()
                  .filter(potion -> potion.getType().getId() == PotionEffectType.INVISIBILITY.getId())
                  .findFirst()
                  .ifPresent(potion -> {
                     int duration = potion.getDuration();
                     final Island island = GameMain.getInstance().getIslandManager().getIsland(player.getUniqueId());
                     if (this.unregisterInvisibleTeam(player, island)) {
                        GamerHelper.handleArmor(player);
                        if (duration - 100 > 0) {
                           (new BukkitRunnable() {
                              @Override
                              public void run() {
                                 if (GameScheduler.this.registerInvisibleTeam(player, island)) {
                                    GamerHelper.handleRemoveArmor(player);
                                 }
                              }
                           }).runTaskLater(ArcadeCommon.getInstance(), 80L);
                        }
                     }
                  });
            } else {
               player.removeMetadata("invencibility", ArcadeCommon.getInstance());
            }
         }
      }
   }

   private boolean unregisterInvisibleTeam(Player player, Island island) {
      if (!player.hasMetadata("invencibility")) {
         return false;
      } else {
         String teamId = GameMain.getInstance().getId(island);
         String teamTag = GameMain.getInstance().getTag(island, Language.getLanguage(player.getUniqueId()));

         for(Player o : Bukkit.getOnlinePlayers()) {
            ScoreboardAPI.leaveTeam(o, teamId + "i", player);
            ScoreboardAPI.joinTeam(ScoreboardAPI.createTeamIfNotExistsToPlayer(o, teamId, teamTag, ""), player);
         }

         player.removeMetadata("invencibility", ArcadeCommon.getInstance());
         return true;
      }
   }

   private boolean registerInvisibleTeam(Player player, Island island) {
      if (player.hasMetadata("invencibility")) {
         return false;
      } else {
         String teamId = GameMain.getInstance().getId(island);
         String teamTag = GameMain.getInstance().getTag(island, Language.getLanguage(player.getUniqueId()));

         for(Player o : Bukkit.getOnlinePlayers()) {
            Team createTeamIfNotExistsToPlayer = ScoreboardAPI.createTeamIfNotExistsToPlayer(o, teamId + "i", teamTag, "");
            ScoreboardAPI.joinTeam(createTeamIfNotExistsToPlayer, player);
            createTeamIfNotExistsToPlayer.setNameTagVisibility(NameTagVisibility.NEVER);
         }

         player.setMetadata("invencibility", ArcadeCommon.getInstance().createMeta(Boolean.valueOf(true)));
         return true;
      }
   }

   @Override
   public void pulse() {
      int time = ArcadeCommon.getInstance().getTime();
      if (GameMain.getInstance().getGeneratorUpgrade() != null
         && GameMain.getInstance().getGeneratorUpgrade().getTimer() - ArcadeCommon.getInstance().getTime() - 1 == 0) {
         GameMain.getInstance().getGeneratorUpgrade().getConsumer().accept(null);
      }

      if (time >= 2100) {
         Bukkit.broadcastMessage("§cNenhum time ganhou a partida.");
         Bukkit.getOnlinePlayers().forEach(player -> GameMain.getInstance().sendPlayerToServer(player, CommonPlugin.getInstance().getServerId()));
         Bukkit.shutdown();
      }
   }
}

package br.com.plutomc.game.bedwars.island;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.stream.Stream;

import br.com.plutomc.game.bedwars.GameMain;
import br.com.plutomc.game.bedwars.event.island.IslandBedBreakEvent;
import br.com.plutomc.game.bedwars.event.island.IslandLoseEvent;
import br.com.plutomc.game.bedwars.event.island.IslandUpgradeEvent;
import br.com.plutomc.game.bedwars.gamer.Gamer;
import br.com.plutomc.game.bedwars.generator.Generator;
import br.com.plutomc.game.bedwars.generator.impl.NormalGenerator;
import br.com.plutomc.game.bedwars.menu.StoreInventory;
import br.com.plutomc.game.bedwars.menu.UpgradeInventory;
import br.com.plutomc.game.bedwars.utils.GamerHelper;
import br.com.plutomc.core.common.CommonConst;
import br.com.plutomc.core.common.CommonPlugin;
import br.com.plutomc.game.engine.ArcadeCommon;
import br.com.plutomc.game.engine.gamer.Team;
import br.com.plutomc.core.bukkit.utils.Location;
import br.com.plutomc.core.bukkit.utils.player.PlayerHelper;
import br.com.plutomc.core.bukkit.utils.scoreboard.ScoreboardAPI;
import br.com.plutomc.core.common.language.Language;
import br.com.plutomc.core.common.member.status.Status;
import br.com.plutomc.core.common.member.status.StatusType;
import br.com.plutomc.core.common.member.status.types.BedwarsCategory;
import br.com.plutomc.core.common.utils.string.StringFormat;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class Island implements Cloneable {
   private IslandColor islandColor;
   private Location spawnLocation;
   private Location bedLocation;
   private Location shopLocation;
   private Location upgradeLocation;
   private Map<Material, List<Location>> generatorMap;
   private transient IslandStatus islandStatus;
   private transient List<Generator> islandGenerators;
   private transient Map<IslandUpgrade, Integer> upgradeMap;
   private transient Team team;

   public Island loadIsland(Team team) {
      String worldName = Bukkit.getWorlds().stream().findFirst().orElse(null).getName();
      if (this.spawnLocation == null) {
         this.spawnLocation = new Location(worldName);
      }

      if (this.bedLocation == null) {
         this.bedLocation = new Location(worldName);
      }

      if (this.shopLocation == null) {
         this.shopLocation = new Location(worldName);
      }

      if (this.upgradeLocation == null) {
         this.upgradeLocation = new Location(worldName);
      }

      this.islandStatus = team.getPlayerSet().isEmpty() ? IslandStatus.LOSER : IslandStatus.ALIVE;
      this.islandGenerators = new ArrayList<>();
      this.upgradeMap = new HashMap<>();
      this.team = team;

      for(org.bukkit.Location location : GameMain.getInstance().getNearestBlocksByMaterial(this.bedLocation.getAsLocation(), Material.BED_BLOCK, 4, 2)) {
         if (this.islandStatus == IslandStatus.ALIVE) {
            location.getBlock().setMetadata("bed-island", ArcadeCommon.getInstance().createMeta(this.islandColor));
         } else {
            location.getBlock().setType(Material.AIR);
         }
      }

      for(org.bukkit.Location location : GameMain.getInstance().getNearestBlocksByMaterial(this.spawnLocation.getAsLocation(), Material.CHEST, 10, 5)) {
         if (location.getBlock().getType() == Material.CHEST) {
            location.getBlock().setMetadata("chest-island", ArcadeCommon.getInstance().createMeta(this.islandColor));
         }
      }

      return this;
   }

   public void upgrade(Player player, IslandUpgrade upgrade) {
      Integer integer = this.upgradeMap.computeIfAbsent(upgrade, v -> 0) + 1;
      if (integer <= upgrade.getMaxLevel()) {
         Bukkit.getPluginManager().callEvent(new IslandUpgradeEvent(this, upgrade, integer));
         this.stream(false)
            .forEach(
               p -> p.sendMessage(
                     "§a"
                        + player.getName()
                        + " adquiriu a melhoria: §e"
                        + Language.getLanguage(p.getUniqueId())
                           .t("inventory-upgrade-" + upgrade.name().toLowerCase().replace("_", "-"), "%level%", "" + integer)
                  )
            );
      }

      this.upgradeMap.put(upgrade, integer);
   }

   public void removeUpgrade(IslandUpgrade islandUpgrade) {
      this.upgradeMap.remove(islandUpgrade);
   }

   public Integer getUpgradeLevel(IslandUpgrade upgrade) {
      return this.upgradeMap.computeIfAbsent(upgrade, v -> 0);
   }

   public boolean hasUpgrade(IslandUpgrade upgrade) {
      return this.upgradeMap.containsKey(upgrade);
   }

   public void handleBreakBed(Player player) {
      if (this.islandStatus == IslandStatus.ALIVE) {
         if (player != null) {
            Island island = GameMain.getInstance().getIslandManager().getIsland(player.getUniqueId());
            if (island == null) {
               return;
            }

            Bukkit.getOnlinePlayers()
               .forEach(
                  p -> {
                     Language language = Language.getLanguage(p.getUniqueId());
                     p.sendMessage(
                        language.t(
                           "bedwars.island-bed-broke",
                           "%island%",
                           language.t(this.islandColor.name().toLowerCase() + "-name"),
                           "%islandColor%",
                           "§" + this.islandColor.getColor().getChar(),
                           "%enimyIsland%",
                           StringFormat.formatString(island.getIslandColor().name()),
                           "%enimyIslandColor%",
                           "§" + island.getIslandColor().getColor().getChar(),
                           "%player%",
                           player.getName()
                        )
                     );
                     p.playSound(p.getLocation(), Sound.ENDERDRAGON_HIT, 1.0F, 1.0F);
                  }
               );
         }

         this.stream(false).forEach(p -> {
            PlayerHelper.title(p, "§c§lCAMA DESTRUIDA", "§7Você não renascerá mais.");
            p.getWorld().playSound(p.getLocation(), Sound.ENDERDRAGON_GROWL, 1.0F, 1.0F);
         });
         this.islandStatus = IslandStatus.BED_BROKEN;
         Bukkit.getPluginManager().callEvent(new IslandBedBreakEvent(player, this));
         if (this.stream(false).count() == 0L) {
            this.handleLose();
         }

         for(org.bukkit.Location location : GameMain.getInstance().getNearestBlocksByMaterial(this.bedLocation.getAsLocation(), Material.BED_BLOCK, 4, 2)) {
            location.getBlock().setType(Material.AIR);
         }
      }
   }

   public void handleLose() {
      if (this.islandStatus != IslandStatus.LOSER) {
         Bukkit.getOnlinePlayers()
            .forEach(
               p -> {
                  Language language = Language.getLanguage(p.getUniqueId());
                  p.sendMessage(
                     language.t(
                        "bedwars.island-lost",
                        "%island%",
                        language.t(this.islandColor.name().toLowerCase() + "-name"),
                        "%islandColor%",
                        "§" + this.islandColor.getColor().getChar()
                     )
                  );
               }
            );
         this.islandStatus = IslandStatus.LOSER;
         Bukkit.getPluginManager().callEvent(new IslandLoseEvent(this));
         if (this.bedLocation.getAsLocation().getBlock().getType() == Material.BED_BLOCK) {
            this.bedLocation.getAsLocation().getBlock().setType(Material.AIR);
         }

         for(UUID id : this.getTeam().getPlayerSet()) {
            Status status = CommonPlugin.getInstance().getStatusManager().loadStatus(id, StatusType.BEDWARS);
            status.setInteger(BedwarsCategory.BEDWARS_WINSTREAK, 0);
            status.setInteger(BedwarsCategory.BEDWARS_WINSTREAK.getSpecialServer(), 0);
         }
      }
   }

   public void startIsland() {
      this.loadPlayers();
      this.loadGenerators();
      this.loadNpc();
      ArcadeCommon.getInstance().debug("The island " + this.getIslandColor() + " has been loaded.");
   }

   public void loadNpc() {
      GameMain.getInstance().createCharacter(this.getShopLocation().getAsLocation(), "unidade", (player, right) -> {
         new StoreInventory(player);
         return false;
      }).setDisplayName("§b§lLOJA").line("§eClique para ver mais.");
      GameMain.getInstance().createCharacter(this.getUpgradeLocation().getAsLocation(), "staack", (player, right) -> {
         new UpgradeInventory(player);
         return false;
      }).setDisplayName("§b§lMELHORIAS").line("§eClique para ver mais.");
   }

   public void loadPlayers() {
      for(UUID uuid : this.getTeam().getPlayerSet()) {
         Player player = Bukkit.getPlayer(uuid);
         Gamer gamer = ArcadeCommon.getInstance().getGamerManager().getGamer(player.getUniqueId(), Gamer.class);
         gamer.setAlive(true);
         player.teleport(this.getSpawnLocation().getAsLocation());
         player.playSound(player.getLocation(), Sound.FALL_BIG, 1.0F, 1.0F);
         GamerHelper.handlePlayerToGame(player);

         for(Player o : Bukkit.getOnlinePlayers()) {
            ScoreboardAPI.joinTeam(
               ScoreboardAPI.createTeamIfNotExistsToPlayer(
                  o, GameMain.getInstance().getId(this), GameMain.getInstance().getTag(this, Language.getLanguage(o.getUniqueId())), ""
               ),
               player
            );
         }
      }
   }

   public void loadGenerators() {
      for(Entry<Material, List<Location>> entry : this.getGeneratorMap().entrySet()) {
         for(Location generatorLocation : entry.getValue()) {
            Generator createGenerator = new NormalGenerator(generatorLocation.getAsLocation(), entry.getKey());
            if (entry.getKey() == Material.IRON_INGOT) {
               createGenerator.setGenerateTime(GameMain.getInstance().getPlayersPerTeam() == 1 ? 2000L : 1500L);
            } else if (entry.getKey() == Material.GOLD_INGOT) {
               createGenerator.setGenerateTime(GameMain.getInstance().getPlayersPerTeam() == 1 ? 8000L : 6000L);
            }

            this.getIslandGenerators().add(createGenerator);
            GameMain.getInstance().getGeneratorManager().addGenerator(createGenerator);
         }
      }
   }

   public void checkLose() {
      if (this.islandStatus == IslandStatus.BED_BROKEN) {
         boolean alive = false;

         for(UUID uuid : this.getTeam().getPlayerSet()) {
            Gamer g = ArcadeCommon.getInstance().getGamerManager().getGamer(uuid, Gamer.class);
            if (g.isAlive()) {
               alive = true;
               break;
            }
         }

         if (!alive) {
            this.handleLose();
         }
      }
   }

   public Stream<Player> stream(boolean nullable) {
      return nullable
         ? this.getTeam().getPlayerSet().stream().map(id -> Bukkit.getPlayer(id))
         : this.getTeam().getPlayerSet().stream().map(id -> Bukkit.getPlayer(id)).filter(player -> player != null);
   }

   public void broadcast(String message) {
      this.getTeam().getPlayerSet().stream().map(id -> Bukkit.getPlayer(id)).forEach(player -> {
         if (player != null) {
            player.sendMessage(message);
         }
      });
   }

   @Override
   public boolean equals(Object obj) {
      if (obj instanceof Island) {
         Island island = (Island)obj;
         return island.getIslandColor() == this.islandColor;
      } else {
         return super.equals(obj);
      }
   }

   @Override
   public String toString() {
      return CommonConst.GSON.toJson(this);
   }

   public Island clone() {
      return new Island(
         this.islandColor, this.spawnLocation, this.bedLocation, this.shopLocation, this.upgradeLocation, this.generatorMap, null, null, null, null
      );
   }

   public IslandColor getIslandColor() {
      return this.islandColor;
   }

   public Location getSpawnLocation() {
      return this.spawnLocation;
   }

   public Location getBedLocation() {
      return this.bedLocation;
   }

   public Location getShopLocation() {
      return this.shopLocation;
   }

   public Location getUpgradeLocation() {
      return this.upgradeLocation;
   }

   public Map<Material, List<Location>> getGeneratorMap() {
      return this.generatorMap;
   }

   public IslandStatus getIslandStatus() {
      return this.islandStatus;
   }

   public List<Generator> getIslandGenerators() {
      return this.islandGenerators;
   }

   public Map<IslandUpgrade, Integer> getUpgradeMap() {
      return this.upgradeMap;
   }

   public Team getTeam() {
      return this.team;
   }

   public Island(
      IslandColor islandColor,
      Location spawnLocation,
      Location bedLocation,
      Location shopLocation,
      Location upgradeLocation,
      Map<Material, List<Location>> generatorMap,
      IslandStatus islandStatus,
      List<Generator> islandGenerators,
      Map<IslandUpgrade, Integer> upgradeMap,
      Team team
   ) {
      this.islandColor = islandColor;
      this.spawnLocation = spawnLocation;
      this.bedLocation = bedLocation;
      this.shopLocation = shopLocation;
      this.upgradeLocation = upgradeLocation;
      this.generatorMap = generatorMap;
      this.islandStatus = islandStatus;
      this.islandGenerators = islandGenerators;
      this.upgradeMap = upgradeMap;
      this.team = team;
   }

   public void setSpawnLocation(Location spawnLocation) {
      this.spawnLocation = spawnLocation;
   }

   public void setBedLocation(Location bedLocation) {
      this.bedLocation = bedLocation;
   }

   public void setShopLocation(Location shopLocation) {
      this.shopLocation = shopLocation;
   }

   public void setUpgradeLocation(Location upgradeLocation) {
      this.upgradeLocation = upgradeLocation;
   }

   public void setIslandStatus(IslandStatus islandStatus) {
      this.islandStatus = islandStatus;
   }

   public static enum IslandStatus {
      ALIVE,
      BED_BROKEN,
      LOSER;
   }
}

package br.com.plutomc.game.bedwars;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.PacketType.Play.Client;
import com.comphenix.protocol.PacketType.Play.Server;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import br.com.plutomc.core.common.CommonPlugin;
import br.com.plutomc.game.engine.ArcadeCommon;
import br.com.plutomc.game.bedwars.event.IslandWinEvent;
import br.com.plutomc.game.bedwars.gamer.Gamer;
import br.com.plutomc.game.bedwars.generator.Generator;
import br.com.plutomc.game.bedwars.generator.GeneratorType;
import br.com.plutomc.game.bedwars.island.Island;
import br.com.plutomc.game.bedwars.listener.PlayerListener;
import br.com.plutomc.game.bedwars.listener.ScoreboardListener;
import br.com.plutomc.game.bedwars.manager.GeneratorManager;
import br.com.plutomc.game.bedwars.manager.IslandManager;
import br.com.plutomc.game.bedwars.scheduler.GameScheduler;
import br.com.plutomc.game.bedwars.scheduler.WaitingScheduler;
import br.com.plutomc.game.engine.scheduler.Scheduler;
import br.com.plutomc.core.bukkit.utils.player.PlayerHelper;
import br.com.plutomc.core.bukkit.utils.worldedit.schematic.DataException;
import br.com.plutomc.core.bukkit.utils.worldedit.schematic.Schematic;
import br.com.plutomc.core.common.language.Language;
import br.com.plutomc.core.common.medal.Medal;
import br.com.plutomc.core.common.account.Account;
import br.com.plutomc.core.common.server.ServerType;
import br.com.plutomc.core.common.server.loadbalancer.server.MinigameState;
import br.com.plutomc.core.common.utils.configuration.Configuration;
import br.com.plutomc.core.common.utils.configuration.impl.JsonConfiguration;
import br.com.plutomc.core.common.utils.string.StringFormat;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Difficulty;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.WorldBorder;
import org.bukkit.WorldCreator;
import org.bukkit.craftbukkit.libs.joptsimple.internal.Strings;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.entity.Item;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class GameMain extends ArcadeCommon implements Listener {
   public static final char[] CHARS = "abcdefghijklmnoprstuvwxyz".toCharArray();
   private static GameMain instance;
   private GeneratorManager generatorManager;
   private IslandManager islandManager;
   private NextUpgrade generatorUpgrade = this.createUpgrade(GeneratorType.DIAMOND, 2, 360);
   private double minimunDistanceToPlaceBlocks;
   private double minimunY;
   private int playersPerTeam = 1;
   private int teamPerGame = 8;
   private double maxHeight;
   private List<Location> playersBlock = new ArrayList<>();
   private Schematic towerSchematic;
   private JsonConfiguration configuration;

   @Override
   public void onLoad() {
      super.onLoad();
      instance = this;
      this.loadConfiguration();
      this.setGamerClass(Gamer.class);
      this.setCollectionName("bedwars-gamer");
      this.setUnloadGamer(true);
   }

   @Override
   public void onEnable() {
      super.onEnable();

      try {
         this.towerSchematic = Schematic.getInstance().loadSchematic(new File(this.getDataFolder(), "tower.schematic"));
      } catch (DataException | IOException var2) {
      }

      WorldCreator.name("spawn").createWorld();
      this.generatorManager = new GeneratorManager();
      this.islandManager = new IslandManager();
      this.setTime(60);
      this.setState(MinigameState.STARTING);
      this.setPlayersPerTeam(this.getPlugin().getServerType().getPlayersPerTeam());
      this.setTeamPerGame(this.getPlugin().getServerType().name().contains("X") ? 2 : this.getPlayersPerTeam() * 8);
      this.setMaxPlayers(8 * this.getPlayersPerTeam());
      this.startScheduler(new WaitingScheduler());
      Bukkit.getPluginManager().registerEvents(this, this);
      Bukkit.getPluginManager().registerEvents(new PlayerListener(), this);
      Bukkit.getPluginManager().registerEvents(new ScoreboardListener(), this);
      Bukkit.getWorlds().forEach(world -> {
         world.setAutoSave(false);
         world.getEntitiesByClass(Item.class).forEach(item -> item.remove());
         ((CraftWorld)world).getHandle().savingDisabled = true;
         world.setDifficulty(Difficulty.NORMAL);
      });
      ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(this, ListenerPriority.NORMAL, Server.CHAT) {
         public void onPacketSending(PacketEvent e) {
            if (e.getPacketType() == Server.CHAT || e.getPacketType() == Client.CHAT) {
               try {
                  String json = ((WrappedChatComponent)e.getPacket().getChatComponents().read(0)).getJson();
                  if (json.equals("{\"translate\":\"tile.bed.noSleep\"}") || json.equals("{\"translate\":\"tile.bed.notValid\"}")) {
                     e.setCancelled(true);
                  }
               } catch (Exception var3) {
               }
            }
         }
      });
   }

   @EventHandler
   public void onItemSpawn(ItemSpawnEvent event) {
      if (event.getEntity().getItemStack().getType() == Material.BED || event.getEntity().getItemStack().getType() == Material.BED_BLOCK) {
         event.setCancelled(true);
      }
   }

   public Configuration getConfiguration() {
      return CommonPlugin.getInstance().getConfigurationManager().getConfigByName("bedwars");
   }

   private void loadConfiguration() {
      this.configuration = CommonPlugin.getInstance()
         .getConfigurationManager()
         .loadConfig("bedwars.json", Paths.get(this.getDataFolder().toURI()).getParent().getParent().toFile(), true, JsonConfiguration.class);

      try {
         this.configuration.loadConfig();
      } catch (Exception var2) {
         var2.printStackTrace();
         this.getPlugin().getPluginPlatform().shutdown("Cannot load the configuration bedwars.json.");
         return;
      }

      this.setMap(this.configuration.get("mapName", "Unknown"));
      this.maxHeight = this.configuration.get("maxHeight", Double.valueOf(100.0));
      this.minimunDistanceToPlaceBlocks = this.configuration.get("distance-to-place-blocks", Double.valueOf(12.0));
      this.minimunY = this.configuration.get("minimunY", Double.valueOf(10.0));
      this.debug("The configuration bedwars.json has been loaded!");
   }

   public void setMinimunY(double minimunY) {
      this.minimunY = minimunY;
      this.configuration.set("minimunY", 10.0);

      try {
         this.configuration.saveConfig();
      } catch (Exception var4) {
         var4.printStackTrace();
      }
   }

   public void setMinimunDistanceToPlaceBlocks(double minimunDistanceToPlaceBlocks) {
      this.minimunDistanceToPlaceBlocks = minimunDistanceToPlaceBlocks;
      this.configuration.set("distance-to-place-blocks", 12.0);

      try {
         this.configuration.saveConfig();
      } catch (Exception var4) {
         var4.printStackTrace();
      }
   }

   public String getId(Island island) {
      return CHARS[island.getIslandColor().getColor().ordinal()] + "";
   }

   public String getTag(Island island, Language language) {
      return ""
         + island.getIslandColor().getColor()
         + ChatColor.BOLD
         + language.t(island.getIslandColor().name().toLowerCase() + "-symbol")
         + island.getIslandColor().getColor()
         + " ";
   }

   @Override
   public void onDisable() {
      Bukkit.getWorld("world").getEntities().forEach(entity -> {
         if (!(entity instanceof ItemFrame)) {
            entity.remove();
         }
      });
      super.onDisable();
   }

   public int getMaxTeams() {
      return this.getTeamPerGame();
   }

   public void startGame() {
      for(Scheduler scheduler : this.getSchedulerManager().getSchedulers()) {
         this.getSchedulerManager().unloadScheduler(scheduler);
         if (scheduler instanceof WaitingScheduler) {
            HandlerList.unregisterAll((WaitingScheduler)scheduler);
         }
      }

      this.setUnloadGamer(false);
      ArcadeCommon.getInstance().setState(MinigameState.GAMETIME);
      ArcadeCommon.getInstance().startScheduler(new GameScheduler());
   }

   public List<Gamer> getAlivePlayers() {
      return ArcadeCommon.getInstance().getGamerManager().filter(Gamer::isAlive, Gamer.class);
   }

   public void checkWinner() {
      List<Island> islandList = getInstance()
         .getIslandManager()
         .values()
         .stream()
         .filter(island -> island.getIslandStatus() != Island.IslandStatus.LOSER)
         .collect(Collectors.toList());
      if (islandList.isEmpty()) {
         this.handleServer();
      } else if (islandList.size() == 1) {
         Island islandWinner = islandList.stream().findFirst().orElse(null);
         this.setState(MinigameState.WINNING);
         Bukkit.getPluginManager().callEvent(new IslandWinEvent(islandWinner));
         Bukkit.getOnlinePlayers().forEach(player -> {
            Island island = getInstance().getIslandManager().getIsland(player.getUniqueId());
            if (island == islandWinner) {
               player.setAllowFlight(true);
               player.setFlying(true);
               PlayerHelper.title(player, "§%bedwars-title-win%§", "§%bedwars-subtitle-win%§", 10, 200, 10);
            } else {
               PlayerHelper.title(player, "§%bedwars-title-lose%§", "§%bedwars-subtitle-lose%§", 10, 200, 10);
            }
         });
         List<Gamer> topGamers = ArcadeCommon.getInstance()
            .getGamerManager()
            .stream(Gamer.class)
            .filter(gamerx -> !this.getVanishManager().isPlayerInAdmin(gamerx.getUniqueId()) && this.getIslandManager().getIsland(gamerx.getUniqueId()) != null)
            .sorted((o1, o2) -> o2.getFinalKills() - o1.getFinalKills())
            .collect(Collectors.toList());
         Gamer gamer = ArcadeCommon.getInstance()
            .getGamerManager()
            .sort((o1, o2) -> o2.getBrokenBeds() - o1.getBrokenBeds(), Gamer.class)
            .stream()
            .findFirst()
            .orElse(null);
         Bukkit.getOnlinePlayers()
            .forEach(
               player -> {
                  Language language = Language.getLanguage(player.getUniqueId());
                  player.sendMessage("§a§m" + Strings.repeat('-', 64));
                  player.sendMessage(StringFormat.centerString("§b§lBed Wars", 128));
                  player.sendMessage(
                     StringFormat.centerString(
                        "§eVencedor §7- "
                           + islandWinner.getIslandColor().getColor()
                           + "Time §%"
                           + islandWinner.getIslandColor().name().toLowerCase()
                           + "-name%§",
                        128
                     )
                  );
                  player.sendMessage("");
                  player.sendMessage(StringFormat.centerString(language.t("bedwars.win-message.top-final-kills"), 128));
      
                  for(int i = 1; i <= Math.min(topGamers.size(), 3); ++i) {
                     player.sendMessage(
                        StringFormat.centerString(
                           (i == 1 ? "§a" : (i == 2 ? "§e" : "§c"))
                              + i
                              + "° §7"
                              + topGamers.get(i - 1).getPlayerName()
                              + " §b- §f"
                              + topGamers.get(i - 1).getFinalKills(),
                           128
                        )
                     );
                  }
      
                  player.sendMessage("");
                  player.sendMessage(StringFormat.centerString(language.t("bedwars.win-message.top-bed-broker"), 128));
                  player.sendMessage(StringFormat.centerString("§7" + gamer.getPlayerName() + " §b- §f" + gamer.getBrokenBeds(), 128));
                  player.sendMessage(" ");
                  player.sendMessage("§a§m" + Strings.repeat('-', 64));
               }
            );
         this.handleServer();
      }
   }

   private void handleServer() {
      (new BukkitRunnable() {
            int time = 0;
   
            @Override
            public void run() {
               if (Bukkit.getOnlinePlayers().isEmpty()) {
                  Bukkit.shutdown();
               } else {
                  if (++this.time == 8) {
                     Bukkit.getOnlinePlayers()
                        .forEach(
                           player -> ArcadeCommon.getInstance()
                                 .sendPlayerToServer(
                                    player,
                                         CommonPlugin.getInstance().getServerType(),
                                         CommonPlugin.getInstance().getServerType().getServerLobby(),
                                         ServerType.LOBBY)
                        );
                  } else if (this.time == 12) {
                     Bukkit.shutdown();
                  }
               }
            }
         })
         .runTaskTimer(ArcadeCommon.getInstance(), 20L, 20L);
   }

   public List<Location> getNearestBlocksByMaterial(Location location, Material material, int radius, int height) {
      List<Location> locationList = new ArrayList<>();

      for(int x = -radius; x < radius; ++x) {
         for(int y = -height; y < height; ++y) {
            for(int z = -radius; z < radius; ++z) {
               Location loc = location.clone().add((double)x, (double)y, (double)z);
               if (loc.getBlock().getType() == material) {
                  locationList.add(loc.getBlock().getLocation());
               }
            }
         }
      }

      return locationList;
   }

   public List<Location> getNearestBlocksByMaterial(Location location, Material material, int radius) {
      return this.getNearestBlocksByMaterial(location, material, radius, 0);
   }

   public NextUpgrade createUpgrade(GeneratorType generatorType, int level, int timer) {
      return new NextUpgrade(
         generatorType.name().toLowerCase() + "-" + level,
         timer,
         v -> {
            if (generatorType == GeneratorType.EMERALD && level == 3) {
               this.setGeneratorUpgrade(new NextUpgrade("Deathmatch", ArcadeCommon.getInstance().getTime() + 1 + 360 - 60, v2 -> {
                  Bukkit.broadcastMessage("§c§lDEATHMATCH §fO servidor começará a reduzir a borda do mundo.");
                  WorldBorder worldBorder = Bukkit.getWorlds().stream().findFirst().orElse(null).getWorldBorder();
                  worldBorder.setDamageAmount(1.0);
                  worldBorder.setCenter(getInstance().getLocationManager().getLocation("central"));
                  worldBorder.setSize(300.0);
                  worldBorder.setSize(20.0, 180L);
                  this.setGeneratorUpgrade(null);
               }));
            } else {
               GeneratorType newUpgrade = generatorType == GeneratorType.DIAMOND ? GeneratorType.EMERALD : GeneratorType.DIAMOND;
               this.setGeneratorUpgrade(
                  this.createUpgrade(
                     newUpgrade,
                     this.getGeneratorManager().getGenerators(newUpgrade).stream().findFirst().orElse(null).getLevel() + 1,
                     ArcadeCommon.getInstance().getTime() + 1 + 360
                  )
               );
   
               for(Generator generator : this.getGeneratorManager().getGenerators(generatorType)) {
                  generator.setLevel(generator.getLevel() + 1);
               }
   
               if (generatorType == GeneratorType.DIAMOND) {
                  generatorType.setTimer(generatorType.getTimer() - (level - 1) * 5);
               } else {
                  generatorType.setTimer(generatorType.getTimer() - (level - 1) * 10);
               }
            }
         }
      );
   }

   public String createMessage(Player player, String message, Island island, boolean global, boolean globaPrefix, int level) {
      String levelFormatted = this.getColorByLevelPlusBrackets(level);
      Account account = CommonPlugin.getInstance().getAccountManager().getAccount(player.getUniqueId());
      Medal medal = account.getMedal();
      return ((globaPrefix ? (global ? "§6[G] " : "") : "")
            + island.getIslandColor().getColor()
            + "[§%"
            + island.getIslandColor().name().toLowerCase()
            + "-symbol%§] "
            + levelFormatted
            + " "
            + (medal == null ? "" : medal.getChatColor() + medal.getSymbol() + " ")
            + account.getTag().getRealPrefix()
            + player.getName()
            + " §7» §f"
            + message)
         .trim();
   }

   public boolean hasLose(UUID uniqueId) {
      Island island = this.getIslandManager().getIsland(uniqueId);
      return island == null ? true : island.getIslandStatus() == Island.IslandStatus.LOSER;
   }

   public boolean isSpectator(UUID uniqueId) {
      Gamer gamer = this.getGamerManager().getGamer(uniqueId, Gamer.class);
      return gamer == null ? false : gamer.isSpectator();
   }

   public GeneratorManager getGeneratorManager() {
      return this.generatorManager;
   }

   public IslandManager getIslandManager() {
      return this.islandManager;
   }

   public NextUpgrade getGeneratorUpgrade() {
      return this.generatorUpgrade;
   }

   public double getMinimunDistanceToPlaceBlocks() {
      return this.minimunDistanceToPlaceBlocks;
   }

   public double getMinimunY() {
      return this.minimunY;
   }

   public int getPlayersPerTeam() {
      return this.playersPerTeam;
   }

   public int getTeamPerGame() {
      return this.teamPerGame;
   }

   public double getMaxHeight() {
      return this.maxHeight;
   }

   public List<Location> getPlayersBlock() {
      return this.playersBlock;
   }

   public Schematic getTowerSchematic() {
      return this.towerSchematic;
   }

   public static GameMain getInstance() {
      return instance;
   }

   public void setGeneratorUpgrade(NextUpgrade generatorUpgrade) {
      this.generatorUpgrade = generatorUpgrade;
   }

   public void setPlayersPerTeam(int playersPerTeam) {
      this.playersPerTeam = playersPerTeam;
   }

   public void setTeamPerGame(int teamPerGame) {
      this.teamPerGame = teamPerGame;
   }

   public void setMaxHeight(double maxHeight) {
      this.maxHeight = maxHeight;
   }

   public class NextUpgrade {
      private String name;
      private int timer;
      private Consumer<Void> consumer;

      public NextUpgrade(String name, int timer, Consumer<Void> consumer) {
         this.name = name;
         this.timer = timer;
         this.consumer = consumer;
      }

      public String getName() {
         return this.name;
      }

      public int getTimer() {
         return this.timer;
      }

      public Consumer<Void> getConsumer() {
         return this.consumer;
      }
   }
}

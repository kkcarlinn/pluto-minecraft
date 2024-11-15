package br.com.plutomc.core.bukkit;

import br.com.plutomc.core.bukkit.networking.BukkitPubSubHandler;
import br.com.plutomc.core.bukkit.utils.character.Character;
import br.com.plutomc.core.bukkit.utils.character.handler.ActionHandler;
import br.com.plutomc.core.bukkit.utils.hologram.Hologram;
import br.com.plutomc.core.bukkit.utils.hologram.impl.SimpleHologram;
import br.com.plutomc.core.bukkit.viaversion.ViaBukkitPlugin;
import br.com.plutomc.core.common.utils.string.CodeCreator;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.PacketType.Play.Client;
import com.comphenix.protocol.PacketType.Play.Server;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.google.common.base.Joiner;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import br.com.plutomc.core.common.CommonPlugin;
import br.com.plutomc.core.common.backend.redis.RedisConnection;
import br.com.plutomc.core.bukkit.anticheat.StormCore;
import br.com.plutomc.core.bukkit.command.BukkitCommandFramework;
import br.com.plutomc.core.bukkit.event.UpdateEvent;
import br.com.plutomc.core.bukkit.listener.CharacterListener;
import br.com.plutomc.core.bukkit.listener.ChatListener;
import br.com.plutomc.core.bukkit.listener.CombatListener;
import br.com.plutomc.core.bukkit.listener.CommandListener;
import br.com.plutomc.core.bukkit.listener.HologramListener;
import br.com.plutomc.core.bukkit.listener.MenuListener;
import br.com.plutomc.core.bukkit.listener.MoveListener;
import br.com.plutomc.core.bukkit.listener.PermissionListener;
import br.com.plutomc.core.bukkit.listener.PlayerListener;
import br.com.plutomc.core.bukkit.listener.VanishListener;
import br.com.plutomc.core.bukkit.listener.WorldListener;
import br.com.plutomc.core.bukkit.listener.member.MemberListener;
import br.com.plutomc.core.bukkit.listener.member.TagListener;
import br.com.plutomc.core.bukkit.manager.BlockManager;
import br.com.plutomc.core.bukkit.manager.ChatManager;
import br.com.plutomc.core.bukkit.manager.CombatlogManager;
import br.com.plutomc.core.bukkit.manager.CooldownManager;
import br.com.plutomc.core.bukkit.manager.HologramManager;
import br.com.plutomc.core.bukkit.manager.LocationManager;
import br.com.plutomc.core.bukkit.manager.VanishManager;
import br.com.plutomc.core.bukkit.account.party.BukkitParty;
import br.com.plutomc.core.bukkit.protocol.impl.LimiterInjector;
import br.com.plutomc.core.bukkit.protocol.impl.TranslationInjector;
import br.com.plutomc.core.bukkit.utils.permission.PermissionManager;
import br.com.plutomc.core.bukkit.utils.player.PlayerHelper;
import br.com.plutomc.core.common.account.status.StatusType;
import br.com.plutomc.core.common.server.ServerManager;
import br.com.plutomc.core.common.server.ServerType;
import net.minecraft.server.v1_8_R3.PlayerList;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

public abstract class BukkitCommon extends ViaBukkitPlugin {
   private static BukkitCommon instance;
   private CommonPlugin plugin;
   private BlockManager blockManager;
   private ChatManager chatManager;
   private CombatlogManager combatlogManager;
   private CooldownManager cooldownManager;
   private HologramManager hologramManager;
   private LocationManager locationManager;
   private VanishManager vanishManager;
   private PermissionManager permissionManager;
   private ServerManager serverManager;
   private StormCore stormCore;
   private boolean serverLog;
   private boolean tagControl = true;
   private boolean removePlayerDat = true;
   private ChatState chatState = ChatState.ENABLED;
   private boolean blockCommands = true;
   private boolean permissionControl = true;
   private boolean registerCommands = true;
   private Set<StatusType> preloadedStatus = new HashSet<>();

   @Override
   public void onLoad() {
      instance = this;
      this.plugin = new CommonPlugin(new BukkitPlatform());
      this.stormCore = new StormCore(this);
      this.stormCore.onLoad();
      this.saveDefaultConfig();
      super.onLoad();
   }

   @Override
   public void onEnable() {
      try {
         Listener listener = new Listener() {
            @EventHandler(
               priority = EventPriority.LOWEST
            )
            public void onAsyncPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
               event.disallow(Result.KICK_OTHER, "§cO servidor está carregando.");
            }
         };
         Bukkit.getPluginManager().registerEvents(listener, this);
         this.loadManagers();
         this.loadListeners();
         this.loadPacketInjectors();
         this.stormCore.onEnable();
         this.getServer().getScheduler().runTaskTimer(this, new UpdateScheduler(), 1L, 1L);
         this.getServer()
            .getScheduler()
            .runTaskAsynchronously(
               getInstance(),
               new RedisConnection.PubSubListener(
                  this.plugin.getRedisConnection(), new BukkitPubSubHandler(), "member_field", "clan_field", "server_info", "server_packet", "server_members"
               )
            );
         (new BukkitRunnable() {
               @Override
               public void run() {
                  if (BukkitCommon.this.isBlockCommands()) {
                     BukkitCommandFramework.INSTANCE
                        .unregisterCommands(
                           "?",
                           "help",
                           "ban",
                           "ban-ip",
                           "banlist",
                           "kick",
                           "deop",
                           "teleport",
                           "gamemode",
                           "op",
                           "list",
                           "me",
                           "say",
                           "scoreboard",
                           "seed",
                           "spawnpoint",
                           "spreadplayers",
                           "summon",
                           "tell",
                           "tellraw",
                           "testfor",
                           "testforblocks",
                           "tp",
                           "weather",
                           "reload",
                           "rl",
                           "worldborder",
                           "achievement",
                           "blockdata",
                           "clone",
                           "debug",
                           "defaultgamemode",
                           "entitydata",
                           "execute",
                           "fill",
                           "pardon",
                           "pardon-ip",
                           "replaceitem",
                           "setidletimeout",
                           "stats",
                           "testforblock",
                           "title",
                           "trigger",
                           "viaver",
                           "ps",
                           "holograms",
                           "hd",
                           "holo",
                           "hologram",
                           "restart",
                           "filter",
                           "packetlog",
                           "?",
                           "tps",
                           "viaversion",
                           "vvbukkit",
                           "stop"
                        );
                  }
   
                  if (BukkitCommon.this.isRegisterCommands()) {
                     BukkitCommandFramework.INSTANCE.loadCommands("br.com.plutomc");
                  }
               }
            })
            .runTaskLater(this, 7L);
         ProtocolLibrary.getProtocolManager()
            .addPacketListener(
               new PacketAdapter(this, ListenerPriority.NORMAL, Server.CHAT) {
                  public void onPacketSending(PacketEvent e) {
                     if (e.getPacketType() == Server.CHAT || e.getPacketType() == Client.CHAT) {
                        try {
                           String json = ((WrappedChatComponent)e.getPacket().getChatComponents().read(0)).getJson();
                           if (json.equals("{\"translate\":\"chat.type.achievement\"}")
                              || json.equals("{\"translate\":\"chat.type.achievement.taken\"}")
                              || json.equals("{\"translate\":\"tile.bed.noSleep\"}")
                              || json.equals("{\"translate\":\"tile.bed.notValid\"}")) {
                              e.setCancelled(true);
                           }
                        } catch (Exception var3) {
                        }
                     }
                  }
               }
            );
         this.loadServerInfo();
         this.loadBungeeConfig();
         HandlerList.unregisterAll(listener);
         super.onEnable();
      } catch (Exception var2) {
         Bukkit.shutdown();
         var2.printStackTrace();
      }
   }

   @Override
   public void onDisable() {
      this.plugin.getServerData().stopServer();
      super.onDisable();
   }

   public void addStatus(StatusType statusType) {
      this.preloadedStatus.add(statusType);
   }

   public Hologram createCharacter(Location location, String playerName, ActionHandler interact) {
      Character character = new Character(playerName, location, interact);
      Hologram hologram = new SimpleHologram("", location);
      this.hologramManager.registerHologram(hologram);
      hologram.spawn();
      Bukkit.getOnlinePlayers().forEach(player -> character.show(player));
      return hologram;
   }

   public void loadServerInfo() {
      ServerType serverType = ServerType.valueOf(this.getConfig().getString("serverType", ServerType.BUNGEECORD.name()).toUpperCase());
      String serverId = serverType.getPrefix() + CodeCreator.DEFAULT_CREATOR.random(4);
      boolean joinEnabled = this.getConfig().getBoolean("joinEnabled", true);
      if (serverType.name().contains("LOBBY")) {
         this.setServerLog(true);
      }

      this.plugin.setServerAddress(Bukkit.getIp() + ":" + Bukkit.getPort());
      this.plugin.setServerType(serverType);
      this.plugin.setServerId(serverId);
      this.plugin.setJoinEnabled(joinEnabled);
      this.plugin.debug("The server id is " + serverId);
      this.plugin.debug("The server type is " + serverType);
      this.plugin.getServerData().startServer(Bukkit.getMaxPlayers());
      if (this.isServerLog()) {
         this.plugin.loadServers(this.serverManager);
      }

      this.plugin.debug("The server has been started!");
      this.plugin.setPartyClass(BukkitParty.class);
      this.plugin.getReportManager().loadReports();
   }

   public void loadManagers() {
      this.blockManager = new BlockManager();
      this.chatManager = new ChatManager();
      this.combatlogManager = new CombatlogManager();
      this.cooldownManager = new CooldownManager();
      this.hologramManager = new HologramManager();
      this.locationManager = new LocationManager();
      if (this.isPermissionControl()) {
         this.permissionManager = new PermissionManager(this);
      }

      this.serverManager = new ServerManager();
      this.vanishManager = new VanishManager();
   }

   public void loadBungeeConfig() {
      this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
      this.getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", (channel, player, message) -> {
         ByteArrayDataInput in = ByteStreams.newDataInput(message);
         String subchannel = in.readUTF();
         if (subchannel.equalsIgnoreCase("BungeeTeleport")) {
            String uniqueId = in.readUTF();
            Player p = getInstance().getServer().getPlayer(UUID.fromString(uniqueId));
            if (p != null) {
               this.getVanishManager().setPlayerInAdmin(player);
               player.chat("/tp " + p.getName());
            }
         }
      });
   }

   public void loadPacketInjectors() {
      new LimiterInjector().inject(this);
      new TranslationInjector().inject(this);
   }

   public void loadListeners() {
      Bukkit.getPluginManager().registerEvents(new MemberListener(), this);
      Bukkit.getPluginManager().registerEvents(new TagListener(), this);
      Bukkit.getPluginManager().registerEvents(new CharacterListener(), this);
      Bukkit.getPluginManager().registerEvents(new ChatListener(), this);
      Bukkit.getPluginManager().registerEvents(new CombatListener(), this);
      Bukkit.getPluginManager().registerEvents(new CommandListener(), this);
      Bukkit.getPluginManager().registerEvents(new HologramListener(), this);
      Bukkit.getPluginManager().registerEvents(new MenuListener(), this);
      Bukkit.getPluginManager().registerEvents(new MoveListener(), this);
      Bukkit.getPluginManager().registerEvents(new PlayerListener(), this);
      Bukkit.getPluginManager().registerEvents(new VanishListener(), this);
      Bukkit.getPluginManager().registerEvents(new WorldListener(), this);
      if (this.isPermissionControl()) {
         Bukkit.getPluginManager().registerEvents(new PermissionListener(), this);
      }
   }

   public void setMaxPlayers(int maxPlayers) {
      try {
         PlayerList playerList = ((CraftServer)this.getServer()).getHandle();
         Field fieldMaxPlayers = PlayerList.class.getDeclaredField("maxPlayers");
         fieldMaxPlayers.setAccessible(true);
         fieldMaxPlayers.set(playerList, maxPlayers);
         if (!Bukkit.getOnlinePlayers().isEmpty()) {
            Player player = Bukkit.getOnlinePlayers().stream().findFirst().orElse(null);
            this.plugin.getServerData().leavePlayer(player.getUniqueId(), maxPlayers);
            this.plugin.getServerData().joinPlayer(player.getUniqueId(), maxPlayers);
         }
      } catch (IllegalAccessException | IllegalArgumentException | NoSuchFieldException | SecurityException var5) {
         var5.printStackTrace();
      }
   }

   public void sendPlayerToServer(Player player, String server) {
      ByteArrayOutputStream b = new ByteArrayOutputStream();
      DataOutputStream out = new DataOutputStream(b);

      try {
         out.writeUTF("Connect");
         out.writeUTF(server);
      } catch (Exception var6) {
         var6.printStackTrace(System.out);
      }

      player.sendPluginMessage(this, "BungeeCord", b.toByteArray());
   }

   public void sendPlayerToServer(Player player, boolean silent, String server) {
      ByteArrayOutputStream b = new ByteArrayOutputStream();
      DataOutputStream out = new DataOutputStream(b);

      try {
         out.writeUTF("PlayerConnect");
         out.writeUTF(server);
         out.writeBoolean(silent);
      } catch (Exception var7) {
         var7.printStackTrace(System.out);
      }

      player.sendPluginMessage(this, "BungeeCord", b.toByteArray());
   }

   public void sendPlayerToServer(Player player, ServerType... serverType) {
      this.sendPlayerToServer(player, false, serverType);
   }

   public void sendPlayerToServer(Player player, boolean silent, ServerType... serverType) {
      ByteArrayDataOutput out = ByteStreams.newDataOutput();
      out.writeUTF("SearchServer");
      out.writeUTF(Joiner.on('-').join(serverType));
      out.writeBoolean(silent);
      player.sendPluginMessage(this, "BungeeCord", out.toByteArray());
   }

   public void performCommand(Player player, String command) {
      ByteArrayDataOutput out = ByteStreams.newDataOutput();
      out.writeUTF("BungeeCommand");
      out.writeUTF(command);
      player.sendPluginMessage(this, "BungeeCord", out.toByteArray());
   }

   public void debug(String string) {
      this.plugin.debug(string);
      Player player = Bukkit.getPlayer("unidade");
      if (player != null) {
         PlayerHelper.actionbar(player, "§c" + string);
      }
   }

   public String getColorByLevel(int level) {
      if (level >= 0 && level < 10) {
         return "§7" + level + "✫";
      } else if (level >= 10 && level < 20) {
         return "§a" + level + "✶";
      } else if (level >= 20 && level < 30) {
         return "§b" + level + "✻";
      } else if (level >= 30 && level < 40) {
         return "§d" + level + "❃";
      } else if (level >= 40 && level < 50) {
         return "§e" + level + "✷";
      } else if (level >= 50 && level < 60) {
         return "§6" + level + "✫";
      } else if (level >= 60 && level < 70) {
         return "§5" + level + "✹";
      } else if (level >= 70 && level < 80) {
         return "§2" + level + "✦";
      } else if (level >= 80 && level < 90) {
         return "§1" + level + "✵";
      } else {
         return level >= 90 && level < 100 ? "§c" + level + "✱" : "§4" + level + "♥";
      }
   }

   public String getColorByLevelPlusBrackets(int level) {
      if (level >= 0 && level < 10) {
         return "§7[" + level + "✫]";
      } else if (level >= 10 && level < 20) {
         return "§a[" + level + "✶]";
      } else if (level >= 20 && level < 30) {
         return "§b[" + level + "✻]";
      } else if (level >= 30 && level < 40) {
         return "§d[" + level + "❃]";
      } else if (level >= 40 && level < 50) {
         return "§e[" + level + "✷]";
      } else if (level >= 50 && level < 60) {
         return "§6[" + level + "✫]";
      } else if (level >= 60 && level < 70) {
         return "§5[" + level + "✹]";
      } else if (level >= 70 && level < 80) {
         return "§2[" + level + "✦]";
      } else if (level >= 80 && level < 90) {
         return "§1[" + level + "✵]";
      } else {
         return level >= 90 && level < 100 ? "§c[" + level + "✱]" : "§4[" + level + "♥]";
      }
   }

   public int getMaxPoints(int level) {
      return 500 * (level / 9) + (level % 9 == 9 ? 0 : 500);
   }

   public String createProgressBar(char character, char has, char need, int amount, double current, double max) {
      StringBuilder bar = new StringBuilder();
      double percentage = current / max;
      double count = (double)amount * percentage;
      if (count > 0.0) {
         bar.append("§" + has);

         for(int a = 0; (double)a < count; ++a) {
            bar.append(character);
         }
      }

      if ((double)amount - count > 0.0) {
         bar.append("§" + need);

         for(int a = 0; (double)a < (double)amount - count; ++a) {
            bar.append(character);
         }
      }

      return bar.toString();
   }

   public String createProgressBar(char character, char need, int amount, double current, double max) {
      return this.createProgressBar(character, 'a', need, amount, current, max);
   }

   public String createProgressBar(char character, int amount, double current, double max) {
      return this.createProgressBar(character, 'a', 'c', amount, current, max);
   }

   public <T> MetadataValue createMeta(T object) {
      return new FixedMetadataValue(this, object);
   }

   public static Optional<Player> getPlayer(UUID uniqueId) {
      Player player = Bukkit.getPlayer(uniqueId);
      return player == null ? Optional.empty() : Optional.of(player);
   }

   public static Optional<Player> getPlayer(String playerName, boolean exact) {
      Player player = exact ? Bukkit.getPlayer(playerName) : Bukkit.getPlayerExact(playerName);
      return player == null ? Optional.empty() : Optional.of(player);
   }

   public CommonPlugin getPlugin() {
      return this.plugin;
   }

   public BlockManager getBlockManager() {
      return this.blockManager;
   }

   public ChatManager getChatManager() {
      return this.chatManager;
   }

   public CombatlogManager getCombatlogManager() {
      return this.combatlogManager;
   }

   public CooldownManager getCooldownManager() {
      return this.cooldownManager;
   }

   public HologramManager getHologramManager() {
      return this.hologramManager;
   }

   public LocationManager getLocationManager() {
      return this.locationManager;
   }

   public VanishManager getVanishManager() {
      return this.vanishManager;
   }

   public PermissionManager getPermissionManager() {
      return this.permissionManager;
   }

   public ServerManager getServerManager() {
      return this.serverManager;
   }

   public StormCore getStormCore() {
      return this.stormCore;
   }

   public boolean isServerLog() {
      return this.serverLog;
   }

   public boolean isTagControl() {
      return this.tagControl;
   }

   public boolean isRemovePlayerDat() {
      return this.removePlayerDat;
   }

   public ChatState getChatState() {
      return this.chatState;
   }

   public boolean isBlockCommands() {
      return this.blockCommands;
   }

   public boolean isPermissionControl() {
      return this.permissionControl;
   }

   public boolean isRegisterCommands() {
      return this.registerCommands;
   }

   public Set<StatusType> getPreloadedStatus() {
      return this.preloadedStatus;
   }

   public static BukkitCommon getInstance() {
      return instance;
   }

   public void setServerLog(boolean serverLog) {
      this.serverLog = serverLog;
   }

   public void setTagControl(boolean tagControl) {
      this.tagControl = tagControl;
   }

   public void setRemovePlayerDat(boolean removePlayerDat) {
      this.removePlayerDat = removePlayerDat;
   }

   public void setChatState(ChatState chatState) {
      this.chatState = chatState;
   }

   public void setBlockCommands(boolean blockCommands) {
      this.blockCommands = blockCommands;
   }

   public void setPermissionControl(boolean permissionControl) {
      this.permissionControl = permissionControl;
   }

   public void setRegisterCommands(boolean registerCommands) {
      this.registerCommands = registerCommands;
   }

   public static enum ChatState {
      DISABLED,
      PAYMENT,
      YOUTUBER,
      ENABLED;
   }

   public class UpdateScheduler implements Runnable {
      private long currentTick;

      @Override
      public void run() {
         ++this.currentTick;
         Bukkit.getPluginManager().callEvent(new UpdateEvent(UpdateEvent.UpdateType.TICK, this.currentTick));
         if ((double)this.currentTick % 20.0 == 0.0) {
            Bukkit.getPluginManager().callEvent(new UpdateEvent(UpdateEvent.UpdateType.SECOND, this.currentTick));
         }

         if ((double)this.currentTick % 1200.0 == 0.0) {
            Bukkit.getPluginManager().callEvent(new UpdateEvent(UpdateEvent.UpdateType.MINUTE, this.currentTick));
         }
      }
   }
}

package br.com.plutomc.core.bungee;

import br.com.plutomc.core.bungee.member.BungeeParty;
import br.com.plutomc.core.common.docker.DemandDockerServer;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import br.com.plutomc.core.common.CommonConst;
import br.com.plutomc.core.common.CommonPlugin;
import br.com.plutomc.core.common.backend.redis.RedisConnection;
import br.com.plutomc.core.bungee.command.BungeeCommandFramework;
import br.com.plutomc.core.bungee.listener.DataListener;
import br.com.plutomc.core.bungee.listener.LogListener;
import br.com.plutomc.core.bungee.listener.LoginListener;
import br.com.plutomc.core.bungee.listener.MemberListener;
import br.com.plutomc.core.bungee.listener.MessageListener;
import br.com.plutomc.core.bungee.listener.ServerListener;
import br.com.plutomc.core.bungee.manager.BungeeServerManager;
import br.com.plutomc.core.bungee.manager.LoginManager;
import br.com.plutomc.core.bungee.networking.BungeePubSubHandler;
import br.com.plutomc.core.common.server.ServerManager;
import br.com.plutomc.core.common.utils.skin.Skin;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import net.md_5.bungee.connection.InitialHandler;
import net.md_5.bungee.connection.LoginResult;
import net.md_5.bungee.connection.LoginResult.Property;

public class BungeeMain extends Plugin {
   private static BungeeMain instance;
   private CommonPlugin plugin;
   private ServerManager serverManager;
   private LoginManager loginManager;
   private RedisConnection.PubSubListener pubSubListener;
   private Configuration config;
   private int playersRecord;
   private boolean whitelistEnabled;
   private long whitelistExpires;
   private List<String> whiteList = new ArrayList<>();
   private List<String> messages;

   public void onLoad() {
      instance = this;
      this.plugin = new CommonPlugin(new BungeePlatform());
      super.onLoad();
   }

   public void onEnable() {
      this.loadConfiguration();
      this.serverManager = new BungeeServerManager();
      this.loginManager = new LoginManager();
      this.getProxy().getPluginManager().registerListener(this, new DataListener());
      this.getProxy().getPluginManager().registerListener(this, new LoginListener());
      this.getProxy().getPluginManager().registerListener(this, new LogListener());
      this.getProxy().getPluginManager().registerListener(this, new MemberListener());
      this.getProxy().getPluginManager().registerListener(this, new MessageListener());
      this.getProxy().getPluginManager().registerListener(this, new ServerListener());
      ProxyServer.getInstance().getServers().remove("lobby");
      this.plugin.loadServers(this.serverManager);
      this.plugin.setServerId("bungeecord.plutomc.com.br");
      this.plugin.getServerData().startServer(3000);
      this.plugin.getServerData().setJoinEnabled(!this.whitelistEnabled);
      this.plugin.getMemberData().reloadPlugins();
      this.plugin.setPartyClass(BungeeParty.class);
      this.plugin.getReportManager().getReports().stream().forEach(report -> report.setOnline(false));
      new BungeeCommandFramework(this).loadCommands("br.com.plutomc");
      this.getProxy()
         .getScheduler()
         .runAsync(
            this,
            this.pubSubListener = new RedisConnection.PubSubListener(
               this.plugin.getRedisConnection(), new BungeePubSubHandler(), "member_field", "server_info", "server_packet"
            )
         );
      this.getProxy().getScheduler().schedule(this, () -> {
         if (!this.getMessages().isEmpty()) {
            ProxyServer.getInstance().broadcast(this.getMessages().get(CommonConst.RANDOM.nextInt(this.getMessages().size())));
         }
      }, 2L, 2L, TimeUnit.MINUTES);

      new DemandDockerServer().startServer("lobby", 25566, "1");
      super.onEnable();
   }

   public boolean isMaintenance() {
      if (this.whitelistEnabled) {
         if (this.whitelistExpires == -1L || this.whitelistExpires > System.currentTimeMillis()) {
            return true;
         }

         this.setWhitelistEnabled(false, -1L);
      }

      return false;
   }

   public void addMemberToWhiteList(String playerName) {
      if (!this.whiteList.contains(playerName.toLowerCase())) {
         this.whiteList.add(playerName.toLowerCase());
         this.getConfig().set("whiteList", this.whiteList);
         this.saveConfig();
      }
   }

   public void removeMemberFromWhiteList(String playerName) {
      if (this.whiteList.contains(playerName.toLowerCase())) {
         this.whiteList.remove(playerName.toLowerCase());
         this.getConfig().set("whiteList", this.whiteList);
         this.saveConfig();
      }
   }

   public boolean isMemberInWhiteList(String playerName) {
      return this.whiteList.contains(playerName.toLowerCase());
   }

   public void setWhitelistEnabled(boolean whitelistEnabled, long time) {
      this.whitelistEnabled = whitelistEnabled;
      this.whitelistExpires = time;
      this.getConfig().set("whitelistEnabled", whitelistEnabled);
      this.getConfig().set("whitelistExpires", this.whitelistExpires);
      this.saveConfig();
      this.plugin.getServerData().setJoinEnabled(!whitelistEnabled);
   }

   public void onDisable() {
      this.plugin.getServerData().stopServer();
      new DemandDockerServer().stopServer("lobby");
      super.onDisable();
   }

   private void loadConfiguration() {
      try {
         if (!this.getDataFolder().exists()) {
            this.getDataFolder().mkdir();
         }

         File configFile = new File(this.getDataFolder(), "config.yml");
         if (!configFile.exists()) {
            try {
               configFile.createNewFile();

               try (
                  InputStream is = this.getResourceAsStream("config.yml");
                  OutputStream os = new FileOutputStream(configFile);
               ) {
                  ByteStreams.copy(is, os);
               }
            } catch (IOException var36) {
               throw new RuntimeException("Unable to create configuration file", var36);
            }
         }

         this.config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(configFile);
         this.loadDefaultConfig(this.config);
      } catch (IOException var37) {
         var37.printStackTrace();
      }
   }

   private void loadDefaultConfig(Configuration config2) {
      this.whitelistEnabled = this.config.getBoolean("whitelistEnabled", false);
      this.whitelistExpires = this.config.getLong("whitelistExpires", -1L);
      this.whiteList = new ArrayList<>(this.config.getStringList("whiteList"));
      this.messages = this.config.getStringList("messages.broadcast");
   }

   public void addMessage(String message) {
      this.messages.add(message);
      this.config.set("messages.broadcast", this.messages);
      this.saveConfig();
   }

   public void removeMessage(int index) {
      this.messages.remove(index);
      this.config.set("messages.broadcast", this.messages);
      this.saveConfig();
   }

   public void saveConfig() {
      try {
         ConfigurationProvider.getProvider(YamlConfiguration.class).save(this.config, new File(this.getDataFolder(), "config.yml"));
      } catch (IOException var2) {
         var2.printStackTrace();
      }
   }

   public int getAveragePing(Collection<ProxiedPlayer> players) {
      int averagePing = 0;

      for(ProxiedPlayer player : players) {
         averagePing += player.getPing();
      }

      return averagePing / Math.max(ProxyServer.getInstance().getPlayers().size(), 1);
   }

   public void teleport(ProxiedPlayer player, ProxiedPlayer target) {
      player.connect(target.getServer().getInfo());
      ProxyServer.getInstance().getScheduler().schedule(getInstance(), () -> {
         ByteArrayDataOutput out = ByteStreams.newDataOutput();
         out.writeUTF("BungeeTeleport");
         out.writeUTF(target.getUniqueId().toString());
         player.getServer().sendData("BungeeCord", out.toByteArray());
      }, 300L, TimeUnit.MILLISECONDS);
   }

   public void loadTexture(PendingConnection connection, Skin skin) {
      if (skin != null) {
         InitialHandler initialHandler = (InitialHandler)connection;
         LoginResult loginProfile = initialHandler.getLoginProfile();
         Property property = new Property("textures", skin.getValue(), skin.getSignature());
         if (loginProfile == null || loginProfile == null && property == null) {
            LoginResult loginResult = new LoginResult(
               connection.getUniqueId().toString().replace("-", ""), connection.getName(), property == null ? new Property[0] : new Property[]{property}
            );

            try {
               Class<?> initialHandlerClass = connection.getClass();
               Field profileField = initialHandlerClass.getDeclaredField("loginProfile");
               profileField.setAccessible(true);
               profileField.set(connection, loginResult);
            } catch (Exception var9) {
               var9.printStackTrace();
            }
         } else if (property != null) {
            loginProfile.setProperties(new Property[]{property});
         }
      }
   }

   public CommonPlugin getPlugin() {
      return this.plugin;
   }

   public ServerManager getServerManager() {
      return this.serverManager;
   }

   public LoginManager getLoginManager() {
      return this.loginManager;
   }

   public RedisConnection.PubSubListener getPubSubListener() {
      return this.pubSubListener;
   }

   public Configuration getConfig() {
      return this.config;
   }

   public int getPlayersRecord() {
      return this.playersRecord;
   }

   public boolean isWhitelistEnabled() {
      return this.whitelistEnabled;
   }

   public long getWhitelistExpires() {
      return this.whitelistExpires;
   }

   public List<String> getWhiteList() {
      return this.whiteList;
   }

   public List<String> getMessages() {
      return this.messages;
   }

   public static BungeeMain getInstance() {
      return instance;
   }

   public void setPlayersRecord(int playersRecord) {
      this.playersRecord = playersRecord;
   }
}

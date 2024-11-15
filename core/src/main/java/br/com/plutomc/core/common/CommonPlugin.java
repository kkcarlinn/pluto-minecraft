package br.com.plutomc.core.common;

import br.com.plutomc.core.common.backend.data.impl.DiscordDataImpl;
import br.com.plutomc.core.common.backend.data.impl.ServerDataImpl;
import br.com.plutomc.core.common.backend.mongodb.MongoConnection;
import br.com.plutomc.core.common.backend.redis.RedisConnection;
import br.com.plutomc.core.common.account.party.Party;
import br.com.plutomc.core.common.permission.Group;
import br.com.plutomc.core.common.server.ServerManager;
import br.com.plutomc.core.common.server.ServerType;
import br.com.plutomc.core.common.server.loadbalancer.BaseBalancer;
import br.com.plutomc.core.common.server.loadbalancer.server.MinigameServer;
import br.com.plutomc.core.common.server.loadbalancer.server.MinigameState;
import br.com.plutomc.core.common.server.loadbalancer.server.ProxiedServer;
import br.com.plutomc.core.common.utils.configuration.DefaultFileCreator;
import br.com.plutomc.core.common.utils.mojang.NameFetcher;
import br.com.plutomc.core.common.utils.mojang.UUIDFetcher;
import com.google.common.base.Charsets;
import com.google.gson.stream.JsonReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.logging.Logger;
import br.com.plutomc.core.common.backend.data.DiscordData;
import br.com.plutomc.core.common.backend.data.AccountData;
import br.com.plutomc.core.common.backend.data.PartyData;
import br.com.plutomc.core.common.backend.data.ServerData;
import br.com.plutomc.core.common.backend.data.SkinData;
import br.com.plutomc.core.common.backend.data.impl.AccountDataImpl;
import br.com.plutomc.core.common.backend.data.impl.PartyDataImpl;
import br.com.plutomc.core.common.backend.data.impl.SkinDataImpl;
import br.com.plutomc.core.common.manager.ConfigurationManager;
import br.com.plutomc.core.common.manager.AccountManager;
import br.com.plutomc.core.common.manager.PartyManager;
import br.com.plutomc.core.common.manager.ReportManager;
import br.com.plutomc.core.common.manager.StatusManager;
import br.com.plutomc.core.common.packet.types.configuration.ConfigurationFieldUpdate;
import br.com.plutomc.core.common.packet.types.configuration.ConfigurationUpdate;
import br.com.plutomc.core.common.utils.FileCreator;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommonPlugin {

   @Getter
   private static CommonPlugin instance;
   private PluginPlatform pluginPlatform;
   private PluginInfo pluginInfo;
   private FileCreator fileCreator;
   private ConfigurationManager configurationManager = new ConfigurationManager();
   private AccountManager accountManager = new AccountManager();
   private PartyManager partyManager = new PartyManager();
   private StatusManager statusManager = new StatusManager();
   private ReportManager reportManager = new ReportManager();
   private DiscordData discordData;
   private AccountData accountData;
   private PartyData partyData;
   private ServerData serverData;
   private SkinData skinData;
   private UUIDFetcher uuidFetcher = new UUIDFetcher();
   private NameFetcher nameFetcher = new NameFetcher();
   private String serverId = "plutomc.com.br";
   private String serverAddress = "0.0.0.0";
   private ServerType serverType = ServerType.BUNGEECORD;
   private boolean joinEnabled = true;
   private String map = "Unknown";
   private MinigameState minigameState = MinigameState.NONE;
   private int serverTime;
   private Class<? extends Party> partyClass;
   private MongoConnection mongoConnection;
   private RedisConnection redisConnection;

   public static void set(Group group, int id) throws Exception {
      Field field = Group.class.getDeclaredField("id");
      field.setAccessible(true);
      field.set(group, id);
   }

   public CommonPlugin(PluginPlatform pluginPlatform) {
      instance = this;
      this.pluginPlatform = pluginPlatform;
      this.fileCreator = new DefaultFileCreator();
      this.loadConfig();

      try {
         this.mongoConnection = new MongoConnection(this.pluginInfo.getMongoCredentials());
         this.redisConnection = new RedisConnection(this.pluginInfo.getRedisCredentials());
         this.mongoConnection.connect();
         this.redisConnection.connect();
         this.setDiscordData(new DiscordDataImpl(this.redisConnection));
         this.setAccountData(new AccountDataImpl(this.mongoConnection, this.redisConnection));
         this.setPartyData(new PartyDataImpl(this.mongoConnection));
         this.setServerData(new ServerDataImpl(this.redisConnection));
         this.setSkinData(new SkinDataImpl(this.redisConnection));
      } catch (Exception var3) {
         pluginPlatform.shutdown("The database has not loaded!");
         var3.printStackTrace();
      }

      this.reportManager.loadReports();
   }

   public void loadConfig() {
      try {
         FileInputStream fileInputStream = new FileInputStream(this.fileCreator.createFile("server.json", CommonConst.PRINCIPAL_DIRECTORY));
         InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, StandardCharsets.UTF_8);
         JsonReader jsonReader = new JsonReader(inputStreamReader);
         this.pluginInfo = CommonConst.GSON_PRETTY.fromJson(jsonReader, PluginInfo.class);
         jsonReader.close();
         inputStreamReader.close();
         fileInputStream.close();
      } catch (Exception var4) {
         var4.printStackTrace();
         this.pluginPlatform.shutdown("The configuration server.json not found!");
      }
   }

   public void saveConfig(String fieldName) {
      try {
         this.pluginInfo.sort();
         String json = CommonConst.GSON_PRETTY.toJson(this.pluginInfo);
         FileOutputStream fileOutputStream = new FileOutputStream(this.fileCreator.createFile("server.json", CommonConst.PRINCIPAL_DIRECTORY));
         OutputStreamWriter outputStreamReader = new OutputStreamWriter(fileOutputStream, StandardCharsets.UTF_8);
         BufferedWriter bufferedWriter = new BufferedWriter(outputStreamReader);
         bufferedWriter.write(json);
         bufferedWriter.flush();
         bufferedWriter.close();
         fileOutputStream.close();
         outputStreamReader.close();
      } catch (Exception var6) {
         var6.printStackTrace();
         this.pluginPlatform.shutdown("The configuration server.json not found!");
      }

      getInstance().getServerData().sendPacket(new ConfigurationFieldUpdate(fieldName));
   }

   public void saveConfig() {
      try {
         this.pluginInfo.sort();
         String json = CommonConst.GSON_PRETTY.toJson(this.pluginInfo);
         FileOutputStream fileOutputStream = new FileOutputStream(this.fileCreator.createFile("server.json", CommonConst.PRINCIPAL_DIRECTORY));
         OutputStreamWriter outputStreamReader = new OutputStreamWriter(fileOutputStream, StandardCharsets.UTF_8);
         BufferedWriter bufferedWriter = new BufferedWriter(outputStreamReader);
         bufferedWriter.write(json);
         bufferedWriter.flush();
         bufferedWriter.close();
         fileOutputStream.close();
         outputStreamReader.close();
      } catch (Exception var5) {
         var5.printStackTrace();
         this.pluginPlatform.shutdown("The configuration server.json not found!");
      }

      getInstance().getServerData().sendPacket(new ConfigurationUpdate());
   }

   public Logger getLogger() {
      return this.pluginPlatform.getLogger();
   }

   public void debug(String message) {
      if (this.pluginInfo.isDebug()) {
         System.out.println("[DEBUG] (" + serverId + ") " + message);
      }
   }

   public void loadServers(ServerManager serverManager) {
      for(Entry<String, Map<String, String>> entry : this.getServerData().loadServers().entrySet()) {
         try {
            if (entry.getValue().containsKey("type")
               && entry.getValue().containsKey("address")
               && entry.getValue().containsKey("maxplayers")
               && ServerType.valueOf(entry.getValue().get("type").toUpperCase()) != ServerType.BUNGEECORD) {
               ProxiedServer server = serverManager.addActiveServer(
                  entry.getValue().get("address"),
                  entry.getKey(),
                  ServerType.valueOf(entry.getValue().get("type").toUpperCase()),
                  Integer.valueOf(entry.getValue().get("maxplayers")),
                  Long.valueOf(entry.getValue().get("starttime"))
               );
               server.setOnlinePlayers(this.getServerData().getPlayers(entry.getKey()));
               server.setJoinEnabled(Boolean.valueOf(entry.getValue().get("joinenabled")));
               if (server instanceof MinigameServer) {
                  MinigameServer minigameServer = (MinigameServer)server;
                  minigameServer.setTime(this.getServerData().getTime(entry.getKey()));
                  minigameServer.setMap(this.getServerData().getMap(entry.getKey()));
                  minigameServer.setState(this.getServerData().getState(entry.getKey()));
               }

               this.debug(
                  "The server "
                     + server.getServerId()
                     + " ("
                     + server.getServerType()
                     + " - "
                     + server.getOnlinePlayers()
                     + "/"
                     + server.getMaxPlayers()
                     + ") has been loaded!"
               );
            }
         } catch (Exception var6) {
         }
      }

      int players = 0;

      for(BaseBalancer<?> server : serverManager.getBalancers().values()) {
         players += server.getTotalNumber();
      }

      serverManager.setTotalPlayers(players);
   }

   public UUID getUniqueId(String name) {
      return this.getUniqueId(name, true);
   }

   public UUID getUniqueId(String name, boolean cracked) {
      UUID uniqueId = this.getAccountData().getUniqueId(name);
      if (uniqueId != null) {
         return uniqueId;
      } else {
         uniqueId = this.uuidFetcher.getUUID(name);
         return uniqueId == null && cracked ? UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(Charsets.UTF_8)) : uniqueId;
      }
   }

   public String formatTime(long time) {
      String[] all = CommonConst.FULL_DATE_FORMAT.format(time).split(" ");
      String month = all[2];
      String[] dates = all[0].split("/");
      return dates[0] + " de " + month.toLowerCase() + " de " + dates[2] + " às " + all[1];
   }

   public static CommonPlugin createVoid() {
      return new CommonPlugin(new PluginPlatform() {
         @Override
         public void shutdown(String message) {
         }

         @Override
         public void runAsync(Runnable runnable, long delay, long repeat) {
            runnable.run();
         }

         @Override
         public void runAsync(Runnable runnable, long delay) {
            runnable.run();
         }

         @Override
         public void runAsync(Runnable runnable) {
            runnable.run();
         }

         @Override
         public void run(Runnable runnable, long delay, long repeat) {
            runnable.run();
         }

         @Override
         public void run(Runnable runnable, long delay) {
            runnable.run();
         }

         @Override
         public UUID getUniqueId(String playerName) {
            return null;
         }

         @Override
         public Logger getLogger() {
            return Logger.getLogger("premium");
         }

         @Override
         public void dispatchCommand(String command) {
         }

         @Override
         public void broadcast(String string) {
         }

         @Override
         public void broadcast(String string, String permission) {
         }

         @Override
         public String getName(UUID uuid) {
            return null;
         }
      });
   }


}

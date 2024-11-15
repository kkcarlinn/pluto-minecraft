package br.com.plutomc.core.common.backend.data.impl;

import br.com.plutomc.core.common.CommonConst;
import br.com.plutomc.core.common.CommonPlugin;
import br.com.plutomc.core.common.backend.Query;
import br.com.plutomc.core.common.backend.data.AccountData;
import br.com.plutomc.core.common.backend.mongodb.MongoConnection;
import br.com.plutomc.core.common.backend.mongodb.MongoQuery;
import br.com.plutomc.core.common.backend.redis.RedisConnection;
import br.com.plutomc.core.common.account.Account;
import br.com.plutomc.core.common.account.AccountVoid;
import br.com.plutomc.core.common.account.status.Status;
import br.com.plutomc.core.common.account.status.StatusType;
import br.com.plutomc.core.common.packet.types.ReportFieldPacket;
import br.com.plutomc.core.common.permission.Group;
import br.com.plutomc.core.common.report.Report;
import br.com.plutomc.core.common.utils.json.JsonBuilder;
import br.com.plutomc.core.common.utils.json.JsonUtils;
import br.com.plutomc.core.common.utils.string.StringFormat;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import br.com.plutomc.core.common.packet.types.ReportCreatePacket;
import br.com.plutomc.core.common.packet.types.ReportDeletePacket;
import org.bson.Document;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;

public class AccountDataImpl implements AccountData {
   private RedisConnection redisDatabase;
   private MongoQuery query;
   private MongoQuery statusQuery;
   private MongoQuery reportQuery;

   public AccountDataImpl(MongoConnection mongoConnection, RedisConnection redisDatabase) {
      this.query = MongoQuery.createDefault(mongoConnection, mongoConnection.getDataBase(), "members");
      this.statusQuery = MongoQuery.createDefault(mongoConnection, mongoConnection.getDataBase(), "status");
      this.reportQuery = MongoQuery.createDefault(mongoConnection, mongoConnection.getDataBase(), "report");
      this.redisDatabase = redisDatabase;
   }

   public AccountDataImpl(Query<JsonElement> query, RedisConnection redisDatabase) {
      this.query = (MongoQuery)query;
      this.redisDatabase = redisDatabase;
   }

   @Override
   public Account loadAccount(UUID uuid) {
      return this.loadAccount(uuid, AccountVoid.class);
   }

   @Override
   public <T extends Account> Collection<T> loadMembersByAddress(String ipAddress, Class<T> clazz) {
      return this.query.find("lastIpAddress", ipAddress).stream().map(json -> CommonConst.GSON.fromJson(json, clazz)).collect(Collectors.toList());
   }

   @Override
   public <T extends Account> T loadAccount(UUID uuid, Class<T> clazz) {
      Account account = CommonPlugin.getInstance().getAccountManager().getAccount(uuid);
      if (account == null) {
         account = this.getRedisPlayer(uuid, clazz);
         if (account == null) {
            JsonElement found = this.query.findOne("uniqueId", uuid.toString());
            if (found != null) {
               account = CommonConst.GSON.fromJson(CommonConst.GSON.toJson(found), clazz);
            }
         }
      }

      return account == null ? null : clazz.cast(account);
   }

   @Override
   public <T extends Account> T loadAccount(String playerName, boolean ignoreCase, Class<T> clazz) {
      return (T)(ignoreCase
         ? this.loadAccount(this.query.findOne("playerName", new Document("$regex", "^" + playerName + "$").append("$options", "i")), clazz)
         : this.loadAccount(this.query.findOne("playerName", playerName), clazz));
   }

   @Override
   public Account loadAccount(String playerName, boolean ignoreCase) {
      return this.loadAccount(playerName, ignoreCase, AccountVoid.class);
   }

   @Override
   public <T extends Account> T loadAccount(String key, String value, boolean ignoreCase, Class<T> clazz) {
      return (T)(ignoreCase
         ? this.loadAccount(this.query.findOne(key, new Document("$regex", "^" + value + "$").append("$options", "i")), clazz)
         : this.loadAccount(this.query.findOne(key, value), clazz));
   }

   public <T extends Account> T loadAccount(JsonElement jsonElement, Class<T> clazz) {
      return jsonElement == null ? null : CommonConst.GSON.fromJson(CommonConst.GSON.toJson(jsonElement), clazz);
   }

   @Override
   public void reloadPlugins() {
      try (Jedis jedis = CommonPlugin.getInstance().getRedisConnection().getPool().getResource()) {
         for(String playerId : jedis.keys("account:*")) {
            jedis.del("account:" + playerId);
         }
      }

      this.query.getCollection().updateMany(new Document(), Filters.eq("$set", Filters.eq("loginConfiguration.logged", false)));
      this.query.getCollection().updateMany(new Document(), Filters.eq("$set", Filters.eq("online", false)));
   }

   public <T extends Account> T getRedisPlayer(UUID uuid, Class<T> clazz) {
      Account player;
      try (Jedis jedis = this.redisDatabase.getPool().getResource()) {
         if (!jedis.exists("account:" + uuid.toString())) {
            return null;
         }

         Map<String, String> fields = jedis.hgetAll("account:" + uuid.toString());
         if (fields == null || fields.isEmpty() || fields.size() < Account.class.getDeclaredFields().length - 1) {
            return null;
         }

         player = JsonUtils.mapToObject(fields, clazz);
      }

      return clazz.cast(player);
   }

   @Override
   public boolean createMember(Account account) {
      boolean needCreate = this.query.findOne("uniqueId", account.getUniqueId().toString()) == null;
      if (needCreate) {
         this.query.create(new String[]{CommonConst.GSON.toJson(account)});
      }

      return needCreate;
   }

   @Override
   public void saveRedisMember(Account account) {
      try (Jedis jedis = this.redisDatabase.getPool().getResource()) {
         jedis.hmset("account:" + account.getUniqueId().toString(), JsonUtils.objectToMap(account));
      }
   }

   @Override
   public boolean deleteMember(UUID uniqueId) {
      boolean needCreate = this.query.findOne("uniqueId", uniqueId.toString()) == null;
      if (!needCreate) {
         this.query.deleteOne("uniqueId", uniqueId.toString());

         try (Jedis jedis = this.redisDatabase.getPool().getResource()) {
            jedis.del("account:" + uniqueId.toString());
         }

         return true;
      } else {
         return false;
      }
   }

   @Override
   public void updateAccount(final Account account, final String fieldName) {
      CommonPlugin.getInstance()
         .getPluginPlatform()
         .runAsync(
            new Runnable() {
               @Override
               public void run() {
                  JsonObject tree = JsonUtils.jsonTree(account);
                  if (tree.has(fieldName)) {
                     JsonElement element = tree.get(fieldName);
      
                     try (Jedis jedis = AccountDataImpl.this.redisDatabase.getPool().getResource()) {
                        Pipeline pipe = jedis.pipelined();
                        jedis.hset("account:" + account.getUniqueId().toString(), fieldName, JsonUtils.elementToString(element));
                        JsonObject json = new JsonObject();
                        json.add("uniqueId", new JsonPrimitive(account.getUniqueId().toString()));
                        json.add("source", new JsonPrimitive(CommonPlugin.getInstance().getServerId()));
                        json.add("field", new JsonPrimitive(fieldName));
                        json.add("value", element);
                        pipe.publish("member_field", json.toString());
                        pipe.sync();
                     } catch (Exception var17) {
                        var17.printStackTrace();
                     }
                  }
      
                  AccountDataImpl.this.query
                     .updateOne(
                        "uniqueId",
                        account.getUniqueId().toString(),
                        new JsonBuilder().addProperty("fieldName", fieldName).add("value", tree.get(fieldName)).build()
                     );
               }
            }
         );
   }

   @Override
   public void cacheMember(UUID uniqueId) {
      try (Jedis jedis = this.redisDatabase.getPool().getResource()) {
         jedis.expire("account:" + uniqueId.toString(), 300);
      }
   }

   @Override
   public boolean checkCache(UUID uniqueId) {
      boolean bool = false;

      try (Jedis jedis = this.redisDatabase.getPool().getResource()) {
         String key = "account:" + uniqueId.toString();
         if (jedis.ttl(key) >= 0L) {
            bool = jedis.persist(key) == 1L;
         }
      } catch (Exception var16) {
         var16.printStackTrace();
      }

      return bool;
   }

   @Override
   public List<Account> getAccountsByGroup(Group group) {
      List<Account> list = new ArrayList<>();
      MongoCursor<Document> iterator = this.query
         .getCollection()
         .find(Filters.eq("groups." + group.getGroupName().toLowerCase(), Filters.eq("$exists", true)))
         .limit(50)
         .iterator();

      while(iterator.hasNext()) {
         list.add(CommonConst.GSON.fromJson(CommonConst.GSON.toJson(iterator.next()), AccountVoid.class));
      }

      return list;
   }

   @Override
   public void closeConnection() {
      this.redisDatabase.close();
   }

   @Override
   public boolean isRedisCached(String playerName) {
      boolean bool = false;

      try (Jedis jedis = this.redisDatabase.getPool().getResource()) {
         String key = "member:mojang-fetcher:" + playerName.toLowerCase();
         long ttl = jedis.ttl(key);
         if (ttl == -1L) {
            bool = true;
         } else if (ttl > 0L) {
            bool = jedis.persist(key) == 1L;
         }
      } catch (Exception var18) {
         var18.printStackTrace();
      }

      return bool;
   }

   @Override
   public boolean isConnectionPremium(String playerName) {
      boolean bool = false;

      try (Jedis jedis = this.redisDatabase.getPool().getResource()) {
         String key = "member:mojang-fetcher:" + playerName.toLowerCase();
         bool = StringFormat.parseBoolean(jedis.hget(key, "premium")).getAsBoolean();
      } catch (Exception var16) {
         var16.printStackTrace();
      }

      return bool;
   }

   @Override
   public void setConnectionStatus(String playerName, UUID uniqueId, boolean premium) {
      try (Jedis jedis = this.redisDatabase.getPool().getResource()) {
         String key = "member:mojang-fetcher:" + playerName.toLowerCase();
         jedis.hset(key, "premium", "" + premium);
         jedis.hset(key, "uniqueId", uniqueId.toString());
         jedis.expire(key, 900);
      }
   }

   @Override
   public UUID getUniqueId(String playerName) {
      UUID uniqueId = null;

      try (Jedis jedis = this.redisDatabase.getPool().getResource()) {
         String key = "member:mojang-fetcher:" + playerName.toLowerCase();
         if (jedis.exists(key)) {
            uniqueId = UUID.fromString(jedis.hget(key, "uniqueId"));
         }
      } catch (Exception var16) {
         var16.printStackTrace();
      }

      return uniqueId;
   }

   @Override
   public void cacheConnection(String playerName, boolean premium) {
      try (Jedis jedis = this.redisDatabase.getPool().getResource()) {
         jedis.expire("member:mojang-fetcher:" + playerName.toLowerCase(), 3600);
      }
   }

   @Override
   public boolean isDiscordCached(String discordId) {
      boolean bool = false;

      try (Jedis jedis = this.redisDatabase.getPool().getResource()) {
         String key = "discord:" + discordId;
         if (jedis.ttl(key) >= 0L) {
            bool = jedis.persist(key) == 1L;
         }
      } catch (Exception var16) {
         var16.printStackTrace();
      }

      return bool;
   }

   @Override
   public UUID getUniqueIdFromDiscord(String discordId) {
      UUID uniqueId = null;

      try (Jedis jedis = this.redisDatabase.getPool().getResource()) {
         String key = "discord:" + discordId;
         if (jedis.exists(key)) {
            uniqueId = UUID.fromString(jedis.get(key));
         }
      } catch (Exception var16) {
         var16.printStackTrace();
      }

      return uniqueId;
   }

   @Override
   public void setDiscordCache(String discordId, UUID uniqueId) {
      try (Jedis jedis = this.redisDatabase.getPool().getResource()) {
         jedis.set("discord:" + discordId, "uniqueId", uniqueId.toString());
      }
   }

   @Override
   public void deleteDiscordCache(String discordId) {
      try (Jedis jedis = this.redisDatabase.getPool().getResource()) {
         jedis.del("discord:" + discordId);
      }
   }

   @Override
   public Status loadStatus(UUID uniqueId, StatusType statusType) {
      Document document = this.statusQuery
         .getDatabase()
         .getCollection("status-" + statusType.name().toLowerCase())
         .find(Filters.eq("uniqueId", uniqueId.toString()))
         .first();
      if (document == null) {
         Status status = new Status(uniqueId, statusType);
         this.createStatus(status);
         return status;
      } else {
         return CommonConst.GSON.fromJson(CommonConst.GSON.toJson(document), Status.class);
      }
   }

   @Override
   public boolean createStatus(Status status) {
      Document document = this.statusQuery
         .getDatabase()
         .getCollection("status-" + status.getStatusType().name().toLowerCase())
         .find(Filters.eq("uniqueId", status.getUniqueId().toString()))
         .first();
      if (document == null) {
         this.statusQuery
            .getDatabase()
            .getCollection("status-" + status.getStatusType().name().toLowerCase())
            .insertOne(Document.parse(CommonConst.GSON.toJson(status)));
         return true;
      } else {
         return false;
      }
   }

   @Override
   public void saveStatus(final Status status, final String fieldName) {
      CommonPlugin.getInstance()
         .getPluginPlatform()
         .runAsync(
            new Runnable() {
               @Override
               public void run() {
                  JsonObject tree = JsonUtils.jsonTree(status);
                  JsonObject jsonObject = new JsonBuilder().addProperty("fieldName", fieldName).add("value", tree.get(fieldName)).build();
                  Object object = JsonUtils.elementToBson(jsonObject.get("value"));
                  if (object != null && !jsonObject.get("value").isJsonNull()) {
                     AccountDataImpl.this.statusQuery
                        .getDatabase()
                        .getCollection("status-" + status.getStatusType().name().toLowerCase())
                        .updateOne(
                           Filters.eq("uniqueId", status.getUniqueId().toString()),
                           new Document("$set", new Document(jsonObject.get("fieldName").getAsString(), object))
                        );
                  } else {
                     AccountDataImpl.this.statusQuery
                        .getDatabase()
                        .getCollection("status-" + status.getStatusType().name().toLowerCase())
                        .updateOne(
                           Filters.eq("uniqueId", status.getUniqueId().toString()),
                           new Document("$unset", new Document(jsonObject.get("fieldName").getAsString(), ""))
                        );
                  }
               }
            }
         );
   }

   @Override
   public Collection<Report> loadReports() {
      return this.reportQuery.find().stream().map(json -> CommonConst.GSON.fromJson(json, Report.class)).collect(Collectors.toList());
   }

   @Override
   public void createReport(Report report) {
      JsonElement jsonElement = this.reportQuery.findOne("reportId", report.getReportId().toString());
      if (jsonElement == null) {
         this.reportQuery.create(new String[]{CommonConst.GSON.toJson(report)});
      }

      CommonPlugin.getInstance().getPluginPlatform().runAsync(() -> CommonPlugin.getInstance().getServerData().sendPacket(new ReportCreatePacket(report)));
   }

   @Override
   public void deleteReport(UUID reportId) {
      CommonPlugin.getInstance().getPluginPlatform().runAsync(() -> CommonPlugin.getInstance().getServerData().sendPacket(new ReportDeletePacket(reportId)));
      this.reportQuery.deleteOne("reportId", reportId.toString());
   }

   @Override
   public void updateReport(final Report report, final String fieldName) {
      CommonPlugin.getInstance()
         .getPluginPlatform()
         .runAsync(
            new Runnable() {
               @Override
               public void run() {
                  JsonObject tree = JsonUtils.jsonTree(report);
                  CommonPlugin.getInstance().getServerData().sendPacket(new ReportFieldPacket(report, fieldName));
                  AccountDataImpl.this.reportQuery
                     .updateOne(
                        "reportId",
                        report.getReportId().toString(),
                        new JsonBuilder().addProperty("fieldName", fieldName).add("value", tree.get(fieldName)).build()
                     );
               }
            }
         );
   }

   public RedisConnection getRedisDatabase() {
      return this.redisDatabase;
   }

   public MongoQuery getQuery() {
      return this.query;
   }

   public MongoQuery getStatusQuery() {
      return this.statusQuery;
   }

   public MongoQuery getReportQuery() {
      return this.reportQuery;
   }
}

package br.com.plutomc.core.common.utils.mojang;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import br.com.plutomc.core.common.CommonPlugin;

public class UUIDFetcher {
   private List<String> apis = new ArrayList<>();
   private LoadingCache<String, UUID> cache = CacheBuilder.newBuilder()
      .expireAfterWrite(1L, TimeUnit.DAYS)
      .expireAfterAccess(1L, TimeUnit.DAYS)
      .build(new CacheLoader<String, UUID>() {
         public UUID load(String name) throws Exception {
            UUID uuid = CommonPlugin.getInstance().getPluginPlatform().getUniqueId(name);
            return uuid == null ? UUIDFetcher.this.cacheOnlyId.get(name) : uuid;
         }
      });
   private LoadingCache<String, UUID> cacheOnlyId = CacheBuilder.newBuilder()
      .expireAfterWrite(1L, TimeUnit.DAYS)
      .expireAfterAccess(1L, TimeUnit.DAYS)
      .build(new CacheLoader<String, UUID>() {
         public UUID load(String name) throws Exception {
            return UUIDFetcher.this.request(name);
         }
      });

   public UUIDFetcher() {
      this.apis.add("https://api.mojang.com/users/profiles/minecraft/%s");
      this.apis.add("https://api.mcuuid.com/json/uuid/%s");
      this.apis.add("https://api.minetools.eu/uuid/%s");
   }

   public UUID request(String name) {
      return this.request(0, this.apis.get(0), name);
   }

   public UUID request(int idx, String api, String name) {
      try {
         URLConnection con = new URL(String.format(api, name)).openConnection();
         JsonElement element = JsonParser.parseReader(new BufferedReader(new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8)));
         if (element instanceof JsonObject) {
            JsonObject object = (JsonObject)element;
            if (object.has("error") && object.has("errorMessage")) {
               throw new Exception(object.get("errorMessage").getAsString());
            }

            if (object.has("id")) {
               return UUIDParser.parse(object.get("id"));
            }

            if (object.has("uuid")) {
               JsonObject uuid = object.getAsJsonObject("uuid");
               if (uuid.has("formatted")) {
                  return UUIDParser.parse(object.get("formatted"));
               }
            }
         }
      } catch (Exception var8) {
         if (++idx < this.apis.size()) {
            api = this.apis.get(idx);
            return this.request(idx, api, name);
         }
      }

      return null;
   }

   public UUID getUUID(String name) {
      if (name == null || name.isEmpty()) {
         return null;
      } else if (!name.matches("[a-zA-Z0-9_]{3,16}")) {
         return UUIDParser.parse(name);
      } else {
         try {
            return this.cache.get(name);
         } catch (Exception var3) {
            return null;
         }
      }
   }

   public UUID getUniqueId(String name) {
      if (name == null || name.isEmpty()) {
         return null;
      } else if (!name.matches("[a-zA-Z0-9_]{3,16}")) {
         return UUIDParser.parse(name);
      } else {
         try {
            return this.cacheOnlyId.get(name);
         } catch (Exception var3) {
            return null;
         }
      }
   }

   public class Response {
      private UUID uniqueId;
      private boolean success;

      public UUID getUniqueId() {
         return this.uniqueId;
      }

      public boolean isSuccess() {
         return this.success;
      }

      public Response(UUID uniqueId, boolean success) {
         this.uniqueId = uniqueId;
         this.success = success;
      }

      public Response() {
      }
   }
}

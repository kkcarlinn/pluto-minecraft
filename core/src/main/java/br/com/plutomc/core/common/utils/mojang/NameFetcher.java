package br.com.plutomc.core.common.utils.mojang;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.gson.JsonArray;
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

public class NameFetcher {
   private List<String> apis = new ArrayList<>();
   private LoadingCache<UUID, String> cache = CacheBuilder.newBuilder().expireAfterWrite(1L, TimeUnit.DAYS).build(new CacheLoader<UUID, String>() {
      public String load(UUID uuid) throws Exception {
         String name = CommonPlugin.getInstance().getPluginPlatform().getName(uuid);
         return name == null ? NameFetcher.this.request(uuid) : name;
      }
   });

   public NameFetcher() {
      this.apis.add("https://api.mojang.com/user/profiles/%s/names");
      this.apis.add("https://sessionserver.mojang.com/session/minecraft/profile/%s");
      this.apis.add("https://api.mcuuid.com/json/name/%s");
      this.apis.add("https://api.minetools.eu/uuid/%s");
   }

   private String request(UUID uuid) {
      return this.request(0, this.apis.get(0), uuid);
   }

   private String request(int idx, String api, UUID uuid) {
      try {
         URLConnection con = new URL(String.format(api, uuid.toString().replace("-", ""))).openConnection();
         JsonElement element = JsonParser.parseReader(new BufferedReader(new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8)));
         if (element instanceof JsonArray) {
            JsonArray names = (JsonArray)element;
            JsonObject name = (JsonObject)names.get(names.size() - 1);
            if (name.has("name")) {
               return name.get("name").getAsString();
            }
         } else if (element instanceof JsonObject) {
            JsonObject object = (JsonObject)element;
            if (object.has("error") && object.has("errorMessage")) {
               throw new Exception(object.get("errorMessage").getAsString());
            }

            if (object.has("name")) {
               return object.get("name").getAsString();
            }
         }
      } catch (Exception var8) {
         if (++idx < this.apis.size()) {
            api = this.apis.get(idx);
            return this.request(idx, api, uuid);
         }
      }

      return null;
   }

   public String getName(UUID uuid) {
      try {
         return this.cache.get(uuid);
      } catch (Exception var3) {
         return null;
      }
   }
}

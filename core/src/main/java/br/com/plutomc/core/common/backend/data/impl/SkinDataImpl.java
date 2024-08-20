package br.com.plutomc.core.common.backend.data.impl;

import br.com.plutomc.core.common.CommonPlugin;
import br.com.plutomc.core.common.backend.data.SkinData;
import br.com.plutomc.core.common.backend.redis.RedisConnection;
import br.com.plutomc.core.common.utils.json.JsonUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import br.com.plutomc.core.common.utils.skin.Skin;
import redis.clients.jedis.Jedis;

public class SkinDataImpl implements SkinData {
   private static final String BASE_PATH = "skin-data:";
   private static final int TIME_TO_EXPIRE = 7200;
   private RedisConnection redisConnection;

   public SkinDataImpl(RedisConnection redisConnection) {
      this.redisConnection = redisConnection;
   }

   @Override
   public Optional<Skin> loadData(String playerName) {
      try (Jedis jedis = this.redisConnection.getPool().getResource()) {
         boolean exists = jedis.ttl("skin-data:" + playerName.toLowerCase()) >= 0L;
         if (exists) {
            Map<String, String> fields = jedis.hgetAll("skin-data:" + playerName.toLowerCase());
            if (fields != null && !fields.isEmpty()) {
               return Optional.of(JsonUtils.mapToObject(fields, Skin.class));
            }
         }
      }

      UUID uniqueId = CommonPlugin.getInstance().getUniqueId(playerName);
      if (uniqueId == null) {
         return null;
      } else {
         String[] skin = this.loadSkinById(uniqueId);
         if (skin == null) {
            return Optional.empty();
         } else {
            Skin skinData = new Skin(playerName, uniqueId, skin[0], skin[1]);
            this.save(skinData, 7200);
            return Optional.of(skinData);
         }
      }
   }

   @Override
   public void save(Skin skin, int seconds) {
      try (Jedis jedis = this.redisConnection.getPool().getResource()) {
         jedis.hmset("skin-data:" + skin.getPlayerName().toLowerCase(), JsonUtils.objectToMap(skin));
         jedis.expire("skin-data:" + skin.getPlayerName().toLowerCase(), 259200);
         jedis.save();
      }
   }

   @Override
   public String[] loadSkinById(UUID uuid) {
      try {
         URLConnection con = new URL(String.format("https://sessionserver.mojang.com/session/minecraft/profile/%s?unsigned=false", uuid.toString()))
            .openConnection();
         JsonElement element = JsonParser.parseReader(new BufferedReader(new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8)));
         if (element instanceof JsonObject) {
            JsonObject object = element.getAsJsonObject();
            if (object.has("properties")) {
               JsonArray jsonArray = object.get("properties").getAsJsonArray();
               JsonObject jsonObject = jsonArray.get(0).getAsJsonObject();
               String value = jsonObject.get("value").getAsString();
               String signature = jsonObject.has("signature") ? jsonObject.get("signature").getAsString() : "";
               return new String[]{value, signature};
            }
         }
      } catch (Exception var9) {
         var9.printStackTrace();
      }

      return null;
   }
}

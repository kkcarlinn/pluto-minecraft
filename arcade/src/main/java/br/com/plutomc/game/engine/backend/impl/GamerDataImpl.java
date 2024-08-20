package br.com.plutomc.game.engine.backend.impl;

import br.com.plutomc.game.engine.ArcadeCommon;
import br.com.plutomc.game.engine.backend.GamerData;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.Optional;
import java.util.UUID;
import br.com.plutomc.core.common.CommonConst;
import br.com.plutomc.core.common.CommonPlugin;
import br.com.plutomc.core.common.backend.mongodb.MongoQuery;
import br.com.plutomc.game.engine.gamer.Gamer;
import br.com.plutomc.core.common.utils.json.JsonBuilder;
import br.com.plutomc.core.common.utils.json.JsonUtils;

public class GamerDataImpl implements GamerData {
   private MongoQuery query = MongoQuery.createDefault(CommonPlugin.getInstance().getMongoConnection(), ArcadeCommon.getInstance().getCollectionName());

   @Override
   public <T extends Gamer> Optional<T> loadGamer(UUID uniqueId) {
      JsonElement jsonElement = this.query.findOne("uniqueId", uniqueId.toString());
      return jsonElement == null ? Optional.empty() : (Optional<T>) Optional.of(CommonConst.GSON.fromJson(jsonElement, ArcadeCommon.getInstance().getGamerClass()));
   }

   @Override
   public void createGamer(Gamer gamer) {
      JsonElement jsonElement = this.query.findOne("uniqueId", gamer.getUniqueId().toString());
      if (jsonElement == null) {
         this.query.create(new String[]{CommonConst.GSON.toJson(gamer)});
      }
   }

   @Override
   public void saveGamer(final Gamer gamer, final String fieldName) {
      CommonPlugin.getInstance()
         .getPluginPlatform()
         .runAsync(
            new Runnable() {
               @Override
               public void run() {
                  JsonObject tree = JsonUtils.jsonTree(gamer);
                  GamerDataImpl.this.query
                     .updateOne(
                        "uniqueId",
                        gamer.getUniqueId().toString(),
                        new JsonBuilder().addProperty("fieldName", fieldName).add("value", tree.get(fieldName)).build()
                     );
               }
            }
         );
   }
}

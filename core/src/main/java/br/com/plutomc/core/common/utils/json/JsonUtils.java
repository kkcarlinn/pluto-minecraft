package br.com.plutomc.core.common.utils.json;

import br.com.plutomc.core.common.CommonConst;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bson.BsonInvalidOperationException;
import org.bson.Document;

public class JsonUtils {
   public static JsonObject jsonTree(Object src) {
      return CommonConst.GSON.toJsonTree(src).getAsJsonObject();
   }

   public static Object elementToBson(JsonElement element) {
      if (element.isJsonPrimitive()) {
         JsonPrimitive primitive = element.getAsJsonPrimitive();
         if (primitive.isString()) {
            return primitive.getAsString();
         }

         if (primitive.isNumber()) {
            return primitive.getAsNumber();
         }

         if (primitive.isBoolean()) {
            return primitive.getAsBoolean();
         }
      } else if (element.isJsonArray()) {
         return CommonConst.GSON.fromJson(element, List.class);
      }

      try {
         return Document.parse(CommonConst.GSON.toJson(element));
      } catch (BsonInvalidOperationException var2) {
         return JsonParser.parseString(CommonConst.GSON.toJson(element));
      }
   }

   public static String elementToString(JsonElement element) {
      if (element.isJsonPrimitive()) {
         JsonPrimitive primitive = element.getAsJsonPrimitive();
         if (primitive.isString()) {
            return primitive.getAsString();
         }
      }

      return CommonConst.GSON.toJson(element);
   }

   public static <T> T mapToObject(Map<String, String> map, Class<T> clazz) {
      JsonObject obj = new JsonObject();

      for(Entry<String, String> entry : map.entrySet()) {
         try {
            obj.add(entry.getKey(), JsonParser.parseString(entry.getValue()));
         } catch (Exception var6) {
            obj.addProperty(entry.getKey(), entry.getValue());
         }
      }

      return CommonConst.GSON.fromJson(obj, clazz);
   }

   public static Map<String, String> objectToMap(Object src) {
      Map<String, String> map = new HashMap<>();
      JsonObject obj = (JsonObject)CommonConst.GSON.toJsonTree(src);

      for(Entry<String, JsonElement> entry : obj.entrySet()) {
         map.put(entry.getKey(), CommonConst.GSON.toJson(entry.getValue()));
      }

      return map;
   }
}

package br.com.plutomc.core.common.packet.types.configuration;

import br.com.plutomc.core.common.CommonConst;
import br.com.plutomc.core.common.CommonPlugin;
import br.com.plutomc.core.common.PluginInfo;
import br.com.plutomc.core.common.packet.Packet;
import br.com.plutomc.core.common.packet.PacketType;
import br.com.plutomc.core.common.utils.json.JsonBuilder;
import br.com.plutomc.core.common.utils.json.JsonUtils;
import br.com.plutomc.core.common.utils.reflection.Reflection;
import com.google.gson.JsonObject;
import java.lang.reflect.Field;

public class ConfigurationFieldUpdate extends Packet {
   private JsonObject jsonObject;

   public ConfigurationFieldUpdate(String fieldName) {
      super(PacketType.CONFIGURATION_FIELD_UPDATE);
      JsonObject tree = JsonUtils.jsonTree(CommonPlugin.getInstance().getPluginInfo());
      this.jsonObject = new JsonBuilder().addProperty("fieldName", fieldName).add("value", tree.get(fieldName)).build();
   }

   @Override
   public void receive() {
      try {
         Field f = Reflection.getField(PluginInfo.class, this.jsonObject.get("fieldName").getAsString());
         Object object = CommonConst.GSON.fromJson(this.jsonObject.get("value"), f.getGenericType());
         f.setAccessible(true);
         f.set(CommonPlugin.getInstance().getPluginInfo(), object);
      } catch (SecurityException | IllegalAccessException | IllegalArgumentException var3) {
         var3.printStackTrace();
      }
   }
}

package br.com.plutomc.core.common.packet.types.configuration;

import br.com.plutomc.core.common.CommonConst;
import br.com.plutomc.core.common.CommonPlugin;
import br.com.plutomc.core.common.PluginInfo;
import br.com.plutomc.core.common.packet.Packet;
import br.com.plutomc.core.common.packet.PacketType;
import com.google.gson.JsonObject;

public class ConfigurationUpdate extends Packet {
   private JsonObject jsonObject = CommonConst.GSON.toJsonTree(CommonPlugin.getInstance().getPluginInfo()).getAsJsonObject();

   public ConfigurationUpdate() {
      super(PacketType.CONFIGURATION_UPDATE);
   }

   @Override
   public void receive() {
      CommonPlugin.getInstance().setPluginInfo(CommonConst.GSON.fromJson(this.jsonObject, PluginInfo.class));
   }
}

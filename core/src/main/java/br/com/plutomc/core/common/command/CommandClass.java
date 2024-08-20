package br.com.plutomc.core.common.command;

import br.com.plutomc.core.common.server.ServerType;
import br.com.plutomc.core.common.CommonPlugin;
import br.com.plutomc.core.common.packet.types.staff.Stafflog;

public interface CommandClass {
   default void staffLog(String message) {
      CommonPlugin.getInstance().getMemberManager().staffLog(message);
   }

   default void staffLog(String message, boolean bungeecord) {
      if (bungeecord && CommonPlugin.getInstance().getServerType() != ServerType.BUNGEECORD) {
         CommonPlugin.getInstance().getServerData().sendPacket(new Stafflog("§7[" + message + "§7]"));
      } else {
         CommonPlugin.getInstance().getMemberManager().staffLog(message);
      }
   }
}

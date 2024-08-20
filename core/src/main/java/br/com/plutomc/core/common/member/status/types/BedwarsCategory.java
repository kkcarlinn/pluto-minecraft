package br.com.plutomc.core.common.member.status.types;

import br.com.plutomc.core.common.CommonPlugin;
import br.com.plutomc.core.common.server.ServerType;

public enum BedwarsCategory {
   BEDWARS_MATCH,
   BEDWARS_LEVEL,
   BEDWARS_POINTS,
   BEDWARS_KILLS,
   BEDWARS_FINAL_KILLS,
   BEDWARS_KILLSTREAK,
   BEDWARS_DEATHS,
   BEDWARS_FINAL_DEATHS,
   BEDWARS_WINS,
   BEDWARS_LOSES,
   BEDWARS_WINSTREAK,
   BEDWARS_BED_BREAK,
   BEDWARS_BED_BROKEN;

   public String getSpecialServer(ServerType serverType) {
      return this.name().toLowerCase().replace("_", "-") + "-" + serverType.name().toLowerCase().split("_")[1];
   }

   public String getSpecialServer() {
      return this.getSpecialServer(CommonPlugin.getInstance().getServerType());
   }
}

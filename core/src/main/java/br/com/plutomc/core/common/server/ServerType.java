package br.com.plutomc.core.common.server;

import br.com.plutomc.core.common.utils.string.StringFormat;

public enum ServerType {
   BUILD,
   LOGIN,
   LOBBY,
   BW_LOBBY,
   SW_LOBBY,
   HG_LOBBY,
   PVP_LOBBY,
   DUELS_LOBBY,
   DUELS_SIMULATOR,
   DUELS_GAPPLE,
   DUELS_NODEBUFF,
   DUELS_SCRIM,
   FPS,
   LAVA,
   ARENA,
   HG,
   MINIPLUTO,
   EVENTO,
   RANKUP,
   SW_SOLO,
   SW_DUOS,
   SW_TRIO,
   SW_SQUAD,
   BW_SOLO,
   BW_DUOS,
   BW_TRIO,
   BW_SQUAD,
   BW_1X1,
   BW_2X2,
   BW_3X3,
   BW_4X4,
   BUNGEECORD,
   DISCORD,
   DUELS_BOXING;

   public int getPlayersPerTeam() {
      if (this.name().contains("SOLO") || this.name().contains("1X1")) {
         return 1;
      } else if (this.name().contains("DUO") || this.name().contains("2X2")) {
         return 2;
      } else {
         return !this.name().contains("TRIO") && !this.name().contains("3X3") ? 4 : 3;
      }
   }

   public ServerType getServerLobby() {
      if (this.name().contains("LOBBY")) {
         return LOBBY;
      } else if(this.name().contains("DUELS")) {
         return DUELS_LOBBY;
      }else if (this.name().contains("BW")) {
         return BW_LOBBY;
      } else if (this.name().contains("SW")) {
         return SW_LOBBY;
      } else {
         switch(this) {
            case HG:
               return HG_LOBBY;
            case ARENA:
            case FPS:
            case LAVA:
               return PVP_LOBBY;
            default:
               return LOBBY;
         }
      }
   }

   public boolean isPvP() {
      switch(this) {
         case ARENA:
         case FPS:
         case LAVA:
         case PVP_LOBBY:
            return true;
         default:
            return false;
      }
   }

   public boolean isHG() {
      switch(this) {
         case HG:
         case EVENTO:
         case MINIPLUTO:
            return true;
         default:
            return false;
      }
   }

   public boolean isLobby() {
      return this.name().contains("LOBBY") || this == LOGIN || this == BUILD;
   }

   public String getName() {
      StringBuilder stringBuilder = new StringBuilder();

      for(String name : this.name().split("_")) {
         stringBuilder.append(StringFormat.formatString(name.replace("BW", "Bedwars").replace("SW", "Skywars"))).append(" ");
      }

      return stringBuilder.toString().trim();
   }

   public static ServerType getTypeByName(String string) {
      try {
         return valueOf(string.toUpperCase());
      } catch (Exception var2) {
         return null;
      }
   }
}

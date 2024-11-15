package br.com.plutomc.core.common.server;

import br.com.plutomc.core.common.utils.string.StringFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ServerType {
   BUILD("build", "bl"),
   LOGIN("lobbyhost", "lh"),
   LOBBY("lobbyhost", "lh"),
   BW_LOBBY("lobbyhost", "blh"),
   SW_LOBBY("lobbyhost", "slh"),
   HG_LOBBY("lobbyhost", "hlh"),
   PVP_LOBBY("lobbyhost", "plh"),
   DUELS_LOBBY("lobbyhost", "dlh"),
   FPS("pvp", "fps"),
   LAVA("pvp", "lava"),
   ARENA("pvp", "arena"),
   HG("hg", "hg"),
   MINIPLUTO("hg", "mp"),
   EVENTO("hg", "ev"),
   SW_SOLO("arcade", "sw"),
   SW_DUOS("arcade", "sw"),
   SW_TRIO("arcade", "sw"),
   SW_SQUAD("arcade", "sw"),
   BW_SOLO("arcade", "bw"),
   BW_DUOS("arcade", "bw"),
   BW_TRIO("arcade", "bw"),
   BW_SQUAD("arcade", "bw"),
   BW_1X1("arcade", "bw"),
   BW_2X2("arcade", "bw"),
   BW_3X3("arcade", "bw"),
   BW_4X4("arcade", "bw"),
   BUNGEECORD("proxy", "proxy"),
   DISCORD("discord", "dc"),;

   private String typeName, prefix;

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

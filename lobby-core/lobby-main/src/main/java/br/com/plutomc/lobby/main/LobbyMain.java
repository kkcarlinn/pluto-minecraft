package br.com.plutomc.lobby.main;

import java.util.Arrays;
import br.com.plutomc.lobby.lobbyhost.LobbyHost;
import br.com.plutomc.lobby.main.listener.ScoreboardListener;
import br.com.plutomc.core.bukkit.utils.character.handler.ActionHandler;
import br.com.plutomc.core.common.server.ServerType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class LobbyMain extends LobbyHost {
   private static LobbyMain instance;

   @Override
   public void onEnable() {
      instance = this;
      super.onEnable();
      Bukkit.getPluginManager().registerEvents(new ScoreboardListener(), this);
      this.createCharacter("npc-hg", "AnjooGaming", new ActionHandler() {
         @Override
         public boolean onInteract(Player player, boolean right) {
            LobbyMain.this.sendPlayerToServer(player, new ServerType[]{ServerType.HG_LOBBY});
            return false;
         }
      }, Arrays.asList(ServerType.HG, ServerType.HG_LOBBY), new String[]{"§bHG"});
      this.createCharacter("npc-pvp", "Budokkan", new ActionHandler() {
         @Override
         public boolean onInteract(Player player, boolean right) {
            LobbyMain.this.sendPlayerToServer(player, new ServerType[]{ServerType.PVP_LOBBY});
            return false;
         }
      }, Arrays.asList(ServerType.PVP_LOBBY, ServerType.ARENA, ServerType.FPS, ServerType.LAVA), new String[]{"§bPvP"});
      this.createCharacter("npc-duels", "stopeey", new ActionHandler() {
         @Override
         public boolean onInteract(Player player, boolean right) {
            LobbyMain.this.sendPlayerToServer(player, new ServerType[]{ServerType.DUELS_LOBBY, ServerType.DUELS_GAPPLE});
            return false;
         }
      }, Arrays.asList(ServerType.DUELS_LOBBY), new String[]{"§bDuels"});
      this.createCharacter(
         "npc-bedwars",
         "Abodicom4You",
         new ActionHandler() {
            @Override
            public boolean onInteract(Player player, boolean right) {
               LobbyMain.this.sendPlayerToServer(player, new ServerType[]{ServerType.BW_LOBBY});
               return false;
            }
         },
         Arrays.asList(
            ServerType.BW_LOBBY, ServerType.BW_SOLO, ServerType.BW_DUOS, ServerType.BW_TRIO, ServerType.BW_SQUAD, ServerType.BW_1X1, ServerType.BW_2X2
         ),
         new String[]{"§bBedwars"}
      );
      this.createCharacter("npc-skywars", "Jauaum", new ActionHandler() {
         @Override
         public boolean onInteract(Player player, boolean right) {
            LobbyMain.this.sendPlayerToServer(player, new ServerType[]{ServerType.SW_LOBBY});
            return false;
         }
      }, Arrays.asList(ServerType.SW_LOBBY, ServerType.SW_SOLO, ServerType.SW_DUOS, ServerType.SW_SQUAD), new String[]{"§bSkywars"});
   }

   public static LobbyMain getInstance() {
      return instance;
   }
}

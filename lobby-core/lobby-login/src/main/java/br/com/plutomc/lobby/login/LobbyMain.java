package br.com.plutomc.lobby.login;

import br.com.plutomc.lobby.login.listener.PlayerListener;
import br.com.plutomc.lobby.login.listener.ScoreboardListener;
import br.com.plutomc.lobby.lobbyhost.LobbyHost;
import org.bukkit.Bukkit;

public class LobbyMain extends LobbyHost {
   @Override
   public void onEnable() {
      super.onEnable();
      Bukkit.getPluginManager().registerEvents(new PlayerListener(), this);
      Bukkit.getPluginManager().registerEvents(new ScoreboardListener(), this);
      this.setMaxPlayers(6);
      this.setPlayerInventory(player -> {
      });
   }
}

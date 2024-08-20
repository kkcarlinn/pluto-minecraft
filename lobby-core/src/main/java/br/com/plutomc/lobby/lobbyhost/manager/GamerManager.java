package br.com.plutomc.lobby.lobbyhost.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import br.com.plutomc.lobby.lobbyhost.gamer.Gamer;

public class GamerManager {
   private Map<UUID, Gamer> gamers = new HashMap<>();

   public void loadGamer(UUID uuid, Gamer gamer) {
      this.gamers.put(uuid, gamer);
   }

   public Gamer getGamer(UUID uuid) {
      return this.gamers.get(uuid);
   }

   public Collection<Gamer> getGamers() {
      return this.gamers.values();
   }

   public void unloadGamer(UUID uuid) {
      this.gamers.remove(uuid);
   }
}

package br.com.plutomc.game.bedwars.manager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CombatlogManager {
   private Map<UUID, Combat> playerMap = new HashMap<>();

   public boolean isInCombatlog(UUID playerId) {
      return this.playerMap.containsKey(playerId) && this.playerMap.get(playerId).getLastHit() + 30000L > System.currentTimeMillis();
   }

   public void putCombatlog(UUID playerId, UUID hittedId) {
      this.playerMap.put(hittedId, new Combat(playerId, System.currentTimeMillis()));
      this.playerMap.put(playerId, new Combat(hittedId, System.currentTimeMillis()));
   }

   public class Combat {
      private UUID lastId;
      private long lastHit;

      public UUID getLastId() {
         return this.lastId;
      }

      public long getLastHit() {
         return this.lastHit;
      }

      public Combat(UUID lastId, long lastHit) {
         this.lastId = lastId;
         this.lastHit = lastHit;
      }
   }
}

package br.com.plutomc.game.engine.gamer;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Team {
   private int teamId;
   private int maxPlayers;
   private Set<UUID> playerSet;

   public Team(int teamId, int maxPlayers) {
      this.teamId = teamId;
      this.maxPlayers = maxPlayers;
      this.playerSet = new HashSet<>();
   }

   public boolean isFull() {
      return this.playerSet.size() >= this.maxPlayers;
   }

   public void team(Gamer gamer) {
      this.playerSet.add(gamer.getUniqueId());
   }

   public void unteam(Gamer gamer) {
      this.playerSet.remove(gamer.getUniqueId());
   }

   public boolean isTeam(UUID uniqueId) {
      return this.playerSet.contains(uniqueId);
   }

   public boolean isTeam(Gamer gamer) {
      return this.isTeam(gamer.getUniqueId());
   }

   public void addPlayer(UUID uniqueId) {
      this.playerSet.add(uniqueId);
   }

   public int getTeamId() {
      return this.teamId;
   }

   public int getMaxPlayers() {
      return this.maxPlayers;
   }

   public Set<UUID> getPlayerSet() {
      return this.playerSet;
   }
}

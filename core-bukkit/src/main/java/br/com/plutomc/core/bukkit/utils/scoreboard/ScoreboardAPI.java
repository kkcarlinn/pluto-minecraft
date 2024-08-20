package br.com.plutomc.core.bukkit.utils.scoreboard;

import java.util.Collection;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Team;

public class ScoreboardAPI {
   public static Team getTeamFromPlayer(Player player, String teamID) {
      if (teamID.length() > 16) {
         teamID = teamID.substring(0, 16);
      }

      return player.getScoreboard().getTeam(teamID);
   }

   public static boolean teamExistsForPlayer(Player player, String teamID) {
      return getTeamFromPlayer(player, teamID) != null;
   }

   public static Team getPlayerCurrentTeamForPlayer(Player player, Player get) {
      return player.getScoreboard().getEntryTeam(get.getName());
   }

   public static boolean playerHasCurrentTeamForPlayer(Player player, Player has) {
      return getPlayerCurrentTeamForPlayer(player, has) != null;
   }

   public static Team createTeamToPlayer(Player player, String teamID, String teamPrefix, String teamSuffix) {
      Team team = getTeamFromPlayer(player, teamID);
      if (team == null) {
         if (teamID.length() > 16) {
            teamID = teamID.substring(0, 16);
         }

         if (teamPrefix.length() > 16) {
            teamPrefix = teamPrefix.substring(0, 16);
         }

         if (teamSuffix.length() > 16) {
            teamSuffix = teamSuffix.substring(0, 16);
         }

         team = player.getScoreboard().registerNewTeam(teamID);
      }

      team.setPrefix(teamPrefix);
      team.setSuffix(teamSuffix);
      return team;
   }

   public static void createTeamForPlayers(Collection<? extends Player> players, String teamID, String teamPrefix, String teamSuffix) {
      for(Player player : players) {
         createTeamToPlayer(player, teamID, teamPrefix, teamSuffix);
      }
   }

   public static void createTeamForOnlinePlayers(String teamID, String teamPrefix, String teamSuffix) {
      for(Player player : Bukkit.getOnlinePlayers()) {
         createTeamToPlayer(player, teamID, teamPrefix, teamSuffix);
      }
   }

   public static Team createTeamIfNotExistsToPlayer(Player player, String teamID, String teamPrefix, String teamSuffix) {
      Team team = getTeamFromPlayer(player, teamID);
      if (team == null) {
         team = createTeamToPlayer(player, teamID, teamPrefix, teamSuffix);
      }

      return team;
   }

   public static void createTeamIfNotExistsForPlayers(Collection<? extends Player> players, String teamID, String teamPrefix, String teamSuffix) {
      for(Player player : players) {
         createTeamIfNotExistsToPlayer(player, teamID, teamPrefix, teamSuffix);
      }
   }

   public static void createTeamIfNotExistsForOnlinePlayers(String teamID, String teamPrefix, String teamSuffix) {
      for(Player player : Bukkit.getOnlinePlayers()) {
         createTeamIfNotExistsToPlayer(player, teamID, teamPrefix, teamSuffix);
      }
   }

   public static void joinTeam(Team team, String join) {
      if (team != null) {
         if (join.length() > 16) {
            join = join.substring(0, 16);
         }

         if (!team.getEntries().contains(join)) {
            team.addEntry(join);
         }
      }
   }

   public static void joinTeam(Team team, Player join) {
      joinTeam(team, join.getName());
   }

   public static void joinTeamForPlayer(Player player, String teamID, String join) {
      joinTeam(getTeamFromPlayer(player, teamID), join);
   }

   public static void joinTeamForPlayer(Player player, String teamID, Player join) {
      Team team = getTeamFromPlayer(player, teamID);
      if (team != null) {
         if (!team.getEntries().contains(join.getName())) {
            leaveCurrentTeamForPlayer(player, join);
            team.addEntry(join.getName());
         }

         Team var4 = null;
      }
   }

   public static void joinTeamForPlayers(Collection<? extends Player> players, String teamID, String join) {
      for(Player player : players) {
         joinTeamForPlayer(player, teamID, join);
      }
   }

   public static void joinTeamForPlayers(Collection<? extends Player> players, String teamID, Player join) {
      for(Player player : players) {
         joinTeamForPlayer(player, teamID, join);
      }
   }

   public static void joinTeamForOnlinePlayers(String join, String teamID) {
      for(Player player : Bukkit.getOnlinePlayers()) {
         joinTeamForPlayer(player, teamID, join);
      }
   }

   public static void joinTeamForOnlinePlayers(Player join, String teamID) {
      for(Player player : Bukkit.getOnlinePlayers()) {
         joinTeamForPlayer(player, teamID, join);
      }
   }

   public static void leaveCurrentTeamForPlayer(Player player, Player leave) {
      Team team = getPlayerCurrentTeamForPlayer(player, leave);
      if (team != null) {
         leaveTeam(team, leave);
      }
   }

   public static void leaveCurrentTeamForPlayers(Collection<? extends Player> players, Player leave) {
      for(Player player : players) {
         leaveCurrentTeamForPlayer(player, leave);
      }
   }

   public static void leaveCurrentTeamForOnlinePlayers(Player leave) {
      for(Player player : Bukkit.getOnlinePlayers()) {
         leaveCurrentTeamForPlayer(player, leave);
      }
   }

   public static void leaveTeam(Team team, String leave) {
      if (team != null) {
         if (leave.length() > 16) {
            leave = leave.substring(0, 16);
         }

         if (team.getEntries().contains(leave)) {
            team.removeEntry(leave);
            unregisterTeamIfEmpty(team);
         }
      }
   }

   public static void leaveTeam(Player player, String teamId, Player leave) {
      Team team = getTeamFromPlayer(player, teamId);
      if (team != null) {
         leaveTeam(team, leave.getName());
      }
   }

   public static void leaveTeam(Team team, Player leave) {
      leaveTeam(team, leave.getName());
   }

   public static void leaveTeamToPlayer(Player player, String teamID, String leave) {
      leaveTeam(getTeamFromPlayer(player, teamID), leave);
   }

   public static void leaveTeamToPlayer(Player player, String teamID, Player leave) {
      leaveTeamToPlayer(player, teamID, leave.getName());
   }

   public static void leaveTeamForPlayers(Collection<? extends Player> players, String teamID, String leave) {
      for(Player player : players) {
         leaveTeamToPlayer(player, teamID, leave);
      }
   }

   public static void leaveTeamForPlayers(Collection<? extends Player> players, String teamID, Player leave) {
      leaveTeamForPlayers(players, teamID, leave.getName());
   }

   public static void leaveTeamForOnlinePlayers(String teamID, String leave) {
      for(Player player : Bukkit.getOnlinePlayers()) {
         leaveTeamToPlayer(player, teamID, leave);
      }
   }

   public static void leaveTeamForOnlinePlayers(String teamID, Player leave) {
      leaveTeamForOnlinePlayers(teamID, leave.getName());
   }

   public static void unregisterTeam(Team team) {
      if (team != null) {
         team.unregister();
      }
   }

   public static void unregisterTeamToPlayer(Player player, String teamID) {
      unregisterTeam(getTeamFromPlayer(player, teamID));
   }

   public static void unregisterTeamForPlayers(Collection<? extends Player> players, String teamID) {
      for(Player player : players) {
         unregisterTeamToPlayer(player, teamID);
      }
   }

   public static void unregisterTeamForOnlinePlayers(String teamID) {
      for(Player player : Bukkit.getOnlinePlayers()) {
         unregisterTeamToPlayer(player, teamID);
      }
   }

   public static void unregisterTeamIfEmpty(Team team) {
      if (team != null && team.getEntries().size() == 0) {
         unregisterTeam(team);
      }
   }

   public static void unregisterTeamIfEmptyToPlayer(Player player, String teamID) {
      unregisterTeamIfEmpty(getTeamFromPlayer(player, teamID));
   }

   public static void unregisterTeamForEmptyForPlayers(Collection<? extends Player> players, String teamID) {
      for(Player player : players) {
         unregisterTeamIfEmptyToPlayer(player, teamID);
      }
   }

   public static void unregisterTeamIfEmptyForOnlinePlayers(String teamID) {
      for(Player player : Bukkit.getOnlinePlayers()) {
         unregisterTeamIfEmptyToPlayer(player, teamID);
      }
   }

   public static void setTeamPrefix(Team team, String prefix) {
      if (team != null) {
         if (prefix.length() > 16) {
            prefix = prefix.substring(0, 16);
         }

         team.setPrefix(prefix);
      }
   }

   public static void setTeamPrefixToPlayer(Player player, String teamID, String prefix) {
      setTeamPrefix(getTeamFromPlayer(player, teamID), prefix);
   }

   public static void setTeamPrefixForPlayers(Collection<? extends Player> players, String teamID, String prefix) {
      for(Player player : players) {
         setTeamPrefixToPlayer(player, teamID, prefix);
      }
   }

   public static void setTeamPrefixForOnlinePlayers(String teamID, String prefix) {
      for(Player player : Bukkit.getOnlinePlayers()) {
         setTeamPrefixToPlayer(player, teamID, prefix);
      }
   }

   public static void setTeamSuffix(Team team, String suffix) {
      if (team != null) {
         if (suffix.length() > 16) {
            suffix = suffix.substring(0, 16);
         }

         team.setSuffix(suffix);
      }
   }

   public static void setTeamSuffixToPlayer(Player player, String teamID, String suffix) {
      setTeamSuffix(getTeamFromPlayer(player, teamID), suffix);
   }

   public static void setTeamSuffixForPlayers(Collection<? extends Player> players, String teamID, String suffix) {
      for(Player player : players) {
         setTeamSuffixToPlayer(player, teamID, suffix);
      }
   }

   public static void setTeamSuffixForOnlinePlayers(String teamID, String suffix) {
      for(Player player : Bukkit.getOnlinePlayers()) {
         setTeamSuffixToPlayer(player, teamID, suffix);
      }
   }

   public static void setTeamPrefixAndSuffix(Team team, String prefix, String suffix) {
      if (team != null) {
         if (prefix.length() > 16) {
            prefix = prefix.substring(0, 16);
         }

         if (suffix.length() > 16) {
            suffix = suffix.substring(0, 16);
         }

         team.setPrefix(prefix);
         team.setSuffix(suffix);
      }
   }

   public static void setTeamPrefixAndSuffixToPlayer(Player player, String teamID, String prefix, String suffix) {
      setTeamPrefixAndSuffix(getTeamFromPlayer(player, teamID), prefix, suffix);
   }

   public static void setTeamPrefixAndSuffixForPlayers(Collection<? extends Player> players, String teamID, String prefix, String suffix) {
      for(Player player : players) {
         setTeamPrefixAndSuffixToPlayer(player, teamID, prefix, suffix);
      }
   }

   public static void setTeamPrefixAndSuffixForOnlinePlayers(String teamID, String prefix, String suffix) {
      for(Player player : Bukkit.getOnlinePlayers()) {
         setTeamPrefixAndSuffixToPlayer(player, teamID, prefix, suffix);
      }
   }

   public static void setTeamDisplayName(Team team, String name) {
      if (team != null) {
         if (name.length() > 16) {
            name = name.substring(0, 16);
         }

         team.setDisplayName(name);
      }
   }

   public static void setTeamDisplayNameToPlayer(Player player, String teamID, String name) {
      setTeamDisplayName(getTeamFromPlayer(player, teamID), name);
   }

   public static void setTeamDisplayNameForPlayers(Collection<? extends Player> players, String teamID, String name) {
      for(Player player : players) {
         setTeamDisplayNameToPlayer(player, teamID, name);
      }
   }

   public static void setTeamDisplayNameForOnlinesPlayers(String teamID, String name) {
      for(Player player : Bukkit.getOnlinePlayers()) {
         setTeamDisplayNameToPlayer(player, teamID, name);
      }
   }

   public static org.bukkit.scoreboard.Objective getObjectiveFromPlayer(Player player, String objectiveID) {
      if (objectiveID.length() > 16) {
         objectiveID = objectiveID.substring(0, 16);
      }

      return player.getScoreboard().getObjective(objectiveID);
   }

   public static org.bukkit.scoreboard.Objective getObjectiveFromPlayer(Player player, DisplaySlot displaySlot) {
      return player.getScoreboard().getObjective(displaySlot);
   }

   public static org.bukkit.scoreboard.Objective createObjectiveToPlayer(Player player, String objectiveID, String displayName, DisplaySlot displaySlot) {
      org.bukkit.scoreboard.Objective objective = getObjectiveFromPlayer(player, objectiveID);
      if (objective == null) {
         if (objectiveID.length() > 16) {
            objectiveID = objectiveID.substring(0, 16);
         }

         objective = player.getScoreboard().registerNewObjective(objectiveID, "dummy");
      }

      if (displayName.length() > 32) {
         displayName = displayName.substring(0, 32);
      }

      objective.setDisplayName(displayName);
      objective.setDisplaySlot(displaySlot);
      return objective;
   }

   public static void createObjectiveForPlayers(Collection<? extends Player> players, String objectiveID, String displayName, DisplaySlot displaySlot) {
      for(Player player : players) {
         createObjectiveToPlayer(player, objectiveID, displayName, displaySlot);
      }
   }

   public static void createObjectiveForOnlinePlayers(String objectiveID, String displayName, DisplaySlot displaySlot) {
      for(Player player : Bukkit.getOnlinePlayers()) {
         createObjectiveToPlayer(player, objectiveID, displayName, displaySlot);
      }
   }

   public static org.bukkit.scoreboard.Objective createObjectiveIfNotExistsToPlayer(
      Player player, String objectiveID, String displayName, DisplaySlot displaySlot
   ) {
      org.bukkit.scoreboard.Objective objective = getObjectiveFromPlayer(player, objectiveID);
      if (objective == null) {
         objective = createObjectiveToPlayer(player, objectiveID, displayName, displaySlot);
         objective.setDisplaySlot(displaySlot);
      }

      return objective;
   }

   public static void createObjectiveIfNotExistsToPlayer(Collection<? extends Player> players, String objectiveID, String displayName, DisplaySlot displaySlot) {
      for(Player player : players) {
         createObjectiveIfNotExistsToPlayer(player, objectiveID, displayName, displaySlot);
      }
   }

   public static void createObjectiveIfNotExistsForOnlinePlayers(String objectiveID, String displayName, DisplaySlot displaySlot) {
      for(Player player : Bukkit.getOnlinePlayers()) {
         createObjectiveIfNotExistsToPlayer(player, objectiveID, displayName, displaySlot);
      }
   }

   public static void setObjectiveDisplayName(org.bukkit.scoreboard.Objective objective, String name) {
      if (objective != null) {
         if (name.length() > 32) {
            name = name.substring(0, 32);
         }

         objective.setDisplayName(name);
      }
   }

   public static void setObjectiveDisplayNameToPlayer(Player player, String objectiveID, String name) {
      setObjectiveDisplayName(getObjectiveFromPlayer(player, objectiveID), name);
   }

   public static void setObjectiveDisplayNameForPlayers(Collection<? extends Player> players, String objectiveID, String name) {
      for(Player player : players) {
         setObjectiveDisplayNameToPlayer(player, objectiveID, name);
      }
   }

   public static void setObjectiveDisplayNameForOnlinePlayers(String objectiveID, String name) {
      for(Player player : Bukkit.getOnlinePlayers()) {
         setObjectiveDisplayNameToPlayer(player, objectiveID, name);
      }
   }

   public static void setObjectiveDisplaySlot(org.bukkit.scoreboard.Objective objective, DisplaySlot displaySlot) {
      if (objective != null) {
         objective.setDisplaySlot(displaySlot);
      }
   }

   public static void setObjectiveDisplaySlotToPlayer(Player player, String objectiveID, DisplaySlot displaySlot) {
      setObjectiveDisplaySlot(getObjectiveFromPlayer(player, objectiveID), displaySlot);
   }

   public static void setObjectiveDisplaySlotForPlayers(Collection<? extends Player> players, String objectiveID, DisplaySlot displaySlot) {
      for(Player player : players) {
         setObjectiveDisplaySlotToPlayer(player, objectiveID, displaySlot);
      }
   }

   public static void setObjectiveDisplaySlotForOnlinePlayers(String objectiveID, DisplaySlot displaySlot) {
      for(Player player : Bukkit.getOnlinePlayers()) {
         setObjectiveDisplaySlotToPlayer(player, objectiveID, displaySlot);
      }
   }

   public static void setScoreOnObjective(org.bukkit.scoreboard.Objective objective, String scoreName, int scoreValue) {
      if (scoreName.length() > 16) {
         scoreName = scoreName.substring(0, 16);
      }

      if (objective != null) {
         objective.getScore(scoreName).setScore(scoreValue);
      }
   }

   public static void setScoreOnObjectiveToPlayer(Player player, String objectiveID, String scoreName, int scoreValue) {
      setScoreOnObjective(getObjectiveFromPlayer(player, objectiveID), scoreName, scoreValue);
   }

   public static void setScoreOnObjectiveToPlayer(Player player, DisplaySlot objectiveSlot, String scoreName, int scoreValue) {
      setScoreOnObjective(getObjectiveFromPlayer(player, objectiveSlot), scoreName, scoreValue);
   }

   public static void setScoreOnObjectiveForPlayers(Collection<? extends Player> players, String objectiveID, String scoreName, int scoreValue) {
      for(Player player : players) {
         setScoreOnObjective(getObjectiveFromPlayer(player, objectiveID), scoreName, scoreValue);
      }
   }

   public static void setScoreOnObjectiveForPlayers(Collection<? extends Player> players, DisplaySlot objectiveSlot, String scoreName, int scoreValue) {
      for(Player player : players) {
         setScoreOnObjective(getObjectiveFromPlayer(player, objectiveSlot), scoreName, scoreValue);
      }
   }

   public static void setScoreOnObjectiveForOnlinePlayers(String objectiveID, String scoreName, int scoreValue) {
      for(Player player : Bukkit.getOnlinePlayers()) {
         setScoreOnObjective(getObjectiveFromPlayer(player, objectiveID), scoreName, scoreValue);
      }
   }

   public static void setScoreOnObjectiveForOnlinePlayers(DisplaySlot objectiveSlot, String scoreName, int scoreValue) {
      for(Player player : Bukkit.getOnlinePlayers()) {
         setScoreOnObjective(getObjectiveFromPlayer(player, objectiveSlot), scoreName, scoreValue);
      }
   }

   public static void addScoreOnObjectiveToPlayer(
      Player player, org.bukkit.scoreboard.Objective objective, String scoreID, int score, String name, String prefix, String suffix
   ) {
      if (objective != null) {
         Team team = createTeamIfNotExistsToPlayer(player, objective.getName() + scoreID, prefix, suffix);
         if (team != null) {
            if (name.length() > 16) {
               name = name.substring(0, 16);
            }

            setScoreOnObjective(objective, name, score);
            joinTeam(team, name);
         }
      }
   }

   public static void addScoreOnObjectiveToPlayer(Player player, String objectiveID, String scoreID, int score, String name, String prefix, String suffix) {
      addScoreOnObjectiveToPlayer(player, getObjectiveFromPlayer(player, objectiveID), scoreID, score, name, prefix, suffix);
   }

   public static void addScoreOnObjectiveToPlayer(
      Player player, DisplaySlot objectiveSlot, String scoreID, int score, String name, String prefix, String suffix
   ) {
      addScoreOnObjectiveToPlayer(player, getObjectiveFromPlayer(player, objectiveSlot), scoreID, score, name, prefix, suffix);
   }

   public static void addScoreOnObjectiveForPlayers(
      Collection<? extends Player> players, String objectiveID, String scoreID, int score, String name, String prefix, String suffix
   ) {
      for(Player player : players) {
         addScoreOnObjectiveToPlayer(player, objectiveID, scoreID, score, name, prefix, suffix);
      }
   }

   public static void addScoreOnObjectiveForPlayers(
      Collection<? extends Player> players, DisplaySlot objectiveSlot, String scoreID, int score, String name, String prefix, String suffix
   ) {
      for(Player player : players) {
         addScoreOnObjectiveToPlayer(player, objectiveSlot, scoreID, score, name, prefix, suffix);
      }
   }

   public static void addScoreOnObjectiveForOnlinePlayers(String objectiveID, String scoreID, int score, String name, String prefix, String suffix) {
      for(Player player : Bukkit.getOnlinePlayers()) {
         addScoreOnObjectiveToPlayer(player, getObjectiveFromPlayer(player, objectiveID), scoreID, score, name, prefix, suffix);
      }
   }

   public static void addScoreOnObjectiveForOnlinePlayers(DisplaySlot objectiveSlot, String scoreID, int score, String name, String prefix, String suffix) {
      for(Player player : Bukkit.getOnlinePlayers()) {
         addScoreOnObjectiveToPlayer(player, objectiveSlot, scoreID, score, name, prefix, suffix);
      }
   }

   public static void updateScoreNameOnObjectiveToPlayer(Player player, org.bukkit.scoreboard.Objective objective, String name) {
      if (objective != null) {
         Team team = getTeamFromPlayer(player, objective.getName());
         if (team != null) {
            setTeamPrefix(team, name);
            team = null;
         }
      }
   }

   public static void updateScoreNameOnObjectiveToPlayer(Player player, String objectiveID, String name) {
      updateScoreNameOnObjectiveToPlayer(player, getObjectiveFromPlayer(player, objectiveID), name);
   }

   public static void updateScoreNameOnObjectiveToPlayer(Player player, DisplaySlot objectiveSlot, String name) {
      updateScoreNameOnObjectiveToPlayer(player, getObjectiveFromPlayer(player, objectiveSlot), name);
   }

   public static void updateScoreNameOnObjectiveForPlayers(Collection<? extends Player> players, String objectiveID, String name) {
      for(Player player : players) {
         updateScoreNameOnObjectiveToPlayer(player, objectiveID, name);
      }
   }

   public static void updateScoreNameOnObjectiveForPlayers(Collection<? extends Player> players, DisplaySlot objectiveSlot, String name) {
      for(Player player : players) {
         updateScoreNameOnObjectiveToPlayer(player, objectiveSlot, name);
      }
   }

   public static void updateScoreNameOnObjectiveForOnlinePlayers(String objectiveID, String name) {
      for(Player player : Bukkit.getOnlinePlayers()) {
         updateScoreNameOnObjectiveToPlayer(player, objectiveID, name);
      }
   }

   public static void updateScoreNameOnObjectiveForOnlinePlayers(DisplaySlot objectiveSlot, String name) {
      for(Player player : Bukkit.getOnlinePlayers()) {
         updateScoreNameOnObjectiveToPlayer(player, getObjectiveFromPlayer(player, objectiveSlot), name);
      }
   }

   public static void updateScoreValueOnObjectiveToPlayer(Player player, org.bukkit.scoreboard.Objective objective, String scoreID, String value) {
      if (objective != null) {
         Team team = getTeamFromPlayer(player, objective.getName());
         if (team != null) {
            setTeamSuffix(team, value);
            team = null;
         }
      }
   }

   public static void updateScoreValueOnObjectiveToPlayer(Player player, String objectiveID, String scoreID, String value) {
      updateScoreValueOnObjectiveToPlayer(player, getObjectiveFromPlayer(player, objectiveID), scoreID, value);
   }

   public static void updateScoreValueOnObjectiveToPlayer(Player player, DisplaySlot objectiveSlot, String scoreID, String value) {
      updateScoreValueOnObjectiveToPlayer(player, getObjectiveFromPlayer(player, objectiveSlot), scoreID, value);
   }

   public static void updateScoreValueOnObjectiveForPlayers(Collection<? extends Player> players, String objectiveID, String scoreID, String value) {
      for(Player player : players) {
         updateScoreValueOnObjectiveToPlayer(player, objectiveID, scoreID, value);
      }
   }

   public static void updateScoreValueOnObjectiveForPlayers(Collection<? extends Player> players, DisplaySlot objectiveSlot, String scoreID, String value) {
      for(Player player : players) {
         updateScoreValueOnObjectiveToPlayer(player, objectiveSlot, scoreID, value);
      }
   }

   public static void updateScoreValueOnObjectiveForOnlinePlayers(String objectiveID, String scoreID, String value) {
      for(Player player : Bukkit.getOnlinePlayers()) {
         updateScoreValueOnObjectiveToPlayer(player, objectiveID, scoreID, value);
      }
   }

   public static void updateScoreValueOnObjectiveForOnlinePlayers(DisplaySlot objectiveSlot, String scoreID, String value) {
      for(Player player : Bukkit.getOnlinePlayers()) {
         updateScoreValueOnObjectiveToPlayer(player, objectiveSlot, scoreID, value);
      }
   }

   public static void updateScoreNameAndValueOnObjectiveToPlayer(
      Player player, org.bukkit.scoreboard.Objective objective, String scoreID, String name, String value
   ) {
      if (objective != null) {
         Team team = getTeamFromPlayer(player, objective.getName());
         if (team != null) {
            setTeamPrefix(team, name);
            setTeamSuffix(team, value);
         }
      }
   }

   public static void updateScoreNameAndValueOnObjectiveToPlayer(Player player, String objectiveID, String scoreID, String name, String value) {
      updateScoreNameAndValueOnObjectiveToPlayer(player, getObjectiveFromPlayer(player, objectiveID), scoreID, name, value);
   }

   public static void updateScoreNameAndValueOnObjectiveToPlayer(Player player, DisplaySlot objectiveSlot, String scoreID, String name, String value) {
      updateScoreNameAndValueOnObjectiveToPlayer(player, getObjectiveFromPlayer(player, objectiveSlot), scoreID, name, value);
   }

   public static void updateScoreNameAndValueOnObjectiveForPlayers(
      Collection<? extends Player> players, String objectiveID, String scoreID, String name, String value
   ) {
      for(Player player : players) {
         updateScoreNameAndValueOnObjectiveToPlayer(player, objectiveID, scoreID, name, value);
      }
   }

   public static void updateScoreNameAndValueOnObjectiveForPlayers(
      Collection<? extends Player> players, DisplaySlot objectiveSlot, String scoreID, String name, String value
   ) {
      for(Player player : players) {
         updateScoreNameAndValueOnObjectiveToPlayer(player, objectiveSlot, scoreID, name, value);
      }
   }

   public static void updateScoreNameAndValueOnObjectiveForOnlinePlayers(String objectiveID, String scoreID, String name, String value) {
      for(Player player : Bukkit.getOnlinePlayers()) {
         updateScoreNameAndValueOnObjectiveToPlayer(player, objectiveID, scoreID, name, value);
      }
   }

   public static void updateScoreNameAndValueOnObjectiveForOnlinePlayers(DisplaySlot objectiveSlot, String scoreID, String name, String value) {
      for(Player player : Bukkit.getOnlinePlayers()) {
         updateScoreNameAndValueOnObjectiveToPlayer(player, objectiveSlot, scoreID, name, value);
      }
   }

   public static void updateScoreOnObjectiveToPlayer(Player player, org.bukkit.scoreboard.Objective objective, String scoreID, String name, String value) {
      if (objective != null) {
         Team team = getTeamFromPlayer(player, objective.getName() + scoreID);
         if (team != null) {
            setTeamPrefix(team, name);
            setTeamSuffix(team, value);
         }
      }
   }

   public static void updateScoreOnObjectiveToPlayer(Player player, String objectiveID, String scoreID, String name, String value) {
      updateScoreOnObjectiveToPlayer(player, getObjectiveFromPlayer(player, objectiveID), scoreID, name, value);
   }

   public static void updateScoreOnObjectiveToPlayer(Player player, DisplaySlot objectiveSlot, String scoreID, String name, String value) {
      updateScoreOnObjectiveToPlayer(player, getObjectiveFromPlayer(player, objectiveSlot), scoreID, name, value);
   }

   public static void updateScoreOnObjectiveForPlayers(Collection<? extends Player> players, String objectiveID, String scoreID, String name, String value) {
      for(Player player : players) {
         updateScoreOnObjectiveToPlayer(player, objectiveID, scoreID, name, value);
      }
   }

   public static void updateScoreOnObjectiveForPlayers(
      Collection<? extends Player> players, DisplaySlot objectiveSlot, String scoreID, String name, String value
   ) {
      for(Player player : players) {
         updateScoreOnObjectiveToPlayer(player, objectiveSlot, scoreID, name, value);
      }
   }

   public static void updateScoreOnObjectiveForOnlinePlayers(String objectiveID, String scoreID, String name, String value) {
      for(Player player : Bukkit.getOnlinePlayers()) {
         updateScoreOnObjectiveToPlayer(player, objectiveID, scoreID, name, value);
      }
   }

   public static void updateScoreOnObjectiveForOnlinePlayers(DisplaySlot objectiveSlot, String scoreID, String name, String value) {
      for(Player player : Bukkit.getOnlinePlayers()) {
         updateScoreOnObjectiveToPlayer(player, objectiveSlot, scoreID, name, value);
      }
   }
}

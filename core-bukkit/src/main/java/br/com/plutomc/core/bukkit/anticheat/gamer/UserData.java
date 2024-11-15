package br.com.plutomc.core.bukkit.anticheat.gamer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import br.com.plutomc.core.bukkit.anticheat.StormCore;
import br.com.plutomc.core.common.CommonPlugin;
import br.com.plutomc.core.bukkit.anticheat.hack.HackData;
import br.com.plutomc.core.bukkit.anticheat.hack.HackType;
import br.com.plutomc.core.common.packet.types.staff.Stafflog;
import br.com.plutomc.core.common.utils.string.MessageBuilder;
import net.md_5.bungee.api.chat.ClickEvent.Action;
import org.bukkit.Bukkit;
import org.bukkit.Location;

public class UserData {
   private final UUID playerId;
   private String playerName;
   private Map<HackType, HackData> hackMap;
   private Location lastLocation;
   private double distanceY;
   private boolean isFalling;
   private boolean isGoingUp;
   private int ping;

   public UserData(UUID playerId, String playerName) {
      this.playerId = playerId;
      this.playerName = playerName;
      this.hackMap = new HashMap<>();
   }

   public void pulse(HackType hackType) {
      this.pulse(hackType, "");
   }

   public void pulse(HackType hackType, String message) {
      HackData hackData = this.hackMap.computeIfAbsent(hackType, v -> new HackData(hackType, this));
      hackData.addTimes();
      int times = hackData.getTimes();
      boolean important = (double)times / (double)hackType.getMaxAlerts() > 0.7;
      String string = "§c"
         + this.getPlayerName()
         + " pode estar usando "
         + hackType.name().toLowerCase()
         + (important ? " no servidor " + CommonPlugin.getInstance().getServerId() : "")
         + (times > hackType.getMaxAlerts() ? " (" + times + ")" : " (" + times + "/" + hackType.getMaxAlerts() + ")");
      String hover = "§fServidor: §a"
         + CommonPlugin.getInstance().getServerId()
         + (message.isEmpty() ? "" : "\n§7" + message.replace("(", "").replace(")", ""));
      if (important) {
         CommonPlugin.getInstance()
            .getServerData()
            .sendPacket(
               new Stafflog(new MessageBuilder(string).setHoverEvent(hover).setClickEvent(Action.SUGGEST_COMMAND, "/teleport " + this.getPlayerName()).create())
                  .anticheat()
                  .bungeecord()
            );
      } else {
         CommonPlugin.getInstance()
            .getAccountManager()
            .getAccounts()
            .stream()
            .filter(member -> member.isStaff() && member.getAccountConfiguration().isAnticheatEnabled())
            .forEach(
               member -> member.sendMessage(
                     new MessageBuilder(string).setHoverEvent(hover).setClickEvent(Action.SUGGEST_COMMAND, "/teleport " + this.getPlayerName()).create()
                  )
            );
      }

      Bukkit.getConsoleSender()
         .sendMessage(
            "§c"
               + this.getPlayerName()
               + " pode estar usando "
               + hackType.name().toLowerCase()
               + " "
               + message
               + " ("
               + times
               + "/"
               + hackType.getMaxAlerts()
               + ")"
         );
      if (times > hackType.getMaxAlerts() && !StormCore.getInstance().getBanPlayerMap().containsKey(this.playerId)) {
         StormCore.getInstance().autoban(this.playerId);
      }
   }

   public UUID getPlayerId() {
      return this.playerId;
   }

   public String getPlayerName() {
      return this.playerName;
   }

   public Map<HackType, HackData> getHackMap() {
      return this.hackMap;
   }

   public Location getLastLocation() {
      return this.lastLocation;
   }

   public double getDistanceY() {
      return this.distanceY;
   }

   public boolean isFalling() {
      return this.isFalling;
   }

   public boolean isGoingUp() {
      return this.isGoingUp;
   }

   public int getPing() {
      return this.ping;
   }

   public void setLastLocation(Location lastLocation) {
      this.lastLocation = lastLocation;
   }

   public void setDistanceY(double distanceY) {
      this.distanceY = distanceY;
   }

   public void setFalling(boolean isFalling) {
      this.isFalling = isFalling;
   }

   public void setGoingUp(boolean isGoingUp) {
      this.isGoingUp = isGoingUp;
   }

   public void setPing(int ping) {
      this.ping = ping;
   }
}

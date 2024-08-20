package br.com.plutomc.core.bukkit.anticheat.hack;

import br.com.plutomc.core.bukkit.anticheat.gamer.UserData;

public class HackData {
   private HackType hackType;
   private long lastNotify;
   private int times;

   public HackData(HackType hackType, UserData userData) {
      this.hackType = hackType;
      this.lastNotify = System.currentTimeMillis();
   }

   public void addTimes() {
      ++this.times;
      this.lastNotify = System.currentTimeMillis();
   }

   public HackType getHackType() {
      return this.hackType;
   }

   public long getLastNotify() {
      return this.lastNotify;
   }

   public int getTimes() {
      return this.times;
   }
}

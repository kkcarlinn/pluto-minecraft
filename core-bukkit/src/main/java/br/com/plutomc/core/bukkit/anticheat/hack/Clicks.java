package br.com.plutomc.core.bukkit.anticheat.hack;

public class Clicks {
   private int clicks;
   private long expireTime = System.currentTimeMillis() + 1000L;

   public void addClick() {
      ++this.clicks;
   }

   public int getClicks() {
      return this.clicks;
   }

   public long getExpireTime() {
      return this.expireTime;
   }
}

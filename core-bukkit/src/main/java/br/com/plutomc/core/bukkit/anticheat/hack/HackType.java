package br.com.plutomc.core.bukkit.anticheat.hack;

public enum HackType {
   AUTOSOUP(10, true),
   KILLAURA(15, true),
   FLY(10, true),
   SPEED(10),
   AUTOCLICK(20),
   MACRO(20),
   REACH(100),
   GLIDE(15, true),
   VELOCITY(100);

   private int maxAlerts;
   private boolean permanent;

   private HackType(int maxAlerts) {
      this(maxAlerts, false);
   }

   private HackType(int maxAlerts, boolean permanent) {
      this.maxAlerts = maxAlerts;
      this.permanent = permanent;
   }

   public int getMaxAlerts() {
      return this.maxAlerts;
   }

   public boolean isPermanent() {
      return this.permanent;
   }
}

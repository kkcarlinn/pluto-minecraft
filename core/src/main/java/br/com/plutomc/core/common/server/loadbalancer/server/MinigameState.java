package br.com.plutomc.core.common.server.loadbalancer.server;

public enum MinigameState {
   PREGAME(true),
   WAITING(true),
   STARTING(true),
   INVINCIBILITY(true),
   GAMETIME,
   WINNING(true),
   NONE,
   FINAL_BATTLE(true);

   private boolean decrementTime;

   public boolean isPregame() {
      switch(this) {
         case STARTING:
         case WAITING:
         case PREGAME:
            return true;
         default:
            return false;
      }
   }

   public boolean isInvencibility() {
      return this == INVINCIBILITY;
   }

   public boolean isGametime() {
      return this == GAMETIME;
   }

   public boolean isEnding() {
      return this == WINNING;
   }

   public boolean isState(MinigameState state) {
      return this == state;
   }

   private MinigameState() {
   }

   private MinigameState(boolean decrementTime) {
      this.decrementTime = decrementTime;
   }

   public boolean isDecrementTime() {
      return this.decrementTime;
   }
}

package br.com.plutomc.core.bungee.manager;

public class LoginManager {
   private int[] average = new int[6];
   private long currentTime = System.currentTimeMillis();
   private int players;

   public static void main(String[] args) throws InterruptedException {
      LoginManager loginManager = new LoginManager();

      while(true) {
         for(int i = 0; (double)i < Math.random() * 16.0; ++i) {
            loginManager.connection();
         }

         loginManager.print();
         System.out.println(loginManager.getAverage());
         Thread.sleep(1000L);
      }
   }

   public void connection() {
      if (this.currentTime < System.currentTimeMillis()) {
         this.doAverage(this.players);
         this.players = 0;
         this.currentTime = System.currentTimeMillis();
      }

      ++this.players;
   }

   public void doAverage(int players) {
      for(int i = this.average.length - 1; i > 0; --i) {
         this.average[i] = this.average[i - 1];
      }

      this.average[0] = players;
   }

   public void print() {
      for(int i = 0; i < this.average.length; ++i) {
         System.out.print(this.average[i] + " ");
      }

      System.out.println();
   }

   public int getLastSecondConnections() {
      return this.average[0];
   }

   public float getAverage() {
      float sum = 0.0F;

      for(int i = 0; i < this.average.length; ++i) {
         sum += (float)this.average[i];
      }

      return sum / (float)this.average.length;
   }

   public long getCurrentTime() {
      return this.currentTime;
   }

   public int getPlayers() {
      return this.players;
   }
}

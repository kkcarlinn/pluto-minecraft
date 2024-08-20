package br.com.plutomc.core.bukkit.utils.cooldown;

import java.util.concurrent.TimeUnit;
import lombok.NonNull;

public class Cooldown {
   @NonNull
   private String name;
   private long duration;
   private long startTime = System.currentTimeMillis();

   public Cooldown(String name, long duration) {
      this.name = name;
      this.duration = duration;
   }

   public void update(long duration, long startTime) {
      this.duration = duration;
      this.startTime = startTime;
   }

   public double getPercentage() {
      return this.getRemaining() * 100.0 / (double)this.duration;
   }

   public double getRemaining() {
      long endTime = this.startTime + TimeUnit.SECONDS.toMillis(this.duration);
      return (double)(-(System.currentTimeMillis() - endTime)) / 1000.0;
   }

   public boolean expired() {
      return this.getRemaining() < 0.0;
   }

   public Cooldown(@NonNull String name) {
      if (name == null) {
         throw new NullPointerException("name is marked non-null but is null");
      } else {
         this.name = name;
      }
   }

   @NonNull
   public String getName() {
      return this.name;
   }

   public void setName(@NonNull String name) {
      if (name == null) {
         throw new NullPointerException("name is marked non-null but is null");
      } else {
         this.name = name;
      }
   }

   public long getDuration() {
      return this.duration;
   }

   public long getStartTime() {
      return this.startTime;
   }
}

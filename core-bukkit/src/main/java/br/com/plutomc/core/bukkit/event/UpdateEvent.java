package br.com.plutomc.core.bukkit.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class UpdateEvent extends Event {
   public static final HandlerList handlers = new HandlerList();
   private UpdateType type;
   private long currentTick;

   public UpdateEvent(UpdateType type) {
      this(type, -1L);
   }

   public UpdateEvent(UpdateType type, long currentTick) {
      this.type = type;
      this.currentTick = currentTick;
   }

   public UpdateType getType() {
      return this.type;
   }

   public long getCurrentTick() {
      return this.currentTick;
   }

   @Override
   public HandlerList getHandlers() {
      return handlers;
   }

   public static HandlerList getHandlerList() {
      return handlers;
   }

   public static enum UpdateType {
      TICK,
      SECOND,
      MINUTE;
   }
}

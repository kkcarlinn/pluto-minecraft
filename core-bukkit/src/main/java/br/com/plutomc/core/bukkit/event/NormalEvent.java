package br.com.plutomc.core.bukkit.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class NormalEvent extends Event {
   private static final HandlerList HANDLER_LIST = new HandlerList();

   @Override
   public HandlerList getHandlers() {
      return HANDLER_LIST;
   }

   public static HandlerList getHandlerList() {
      return HANDLER_LIST;
   }
}

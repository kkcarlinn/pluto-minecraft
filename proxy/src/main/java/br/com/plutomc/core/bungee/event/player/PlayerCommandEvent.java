package br.com.plutomc.core.bungee.event.player;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Cancellable;
import net.md_5.bungee.api.plugin.Event;

public class PlayerCommandEvent extends Event implements Cancellable {
   private final ProxiedPlayer player;
   private final String command;
   private final String[] args;
   private boolean cancelled;

   public PlayerCommandEvent(ProxiedPlayer player, String command, String[] args) {
      this.player = player;
      this.command = command.toLowerCase();
      this.args = args;
   }

   public ProxiedPlayer getPlayer() {
      return this.player;
   }

   public String getCommand() {
      return this.command;
   }

   public String[] getArgs() {
      return this.args;
   }

   public void setCancelled(boolean cancelled) {
      this.cancelled = cancelled;
   }

   public boolean isCancelled() {
      return this.cancelled;
   }
}

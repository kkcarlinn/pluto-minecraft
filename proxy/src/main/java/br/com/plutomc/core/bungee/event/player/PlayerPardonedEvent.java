package br.com.plutomc.core.bungee.event.player;

import br.com.plutomc.core.common.command.CommandSender;
import br.com.plutomc.core.common.account.Account;
import br.com.plutomc.core.common.punish.Punish;
import net.md_5.bungee.api.plugin.Event;

public class PlayerPardonedEvent extends Event {
   private Account punished;
   private Punish punish;
   private CommandSender sender;

   public Account getPunished() {
      return this.punished;
   }

   public Punish getPunish() {
      return this.punish;
   }

   public CommandSender getSender() {
      return this.sender;
   }

   public PlayerPardonedEvent(Account punished, Punish punish, CommandSender sender) {
      this.punished = punished;
      this.punish = punish;
      this.sender = sender;
   }
}

package br.com.plutomc.core.bukkit.event.member;

import br.com.plutomc.core.bukkit.event.PlayerEvent;
import br.com.plutomc.core.common.account.Account;
import org.bukkit.entity.Player;

public class PlayerAuthEvent extends PlayerEvent {
   private Account account;

   public PlayerAuthEvent(Player player, Account account) {
      super(player);
      this.account = account;
   }

   public Account getMember() {
      return this.account;
   }
}

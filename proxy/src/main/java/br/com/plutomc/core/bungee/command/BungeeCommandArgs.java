package br.com.plutomc.core.bungee.command;

import br.com.plutomc.core.bungee.BungeeConst;
import br.com.plutomc.core.bungee.account.BungeeAccount;
import br.com.plutomc.core.common.CommonPlugin;
import br.com.plutomc.core.common.command.CommandArgs;
import br.com.plutomc.core.common.account.Account;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class BungeeCommandArgs extends CommandArgs {
   protected BungeeCommandArgs(CommandSender sender, String label, String[] args, int subCommand) {
      super(
         (br.com.plutomc.core.common.command.CommandSender)(sender instanceof ProxiedPlayer
            ? CommonPlugin.getInstance().getAccountManager().getAccount(((ProxiedPlayer)sender).getUniqueId())
            : BungeeConst.CONSOLE_SENDER),
         label,
         args,
         subCommand
      );
   }

   @Override
   public boolean isPlayer() {
      return this.getSender() instanceof Account;
   }

   public ProxiedPlayer getPlayer() {
      return !this.isPlayer() ? null : ((BungeeAccount)this.getSender()).getProxiedPlayer();
   }
}

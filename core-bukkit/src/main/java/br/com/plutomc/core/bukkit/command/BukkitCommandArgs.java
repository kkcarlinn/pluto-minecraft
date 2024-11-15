package br.com.plutomc.core.bukkit.command;

import br.com.plutomc.core.bukkit.BukkitConst;
import br.com.plutomc.core.common.CommonPlugin;
import br.com.plutomc.core.bukkit.account.BukkitAccount;
import br.com.plutomc.core.common.command.CommandArgs;
import br.com.plutomc.core.common.account.Account;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BukkitCommandArgs extends CommandArgs {
   protected BukkitCommandArgs(CommandSender sender, String label, String[] args, int subCommand) {
      super(
         (br.com.plutomc.core.common.command.CommandSender)(sender instanceof Player
            ? CommonPlugin.getInstance().getAccountManager().getAccount(((Player)sender).getUniqueId())
            : BukkitConst.CONSOLE_SENDER),
         label,
         args,
         subCommand
      );
   }

   @Override
   public boolean isPlayer() {
      return this.getSender() instanceof Account;
   }

   public BukkitAccount getSenderAsBukkitMember() {
      return BukkitAccount.class.cast(this.getSender());
   }

   public Player getPlayer() {
      return !this.isPlayer() ? null : ((BukkitAccount)this.getSender()).getPlayer();
   }
}

package br.com.plutomc.core.bukkit.command.register;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import br.com.plutomc.core.common.command.CommandArgs;
import br.com.plutomc.core.common.command.CommandClass;
import br.com.plutomc.core.common.command.CommandFramework;
import br.com.plutomc.core.common.account.Account;
import br.com.plutomc.core.common.account.configuration.AccountConfiguration;
import br.com.plutomc.core.common.punish.PunishType;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

public class PunishCommand implements CommandClass {
   @CommandFramework.Command(
      name = "ban",
      aliases = {"kick", "mute", "tempban", "tempmute", "tempbanir", "tempmutar", "punish"},
      permission = "command.punish"
   )
   public void banCommand(CommandArgs cmdArgs) {
   }

   @CommandFramework.Command(
      name = "pardon",
      aliases = {"unpunish", "unban", "unmute"},
      permission = "command.pardon"
   )
   public void pardonCommand(CommandArgs cmdArgs) {
   }

   @CommandFramework.Command(
      name = "anticheat",
      aliases = {"ac"},
      permission = "command.anticheat",
      console = false
   )
   public void anticheatCommand(CommandArgs cmdArgs) {
      Account account = cmdArgs.getSenderAsMember(Account.class);
      account.getAccountConfiguration()
         .setCheatState(account.getAccountConfiguration().isAnticheatEnabled() ? AccountConfiguration.CheatState.DISABLED : AccountConfiguration.CheatState.ENABLED);
      account.sendMessage(account.getLanguage().t("command.anticheat.state-" + account.getAccountConfiguration().getCheatState().name().toLowerCase()));
   }

   @CommandFramework.Completer(
      name = "punish"
   )
   public List<String> banCompleter(CommandArgs cmdArgs) {
      return cmdArgs.getArgs().length == 2
         ? Arrays.asList(PunishType.values())
            .stream()
            .filter(state -> state.name().toLowerCase().startsWith(cmdArgs.getArgs()[1].toLowerCase()))
            .map(Enum::name)
            .collect(Collectors.toList())
         : Bukkit.getOnlinePlayers()
            .stream()
            .filter(player -> player.getName().toLowerCase().startsWith(cmdArgs.getArgs()[cmdArgs.getArgs().length - 1].toLowerCase()))
            .map(OfflinePlayer::getName)
            .collect(Collectors.toList());
   }
}

package br.com.plutomc.core.bukkit.command.register;

import br.com.plutomc.core.bukkit.BukkitCommon;
import br.com.plutomc.core.bukkit.event.member.PlayerAuthEvent;
import br.com.plutomc.core.common.CommonPlugin;
import br.com.plutomc.core.bukkit.account.BukkitAccount;
import br.com.plutomc.core.common.command.CommandArgs;
import br.com.plutomc.core.common.command.CommandClass;
import br.com.plutomc.core.common.command.CommandFramework;
import br.com.plutomc.core.common.account.configuration.LoginConfiguration;
import br.com.plutomc.core.common.server.ServerType;
import org.bukkit.Bukkit;

public class LoginCommand implements CommandClass {
   @CommandFramework.Command(
      name = "register",
      aliases = {"registrar"},
      console = false
   )
   public void registerCommand(CommandArgs cmdArgs) {
      BukkitAccount sender = cmdArgs.getSenderAsMember(BukkitAccount.class);
      String[] args = cmdArgs.getArgs();
      if (sender.getLoginConfiguration().isRegistered()) {
         sender.sendMessage("§cVocê já está registrado.");
      } else if (sender.getLoginConfiguration().getAccountType() != LoginConfiguration.AccountType.PREMIUM && !sender.getLoginConfiguration().isCaptcha()) {
         sender.sendMessage("§cComplete o captcha para se logar.");
      } else if (args.length <= 1) {
         sender.sendMessage("§a§eUse §b/" + cmdArgs.getLabel() + " <sua senha> <repita sua senha>§e para se registrar.");
         sender.sendMessage("§cNão utilize coloque os símbolos < e > na sua senha.");
      } else {
         if (args[0].equals(args[1])) {
            sender.sendMessage("§%command.register.success%§");
            sender.getLoginConfiguration().register(args[0]);
            sender.getLoginConfiguration().startSession();
            Bukkit.getPluginManager().callEvent(new PlayerAuthEvent(sender.getPlayer(), sender));
         } else {
            sender.sendMessage("§cAs senhas inseridas não são iguais.");
         }
      }
   }

   @CommandFramework.Command(
      name = "logout",
      aliases = {"deslogar"},
      console = false
   )
   public void logoutCommand(CommandArgs cmdArgs) {
      BukkitAccount sender = cmdArgs.getSenderAsMember(BukkitAccount.class);
      if (sender.getLoginConfiguration().getAccountType() != LoginConfiguration.AccountType.PREMIUM) {
         sender.getLoginConfiguration().logOut();
         sender.getLoginConfiguration().stopSession();
         BukkitCommon.getInstance().sendPlayerToServer(sender.getPlayer(), ServerType.LOGIN);
      }
   }

   @CommandFramework.Command(
      name = "login",
      aliases = {"logar"},
      console = false
   )
   public void loginCommand(CommandArgs cmdArgs) {
      BukkitAccount sender = cmdArgs.getSenderAsMember(BukkitAccount.class);
      String[] args = cmdArgs.getArgs();
      if (!sender.getLoginConfiguration().isRegistered()) {
         sender.sendMessage("§cVocê ainda não se registrou, utilize /register para se autenticar.");
      } else if (sender.getLoginConfiguration().isLogged()) {
         sender.sendMessage("§cVocê já está logado.");
      } else if (args.length == 0) {
         sender.sendMessage("§eUse §b/" + cmdArgs.getLabel() + " <sua senha>§e para se logar.");
      } else {
         if (sender.getLoginConfiguration().isPassword(args[0])) {
            sender.sendMessage("§%command.login.success%§");
            sender.getLoginConfiguration().logIn();
            sender.getLoginConfiguration().startSession();
            Bukkit.getPluginManager().callEvent(new PlayerAuthEvent(sender.getPlayer(), sender));
         } else {
            int attemp = sender.getLoginConfiguration().attemp();
            if (attemp >= 5) {
               sender.getPlayer()
                  .kickPlayer(
                     "§cVocê errou sua senha diversas vezes.\n§f\n§ePara mais informações, acesse §b" + CommonPlugin.getInstance().getPluginInfo().getWebsite()
                  );
            } else {
               sender.sendMessage("§cSenha inserida inválida, você possui mais " + (5 - attemp) + "tentativas.");
            }
         }
      }
   }
}

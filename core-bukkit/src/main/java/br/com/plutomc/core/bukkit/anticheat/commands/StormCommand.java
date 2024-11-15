package br.com.plutomc.core.bukkit.anticheat.commands;

import br.com.plutomc.core.bukkit.BukkitCommon;
import br.com.plutomc.core.bukkit.anticheat.StormCore;
import br.com.plutomc.core.common.CommonPlugin;
import br.com.plutomc.core.bukkit.account.BukkitAccount;
import br.com.plutomc.core.common.command.CommandArgs;
import br.com.plutomc.core.common.command.CommandClass;
import br.com.plutomc.core.common.command.CommandFramework;
import br.com.plutomc.core.common.command.CommandSender;
import br.com.plutomc.core.common.utils.string.StringFormat;
import br.com.plutomc.core.common.utils.supertype.OptionalBoolean;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class StormCommand implements CommandClass {
   @CommandFramework.Command(
      name = "storm",
      aliases = {"st"},
      permission = "command.anticheat"
   )
   public void stormCommand(CommandArgs cmdArgs) {
      CommandSender sender = cmdArgs.getSender();
      String[] args = cmdArgs.getArgs();
      if (args.length == 0) {
         this.handleUsage(sender, cmdArgs.getLabel());
      } else {
         String var4 = args[0].toLowerCase();
         switch(var4) {
            case "forcefield":
               if (args.length == 1) {
                  sender.sendMessage("§eUse §b/" + cmdArgs.getLabel() + " forcefield <player> §apara testar o forcefield.");
                  return;
               }

               Player target = Bukkit.getPlayer(args[1]);
               if (target == null) {
                  sender.sendMessage(sender.getLanguage().t("player-is-not-online", "%player%", args[1]));
                  return;
               }

               boolean invisible = true;
               if (args.length >= 3) {
                  OptionalBoolean optionalBoolean = StringFormat.parseBoolean(args[2]);
                  invisible = optionalBoolean.isPresent() ? optionalBoolean.getAsBoolean() : true;
               }

               sender.sendMessage("§aExecutando o teste, aguarde...");
               StormCore.getInstance().getForcefieldManager().check(target, invisible, (result, throwable) -> {
                  int chance = result.getHits() / result.getEntityCount() * 100;
                  sender.sendMessage("");
                  sender.sendMessage("§aForcefield " + target.getName());
                  sender.sendMessage("  §fHits: §7" + result.getHits());
                  sender.sendMessage("  §fEntities: §7" + result.getEntityCount());
                  sender.sendMessage("  §fResultado: §7" + (chance > 75 ? "§c" : (chance > 50 ? "§e" : "§a")) + chance + "% de chance. ");
                  sender.sendMessage("");
               });
               break;
            case "autosoup":
            case "autoclick":
            case "macro":
               sender.sendMessage("§aEm breve...");
               break;
            default:
               this.handleUsage(sender, cmdArgs.getLabel());
         }
      }
   }

   @CommandFramework.Command(
      name = "anticheatbypass",
      permission = "staff.super"
   )
   public void anticheatbypassCommand(CommandArgs cmdArgs) {
      CommandSender sender = cmdArgs.getSender();
      String[] args = cmdArgs.getArgs();
      if (args.length == 0) {
         sender.sendMessage("§eUse §e/" + cmdArgs.getLabel() + " <player>§e para remover o autoban de um player.");
      } else {
         BukkitAccount member = CommonPlugin.getInstance().getAccountManager().getAccountByName(args[0], BukkitAccount.class);
         if (member == null) {
            sender.sendMessage(sender.getLanguage().t("player-is-not-online", "%player%", args[0]));
         } else {
            member.setAnticheatBypass(!member.isAnticheatBypass());
            sender.sendMessage("§aO modo anticheat bypass do player " + member.getName() + " foi alterado para " + member.isAnticheatBypass() + ".");
         }
      }
   }

   @CommandFramework.Command(
      name = "autoban",
      permission = "command.anticheat"
   )
   public void autobanCommand(CommandArgs cmdArgs) {
      CommandSender sender = cmdArgs.getSender();
      String[] args = cmdArgs.getArgs();
      if (args.length == 0) {
         sender.sendMessage("§eUse §e/" + cmdArgs.getLabel() + " <player>§e para remover o autoban de um player.");
      } else {
         BukkitAccount member = CommonPlugin.getInstance().getAccountManager().getAccountByName(args[0], BukkitAccount.class);
         if (member == null) {
            sender.sendMessage(sender.getLanguage().t("player-is-not-online", "%player%", args[0]));
         } else if (!BukkitCommon.getInstance().getStormCore().getBanPlayerMap().containsKey(member.getUniqueId())) {
            sender.sendMessage("§cO jogador não está na lista de pré banimento.");
         } else {
            sender.sendMessage("§aO jogador foi removido da lista de pré banimento e seus alertas foram limpos.");
            member.getUserData().getHackMap().clear();
            BukkitCommon.getInstance().getStormCore().getBanPlayerMap().remove(member.getUniqueId());
         }
      }
   }

   private void handleUsage(CommandSender sender, String label) {
      sender.sendMessage("§eUse §b/" + label + " forcefield <player> @optional:invisible<true:false> §epara testar o forcefield.");
      sender.sendMessage("§eUse §b/" + label + " autosoup <player> §epara testar o auto-soup.");
      sender.sendMessage("§eUse §b/" + label + " autoclick <player> §epara testar o autoclick.");
   }
}

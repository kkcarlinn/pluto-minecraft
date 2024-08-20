package br.com.plutomc.core.bukkit.command.register;

import br.com.plutomc.core.bukkit.BukkitCommon;
import br.com.plutomc.core.bukkit.menu.staff.AdminInventory;
import br.com.plutomc.core.bukkit.menu.staff.punish.PunishInfoInventory;
import br.com.plutomc.core.bukkit.menu.staff.server.ServerListInventory;
import com.google.common.base.Joiner;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import br.com.plutomc.core.common.CommonPlugin;
import br.com.plutomc.core.bukkit.member.BukkitMember;
import br.com.plutomc.core.common.command.CommandArgs;
import br.com.plutomc.core.common.command.CommandClass;
import br.com.plutomc.core.common.command.CommandFramework;
import br.com.plutomc.core.common.command.CommandSender;
import br.com.plutomc.core.common.language.Language;
import br.com.plutomc.core.common.member.Member;
import br.com.plutomc.core.common.permission.Group;
import br.com.plutomc.core.common.utils.string.StringFormat;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class ModeradorCommand implements CommandClass {

   @CommandFramework.Command(
           name = "punishinfo",
           console = false,
           permission = "command.punish"
   )
   public void punishinfoCommand(CommandArgs cmdArgs) {
      BukkitMember sender = cmdArgs.getSenderAsMember(BukkitMember.class);
      String[] args = cmdArgs.getArgs();
      if (args.length == 0) {
         sender.sendMessage("§eUse §b/" + cmdArgs.getLabel() + " <player>§e para ver as punições de um jogador.");
      } else {
         Member target = CommonPlugin.getInstance().getMemberManager().getMemberByName(cmdArgs.getArgs()[0]);
         if (target == null) {
            target = CommonPlugin.getInstance().getMemberData().loadMember(cmdArgs.getArgs()[0], true);
            if (target == null) {
               sender.sendMessage(sender.getLanguage().t("account-doesnt-exist", "%player%", cmdArgs.getArgs()[0]));
               return;
            }
         }

         new PunishInfoInventory(sender.getPlayer(), target);
      }
   }

   @CommandFramework.Command(
      name = "serverlist",
      console = false,
      permission = "command.server"
   )
   public void serverlistCommand(CommandArgs cmdArgs) {
      new ServerListInventory(cmdArgs.getSenderAsMember(BukkitMember.class).getPlayer(), 1);
   }

   @CommandFramework.Command(
      name = "clearchat",
      aliases = {"cc"},
      permission = "command.clearchat"
   )
   public void clearchatCommand(CommandArgs cmdArgs) {
      for(int i = 0; i < 128; ++i) {
         Bukkit.broadcastMessage(" ");
      }

      this.staffLog("O chat foi limpo pelo " + cmdArgs.getSender().getName());
   }

   @CommandFramework.Command(
      name = "chat",
      permission = "command.chat"
   )
   public void chatCommand(CommandArgs cmdArgs) {
      CommandSender sender = cmdArgs.getSender();
      String[] args = cmdArgs.getArgs();
      if (args.length == 0) {
         sender.sendMessage("§eUse §b/" + cmdArgs.getLabel() + " <on:off>§e para desativar ou ativar o chat.");
      } else {
         BukkitCommon.ChatState chatState = null;
         String var5 = args[0].toLowerCase();
         switch(var5) {
            case "on":
               chatState = BukkitCommon.ChatState.ENABLED;
               break;
            case "off":
               chatState = BukkitCommon.ChatState.DISABLED;
               break;
            case "vips":
               chatState = BukkitCommon.ChatState.PAYMENT;
               break;
            default:
               try {
                  chatState = BukkitCommon.ChatState.valueOf(cmdArgs.getArgs()[0].toUpperCase());
               } catch (Exception var8) {
                  sender.sendMessage("§eUse §b/" + cmdArgs.getLabel() + " <on:off>§e para desativar ou ativar o chat.");
               }
         }

         BukkitCommon.getInstance().setChatState(chatState);
         sender.sendMessage("§aO chat do servidor foi alterado para " + chatState.name() + ".");
         this.staffLog("O " + sender.getName() + " alterou o estado do chat para " + chatState.name() + "");
      }
   }

   @CommandFramework.Command(
      name = "inventorysee",
      aliases = {"invsee", "inv"},
      console = false,
      permission = "command.invsee"
   )
   public void invseeCommand(CommandArgs cmdArgs) {
      CommandSender sender = cmdArgs.getSender();
      String[] args = cmdArgs.getArgs();
      if (args.length == 0) {
         sender.sendMessage("§eUse §b/" + cmdArgs.getLabel() + " <player>§e para abrir o inventário do player.");
      } else {
         Player player = Bukkit.getPlayer(args[0]);
         if (player == null) {
            sender.sendMessage(sender.getLanguage().t("player-is-not-online", "%player%", args[0]));
         } else {
            cmdArgs.getSenderAsMember(BukkitMember.class).getPlayer().openInventory(player.getInventory());
            this.staffLog("O " + sender.getName() + " abriu o inventário de " + player.getName());
         }
      }
   }

   @CommandFramework.Command(
      name = "say",
      permission = "command.say"
   )
   public void sayCommand(CommandArgs cmdArgs) {
      CommandSender sender = cmdArgs.getSender();
      String[] args = cmdArgs.getArgs();
      if (args.length == 0) {
         sender.sendMessage("§eUse §b/" + cmdArgs.getLabel() + " <message>§e para enviar uma mensagem no servidor.");
      } else {
         String message = Joiner.on(' ').join(args).replace('&', '§');
         Bukkit.broadcastMessage("");
         Bukkit.broadcastMessage("§dServer> §f" + message);
         Bukkit.broadcastMessage("");
         this.staffLog("O " + sender.getName() + " mandou uma mensagem global.");
      }
   }

   @CommandFramework.Command(
      name = "admin",
      aliases = {"adm"},
      console = false,
      permission = "command.admin"
   )
   public void adminCommand(CommandArgs cmdArgs) {
      Player player = cmdArgs.getSenderAsMember(BukkitMember.class).getPlayer();

         if (BukkitCommon.getInstance().getVanishManager().isPlayerInAdmin(player)) {
            BukkitCommon.getInstance().getVanishManager().setPlayer(player);
            this.staffLog("O " + player.getName() + " saiu do modo admin");
         } else {
            BukkitCommon.getInstance().getVanishManager().setPlayerInAdmin(player);
            this.staffLog("O " + player.getName() + " entrou no modo admin");
         }

   }

   @CommandFramework.Command(
           name = "staff",
           aliases = {"stf"},
           console = false,
           permission = "command.admin"
   )
   public void adminPrefs(CommandArgs cmdArgs) {
      Player player = cmdArgs.getSenderAsMember(BukkitMember.class).getPlayer();
      new AdminInventory(player, 0L);
   }

   @CommandFramework.Command(
      name = "vanish",
      aliases = {"v"},
      permission = "command.vanish",
      console = false
   )
   public void vanishCommand(CommandArgs cmdArgs) {
      Player player = cmdArgs.getSenderAsMember(BukkitMember.class).getPlayer();
      String[] args = cmdArgs.getArgs();
      Group hidePlayer;
      if (args.length == 0) {
         if (BukkitCommon.getInstance().getVanishManager().isPlayerVanished(player.getUniqueId())) {
            BukkitCommon.getInstance().getVanishManager().showPlayer(player);
            player.sendMessage("§dVocê está visível para todos os jogadores.");
            this.staffLog("O " + player.getName() + " não está mais invisível");
            return;
         }

         hidePlayer = BukkitCommon.getInstance().getVanishManager().hidePlayer(player);
      } else {
         Group group = CommonPlugin.getInstance().getPluginInfo().getGroupByName(args[0]);
         if (group == null) {
            player.sendMessage("§cO grupo " + StringFormat.formatString(args[0]) + " não existe.");
            return;
         }

         hidePlayer = group;
      }

      if (hidePlayer.getId() > cmdArgs.getSender().getServerGroup().getId()) {
         player.sendMessage("§cVocê não pode ficar invisível para um grupo superior ao seu.");
      } else {
         BukkitCommon.getInstance().getVanishManager().setPlayerVanishToGroup(player, hidePlayer);
         player.sendMessage(
            Language.getLanguage(player.getUniqueId()).t("vanish.player-group-hided", "%group%", StringFormat.formatString(hidePlayer.getGroupName()))
         );
         this.staffLog("O " + player.getName() + " ficou invisível para " + StringFormat.formatString(hidePlayer.getGroupName()));
      }
   }

   @CommandFramework.Completer(
      name = "chat"
   )
   public List<String> chatCompleter(CommandArgs cmdArgs) {
      return (List<String>)(cmdArgs.getArgs().length == 1
         ? Arrays.asList(BukkitCommon.ChatState.values())
            .stream()
            .filter(state -> state.name().toLowerCase().startsWith(cmdArgs.getArgs()[0].toLowerCase()))
            .map(Enum::name)
            .collect(Collectors.toList())
         : new ArrayList<>());
   }
}

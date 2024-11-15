package br.com.plutomc.core.bungee.command.register;

import com.google.common.base.Joiner;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import br.com.plutomc.core.common.CommonPlugin;
import br.com.plutomc.core.bungee.BungeeMain;
import br.com.plutomc.core.bungee.account.BungeeAccount;
import br.com.plutomc.core.common.command.CommandArgs;
import br.com.plutomc.core.common.command.CommandClass;
import br.com.plutomc.core.common.command.CommandFramework;
import br.com.plutomc.core.common.command.CommandSender;
import br.com.plutomc.core.common.language.Language;
import br.com.plutomc.core.common.account.Account;
import br.com.plutomc.core.common.report.Report;
import br.com.plutomc.core.common.server.ServerType;
import br.com.plutomc.core.common.server.loadbalancer.server.ProxiedServer;
import br.com.plutomc.core.common.utils.DateUtils;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class ServerCommand implements CommandClass {
   @CommandFramework.Command(
      name = "ping",
      aliases = {"latency"}
   )
   public void pingCommand(CommandArgs cmdArgs) {
      CommandSender sender = cmdArgs.getSender();
      String[] args = cmdArgs.getArgs();
      if (args.length == 0) {
         if (sender.isPlayer()) {
            sender.sendMessage(
               sender.getLanguage()
                  .t("command-ping-your-latency", "%ping%", String.valueOf(cmdArgs.getSenderAsMember(BungeeAccount.class).getProxiedPlayer().getPing()))
            );
         } else {
            sender.sendMessage("§aO ping médio do servidor é de " + BungeeMain.getInstance().getAveragePing(ProxyServer.getInstance().getPlayers()) + "ms.");
         }
      } else {
         ProxiedPlayer player = ProxyServer.getInstance().getPlayer(args[0]);
         if (player == null) {
            sender.sendMessage(sender.getLanguage().t("player-not-found", "%player%", args[0]));
         } else {
            sender.sendMessage(sender.getLanguage().t("command-ping-player-latency", "%player%", player.getName(), "%ping%", String.valueOf(player.getPing())));
         }
      }
   }

   @CommandFramework.Command(
      name = "serverid",
      aliases = {"sid"})
   public void serverIdCommand (CommandArgs args) {
      if(args.isPlayer()) {
            args.getSender().sendMessage("§eNome: §b" +  args.getSenderAsMember().getActualServerId());
            args.getSender().sendMessage("§eCategoria: §b" +  args.getSenderAsMember().getActualServerType().getTypeName());
      } else {
            args.getSender().sendMessage("§cVocê não pode executar esse comando no console.");
      }
   }

   @CommandFramework.Command(
      name = "report",
      aliases = {"rp"}
   )
   public void reportCommand(CommandArgs cmdArgs) {
      CommandSender sender = cmdArgs.getSender();
      String[] args = cmdArgs.getArgs();
      if (args.length <= 1) {
         sender.sendMessage("§eUse §b/" + cmdArgs.getLabel() + " <player> <reason>§e para reportar um jogador.");
      } else {
         Account target = CommonPlugin.getInstance().getAccountManager().getAccountByName(args[0]);
         if (target == null) {
            sender.sendMessage(sender.getLanguage().t("player-is-not-online", "%player%", args[0]));
         } else {
            String reason = Joiner.on(' ').join(Arrays.copyOfRange(args, 1, args.length));
            Report report = CommonPlugin.getInstance().getReportManager().getReportById(target.getUniqueId());
            if (report == null) {
               report = new Report(target, sender, reason, target.isOnline());
               CommonPlugin.getInstance().getReportManager().createReport(report);
            } else {
               if (report.getReportMap().containsKey(sender.getUniqueId())
                  && report.getReportMap().get(sender.getUniqueId()).getCreatedAt() + 300000L > System.currentTimeMillis()) {
                  sender.sendMessage("§cVocê precisa esperar para reportar esse usuário novamente.");
                  return;
               }

               report.addReport(sender, reason);
            }

            sender.sendMessage("§aSua denúncia sobre o jogador " + target.getPlayerName() + " foi enviada ao servidor.");
            CommonPlugin.getInstance().getAccountManager().actionbar("§aUma nova denúncia foi registrada.", "command.report");
            CommonPlugin.getInstance()
               .getAccountManager()
               .getAccounts()
               .stream()
               .filter(m -> m.isStaff() && m.getAccountConfiguration().isReportsEnabled())
               .forEach(m -> m.sendMessage("§eO jogador " + sender.getName() + " denunciou o jogador " + target.getName() + " por " + reason));
         }
      }
   }

   @CommandFramework.Command(
      name = "server",
      aliases = {"connect"},
      console = false
   )
   public void serverCommand(CommandArgs cmdArgs) {
      BungeeAccount member = cmdArgs.getSenderAsMember(BungeeAccount.class);
      ProxiedPlayer player = member.getProxiedPlayer();
      String[] args = cmdArgs.getArgs();
      if (args.length == 0) {
         player.sendMessage(member.getLanguage().t("command-server-usage"));
      } else {
         String serverId = args[0];
         ProxiedServer server = BungeeMain.getInstance().getServerManager().getServerByName(serverId);
         if (server == null || server.getServerInfo() == null) {
            player.sendMessage(member.getLanguage().t("server-not-available"));
         } else if (server.isFull() && !member.hasPermission("server.full")) {
            player.sendMessage(member.getLanguage().t("server-is-full"));
         } else {
            player.connect(server.getServerInfo());
         }
      }
   }

   @CommandFramework.Command(
      name = "evento",
      console = false
   )
   public void eventoCommand(CommandArgs cmdArgs) {
      BungeeAccount member = cmdArgs.getSenderAsMember(BungeeAccount.class);
      ProxiedPlayer player = member.getProxiedPlayer();
      ProxiedServer server = BungeeMain.getInstance().getServerManager().getBalancer(ServerType.EVENTO).next();
      if (server == null || server.getServerInfo() == null) {
         player.sendMessage(member.getLanguage().t("server-not-available"));
      } else if (server.isFull() && !member.hasPermission("server.full")) {
         player.sendMessage(member.getLanguage().t("server-is-full"));
      } else {
         player.connect(server.getServerInfo());
      }
   }

   @CommandFramework.Command(
      name = "lobby",
      aliases = {"hub", "l"},
      console = false
   )
   public void lobbyCommand(CommandArgs cmdArgs) {
      BungeeAccount member = cmdArgs.getSenderAsMember(BungeeAccount.class);
      ProxiedPlayer player = member.getProxiedPlayer();
      ProxiedServer server = BungeeMain.getInstance()
         .getServerManager()
         .getBalancer(BungeeMain.getInstance().getServerManager().getServer(player.getServer().getInfo().getName()).getServerType().getServerLobby())
         .next();
      if (server == null || server.getServerInfo() == null) {
         player.sendMessage(member.getLanguage().t("server-not-available"));
      } else if (server.isFull() && !member.hasPermission("server.full")) {
         player.sendMessage(member.getLanguage().t("server-is-full"));
      } else {
         player.connect(server.getServerInfo());
      }
   }

   @CommandFramework.Command(
      name = "play",
      aliases = {"jogar"},
      console = false
   )
   public void playCommand(CommandArgs cmdArgs) {
      ProxiedPlayer player = cmdArgs.getSenderAsMember(BungeeAccount.class).getProxiedPlayer();
      String[] args = cmdArgs.getArgs();
      if (args.length == 0) {
         player.sendMessage(cmdArgs.getSender().getLanguage().t("command.play.usage"));
      } else {
         ServerType serverType = ServerType.getTypeByName(Joiner.on(' ').join(args).replace(" ", "_"));
         if (serverType == null) {
            player.sendMessage("§cEsse servidor não existe.");
         } else {
            ProxiedServer server = BungeeMain.getInstance().getServerManager().getBalancer(serverType).next();
            Language language = cmdArgs.getSender().getLanguage();
            if (server == null || server.getServerInfo() == null) {
               player.sendMessage("§cNenhum servidor desse modo está disponível no momento.");
            } else if (server.isFull() && !player.hasPermission("server.full")) {
               player.sendMessage(language.t("server-is-full"));
            } else {
               player.connect(server.getServerInfo());
            }
         }
      }
   }

   @CommandFramework.Command(
      name = "bwhitelist",
      aliases = {"bungeewhitelist", "gwhitelist", "maintenance", "manutencao"},
      permission = "command.whitelist"
   )
   public void bwhitelistChatCommand(CommandArgs cmdArgs) {
      String[] args = cmdArgs.getArgs();
      CommandSender sender = cmdArgs.getSender();
      if (args.length == 0) {
         sender.sendMessage("§eUse /" + cmdArgs.getLabel() + " <on:off> <time> para ativar ou desativar a whitelist global.");
         sender.sendMessage("§eUse /" + cmdArgs.getLabel() + " add <player> para adicionar alguem a whitelist.");
         sender.sendMessage("§eUse /" + cmdArgs.getLabel() + " remove <player> para remover alguem da whitelist.");
         sender.sendMessage("§eUse /" + cmdArgs.getLabel() + " group <beta:staff:group> para definir qual grupo irá entrar no servidor.");
      } else {
         String var4 = args[0].toLowerCase();
         switch(var4) {
            case "on":
               Long time = 0L;
               if (args.length > 1) {
                  time = DateUtils.getTime(args[1]);
               }

               BungeeMain.getInstance().setWhitelistEnabled(true, time);
               sender.sendMessage("§aVocê §aativou§a a whitelist!");
               CommonPlugin.getInstance()
                  .getAccountManager()
                  .getAccounts(BungeeAccount.class)
                  .stream()
                  .filter(bungee -> !bungee.hasPermission("command.admin"))
                  .forEach(bungee -> bungee.getProxiedPlayer().disconnect("§cO servidor entrou em manutenção."));
               break;
            case "off":
               BungeeMain.getInstance().setWhitelistEnabled(false, 0L);
               sender.sendMessage("§aVocê §cdesativou§a a whitelist!");
               break;
            case "add":
               if (args.length == 1) {
                  sender.sendMessage("§eUse /" + cmdArgs.getLabel() + " add <player> para adicionar alguem a whitelist.");
               } else {
                  Account account = CommonPlugin.getInstance().getAccountManager().getAccountByName(args[1]);
                  if (account == null) {
                     account = CommonPlugin.getInstance().getAccountData().loadAccount(args[1], true);
                     if (account == null) {
                        sender.sendMessage("§cO jogador " + args[1] + " não existe!");
                        break;
                     }
                  }

                  BungeeMain.getInstance().addMemberToWhiteList(account.getPlayerName());
                  sender.sendMessage("§aO jogador §a" + account.getPlayerName() + "§a foi adicionado a whitelist!");
               }
               break;
            case "remove":
               if (args.length == 1) {
                  sender.sendMessage("§eUse /" + cmdArgs.getLabel() + " remove <player> para remover alguem da whitelist.");
               } else {
                  Account account = CommonPlugin.getInstance().getAccountManager().getAccountByName(args[1]);
                  if (account == null) {
                     account = CommonPlugin.getInstance().getAccountData().loadAccount(args[1], true);
                     if (account == null) {
                        sender.sendMessage("§cO jogador " + args[1] + " não existe!");
                        break;
                     }
                  }

                  BungeeMain.getInstance().removeMemberFromWhiteList(account.getPlayerName());
                  sender.sendMessage("§aO jogador §a" + account.getPlayerName() + "§a foi removido da whitelist!");
               }
               break;
            case "list":
               sender.sendMessage("§aLista de jogadores na whitelist: §a" + Joiner.on(", ").join(BungeeMain.getInstance().getWhiteList()));
         }
      }
   }

   @CommandFramework.Completer(
      name = "report",
      aliases = {"rp"}
   )
   public List<String> teleportCompleter(CommandArgs cmdArgs) {
      return (List<String>)(cmdArgs.getArgs().length == 1
         ? ProxyServer.getInstance()
            .getPlayers()
            .stream()
            .filter(player -> player.getName().toLowerCase().startsWith(cmdArgs.getArgs()[0].toLowerCase()))
            .<String>map(net.md_5.bungee.api.CommandSender::getName)
            .collect(Collectors.toList())
         : new ArrayList<>());
   }

   @CommandFramework.Completer(
      name = "server",
      aliases = {"connect"}
   )
   public List<String> serverCompleter(CommandArgs cmdArgs) {
      return (List<String>)(cmdArgs.getArgs().length == 1
         ? BungeeMain.getInstance()
            .getServerManager()
            .getActiveServers()
            .values()
            .stream()
            .filter(server -> server.getServerId().toLowerCase().startsWith(cmdArgs.getArgs()[0].toLowerCase()))
            .map(ProxiedServer::getServerId)
            .collect(Collectors.toList())
         : new ArrayList<>());
   }

   @CommandFramework.Completer(
      name = "play",
      aliases = {"jogar"}
   )
   public List<String> playCompleter(CommandArgs cmdArgs) {
      return (List<String>)(cmdArgs.getArgs().length == 1
         ? Arrays.asList(ServerType.values())
            .stream()
            .filter(type -> type.name().toLowerCase().startsWith(cmdArgs.getArgs()[0].toLowerCase()))
            .map(Enum::name)
            .collect(Collectors.toList())
         : new ArrayList<>());
   }
}

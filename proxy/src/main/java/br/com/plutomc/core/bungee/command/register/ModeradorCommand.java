package br.com.plutomc.core.bungee.command.register;

import br.com.plutomc.core.bungee.BungeeMain;
import br.com.plutomc.core.bungee.account.BungeeAccount;
import br.com.plutomc.core.common.CommonConst;
import br.com.plutomc.core.common.CommonPlugin;
import br.com.plutomc.core.common.command.CommandArgs;
import br.com.plutomc.core.common.command.CommandClass;
import br.com.plutomc.core.common.command.CommandFramework;
import br.com.plutomc.core.common.command.CommandSender;
import br.com.plutomc.core.common.language.Language;
import br.com.plutomc.core.common.account.Account;
import br.com.plutomc.core.common.server.ServerType;
import br.com.plutomc.core.common.server.loadbalancer.server.ProxiedServer;
import br.com.plutomc.core.common.utils.DateUtils;
import br.com.plutomc.core.common.utils.ProtocolVersion;
import br.com.plutomc.core.common.utils.string.MessageBuilder;
import br.com.plutomc.core.common.utils.string.StringFormat;
import com.google.common.base.Joiner;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ClickEvent.Action;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.lang.management.ManagementFactory;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class ModeradorCommand implements CommandClass {
   @CommandFramework.Command(
      name = "fakelist",
      aliases = {"nicklist"},
      permission = "command.fakelist"
   )
   public void fakelistCommand(CommandArgs cmdArgs) {
      List<Account> list = CommonPlugin.getInstance()
         .getAccountManager()
         .getAccounts()
         .stream()
         .filter(member -> member.isUsingFake())
         .collect(Collectors.toList());
      if (list.isEmpty()) {
         cmdArgs.getSender().sendMessage("§cNinguém está usando fake.");
      } else {
         list.forEach(member -> cmdArgs.getSender().sendMessage("§a" + member.getPlayerName() + " está usando o fake " + member.getFakeName() + "."));
      }
   }

   @CommandFramework.Command(
      name = "glist",
      aliases = {"globallist", "serverinfo"},
      permission = "command.glist"
   )
   public void glistChatCommand(CommandArgs cmdArgs) {
      String[] args = cmdArgs.getArgs();
      CommandSender sender = cmdArgs.getSender();
      if (args.length == 0) {
         ProxiedServer[] servers = BungeeMain.getInstance()
            .getServerManager()
            .getServers()
            .stream()
            .sorted((o1, o2) -> o1.getServerId().compareTo(o2.getServerId()))
            .toArray(ProxiedServer[]::new);
         sender.sendMessage(" §aServidor global: ");
         this.handleInfo(
            sender,
            ProxyServer.getInstance().getPlayers(),
            (int)((System.currentTimeMillis() - ManagementFactory.getRuntimeMXBean().getStartTime()) / 1000L),
            BungeeMain.getInstance().getPlayersRecord(),
            ServerType.BUNGEECORD
         );
         MessageBuilder messageBuilder = new MessageBuilder("    §aServidores: §7");

         for(int i = 0; i < Math.max(servers.length, 24); ++i) {
            ProxiedServer server = servers[i];
            messageBuilder.extra(
               new MessageBuilder("§7" + server.getServerId() + (i == servers.length - 1 ? "." : ", "))
                  .setHoverEvent(
                     "§a"
                        + server.getServerId()
                        + "\n\n  §aPlayers: §7"
                        + server.getOnlinePlayers()
                        + " jogadores\n  §aMáximo de players: §7"
                        + server.getMaxPlayers()
                        + " jogadores\n  §aRecord de players: §7"
                        + server.getPlayersRecord()
                        + " jogadores\n  §aTipo de servidor: §7"
                        + server.getServerType().getName()
                        + "\n  §aLigado há: §7"
                        + StringFormat.formatTime((int)((System.currentTimeMillis() - server.getStartTime()) / 1000L), StringFormat.TimeFormat.NORMAL)
                        + "\n\n§eClique para saber mais."
                  )
                  .setClickEvent("/glist " + server.getServerId())
                  .create()
            );
         }

         sender.sendMessage(messageBuilder.create());
      } else {
         ProxiedServer proxiedServer = BungeeMain.getInstance().getServerManager().getServerByName(args[0]);
         if (proxiedServer != null && proxiedServer.getServerInfo() != null) {
            sender.sendMessage(
               new MessageBuilder(" §aServidor " + proxiedServer.getServerId() + ":")
                  .setHoverEvent("§eClique para se conectar.")
                  .setClickEvent(Action.SUGGEST_COMMAND, "/connect " + proxiedServer.getServerId())
                  .create()
            );
            MessageBuilder messageBuilder = new MessageBuilder("    §a" + proxiedServer.getServerInfo().getPlayers().size() + " players: §7");
            int max = proxiedServer.getServerInfo().getPlayers().size() * 2;
            int i = max - 1;

            for(ProxiedPlayer player : proxiedServer.getServerInfo().getPlayers()) {
               if (i < max - 1) {
                  messageBuilder.extra(new TextComponent("§a, "));
                  --i;
               }

               messageBuilder.extra(
                  new MessageBuilder("§7" + player.getName())
                     .setClickEvent(Action.RUN_COMMAND, "/tp " + player.getName())
                     .setHoverEvent(net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT, "§eClique para teletransportar")
                     .create()
               );
               --i;
            }

            this.handleInfo(
               sender,
               proxiedServer.getServerInfo().getPlayers(),
               (int)((System.currentTimeMillis() - proxiedServer.getStartTime()) / 1000L),
               proxiedServer.getPlayersRecord(),
               proxiedServer.getServerType()
            );
         } else {
            sender.sendMessage(sender.getLanguage().t("server-not-found", "%server%", args[0]));
         }
      }
   }

   @CommandFramework.Command(
      name = "messagebroadcast.remove",
      permission = "staff.super"
   )
   public void messagebroadcastremove(CommandArgs cmdArgs) {
      CommandSender sender = cmdArgs.getSender();
      String[] args = cmdArgs.getArgs();
      if (args.length == 0) {
         sender.sendMessage("§eUse §b/" + cmdArgs.getLabel() + " remove <index> §apara remover.");
      } else {
         OptionalInt integer = StringFormat.parseInt(args[0]);
         if (integer.isPresent()) {
            if (integer.getAsInt() >= 0 && integer.getAsInt() < BungeeMain.getInstance().getMessages().size()) {
               BungeeMain.getInstance().removeMessage(integer.getAsInt());
               sender.sendMessage("§aRemovido com sucesso.");
            } else {
               sender.sendMessage("§cNão existe.");
            }
         } else {
            sender.sendMessage(sender.getLanguage().t("invalid-format-integer", "%value%", args[0]));
         }
      }
   }

   @CommandFramework.Command(
      name = "messagebroadcast",
      permission = "staff.super"
   )
   public void messagebroadcast(CommandArgs cmdArgs) {
      CommandSender sender = cmdArgs.getSender();
      String[] args = cmdArgs.getArgs();
      if (args.length != 0) {
         String message = Joiner.on(' ').join(args);
         BungeeMain.getInstance().addMessage(message.replace('&', '§'));
         sender.sendMessage("§a" + message.replace('&', '§') + " §aadicionada com sucesso.");
      } else {
         sender.sendMessage("§eUse §b/" + cmdArgs.getLabel() + " <message>§e ");

         for(int i = 1; i <= BungeeMain.getInstance().getMessages().size(); ++i) {
            sender.sendMessage("  §a" + i + "° §a" + (String)BungeeMain.getInstance().getMessages().get(i - 1));
         }
      }
   }

   @CommandFramework.Command(
      name = "mojang",
      permission = "command.mojang"
   )
   public void mojangCommand(CommandArgs cmdArgs) {
      CommandSender sender = cmdArgs.getSender();
      int pirateCount = 0;
      int premiumCount = 0;
      int onlineCount = BungeeCord.getInstance().getPlayers().size();

      for(ProxiedPlayer player : BungeeCord.getInstance().getPlayers()) {
         if (player.getPendingConnection().isOnlineMode()) {
            ++premiumCount;
         } else {
            ++pirateCount;
         }
      }

      sender.sendMessage("  §aEstatísticas dos jogadores:");
      sender.sendMessage("    §aOriginais: §7" + premiumCount + " jogadores.");
      sender.sendMessage("    §aPiratas: §7" + pirateCount + " jogadores.");
      sender.sendMessage("    §aTotal online: §7" + onlineCount + " jogadores.");
   }

   @CommandFramework.Command(
      name = "staffchat",
      aliases = {"sc"},
      permission = "command.staff",
      console = false
   )
   public void staffChatCommand(CommandArgs cmdArgs) {
      Account account = cmdArgs.getSenderAsMember();
      account.getAccountConfiguration().setStaffChat(!account.getAccountConfiguration().isStaffChat());
      account.sendMessage("§%command.staffchat." + (account.getAccountConfiguration().isStaffChat() ? "enabled" : "disabled") + "%§");
   }

   @CommandFramework.Command(
      name = "stafflist",
      runAsync = true,
      permission = "command.staff",
      usage = "/<command> <player> <server>"
   )
   public void stafflistCommand(CommandArgs cmdArgs) {
      CommandSender sender = cmdArgs.getSender();
      int groupId = sender.isPlayer() ? cmdArgs.getSenderAsMember().getServerGroup().getId() : Integer.MAX_VALUE;
      int ping = 0;
      long time = 0L;
      Map<ProtocolVersion, Integer> map = new HashMap<>();
      BungeeAccount[] array = CommonPlugin.getInstance()
         .getAccountManager()
         .getAccounts(BungeeAccount.class)
         .stream()
         .sorted((o1, o2) -> (o1.getServerGroup().getId() - o2.getServerGroup().getId()) * -1)
         .filter(memberx -> memberx.getServerGroup().isStaff() && groupId >= memberx.getServerGroup().getId())
         .toArray(x$0 -> new BungeeAccount[x$0]);

      for(BungeeAccount member : array) {
         ping += member.getProxiedPlayer().getPing();
         time += member.getSessionTime();
         ProtocolVersion version = ProtocolVersion.getById(member.getProxiedPlayer().getPendingConnection().getVersion());
         map.putIfAbsent(version, 0);
         map.put(version, map.get(version) + 1);
      }

      ping /= Math.max(array.length, 1);
      time /= (long)Math.max(array.length, 1);
      sender.sendMessage("  §aEquipe online:");
      sender.sendMessage("    §aTempo médio: §7" + StringFormat.formatTime((int)(time / 1000L), StringFormat.TimeFormat.NORMAL));
      sender.sendMessage("    §aPing médio: §7" + ping + "ms");
      sender.sendMessage("    §aEquipe: §7" + array.length + " online");
      MessageBuilder messageBuilder = new MessageBuilder("    §aPlayers: §7");

      for(int i = 0; i < array.length; ++i) {
         BungeeAccount member = array[i];
         messageBuilder.extra(
            new MessageBuilder("§7" + member.getDefaultTag().getRealPrefix() + member.getPlayerName() + (i == array.length - 1 ? "." : ", "))
               .setHoverEvent(
                  "§aTempo online: §7"
                     + StringFormat.formatTime((int)(member.getSessionTime() / 1000L), StringFormat.TimeFormat.NORMAL)
                     + "\n§aPing: §7"
                     + member.getProxiedPlayer().getPing()
                     + "ms\n§aServidor: §7"
                     + member.getActualServerId()
                     + ""
               )
               .setClickEvent(Action.SUGGEST_COMMAND, "/teleport " + member.getProxiedPlayer().getName())
               .create()
         );
      }

      sender.sendMessage(messageBuilder.create());
   }

   @CommandFramework.Command(
      name = "broadcast",
      aliases = {"bc"},
      permission = "command.broadcast"
   )
   public void broadcastCommand(CommandArgs cmdArgs) {
      CommandSender sender = cmdArgs.getSender();
      String[] args = cmdArgs.getArgs();
      if (args.length == 0) {
         sender.sendMessage("§eUse §b/" + cmdArgs.getLabel() + " <message>§e para enviar uma mensagem no servidor.");
      } else {
         String message = Joiner.on(' ').join(args).replace('&', '§');
         ProxyServer.getInstance().broadcast("");
         ProxyServer.getInstance().broadcast("§b§lPLUTO §6» §f" + message);
         ProxyServer.getInstance().broadcast("");
         this.staffLog("O " + sender.getName() + " mandou uma mensagem global na proxy.");
      }
   }

   @CommandFramework.Command(
      name = "bungeewhitelist",
      aliases = {"globalwhitelist", "gwhitelist", "bwhitelist"},
      permission = "command.globalwhitelist"
   )
   public void whitelistCommand(CommandArgs cmdArgs) {
      CommandSender sender = cmdArgs.getSender();
      String[] args = cmdArgs.getArgs();
      if (args.length == 0) {
         sender.sendMessage("§%command.bungeecord.whitelist.usage%§");
      } else {
         String var4 = args[0].toLowerCase();
         switch(var4) {
            case "off":
            case "on":
               boolean enabled = args[0].equalsIgnoreCase("on");
               long time = -1L;
               if (args.length >= 2) {
                  time = DateUtils.getTime(args[1]);
               }

               BungeeMain.getInstance().setWhitelistEnabled(enabled, time);
               sender.sendMessage(
                  sender.getLanguage()
                     .t(
                        "command.bungeecord.whitelist." + (enabled ? "enabled" + (time == -1L ? "" : "-temporary") + "-success" : "disabled-success"),
                        "%time%",
                        DateUtils.getTime(sender.getLanguage(), time)
                     )
               );
               break;
            case "add":
               if (args.length >= 2) {
                  String playerName = args[1];
                  if (BungeeMain.getInstance().isMemberInWhiteList(playerName)) {
                     sender.sendMessage("já está");
                     return;
                  }

                  BungeeMain.getInstance().addMemberToWhiteList(playerName);
                  sender.sendMessage("adicionado");
               }
               break;
            case "remove":
               if (args.length >= 2) {
                  String playerName = args[1];
                  if (!BungeeMain.getInstance().isMemberInWhiteList(playerName)) {
                     sender.sendMessage("não está");
                     return;
                  }

                  BungeeMain.getInstance().addMemberToWhiteList(playerName);
                  sender.sendMessage("removido");
               }
               break;
            default:
               sender.sendMessage("§%command.bungeecord.whitelist.usage%§");
         }
      }
   }

   @CommandFramework.Command(
      name = "bungee",
      permission = "command.bungee"
   )
   public void bungeeCommand(CommandArgs cmdArgs) {
      CommandSender sender = cmdArgs.getSender();
      Language language = sender.getLanguage();
      String[] args = cmdArgs.getArgs();
      if (args.length == 0) {
         sender.sendMessage("§%command-server-usage%§");
      } else {
         String var5 = args[0].toLowerCase();
         switch(var5) {
            case "reload-config":
               try {
                  CommonPlugin.getInstance().loadConfig();
                  sender.sendMessage(language.t("command-server-reload-config-successfully"));
               } catch (Exception var9) {
                  sender.sendMessage(language.t("command-server-reload-config-error"));
                  sender.sendMessage("§c" + var9.getLocalizedMessage());
                  var9.printStackTrace();
               }
               break;
            case "save-config":
               try {
                  CommonPlugin.getInstance().saveConfig();
                  sender.sendMessage(language.t("command-server-save-config-successfully"));
               } catch (Exception var8) {
                  sender.sendMessage(language.t("command-server-save-config-error"));
                  sender.sendMessage("§c" + var8.getLocalizedMessage());
                  var8.printStackTrace();
               }
               break;
            default:
               sender.sendMessage("§%command-server-usage%§");
         }
      }
   }

   @CommandFramework.Command(
     name = "bungeever",
     permission = "*",
     aliases = {"bungeeversion"}
   )
   public void versionCommand(CommandArgs args) {
      args.getSender().sendMessage("§aO core-proxy está na versão " + ProxyServer.getInstance().getPluginManager().getPlugin("proxy").getDescription().getVersion());
   }

   @CommandFramework.Command(
      name = "find",
      permission = "command.find"
   )
   public void findCommand(CommandArgs cmdArgs) {
      CommandSender sender = cmdArgs.getSender();
      String[] args = cmdArgs.getArgs();
      if (args.length == 0) {
         sender.sendMessage("§eUse §b/" + cmdArgs.getLabel() + " <player>§e para saber aonde um jogador está.");
      } else {
         String playerName = args[0];
         ProxiedPlayer target = ProxyServer.getInstance().getPlayer(playerName);
         if (target == null) {
            sender.sendMessage(sender.getLanguage().t("player-not-found", "%player%", playerName));
         } else {
            sender.sendMessage(
               new MessageBuilder("§aO jogador " + target.getName() + " está na sala " + target.getServer().getInfo().getName() + ".")
                  .setHoverEvent("§aClique para se conectar.")
                  .setClickEvent("/tp " + target.getName())
                  .create()
            );
         }
      }
   }

   @CommandFramework.Command(
      name = "go",
      permission = "command.go",
      console = false
   )
   public void goCommand(CommandArgs cmdArgs) {
      CommandSender sender = cmdArgs.getSender();
      String[] args = cmdArgs.getArgs();
      if (args.length == 0) {
         sender.sendMessage("§eUse §b/" + cmdArgs.getLabel() + " <player>§e para ir até um jogador.");
      } else {
         String playerName = args[0];
         ProxiedPlayer target = ProxyServer.getInstance().getPlayer(playerName);
         if (target == null) {
            sender.sendMessage(sender.getLanguage().t("player-not-found", "%player%", playerName));
         } else {
            cmdArgs.getSenderAsMember(BungeeAccount.class).getProxiedPlayer().connect(target.getServer().getInfo());
         }
      }
   }

   @CommandFramework.Command(
      name = "top",
      permission = "command.top"
   )
   public void topCommand(CommandArgs cmdArgs) {
      CommandSender sender = cmdArgs.getSender();
      if (cmdArgs.getArgs().length > 0 && cmdArgs.getArgs()[0].equalsIgnoreCase("gc")) {
         Runtime.getRuntime().gc();
         sender.sendMessage("§aVocê passou o Garbage Collector do java no BungeeCord.");
      } else {
         long usedMemory = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 2L / 1048576L;
         long allocatedMemory = Runtime.getRuntime().totalMemory() / 1048576L;
         sender.sendMessage(" ");
         sender.sendMessage("  §aBungeeCord Usage Info:");
         sender.sendMessage("    §aMemória usada: §7" + usedMemory + "MB (" + usedMemory * 100L / allocatedMemory + "%)");
         sender.sendMessage(
            "    §aMemória livre: §7" + (allocatedMemory - usedMemory) + "MB (" + (allocatedMemory - usedMemory) * 100L / allocatedMemory + "%)"
         );
         sender.sendMessage("    §aMemória máxima: §7" + allocatedMemory + "MB");
         sender.sendMessage("    §aCPU: §7" + CommonConst.DECIMAL_FORMAT.format(CommonConst.getCpuUse()) + "%");
         sender.sendMessage("    §aPing médio: §7" + BungeeMain.getInstance().getAveragePing(ProxyServer.getInstance().getPlayers()) + "ms.");
      }
   }

   @CommandFramework.Command(
      name = "send",
      permission = "command.send"
   )
   public void sendCommand(CommandArgs cmdArgs) {
      CommandSender sender = cmdArgs.getSender();
      String[] args = cmdArgs.getArgs();
      if (args.length <= 1) {
         sender.sendMessage("§eUse §b/" + cmdArgs.getLabel() + " <player:current:all> <serverId:serverType>§e para enviar um jogador para uma sala.");
      } else {
         List<ProxiedPlayer> playerList = new ArrayList();
         if (args[0].equalsIgnoreCase("all")) {
            playerList.addAll(ProxyServer.getInstance().getPlayers());
         } else if (args[0].equalsIgnoreCase("current")) {
            if (!cmdArgs.isPlayer()) {
               sender.sendMessage("§%command-only-for-players%§");
               return;
            }

            playerList.addAll(cmdArgs.getSenderAsMember(BungeeAccount.class).getProxiedPlayer().getServer().getInfo().getPlayers());
         } else if (args[0].contains(",")) {
            String[] split = args[0].split(",");

            for(String playerName : split) {
               ProxiedPlayer player = ProxyServer.getInstance().getPlayer(playerName);
               if (player == null) {
                  sender.sendMessage(sender.getLanguage().t("player-not-found", "%player%", playerName));
                  return;
               }

               playerList.add(player);
            }
         } else {
            ProxiedPlayer player = ProxyServer.getInstance().getPlayer(args[0]);
            if (player == null) {
               sender.sendMessage(sender.getLanguage().t("player-not-found", "%player%", args[0]));
               return;
            }

            playerList.add(player);
         }

         ServerType serverType = null;

         try {
            serverType = ServerType.valueOf(args[1].toUpperCase());
         } catch (Exception var11) {
            serverType = null;
         }

         if (serverType == null) {
            ProxiedServer proxiedServer = BungeeMain.getInstance().getServerManager().getServerByName(args[1]);
            if (proxiedServer == null || proxiedServer.getServerInfo() == null) {
               sender.sendMessage(sender.getLanguage().t("server-not-found", "%server%", args[1]));
               return;
            }

            playerList.forEach(playerx -> playerx.connect(proxiedServer.getServerInfo()));
            if (args[0].equalsIgnoreCase("current")) {
               sender.sendMessage("§aTodos os jogadores da sala foram enviados para o servidor " + proxiedServer.getServerId() + ".");
            } else if (args[0].equalsIgnoreCase("all")) {
               sender.sendMessage("§aTodos os jogadores foram enviados para o servidor " + proxiedServer.getServerId() + ".");
            } else {
               sender.sendMessage(
                  "§aOs jogadores "
                     + (String)playerList.stream().map(net.md_5.bungee.api.CommandSender::getName).collect(Collectors.joining(", "))
                     + " foram enviados para o servidor "
                     + proxiedServer.getServerId()
                     + "."
               );
            }
         } else {
            List<ProxiedServer> servers = BungeeMain.getInstance().getServerManager().getBalancer(serverType).getList();
            if (servers.isEmpty()) {
               sender.sendMessage(sender.getLanguage().t("server-not-found", "%server%", serverType.name()));
               return;
            }

            int index = 0;

            for(ProxiedPlayer player : playerList) {
               player.connect(servers.get(index).getServerInfo());
               if (++index >= servers.size()) {
                  index = 0;
               }
            }

            if (args[0].equalsIgnoreCase("current")) {
               sender.sendMessage("§aTodos os jogadores da sala foram enviados para o servidor " + serverType.name() + ".");
            } else if (args[0].equalsIgnoreCase("all")) {
               sender.sendMessage("§aTodos os jogadores foram enviados para o servidor " + serverType.name() + ".");
            } else {
               sender.sendMessage(
                  "§aOs jogadores "
                     + (String)playerList.stream().map(net.md_5.bungee.api.CommandSender::getName).collect(Collectors.joining(", "))
                     + " foram enviados para o servidor "
                     + serverType.name()
                     + "."
               );
            }
         }
      }
   }

   private void handleInfo(CommandSender sender, Collection<ProxiedPlayer> players, int onlineTime, int playersRecord, ServerType serverType) {
      sender.sendMessage("    §aPlayers: §7" + players.size() + " jogadores");
      sender.sendMessage("    §aRecord de players: §7" + playersRecord);
      int ping = 0;
      Map<ProtocolVersion, Integer> map = new HashMap<>();

      for(ProxiedPlayer player : players) {
         ping += player.getPing();
         ProtocolVersion version = ProtocolVersion.getById(player.getPendingConnection().getVersion());
         map.putIfAbsent(version, 0);
         map.put(version, map.get(version) + 1);
      }

      ping /= Math.max(players.size(), 1);
      sender.sendMessage("    §aPing médio: §7" + ping + "ms");
      if (!players.isEmpty()) {
         sender.sendMessage("    §aVersão: §7");

         for(Entry<ProtocolVersion, Integer> entry : map.entrySet()) {
            sender.sendMessage("      §a- " + entry.getKey().name().replace("MINECRAFT_", "").replace("_", ".") + ": §7" + entry.getValue() + " jogadores");
         }
      }

      if (serverType == ServerType.BUNGEECORD) {
         sender.sendMessage("    §aTipo de servidor: §7" + serverType.getName());
      }

      sender.sendMessage("    §aLigado há: §7" + StringFormat.formatTime(onlineTime, StringFormat.TimeFormat.NORMAL));
   }

   @CommandFramework.Completer(
      name = "teleport",
      aliases = {"tp"}
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
      name = "send"
   )
   public List<String> serverCompleter(CommandArgs cmdArgs) {
      List<String> stringList = new ArrayList<>();
      if (cmdArgs.getArgs().length == 1) {
         for(String possibilities : Arrays.asList("all", "current")) {
            if (possibilities.startsWith(cmdArgs.getArgs()[0].toLowerCase())) {
               stringList.add(possibilities);
            }
         }

         stringList.addAll(
            ProxyServer.getInstance()
               .getPlayers()
               .stream()
               .filter(player -> player.getName().toLowerCase().startsWith(cmdArgs.getArgs()[0].toLowerCase()))
               .map(net.md_5.bungee.api.CommandSender::getName)
               .collect(Collectors.toList())
         );
      } else if (cmdArgs.getArgs().length == 2) {
         stringList.addAll(
            BungeeMain.getInstance()
               .getServerManager()
               .getActiveServers()
               .values()
               .stream()
               .filter(server -> server.getServerId().toLowerCase().startsWith(cmdArgs.getArgs()[1].toLowerCase()))
               .map(ProxiedServer::getServerId)
               .collect(Collectors.toList())
         );
         stringList.addAll(
            Arrays.asList(ServerType.values())
               .stream()
               .filter(server -> server.name().toLowerCase().startsWith(cmdArgs.getArgs()[1].toLowerCase()))
               .map(Enum::name)
               .collect(Collectors.toList())
         );
      }

      return stringList;
   }

   @CommandFramework.Completer(
      name = "find"
   )
   public List<String> findCompleter(CommandArgs cmdArgs) {
      return (List<String>)(cmdArgs.getArgs().length == 1
         ? ProxyServer.getInstance()
            .getPlayers()
            .stream()
            .filter(player -> player.getName().toLowerCase().startsWith(cmdArgs.getArgs()[0].toLowerCase()))
            .<String>map(net.md_5.bungee.api.CommandSender::getName)
            .collect(Collectors.toList())
         : new ArrayList<>());
   }
}

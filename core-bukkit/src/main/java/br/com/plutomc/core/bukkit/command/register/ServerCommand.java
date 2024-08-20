package br.com.plutomc.core.bukkit.command.register;

import br.com.plutomc.core.bukkit.BukkitCommon;
import br.com.plutomc.core.bukkit.menu.profile.StatisticsInventory;
import br.com.plutomc.core.bukkit.utils.ProtocolVersion;
import com.google.common.base.Joiner;
import java.lang.management.ManagementFactory;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import br.com.plutomc.core.common.CommonConst;
import br.com.plutomc.core.common.CommonPlugin;
import br.com.plutomc.core.bukkit.member.BukkitMember;
import br.com.plutomc.core.common.command.CommandArgs;
import br.com.plutomc.core.common.command.CommandClass;
import br.com.plutomc.core.common.command.CommandFramework;
import br.com.plutomc.core.common.command.CommandSender;
import br.com.plutomc.core.common.language.Language;
import br.com.plutomc.core.common.server.ServerType;
import br.com.plutomc.core.common.server.loadbalancer.server.ProxiedServer;
import br.com.plutomc.core.common.utils.DateUtils;
import br.com.plutomc.core.common.utils.configuration.Configuration;
import br.com.plutomc.core.common.utils.string.StringFormat;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_8_R3.MinecraftServer;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class ServerCommand implements CommandClass {
   @CommandFramework.Command(
      name = "servermanager",
      aliases = {"ss", "smanager"},
      permission = "command.server"
   )
   public void servermanagerCommand(CommandArgs cmdArgs) {
      CommandSender sender = cmdArgs.getSender();
      String[] args = cmdArgs.getArgs();
      if (args.length == 0) {
         sender.sendMessage("§%command-server-usage%§");
      } else {
         String var4 = args[0].toLowerCase();
         switch(var4) {
            case "reload-config":
               try {
                  CommonPlugin.getInstance().loadConfig();
                  sender.sendMessage("§%command-server-reload-config-successfully%§");
               } catch (Exception var10) {
                  sender.sendMessage("§%command-server-reload-config-error%§");
                  sender.sendMessage("§c" + var10.getLocalizedMessage());
                  var10.printStackTrace();
               }
               break;
            case "type":
               if (args.length == 1) {
                  sender.sendMessage("§aInsira o tipo do servidor.");
                  return;
               }

               ServerType serverType = null;

               try {
                  serverType = ServerType.valueOf(args[1].toUpperCase());
               } catch (Exception var9) {
                  sender.sendMessage("§cO tipo do servidor não foi encontrado.");
                  return;
               }

               CommonPlugin.getInstance().setServerType(serverType);
               sender.sendMessage("§aO tipo do servidor foi alterado para §a" + serverType.name() + "§a.");
               break;
            case "serverid":
               if (args.length == 1) {
                  sender.sendMessage("§aInsira o nome do servidor.");
                  return;
               }

               String serverId = args[1];
               CommonPlugin.getInstance().setServerId(serverId);
               sender.sendMessage("§aO ID do servidor foi alterado para §a" + serverId + "§a.");
               break;
            case "start":
               CommonPlugin.getInstance().getServerData().startServer(Bukkit.getMaxPlayers());
               CommonPlugin.getInstance().getServerData().updateStatus();
               Bukkit.getOnlinePlayers().forEach(player -> CommonPlugin.getInstance().getServerData().joinPlayer(player.getUniqueId(), Bukkit.getMaxPlayers()));
               sender.sendMessage("§aO servidor foi iniciado com sucesso.");
               break;
            case "stop":
               Bukkit.getOnlinePlayers()
                  .forEach(player -> CommonPlugin.getInstance().getServerData().leavePlayer(player.getUniqueId(), Bukkit.getMaxPlayers()));
               CommonPlugin.getInstance().getServerData().stopServer();
               sender.sendMessage("§aO servidor foi parado com sucesso.");
               break;
            case "save-config":
               try {
                  CommonPlugin.getInstance().saveConfig();
                  sender.sendMessage("§%command-server-save-config-successfully%§");
               } catch (Exception var8) {
                  sender.sendMessage("§%command-server-save-config-error%§");
                  sender.sendMessage("§c" + var8.getLocalizedMessage());
                  var8.printStackTrace();
               }
               break;
            case "debug":
               for(Entry<String, ProxiedServer> entry : BukkitCommon.getInstance().getServerManager().getActiveServers().entrySet()) {
                  sender.sendMessage("  §a" + (String)entry.getKey() + "§7: " + CommonConst.GSON.toJson(entry.getValue()));
               }
               break;
            default:
               sender.sendMessage("§%command-server-usage%§");
         }
      }
   }

   @CommandFramework.Command(
      name = "stats",
      aliases = {"status", "estatisticas"},
      console = false
   )
   public void statsCommand(CommandArgs cmdArgs) {
      new StatisticsInventory(cmdArgs.getSenderAsMember(BukkitMember.class).getPlayer(), null);
   }

   @CommandFramework.Command(
      name = "config",
      permission = "command.config"
   )
   public void configCommand(CommandArgs cmdArgs) {
      CommandSender sender = cmdArgs.getSender();
      String[] args = cmdArgs.getArgs();
      if (args.length == 0) {
         sender.sendMessage("§%command-config-usage%§");
      } else if (args.length == 1 && args[0].equalsIgnoreCase("list")) {
         sender.sendMessage("  §aConfig list:");

         for(String config : CommonPlugin.getInstance().getConfigurationManager().getConfigs()) {
            sender.sendMessage("    §a- §7" + config);
         }
      } else {
         String configName = args[0];
         Configuration configuration = CommonPlugin.getInstance().getConfigurationManager().getConfigByName(configName);
         if (configuration == null) {
            sender.sendMessage(sender.getLanguage().t("command-config-configuration-not-found", "%name%", configName));
         } else {
            String var6 = args[1].toLowerCase();
            switch(var6) {
               case "save":
                  try {
                     configuration.saveConfig();
                     sender.sendMessage(sender.getLanguage().t("command-config-configuration-saved", "%name%", configName));
                  } catch (Exception var10) {
                     sender.sendMessage("§%command-config-could-not-save%§");
                     var10.printStackTrace();
                  }

                  return;
               case "reload":
               case "load":
                  try {
                     configuration.loadConfig();
                     sender.sendMessage(sender.getLanguage().t("command-config-configuration-loaded", "%name%", configName));
                  } catch (Exception var9) {
                     sender.sendMessage("§%command-config-could-not-load%§");
                     var9.printStackTrace();
                  }

                  return;
               default:
                  sender.sendMessage("§%command-config-usage%§");
            }
         }
      }
   }

   @CommandFramework.Command(
      name = "gamemode",
      aliases = {"gm"},
      permission = "command.gamemode"
   )
   public void gamemodeCommand(CommandArgs cmdArgs) {
      CommandSender sender = cmdArgs.getSender();
      Language language = sender.getLanguage();
      String[] args = cmdArgs.getArgs();
      if (args.length == 0) {
         sender.sendMessage(language.t("command-gamemode-usage", "%label%", cmdArgs.getLabel()));
      } else {
         GameMode gameMode = null;
         OptionalInt optionalInt = StringFormat.parseInt(args[0]);
         if (optionalInt.isPresent()) {
            gameMode = GameMode.getByValue(optionalInt.getAsInt());
         } else {
            try {
               gameMode = GameMode.valueOf(args[0].toUpperCase());
            } catch (Exception var8) {
            }
         }

         if (gameMode == null) {
            sender.sendMessage(language.t("command-gamemode-not-found", "%gamemode%", args[0]));
         } else {
            Player target = args.length == 1 && sender.isPlayer() ? cmdArgs.getSenderAsMember(BukkitMember.class).getPlayer() : Bukkit.getPlayer(args[1]);
            if (target == null) {
               sender.sendMessage(language.t("player-not-found", "%player%", args[1]));
            } else {
               target.setGameMode(gameMode);
               if (target.getUniqueId().equals(sender.getUniqueId())) {
                  sender.sendMessage(language.t("command-gamemode-your-gamemode-changed", "%gamemode%", StringFormat.formatString(gameMode.name())));
               } else {
                  sender.sendMessage(
                     language.t(
                        "command-gamemode-target-gamemode-changed", "%gamemode%", StringFormat.formatString(gameMode.name()), "%target%", target.getName()
                     )
                  );
               }
            }
         }
      }
   }

   @CommandFramework.Command(
      name = "clear",
      permission = "command.clear"
   )
   public void clearCommand(CommandArgs cmdArgs) {
      CommandSender sender = cmdArgs.getSender();
      String[] args = cmdArgs.getArgs();
      if (args.length == 0) {
         if (cmdArgs.isPlayer()) {
            Player player = cmdArgs.getSenderAsMember(BukkitMember.class).getPlayer();
            player.getInventory().clear();
            player.getInventory().setArmorContents(new ItemStack[4]);
            player.getActivePotionEffects().clear();
            sender.sendMessage("§%command-clear-cleared-inventory%§");
         } else {
            sender.sendMessage("§%command-only-for-player%§");
         }
      } else {
         Player player = Bukkit.getPlayer(args[0]);
         if (player == null) {
            sender.sendMessage(sender.getLanguage().t("player-not-found", "%player%", args[0]));
            return;
         }

         player.getInventory().clear();
         player.getInventory().setArmorContents(new ItemStack[4]);
         player.getActivePotionEffects().clear();
         sender.sendMessage(sender.getLanguage().t("command-clear-cleared-player-inventory", "%player%", player.getName()));
      }
   }

   @CommandFramework.Command(
      name = "tpworld",
      aliases = {"tpw"},
      permission = "command.teleport"
   )
   public void teleportworldCommand(CommandArgs cmdArgs) {
      Player player = cmdArgs.getSenderAsMember(BukkitMember.class).getPlayer();
      String[] args = cmdArgs.getArgs();
      World world = Bukkit.getWorld(args[0]);
      if (world == null) {
         world = WorldCreator.name(args[0]).createWorld();
      }

      player.teleport(new Location(Bukkit.getWorld(args[0]), 0.0, 0.0, 0.0));
   }

   @CommandFramework.Command(
      name = "teleport",
      aliases = {"tp"},
      permission = "command.teleport"
   )
   public void teleportCommand(CommandArgs cmdArgs) {
      CommandSender sender = cmdArgs.getSender();
      String[] args = cmdArgs.getArgs();
      switch(args.length) {
         case 1:
            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
               sender.sendMessage(sender.getLanguage().t("player-not-found", "%player%", args[0]));
               return;
            }

            if (sender.isPlayer()) {
               cmdArgs.getSenderAsMember(BukkitMember.class).getPlayer().teleport(target);
               sender.sendMessage(sender.getLanguage().t("command-teleport-teleported-to-target", "%target%", target.getName()));
            } else {
               sender.sendMessage("§%command-only-for-console%§");
            }
            break;
         case 2:
            if (args[0].equalsIgnoreCase("location")) {
               if (cmdArgs.isPlayer()) {
                  Player player = cmdArgs.getSenderAsMember(BukkitMember.class).getPlayer();
                  String locationName = args[1];
                  if (BukkitCommon.getInstance().getLocationManager().hasLocation(locationName)) {
                     Location location = BukkitCommon.getInstance().getLocationManager().getLocation(locationName);
                     player.teleport(location);
                     player.sendMessage(sender.getLanguage().t("command-teleport-teleported-to-locationname", "%location%", locationName));
                  } else {
                     sender.sendMessage(sender.getLanguage().t("command-teleport-location-not-found", "%location%", locationName));
                  }
               } else {
                  sender.sendMessage("§%command-only-for-console%§");
               }

               return;
            }

            Player player = Bukkit.getPlayer(args[0]);
            if (player == null) {
               sender.sendMessage(sender.getLanguage().t("player-not-found", "%player%", args[0]));
               return;
            }

            target = Bukkit.getPlayer(args[1]);
            if (target == null) {
               sender.sendMessage(sender.getLanguage().t("player-not-found", "%player%", args[1]));
               return;
            }

            player.teleport(target);
            sender.sendMessage(
               sender.getLanguage().t("command-teleport-teleported-player-to-target", "%player%", player.getName(), "%target%", target.getName())
            );
            break;
         case 3:
            if (sender.isPlayer()) {
               player = cmdArgs.getSenderAsMember(BukkitMember.class).getPlayer();
               OptionalDouble optionalX = StringFormat.parseDouble(args[0]);
               OptionalDouble optionalY = StringFormat.parseDouble(args[1]);
               OptionalDouble optionalZ = StringFormat.parseDouble(args[2]);
               double x;
               if (args[0].equals("~")) {
                  x = player.getLocation().getX();
               } else {
                  if (!optionalX.isPresent()) {
                     sender.sendMessage(sender.getLanguage().t("invalid-format-double", "%value%", args[0]));
                     return;
                  }

                  x = optionalX.getAsDouble();
               }

               double y;
               if (args[1].equals("~")) {
                  y = player.getLocation().getY();
               } else {
                  if (!optionalY.isPresent()) {
                     sender.sendMessage(sender.getLanguage().t("invalid-format-double", "%value%", args[1]));
                     return;
                  }

                  y = optionalY.getAsDouble();
               }

               double z;
               if (args[2].equals("~")) {
                  z = player.getLocation().getZ();
               } else {
                  if (!optionalZ.isPresent()) {
                     sender.sendMessage(sender.getLanguage().t("invalid-format-double", "%value%", args[2]));
                     return;
                  }

                  z = optionalZ.getAsDouble();
               }

               Location location = new Location(player.getWorld(), x, y, z);
               if (!location.getChunk().isLoaded()) {
                  location.getChunk().load();
               }

               NumberFormat numberFormat = new DecimalFormat("#.##");
               player.setFallDistance(-1.0F);
               player.teleport(location);
               sender.sendMessage(
                  sender.getLanguage()
                     .t("command-teleport-teleported-to-location", "%x%", numberFormat.format(x), "%y%", numberFormat.format(y), "%z%", numberFormat.format(z))
               );
            } else {
               sender.sendMessage("§%command-only-for-console%§");
            }
            break;
         default:
            sender.sendMessage(sender.getLanguage().t("command-teleport-usage", "%label%", cmdArgs.getLabel()));
      }
   }

   @CommandFramework.Command(
      name = "setlocation",
      permission = "command.location",
      console = false
   )
   public void setlocationCommand(CommandArgs cmdArgs) {
      String[] args = cmdArgs.getArgs();
      BukkitMember member = (BukkitMember)cmdArgs.getSender();
      if (args.length == 0) {
         member.sendMessage("§eUse /" + cmdArgs.getLabel() + " <locationName> para salvar a localização.");
      } else {
         String string = Joiner.on(' ').join(Arrays.copyOfRange(args, 0, args.length)).toLowerCase();
         member.sendMessage("§aLocalização " + args[0] + " com sucesso!");
         BukkitCommon.getInstance().getLocationManager().saveAndLoadLocation(string.replace(' ', '_'), member.getPlayer().getLocation());
      }
   }

   @CommandFramework.Command(
      name = "stop",
      aliases = {"fechar"},
      permission = "command.stop"
   )
   public void stopCommand(CommandArgs cmdArgs) {
      CommandSender sender = cmdArgs.getSender();
      String[] args = cmdArgs.getArgs();
      int time = 0;
      if (args.length >= 1) {
         OptionalInt optional = StringFormat.parseInt(args[0]);
         if (!optional.isPresent()) {
            sender.sendMessage(sender.getLanguage().t("invalid-format-integer", "%value%", args[0]));
            return;
         }

         time = optional.getAsInt();
      }

      final String reason = args.length >= 2 ? Joiner.on(' ').join(Arrays.copyOfRange(args, 2, args.length)) : "Sem motivo";
      final int t = time;
      (new BukkitRunnable() {
            int totalTime = t;
   
            @Override
            public void run() {
               if (this.totalTime <= -2) {
                  Bukkit.getOnlinePlayers().forEach(player -> player.kickPlayer("§cServer closed."));
                  Bukkit.shutdown();
               } else {
                  if (this.totalTime <= 0) {
                     if (this.totalTime == 0) {
                        Bukkit.getOnlinePlayers()
                           .forEach(
                              player -> player.sendMessage(
                                    reason.equals("Sem motivo")
                                       ? Language.getLanguage(player.getUniqueId()).t("server-was-closed")
                                       : Language.getLanguage(player.getUniqueId()).t("server-was-closed-reason", "%reason%", reason)
                                 )
                           );
                     }
   
                     if (Bukkit.getOnlinePlayers().isEmpty()) {
                        Bukkit.shutdown();
                     } else {
                        Bukkit.getOnlinePlayers()
                           .forEach(
                              player -> BukkitCommon.getInstance()
                                    .sendPlayerToServer(player, true, CommonPlugin.getInstance().getServerType().getServerLobby(), ServerType.LOBBY)
                           );
                     }
                  }
   
                  --this.totalTime;
               }
            }
         })
         .runTaskTimer(BukkitCommon.getInstance(), 20L, 20L);
   }

   @CommandFramework.Command(
      name = "tps",
      aliases = {"ticks"},
      permission = "command.tps"
   )
   public void tpsCommand(CommandArgs cmdArgs) {
      CommandSender sender = cmdArgs.getSender();
      if (cmdArgs.getArgs().length != 0) {
         if (cmdArgs.getArgs()[0].equalsIgnoreCase("gc")) {
            Runtime.getRuntime().gc();
            sender.sendMessage("§aVocê passou o GarbargeCollector no servidor.");
         } else {
            World world = Bukkit.getWorld(cmdArgs.getArgs()[0]);
            if (world == null) {
               sender.sendMessage("§cO mundo " + cmdArgs.getArgs()[0] + " não existe.");
            } else {
               sender.sendMessage(" §aMundo " + world.getName());
               sender.sendMessage("    §aEntidades: §7" + world.getEntities().size());
               sender.sendMessage("    §aLoaded chunks: §7" + world.getLoadedChunks().length);
            }
         }
      } else {
         long usedMemory = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 2L / 1048576L;
         long allocatedMemory = Runtime.getRuntime().totalMemory() / 1048576L;
         sender.sendMessage(" §aServidor " + CommonPlugin.getInstance().getServerId() + ":");
         sender.sendMessage("    §aPlayers: §7" + Bukkit.getOnlinePlayers().size() + " jogadores");
         sender.sendMessage("    §aMáximo de players: §7" + Bukkit.getMaxPlayers() + " jogadores");
         sender.sendMessage("    §aMemória: §7" + usedMemory + "/" + allocatedMemory + " MB");
         sender.sendMessage(
            "    §aLigado há: §7"
               + DateUtils.formatDifference(sender.getLanguage(), (System.currentTimeMillis() - ManagementFactory.getRuntimeMXBean().getStartTime()) / 1000L)
         );
         sender.sendMessage("    §aTPS: ");
         sender.sendMessage("      §a1m: §7" + this.format(MinecraftServer.getServer().recentTps[0]));
         sender.sendMessage("      §a5m: §7" + this.format(MinecraftServer.getServer().recentTps[1]));
         sender.sendMessage("      §a15m: §7" + this.format(MinecraftServer.getServer().recentTps[2]));
         int ping = 0;
         Map<ProtocolVersion, Integer> map = new HashMap<>();

         for(Player player : Bukkit.getOnlinePlayers()) {
            ping += ProtocolVersion.getPing(player);
            ProtocolVersion version = ProtocolVersion.getProtocolVersion(player);
            map.putIfAbsent(version, 0);
            map.put(version, map.get(version) + 1);
         }

         ping /= Math.max(Bukkit.getOnlinePlayers().size(), 1);
         sender.sendMessage("    §aPing médio: §7" + ping + "ms");
         if (!Bukkit.getOnlinePlayers().isEmpty()) {
            sender.sendMessage("    §aVersão: §7");

            for(Entry<ProtocolVersion, Integer> entry : map.entrySet()) {
               sender.sendMessage("      §a- " + entry.getKey().name().replace("MINECRAFT_", "").replace("_", ".") + ": §7" + entry.getValue() + " jogadores");
            }
         }
      }
   }

   @CommandFramework.Command(
      name = "memoryinfo",
      permission = "command.tps"
   )
   public void memoryinfoCommand(CommandArgs cmdArgs) {
      long usedMemory = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 2L / 1048576L;
      long allocatedMemory = Runtime.getRuntime().totalMemory() / 1048576L;
      cmdArgs.getSender().sendMessage("  §aMemory Info:");
      cmdArgs.getSender().sendMessage("    §aMemória usada: §7" + usedMemory + "MB (" + usedMemory * 100L / allocatedMemory + "%)");
      cmdArgs.getSender()
         .sendMessage("    §aMemória livre: §7" + (allocatedMemory - usedMemory) + "MB (" + (allocatedMemory - usedMemory) * 100L / allocatedMemory + "%)");
      cmdArgs.getSender().sendMessage("    §aMemória máxima: §7" + allocatedMemory + "MB");
      cmdArgs.getSender().sendMessage("    §aCPU: §7" + CommonConst.DECIMAL_FORMAT.format(CommonConst.getCpuUse()) + "%");
   }

   private String format(double tps) {
      return (tps > 18.0 ? ChatColor.GREEN : (tps > 16.0 ? ChatColor.YELLOW : ChatColor.RED))
         + (tps > 20.0 ? "*" : "")
         + Math.min((double)Math.round(tps * 100.0) / 100.0, 20.0);
   }

   @CommandFramework.Completer(
      name = "party"
   )
   public List<String> partyCompleter(CommandArgs cmdArgs) {
      List<String> returnList = new ArrayList<>();
      if (cmdArgs.getArgs().length == 1) {
         List<String> arguments = Arrays.asList("criar", "convidar", "aceitar", "expulsar", "sair", "chat");
         if (cmdArgs.getArgs()[0].isEmpty()) {
            for(String argument : arguments) {
               returnList.add(argument);
            }
         } else {
            for(String argument : arguments) {
               if (argument.toLowerCase().startsWith(cmdArgs.getArgs()[0].toLowerCase())) {
                  returnList.add(argument);
               }
            }

            for(Player player : Bukkit.getOnlinePlayers()) {
               if (player.getName().toLowerCase().startsWith(cmdArgs.getArgs()[0].toLowerCase())) {
                  returnList.add(player.getName());
               }
            }
         }
      }

      return returnList;
   }

   @CommandFramework.Completer(
      name = "servermanager",
      aliases = {"ss", "smanager"}
   )
   public List<String> servermanagerCompleter(CommandArgs cmdArgs) {
      List<String> returnList = new ArrayList<>();
      if (cmdArgs.getArgs().length == 1) {
         List<String> arguments = Arrays.asList("reload-config", "save-config");
         if (cmdArgs.getArgs()[0].isEmpty()) {
            for(String argument : arguments) {
               returnList.add(argument);
            }
         } else {
            for(String argument : arguments) {
               if (argument.toLowerCase().startsWith(cmdArgs.getArgs()[0].toLowerCase())) {
                  returnList.add(argument);
               }
            }
         }
      }

      return returnList;
   }

   @CommandFramework.Completer(
      name = "setlocation"
   )
   public List<String> serverCompleter(CommandArgs cmdArgs) {
      if (cmdArgs.isPlayer() && cmdArgs.getArgs().length == 1) {
         List<String> arg = new ArrayList<>();
         if (cmdArgs.getArgs()[0].isEmpty()) {
            for(String tag : BukkitCommon.getInstance().getLocationManager().getLocations()) {
               arg.add(tag);
            }
         } else {
            for(String tag : BukkitCommon.getInstance().getLocationManager().getLocations()) {
               if (tag.startsWith(cmdArgs.getArgs()[0].toLowerCase())) {
                  arg.add(tag);
               }
            }
         }

         return arg;
      } else {
         return new ArrayList<>();
      }
   }

   @CommandFramework.Completer(
      name = "gamemode",
      aliases = {"gm"}
   )
   public List<String> gamemodeCompleter(CommandArgs cmdArgs) {
      List<String> returnList = new ArrayList<>();
      if (cmdArgs.getArgs().length == 1) {
         returnList.addAll(
            Arrays.asList(GameMode.values())
               .stream()
               .filter(gameMode -> gameMode.name().toLowerCase().startsWith(cmdArgs.getArgs()[0].toLowerCase()))
               .map(Enum::name)
               .collect(Collectors.toList())
         );
      } else if (cmdArgs.getArgs().length == 2) {
         returnList.addAll(
            Bukkit.getOnlinePlayers()
               .stream()
               .filter(player -> player.getName().toLowerCase().startsWith(cmdArgs.getArgs()[1].toLowerCase()))
               .map(OfflinePlayer::getName)
               .collect(Collectors.toList())
         );
      }

      return returnList;
   }
}

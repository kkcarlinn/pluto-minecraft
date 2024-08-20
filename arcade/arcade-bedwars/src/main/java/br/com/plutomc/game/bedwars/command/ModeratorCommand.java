package br.com.plutomc.game.bedwars.command;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.OptionalDouble;
import java.util.OptionalInt;

import br.com.plutomc.game.bedwars.GameMain;
import br.com.plutomc.game.bedwars.generator.Generator;
import br.com.plutomc.game.bedwars.generator.GeneratorType;
import br.com.plutomc.game.bedwars.menu.creator.IslandCreatorInventory;
import br.com.plutomc.core.bukkit.command.BukkitCommandArgs;
import br.com.plutomc.game.bedwars.island.Island;
import br.com.plutomc.game.bedwars.island.IslandColor;
import br.com.plutomc.core.bukkit.member.BukkitMember;
import br.com.plutomc.core.bukkit.utils.Location;
import br.com.plutomc.core.bukkit.utils.item.ActionItemStack;
import br.com.plutomc.core.bukkit.utils.item.ItemBuilder;
import br.com.plutomc.core.common.command.CommandArgs;
import br.com.plutomc.core.common.command.CommandClass;
import br.com.plutomc.core.common.command.CommandFramework;
import br.com.plutomc.core.common.command.CommandSender;
import br.com.plutomc.core.common.utils.string.MessageBuilder;
import br.com.plutomc.core.common.utils.string.StringFormat;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ModeratorCommand implements CommandClass {
   @CommandFramework.Command(
      name = "start",
      permission = "command.start"
   )
   public void startCommand(BukkitCommandArgs cmdArgs) {
      if (GameMain.getInstance().getState().isPregame()) {
         GameMain.getInstance().setTimer(true);
         GameMain.getInstance().startGame();
      } else {
         cmdArgs.getSender().sendMessage("§cA partida já iniciou.");
      }
   }

   @CommandFramework.Command(
      name = "setprotection",
      permission = "command.island"
   )
   public void setprotectionCommand(CommandArgs cmdArgs) {
      CommandSender sender = cmdArgs.getSender();
      String[] args = cmdArgs.getArgs();
      if (args.length == 0) {
         sender.sendMessage("§eUse §b/setprotection <double>§e para mudar a proteção.");
      } else {
         OptionalDouble optionalDouble = StringFormat.parseDouble(args[0]);
         if (!optionalDouble.isPresent()) {
            sender.sendMessage(sender.getLanguage().t("invalid-format-double", "%value%", args[0]));
         } else {
            GameMain.getInstance().setMinimunDistanceToPlaceBlocks(optionalDouble.getAsDouble());
            sender.sendMessage("§aA proteção de blocos das ilhas foi alterado para " + optionalDouble.getAsDouble() + ".");
         }
      }
   }

   @CommandFramework.Command(
      name = "island",
      permission = "command.island"
   )
   public void islandCommand(CommandArgs cmdArgs) {
      CommandSender sender = cmdArgs.getSender();
      Player player = cmdArgs.getSenderAsMember(BukkitMember.class).getPlayer();
      String[] args = cmdArgs.getArgs();
      if (args.length == 0) {
         sender.sendMessage("§%command-island-usage%§");
      } else {
         List<Island> islandList = GameMain.getInstance().getConfiguration().getList("islands", Island.class);
         if (args.length == 1) {
            if (args[0].equalsIgnoreCase("list")) {
               return;
            }

            if (args[0].equalsIgnoreCase("save")) {
               if (sender.isPlayer()) {
                  player.performCommand("config bedwars save");
               } else {
                  Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "config bedwars save");
               }

               return;
            }

            final IslandColor iColor;

            try {
               iColor = IslandColor.valueOf(args[0].toUpperCase());
            } catch (Exception var20) {
               sender.sendMessage(sender.getLanguage().t("command-island-color-not-exist", "%island%", args[0]));
               return;
            }

            Island island = islandList.stream().filter(i -> i.getIslandColor() == iColor).findFirst().orElse(null);
            if (island == null) {
               sender.sendMessage(
                  sender.getLanguage()
                     .t(
                        "command-island-island-not-exist",
                        "%island%",
                        StringFormat.formatString(iColor.name()),
                        "%islandColor%",
                        "§" + iColor.getColor().getChar()
                     )
               );
               return;
            }

            sender.sendMessage("  §fIsland " + iColor.getColor() + "§%" + iColor.name().toLowerCase() + "-name%§");

            for(Field field : Island.class.getDeclaredFields()) {
               try {
                  field.setAccessible(true);
                  sender.sendMessage("    §f" + field.getName() + ": " + field.get(island));
               } catch (IllegalAccessException | IllegalArgumentException var19) {
                  var19.printStackTrace();
               }
            }
         } else {
            IslandColor iColor = null;

            try {
               iColor = IslandColor.valueOf(args[0].toUpperCase());
            } catch (Exception var18) {
               sender.sendMessage(sender.getLanguage().t("command-island-color-not-exist", "%island%", args[0]));
               return;
            }

            IslandColor finalIColor = iColor;
            Island island = islandList.stream().filter(i -> i.getIslandColor() == finalIColor).findFirst().orElse(null);
            if (!args[1].equalsIgnoreCase("create") && island == null) {
               sender.sendMessage("§%command-island-island-not-exist%§");
               return;
            }

            String var9 = args[1].toLowerCase();
            switch(var9) {
               case "create":
                  IslandColor finalIColor1 = iColor;
                  if (islandList.stream().filter(i -> i.getIslandColor() == finalIColor1).findFirst().orElse(null) == null) {
                     Island islandToCreate = new Island(
                        iColor,
                        new Location(),
                        new Location(),
                        new Location(),
                        new Location(),
                        new HashMap<>(),
                        Island.IslandStatus.ALIVE,
                        new ArrayList<>(),
                        new HashMap<>(),
                        null
                     );
                     islandList.add(islandToCreate);
                     sender.sendMessage(
                        sender.getLanguage()
                           .t(
                              "command-island-created-success",
                              "%island%",
                              StringFormat.formatString(islandToCreate.getIslandColor().name()),
                              "%islandColor%",
                              "§" + islandToCreate.getIslandColor().getColor().getChar()
                           )
                     );
                     if (sender.isPlayer()) {
                        player.getInventory().addItem(new ItemStack[]{this.createItem(sender, islandToCreate)});
                     }
                  } else {
                     sender.sendMessage("§%command-island-island-already-exist%§");
                  }
                  break;
               case "edit":
                  if (sender.isPlayer()) {
                     player.getInventory().addItem(new ItemStack[]{this.createItem(sender, island)});
                  } else {
                     sender.sendMessage("§%command-only-for-player%§");
                  }
                  break;
               case "setlocation":
                  String fieldName = args[2];
                  Field field = null;
                  Field[] location = Island.class.getDeclaredFields();
                  int e = location.length;
                  int optionalY = 0;

                  for(; optionalY < e; ++optionalY) {
                     Field f = location[optionalY];
                     if (f.getName().equalsIgnoreCase(fieldName)) {
                        field = f;
                     }
                  }

                  if (field == null) {
                     sender.sendMessage(sender.getLanguage().t("command-island-field-not-exist", "%field%", fieldName));
                  } else {
                     Location locationx = null;
                     Location var31;
                     if (args.length >= 6) {
                        OptionalDouble optionalX = StringFormat.parseDouble(args[3]);
                        OptionalDouble optionalYx = StringFormat.parseDouble(args[4]);
                        OptionalDouble optionalZ = StringFormat.parseDouble(args[5]);
                        if (!optionalX.isPresent() || !optionalYx.isPresent() || !optionalZ.isPresent()) {
                           sender.sendMessage("§%number-format%§");
                           return;
                        }

                        var31 = new Location(
                           (cmdArgs.isPlayer() ? player.getLocation().getWorld() : Bukkit.getWorlds().stream().findFirst().orElse(null)).getName(),
                           optionalX.getAsDouble(),
                           optionalYx.getAsDouble(),
                           optionalZ.getAsDouble()
                        );
                     } else {
                        if (!sender.isPlayer()) {
                           sender.sendMessage("§%command-only-for-player%§");
                           return;
                        }

                        var31 = Location.fromLocation(player.getLocation());
                     }

                     try {
                        field.setAccessible(true);
                        field.set(island, var31);
                        sender.sendMessage("§aLocalização atualizada.");
                     } catch (IllegalAccessException | IllegalArgumentException var17) {
                        sender.sendMessage("§%command-island-not-loaded-location%§");
                        var17.printStackTrace();
                     }
                  }
            }
         }
      }
   }

   private ItemStack createItem(CommandSender sender, final Island islandToCreate) {
      return new ActionItemStack(
            new ItemBuilder()
               .name(
                  sender.getLanguage()
                     .t(
                        "bedwars.creator.item-name",
                        "%island%",
                        StringFormat.formatString(islandToCreate.getIslandColor().name()),
                        "%islandColor%",
                        "§" + islandToCreate.getIslandColor().getColor().getChar()
                     )
               )
               .type(Material.BARRIER)
               .build(),
            new ActionItemStack.Interact() {
               @Override
               public boolean onInteract(Player player, Entity entity, Block block, ItemStack item, ActionItemStack.ActionType action) {
                  new IslandCreatorInventory(player, islandToCreate);
                  return true;
               }
            }
         )
         .getItemStack();
   }

   @CommandFramework.Command(
      name = "generator",
      permission = "command.generator"
   )
   public void generatorCommand(CommandArgs cmdArgs) {
      CommandSender sender = cmdArgs.getSender();
      Player player = cmdArgs.getSenderAsMember(BukkitMember.class).getPlayer();
      String[] args = cmdArgs.getArgs();
      if (args.length == 0) {
         this.handleGeneratorUsage(sender);
      } else {
         String var5 = args[0].toLowerCase();
         switch(var5) {
            case "list":
               sender.sendMessage("  §aLista de Geradores");

               for(GeneratorType generatorType : GeneratorType.values()) {
                  List<Generator> generators = GameMain.getInstance().getGeneratorManager().getGenerators(generatorType);
                  if (generators != null) {
                     sender.sendMessage("    §fGeradores de " + generatorType.getColor() + generatorType.name());

                     for(int index = 0; index < generators.size(); ++index) {
                        Generator generator = generators.get(index);
                        MessageBuilder messageBuilder = new MessageBuilder("      §fGerador " + (index + 1))
                           .setClickEvent("/" + cmdArgs.getLabel() + " " + generatorType.name() + " " + (index + 1));
                        sender.sendMessage(messageBuilder.create());
                        sender.sendMessage(
                           messageBuilder.setMessage(
                                 "        §fLocation: §7"
                                    + generator.getLocation().getX()
                                    + ", "
                                    + generator.getLocation().getY()
                                    + ", "
                                    + generator.getLocation().getZ()
                              )
                              .create()
                        );
                     }
                  }
               }
               break;
            default:
               GeneratorType generatorType = null;

               try {
                  generatorType = GeneratorType.valueOf(args[0].toUpperCase());
               } catch (Exception var15) {
                  sender.sendMessage("§cO generador não existe.");
                  return;
               }

               if (args.length == 2) {
                  if (args[1].equalsIgnoreCase("create")) {
                     if (sender.isPlayer()) {
                        GameMain.getInstance().getGeneratorManager().createGenerator(generatorType, Location.fromLocation(player.getLocation()), true);
                        sender.sendMessage("§aO jogador de " + generatorType.name() + " foi criado com sucesso.");
                     } else {
                        sender.sendMessage("§%command-only-for-player%§");
                     }
                  } else {
                     OptionalInt parseInt = StringFormat.parseInt(args[2]);
                     if (parseInt.isPresent()) {
                        Generator generator = GameMain.getInstance().getGeneratorManager().getGenerator(generatorType, parseInt.getAsInt());
                        if (generator == null) {
                           sender.sendMessage("§cNenhum gerador encontrado.");
                        } else {
                           sender.sendMessage("  Gerador " + (parseInt.getAsInt() + 1));
                           sender.sendMessage(
                              "    Location: §7"
                                 + generator.getLocation().getX()
                                 + ", "
                                 + generator.getLocation().getY()
                                 + ", "
                                 + generator.getLocation().getZ()
                           );
                           sender.sendMessage("    Level: " + generator.getLevel());
                           sender.sendMessage("    Time: " + generator.getGenerateTime() / 1000L);
                        }
                     } else {
                        this.handleGeneratorUsage(sender);
                     }
                  }
               } else if (args.length >= 3) {
                  if (args[1].equalsIgnoreCase("setlocation")) {
                     if (sender.isPlayer()) {
                        OptionalInt parseInt = StringFormat.parseInt(args[2]);
                        if (parseInt.isPresent()) {
                           if (GameMain.getInstance()
                              .getGeneratorManager()
                              .setLocation(generatorType, parseInt.getAsInt() - 1, Location.fromLocation(player.getLocation()), true)) {
                              sender.sendMessage("localizacao atulaizada.");
                           } else {
                              sender.sendMessage("§cNenhum gerador encontrado.");
                           }
                        } else {
                           sender.sendMessage("§%number-format%§");
                        }
                     } else {
                        sender.sendMessage("§%command-only-for-player%§");
                     }
                  } else {
                     this.handleGeneratorUsage(sender);
                  }
               } else {
                  this.handleGeneratorUsage(sender);
               }
         }
      }
   }

   @CommandFramework.Completer(
      name = "island"
   )
   public List<String> islandCompleter(CommandArgs cmdArgs) {
      String[] args = cmdArgs.getArgs();
      List<String> list = new ArrayList<>();
      if (args.length == 1) {
         for(IslandColor color : IslandColor.values()) {
            if (color.name().toLowerCase().startsWith(args[0].toLowerCase())) {
               list.add(color.name());
            }
         }
      } else if (args.length == 2) {
         for(String completer : Arrays.asList("create", "edit", "setlocation")) {
            if (completer.toLowerCase().startsWith(args[1].toLowerCase())) {
               list.add(completer);
            }
         }
      } else if (args.length == 3 && args[1].equalsIgnoreCase("setlocation")) {
         for(Field field : Island.class.getDeclaredFields()) {
            if (field.getName().toLowerCase().startsWith(args[2].toLowerCase()) && field.getName().toLowerCase().contains("location")) {
               list.add(field.getName());
            }
         }
      }

      return list;
   }

   private void handleGeneratorUsage(CommandSender sender) {
      sender.sendMessage("generator list");
      sender.sendMessage("generator <type> create");
      sender.sendMessage("generator <type> setlocation <index>");
   }
}

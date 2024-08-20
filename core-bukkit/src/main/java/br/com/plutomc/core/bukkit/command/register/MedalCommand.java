package br.com.plutomc.core.bukkit.command.register;

import br.com.plutomc.core.bukkit.BukkitCommon;
import br.com.plutomc.core.bukkit.manager.ChatManager;
import br.com.plutomc.core.bukkit.utils.menu.confirm.ConfirmInventory;
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
import br.com.plutomc.core.common.medal.Medal;
import br.com.plutomc.core.common.member.Member;
import br.com.plutomc.core.common.permission.Tag;
import br.com.plutomc.core.common.utils.string.MessageBuilder;
import br.com.plutomc.core.common.utils.string.StringFormat;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent.Action;

public class MedalCommand implements CommandClass {
   @CommandFramework.Command(
      name = "medalmanager",
      permission = "command.medalmanager"
   )
   public void medalManagerCommand(CommandArgs cmdArgs) {
      CommandSender sender = cmdArgs.getSender();
      String[] args = cmdArgs.getArgs();
      if (args.length == 0) {
         this.handleMedalUsage(sender, cmdArgs.getLabel());
      } else {
         String var4 = args[0].toLowerCase();
         switch(var4) {
            case "deletar":
               if (args.length == 1) {
                  sender.sendMessage("§eUse §b/" + cmdArgs.getLabel() + " deletar <medalName>§e para deletar uma medalha");
                  return;
               }

               Medal medal = CommonPlugin.getInstance().getPluginInfo().getMedalByName(args[0]);
               if (medal == null) {
                  sender.sendMessage("§cA medalha \"" + args[0] + "\" não existe.");
                  return;
               }

               if (sender.isPlayer()) {
                  new ConfirmInventory(cmdArgs.getSenderAsMember(BukkitMember.class).getPlayer(), "Confirme a ação.", bool -> {
                     if (bool) {
                        CommonPlugin.getInstance().getPluginInfo().getMedalMap().remove(medal.getMedalName().toLowerCase());
                        sender.sendMessage("§aMedalha deletada com sucesso.");
                     }
                  }, null);
               } else {
                  CommonPlugin.getInstance().getPluginInfo().getMedalMap().remove(medal.getMedalName().toLowerCase());
                  sender.sendMessage("§aMedalha deletada com sucesso.");
               }
               break;
            case "list":
               sender.sendMessage("  §eLista de medalha: ");

               for(Medal medal1 : CommonPlugin.getInstance().getPluginInfo().getMedalMap().values()) {
                  sender.sendMessage(
                     new MessageBuilder("    §f- " + medal1.getChatColor() + medal1.getMedalName() + "")
                        .setHoverEvent(
                           ""
                              + medal1.getChatColor()
                              + medal1.getMedalName()
                              + "\n\n§eInfo:\n  §fSímbolo: §7"
                              + medal1.getChatColor()
                              + medal1.getSymbol()
                              + "\n  §fCor: §7"
                              + medal1.getChatColor()
                              + StringFormat.formatString(medal1.getChatColor().name())
                        )
                        .create()
                  );
               }
               break;
            case "create":
               if (args.length == 1) {
                  sender.sendMessage("§eUse §b/" + cmdArgs.getLabel() + " create <medalName>§e para criar uma medalha.");
                  return;
               }

               String medalName = args[1];
               ChatManager.Callback confirm = (cancel, answers) -> {
                  if (cancel) {
                     sender.sendMessage("§cOperation cancelled.");
                  } else {
                     String color = answers[0];
                     ChatColor chatColor = ChatColor.getByChar(color.contains("&") ? color.charAt(1) : color.toCharArray()[0]);
                     if (chatColor == null) {
                        chatColor = ChatColor.valueOf(color.toUpperCase());
                     }

                     String symbol = "" + answers[1].toCharArray()[0];
                     String[] aliases = answers[2].contains(", ") ? answers[2].split(", ") : answers[2].split(",");
                     if (CommonPlugin.getInstance().getPluginInfo().getMedalByName(medalName) == null) {
                        Medal medalx = new Medal(medalName, symbol, chatColor.name().toUpperCase(), Arrays.asList(aliases));
                        CommonPlugin.getInstance().getPluginInfo().loadMedal(medalx);
                        sender.sendMessage("§eA medalha " + medalName + " §7(" + chatColor + symbol + "§7)§e foi criada com sucesso.");
                     } else {
                        sender.sendMessage("§cA medalha inserida já existe.");
                     }
                  }
               };
               ChatManager.Validator validator = (message, index) -> {
                  switch(index) {
                     case 0:
                        try {
                           return ChatColor.valueOf(message) != null;
                        } catch (Exception var3x) {
                           return (message.contains("&") ? ChatColor.getByChar(message.toCharArray()[1]) : ChatColor.getByChar(message.toCharArray()[0]))
                              != null;
                        }
                     case 1:
                        return true;
                     case 2:
                        return !message.isEmpty() && message.length() > 3;
                     default:
                        return true;
                  }
               };
               BukkitCommon.getInstance()
                  .getChatManager()
                  .loadChat(
                     sender,
                     confirm,
                     validator,
                     "§ePara criar uma medalha, digite qual será a cor usada\n§ePaleta de cores disponíveis: "
                        + Joiner.on(' ')
                           .join(Arrays.asList(ChatColor.values()).stream().map(chatColor -> "" + chatColor + chatColor.getName()).collect(Collectors.toList())),
                     "§eInsira qual será o simbolo da medalha:",
                     "§eInsira quais serão as aliases da medalha, utilizando vírgula para separar"
                  );
               break;
            default:
               Member member = CommonPlugin.getInstance().getMemberManager().getMemberByName(args[0]);
               if (member == null) {
                  member = CommonPlugin.getInstance().getMemberData().loadMember(args[0], true);
                  if (member == null) {
                     sender.sendMessage(sender.getLanguage().t("account-doesnt-exist", "%player%", args[0]));
                     return;
                  }
               }

               if (args.length == 1) {
                  if (member.getMedals().isEmpty()) {
                     sender.sendMessage("§cO jogador " + member.getPlayerName() + " não possui medalhas.");
                  } else {
                     sender.sendMessage("  §eMembro " + member.getPlayerName());
                  }

                  Member m = member;

                  for(Medal medal1 : CommonPlugin.getInstance()
                     .getPluginInfo()
                     .getMedalMap()
                     .values()
                     .stream()
                     .filter(medalx -> m.hasMedal(medalx))
                     .collect(Collectors.toList())) {
                     sender.sendMessage(
                        new MessageBuilder("    §f- " + medal1.getChatColor() + medal1.getMedalName() + "")
                           .setHoverEvent(
                              ""
                                 + medal1.getChatColor()
                                 + medal1.getMedalName()
                                 + "\n\n  §eInfo:\n  §fSímbolo: §7"
                                 + medal1.getChatColor()
                                 + medal1.getSymbol()
                                 + "\n  §fCor: §7"
                                 + medal1.getChatColor()
                                 + StringFormat.formatString(medal1.getChatColor().name())
                           )
                           .create()
                     );
                  }

                  return;
               }

               String var14 = args[1].toLowerCase();
               switch(var14) {
                  case "add":
                     if (args.length == 2) {
                        sender.sendMessage("§eUse §b/" + cmdArgs.getLabel() + " <player> add <medal>§e para adicionar uma medalha a um jogador.");
                        return;
                     }

                     Medal medal1 = CommonPlugin.getInstance().getPluginInfo().getMedalByName(args[2]);
                     if (medal1 == null) {
                        sender.sendMessage("§cA medalha \"" + args[2] + "\" não existe.");
                        return;
                     }

                     if (member.addMedal(medal1)) {
                        sender.sendMessage("§aVocê deu a medalha " + medal1.getChatColor() + medal1.getMedalName() + "§e para o " + member.getPlayerName() + ".");
                     } else {
                        sender.sendMessage(
                           "§cO jogador " + member.getPlayerName() + " já possui a medalha " + medal1.getChatColor() + medal1.getMedalName() + "§c."
                        );
                     }
                     break;
                  case "remove":
                     if (args.length == 2) {
                        sender.sendMessage("§eUse §b/" + cmdArgs.getLabel() + " <player> remove <medal>§e para remover uma medalha de um jogador.");
                        return;
                     }

                     medal1 = CommonPlugin.getInstance().getPluginInfo().getMedalByName(args[2]);
                     if (medal1 == null) {
                        sender.sendMessage("§cA medalha \"" + args[2] + "\" não existe.");
                        return;
                     }

                     if (member.removeMedal(medal1)) {
                        sender.sendMessage("§aVocê removeu a medalha " + medal1.getChatColor() + medal1.getMedalName() + "§a do " + member.getPlayerName() + ".");
                     } else {
                        sender.sendMessage(
                           "§cO jogador " + member.getPlayerName() + " não possui a medalha " + medal1.getChatColor() + medal1.getMedalName() + "§c."
                        );
                     }
                     break;
                  default:
                     sender.sendMessage("§eUse §b/" + cmdArgs.getLabel() + " <player> remove <medal>§e para adicionar uma medalha a um jogador.");
                     sender.sendMessage("§eUse §b/" + cmdArgs.getLabel() + " <player> add <medal>§e para remover uma medalha de um jogador.");
               }
         }
      }
   }

   @CommandFramework.Command(
      name = "medal",
      aliases = {"medalhas", "medals", "emblemas"},
      console = false
   )
   public void medalCommand(CommandArgs cmdArgs) {
      Member sender = cmdArgs.getSenderAsMember();
      String[] args = cmdArgs.getArgs();
      if (args.length != 0) {
         String medalName = Joiner.on(' ').join(args);
         if (!medalName.equalsIgnoreCase("nenhum") && !medalName.equalsIgnoreCase("nenhuma") && !medalName.equalsIgnoreCase("remover")) {
            Medal medal = CommonPlugin.getInstance().getPluginInfo().getMedalByName(medalName);
            if (medal == null) {
               sender.sendMessage("§cA medalha \"" + medalName + "\" não existe.");
            } else if (!sender.hasMedal(medal)) {
               sender.sendMessage("§cVocê não possui a medalha " + medal.getChatColor() + medal.getMedalName() + "§c.");
            } else {
               sender.setMedal(medal);
               sender.sendMessage("§aSua medalha foi alterada para " + medal.getChatColor() + medal.getMedalName() + "§a.");
            }
         } else {
            sender.setMedal(null);
            sender.sendMessage("§aSua medalha foi removida.");
         }
      } else {
         if (sender.getMedals().isEmpty()) {
            sender.sendMessage("§cVocê não possui nenhuma medalha.");
         } else {
            MessageBuilder messageBuilder = new MessageBuilder("§aSuas medalhas: ");
            messageBuilder.extra(
               new MessageBuilder("§7Nenhum§f, ")
                  .setHoverEvent("§eRemover sua medalha.\n\n§aClique para selecionar.")
                  .setClickEvent(Action.RUN_COMMAND, "/medal nenhum")
                  .create()
            );
            List<Medal> medals = CommonPlugin.getInstance()
               .getPluginInfo()
               .getMedalMap()
               .values()
               .stream()
               .filter(medalx -> sender.hasMedal(medalx))
               .collect(Collectors.toList());
            int size = medals.size();

            for(int i = 0; i < size; ++i) {
               Medal medal = medals.get(i);
               messageBuilder.extra(
                  new MessageBuilder("" + medal.getChatColor() + medal.getSymbol() + (i == size - 1 ? "§f." : "§f,"))
                     .setHoverEvent("" + medal.getChatColor() + medal.getMedalName() + "\n\n§aClique para selecionar.")
                     .setClickEvent(Action.RUN_COMMAND, "/medal " + medal.getMedalName())
                     .create()
               );
            }

            sender.sendMessage(messageBuilder.create());
         }
      }
   }

   @CommandFramework.Completer(
      name = "tag"
   )
   public List<String> tagCompleter(CommandArgs cmdArgs) {
      if (cmdArgs.isPlayer() && cmdArgs.getArgs().length == 1) {
         List<String> tagList = new ArrayList<>();
         BukkitMember member = (BukkitMember)CommonPlugin.getInstance().getMemberManager().getMember(cmdArgs.getSender().getUniqueId());
         if (cmdArgs.getArgs()[0].isEmpty()) {
            for(Tag tag : CommonPlugin.getInstance().getPluginInfo().getTags()) {
               if (member.hasTag(tag)) {
                  tagList.add(tag.getTagName().toLowerCase());
               }
            }
         } else {
            for(Tag tag : CommonPlugin.getInstance().getPluginInfo().getTags()) {
               if (member.hasTag(tag) && tag.getTagName().toLowerCase().startsWith(cmdArgs.getArgs()[0].toLowerCase())) {
                  tagList.add(tag.getTagName().toLowerCase());
               }
            }
         }

         return tagList;
      } else {
         return new ArrayList<>();
      }
   }

   private void handleMedalUsage(CommandSender sender, String label) {
      sender.sendMessage("§eUse §b/" + label + " create <medalName>§e para criar uma medalha.");
      sender.sendMessage("§eUse §b/" + label + " deletar <medalName>§e para deletar uma medalha.");
      sender.sendMessage("§eUse §b/" + label + " list§e para listar as medalhas.");
      sender.sendMessage("§eUse §b/" + label + " <player>§e para listar as medalhas de um jogador.");
      sender.sendMessage("");
      sender.sendMessage("§eUse §b/" + label + " <player> add <medal>§e para adicionar uma medalha a um jogador.");
      sender.sendMessage("§eUse §b/" + label + " <player> remove <medal>§e para adicionar uma medalha a um jogador.");
   }
}

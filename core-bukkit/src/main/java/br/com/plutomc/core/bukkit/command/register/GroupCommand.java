package br.com.plutomc.core.bukkit.command.register;

import br.com.plutomc.core.bukkit.BukkitCommon;
import br.com.plutomc.core.bukkit.manager.ChatManager;
import br.com.plutomc.core.bukkit.member.BukkitMember;
import br.com.plutomc.core.bukkit.menu.group.MemberGroupListInventory;
import br.com.plutomc.core.common.CommonPlugin;
import br.com.plutomc.core.common.command.CommandArgs;
import br.com.plutomc.core.common.command.CommandClass;
import br.com.plutomc.core.common.command.CommandFramework;
import br.com.plutomc.core.common.command.CommandSender;
import br.com.plutomc.core.common.member.Member;
import br.com.plutomc.core.common.permission.Group;
import br.com.plutomc.core.common.permission.GroupInfo;
import br.com.plutomc.core.common.permission.Tag;
import br.com.plutomc.core.common.utils.DateUtils;
import br.com.plutomc.core.common.utils.string.MessageBuilder;
import br.com.plutomc.core.common.utils.string.StringFormat;
import br.com.plutomc.core.common.utils.supertype.OptionalBoolean;
import com.google.common.base.Joiner;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.Field;
import java.util.*;

public class GroupCommand implements CommandClass {
   @CommandFramework.Command(
      name = "group",
      permission = "command.group"
   )
   public void groupCommand(final CommandArgs cmdArgs) {
      CommandSender sender = cmdArgs.getSender();
      String[] args = cmdArgs.getArgs();
      if (args.length == 0) {
         sender.sendMessage("§eUse §b/group info§e para ver as informações do seu grupo.");
         sender.sendMessage("§eUse §b/group info <group>§e para ver as informações do grupo.");
         sender.sendMessage("§eUse §b/group playerlist <group>§e para ver as informações do grupo.");
         sender.sendMessage("§eUse §b/group list§e para listar os grupos.");
         sender.sendMessage("");
         sender.sendMessage("§eUse §b/group <player> add <group>§e para adicionar um grupo a alguém.");
         sender.sendMessage("§eUse §b/group <player> remove <group>§e para remover um grupo de alguém.");
         sender.sendMessage("§eUse §b/group <player> set <group>§e para setar um grupo a alguém.");
         sender.sendMessage("");
         sender.sendMessage("§eUse §b/group create <groupName>§e para criar um grupo.");
         sender.sendMessage("§eUse §b/group manager <groupName>§e para gerenciar um grupo.");
         sender.sendMessage("§eUse §b/group delete <groupName>§e para deletar um grupo.");
         sender.sendMessage("§eUse §b/group createtag ");
      } else {
         String var4 = args[0].toLowerCase();
         switch(var4) {
            case "list":
               Collection<Group> groupList = CommonPlugin.getInstance().getPluginInfo().getGroupMap().values();
               sender.sendMessage("  §aGrupos disponíveis:");

               for(Group group : groupList) {
                  sender.sendMessage(
                     new MessageBuilder("    §f- " + StringFormat.formatString(group.getGroupName()))
                        .setHoverEvent(
                           "§fName: §7"
                              + StringFormat.formatString(group.getGroupName())
                              + "\n"
                              + (group.getPermissions().isEmpty() ? "" : "\n§fPermissions:\n  - §7" + Joiner.on("\n  - §7").join(group.getPermissions()))
                        )
                        .create()
                  );
               }
               break;
            case "playerlist":
               if (!sender.isPlayer()) {
                  sender.sendMessage("§cSomente jogadores podem executar esse comando.");
                  return;
               }

               if (!sender.hasPermission("command.group.playerlist")) {
                  sender.sendMessage("§cVocê não tem permissão para executar esse argumento.");
                  return;
               }

               Group g = null;
               if (args.length == 1) {
                  g = cmdArgs.getSenderAsMember().getServerGroup();
               } else {
                  g = CommonPlugin.getInstance().getPluginInfo().getGroupByName(args[1]);
                  if (g == null) {
                     sender.sendMessage(sender.getLanguage().t("group-not-found", "%group%", args[1]));
                     return;
                  }
               }

               sender.sendMessage("§aAguarde, isso pode demorar um pouco.");
               Group finalG = g;
               (new BukkitRunnable() {
                  @Override
                  public void run() {
                     final List<Member> memberList = CommonPlugin.getInstance().getMemberData().getMembersByGroup(finalG);
                     (new BukkitRunnable() {
                        @Override
                        public void run() {
                           new MemberGroupListInventory(cmdArgs.getSenderAsMember(BukkitMember.class).getPlayer(), finalG, memberList);
                        }
                     }).runTask(BukkitCommon.getInstance());
                  }
               }).runTaskAsynchronously(BukkitCommon.getInstance());
               break;
            case "info":
               Group group = null;
               if (args.length == 1) {
                  group = cmdArgs.getSenderAsMember().getServerGroup();
               } else {
                  group = CommonPlugin.getInstance().getPluginInfo().getGroupByName(args[1]);
                  if (group == null) {
                     sender.sendMessage(sender.getLanguage().t("group-not-found", "%group%", args[1]));
                     return;
                  }
               }

               sender.sendMessage("  §aGrupo " + StringFormat.formatString(group.getGroupName()));
               sender.sendMessage("    §fID: §7" + group.getId());
               sender.sendMessage("    §fPermissões:");

               for(String permission : group.getPermissions()) {
                  sender.sendMessage("      §f- §7" + permission);
               }
               break;
            case "permission":
               if (!sender.hasPermission("command.group.create")) {
                  sender.sendMessage("§cVocê não tem permissão para alterar as permissões de um grupo.");
                  return;
               }

               group = null;
               if (args.length <= 2) {
                  group = cmdArgs.getSenderAsMember().getServerGroup();
                  sender.sendMessage(
                     "§eUse §b/"
                        + cmdArgs.getLabel()
                        + " "
                        + Joiner.on(' ').join(args)
                        + " <add:remove> <permission>§e para adicionar ou remove uma permissão do grupo."
                  );
                  sender.sendMessage("§eUse §b/" + cmdArgs.getLabel() + " " + Joiner.on(' ').join(args) + " list§e para listar as permissões do grupo.");
                  return;
               }

               group = CommonPlugin.getInstance().getPluginInfo().getGroupByName(args[1]);
               if (group == null) {
                  sender.sendMessage(sender.getLanguage().t("group-not-found", "%group%", args[1]));
                  return;
               }

               if (args.length <= 3) {
                  if (!args[2].equalsIgnoreCase("list")) {
                     group = cmdArgs.getSenderAsMember().getServerGroup();
                     sender.sendMessage("§eUse §b/" + cmdArgs.getLabel() + " " + Joiner.on(' ').join(args) + " <permission>§e para adicionar ou remove r");
                     return;
                  }

                  sender.sendMessage("  §aGrupo " + StringFormat.formatString(group.getGroupName()));
                  sender.sendMessage("    §fPermissões:");

                  for(String permission : group.getPermissions()) {
                     sender.sendMessage("      §f- §7" + permission);
                  }

                  return;
               }

               String permission = args[3].toLowerCase();
               boolean add = args[2].equalsIgnoreCase("add");
               if (add) {
                  if (group.getPermissions().contains(permission)) {
                     sender.sendMessage("§cO grupo " + StringFormat.formatString(group.getGroupName()) + " já tem a permissão \"" + permission + "\".");
                  } else {
                     group.getPermissions().add(permission);
                     CommonPlugin.getInstance().saveConfig("groupMap");
                     sender.sendMessage("§aPermissão \"" + permission + "\" foi adicionada ao grupo " + StringFormat.formatString(group.getGroupName()) + ".");
                     this.staffLog("O grupo " + StringFormat.formatString(group.getGroupName()) + " teve a permissão " + permission + " adicionadada.", true);
                  }
               } else if (!group.getPermissions().contains(permission)) {
                  sender.sendMessage("§cO grupo " + StringFormat.formatString(group.getGroupName()) + " não tem a permissão \"" + permission + "\".");
               } else {
                  group.getPermissions().remove(permission);
                  sender.sendMessage("§aPermissão \"" + permission + "\" foi removida do grupo " + StringFormat.formatString(group.getGroupName()) + ".");
                  CommonPlugin.getInstance().saveConfig("groupMap");
                  this.staffLog("O grupo " + StringFormat.formatString(group.getGroupName()) + " teve a permissão " + permission + " removida.", true);
               }
               break;
            case "create":
               if (!sender.hasPermission("command.group.create")) {
                  sender.sendMessage("§cVocê não tem permissão para criar grupo.");
                  return;
               }

               if (args.length == 1) {
                  sender.sendMessage("§eUse §b/group create <group>§e para criar um grupo.");
               } else {
                  String groupName = args[1];
                  ChatManager.Callback confirm = (cancel, answers) -> {
                     if (cancel) {
                        sender.sendMessage("§cOperation cancelled.");
                     } else {
                        int id = StringFormat.parseInt(answers[0]).getAsInt();
                        boolean defaultGroup = StringFormat.parseBoolean(sender.getLanguage(), answers[1]).getAsBoolean();
                        boolean isStaff = StringFormat.parseBoolean(sender.getLanguage(), answers[2]).getAsBoolean();
                        Group groupx = new Group(id, groupName, new ArrayList<>(), defaultGroup, isStaff);
                        if (defaultGroup) {
                        }

                        boolean sort = false;
                        if (CommonPlugin.getInstance().getPluginInfo().getGroupById(id) != null) {
                           CommonPlugin.getInstance()
                              .getPluginInfo()
                              .getGroupMap()
                              .values()
                              .stream()
                              .filter(g1 -> g1.getId() >= id)
                              .forEach(g1 -> g1.setId(g1.getId() + 1));
                           sort = true;
                        }

                        CommonPlugin.getInstance().getPluginInfo().loadGroup(groupx);
                        CommonPlugin.getInstance().saveConfig();
                        sender.sendMessage("§aO grupo " + groupName + " foi criada.");
                        this.staffLog("O grupo " + StringFormat.formatString(groupx.getGroupName()) + " foi criado.", true);
                        if (sort) {
                           CommonPlugin.getInstance().getPluginInfo().sortGroup();
                        }
                     }
                  };
                  ChatManager.Validator validator = (message, index) -> {
                     switch(index) {
                        case 0:
                           OptionalInt var4x = StringFormat.parseInt(message);
                           if (!var4x.isPresent()) {
                              sender.sendMessage(sender.getLanguage().t("number-format-invalid", "%number%", message));
                              return false;
                           }

                           return true;
                        case 1:
                        case 2:
                           OptionalBoolean optionalBool = StringFormat.parseBoolean(sender.getLanguage(), message);
                           if (!optionalBool.isPresent()) {
                              sender.sendMessage(sender.getLanguage().t("format-invalid", "%object%", message));
                              return false;
                           }
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
                        "§aInsira o id do grupo (número).",
                        "§aInsira se o grupo é \"default\" (true ou false).",
                        "§aInsira se o grupo é \"staff\" (true ou false)."
                     );
               }
               break;
            case "createtag":
               if (args.length == 1) {
                  sender.sendMessage("§eUse §b/group createtag <group>§e para criar um grupo.");
               } else {
                  String tagName = args[1];
                  ChatManager.Callback confirm = (cancel, answers) -> {
                     if (cancel) {
                        sender.sendMessage("§cOperation cancelled.");
                     } else {
                        int id = StringFormat.parseInt(answers[0]).getAsInt();
                        String tagPrefix = answers[1].replace('&', '§');
                        String score = answers[2].replace('&', '§');
                        List<String> aliases = (List<String>)(answers[3].equalsIgnoreCase("nenhum")
                           ? new ArrayList<>()
                           : Arrays.asList(answers[2].contains(", ") ? answers[2].split(", ") : answers[2].split(",")));
                        boolean exclusive = StringFormat.parseBoolean(sender.getLanguage(), answers[3]).getAsBoolean();
                        boolean defaultTag = StringFormat.parseBoolean(sender.getLanguage(), answers[4]).getAsBoolean();
                        Tag tag = new Tag(id, tagName, tagPrefix, score, aliases, exclusive, defaultTag);
                        boolean sort = false;
                        if (CommonPlugin.getInstance().getPluginInfo().getTagById(id) != null) {
                           CommonPlugin.getInstance()
                              .getPluginInfo()
                              .getTagMap()
                              .values()
                              .stream()
                              .filter(t -> t.getTagId() >= id)
                              .forEach(t -> t.setTagId(t.getTagId() + 1));
                           sort = true;
                        }

                        CommonPlugin.getInstance().getPluginInfo().loadTag(tag);
                        CommonPlugin.getInstance().saveConfig();
                        sender.sendMessage("§aO tag " + tagName + " foi criada.");
                        this.staffLog("A tag " + StringFormat.formatString(tag.getTagName()) + " foi criada.", true);
                        if (sort) {
                           CommonPlugin.getInstance().getPluginInfo().sortGroup();
                        }
                     }
                  };
                  ChatManager.Validator validator = (message, index) -> {
                     switch(index) {
                        case 0:
                           OptionalInt var4x = StringFormat.parseInt(message);
                           if (!var4x.isPresent()) {
                              sender.sendMessage(sender.getLanguage().t("number-format-invalid", "%number%", message));
                              return false;
                           }

                           return true;
                        case 3:
                        case 4:
                           OptionalBoolean optionalBool = StringFormat.parseBoolean(sender.getLanguage(), message);
                           if (!optionalBool.isPresent()) {
                              sender.sendMessage(sender.getLanguage().t("format-invalid", "%object%", message));
                              return false;
                           }
                        case 1:
                        case 2:
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
                        "§aInsira o id do tag (número).",
                        "§aInsira a tag do grupo sem espaço e com cor usando o símbolo &.",
                        "§aInsire como a tag deve aparecer na scoreboard com cor usando o simbolo &",
                        "§aInsira as aliases usando \",\" para separar.",
                        "§aInsira se a tag é exclusiva ou não (true ou false).",
                        "§aInsira se a tag é \"default\" ou não (true ou false)."
                     );
               }
               break;
            case "delete":
            case "remove":
                group = CommonPlugin.getInstance().getPluginInfo().getGroupByName(args[1]);
               if (group == null) {
                  sender.sendMessage(sender.getLanguage().t("group-not-found", "%group%", args[1]));
                  return;
               }

               CommonPlugin.getInstance().getPluginInfo().getGroupMap().remove(group.getGroupName().toLowerCase());
               sender.sendMessage(sender.getLanguage().t("command.group.deleted-group", "%groupName%", StringFormat.formatString(group.getGroupName())));
               this.staffLog("O grupo " + StringFormat.formatString(group.getGroupName()) + " foi deletado.", true);
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

               Group actualGroup = member.getServerGroup();
               if (args.length <= 1) {
                  GroupInfo groupInfo = member.getServerGroup(actualGroup.getGroupName());
                  sender.sendMessage("  §aMembro " + member.getPlayerName());
                  sender.sendMessage(
                     new MessageBuilder("    §fGrupo: §7" + StringFormat.formatString(actualGroup.getGroupName()))
                        .setHoverEvent("§aClique para ver informações do grupo.")
                        .setClickEvent("/group info " + actualGroup.getGroupName().toLowerCase())
                        .create()
                  );
                  sender.sendMessage(
                     "    §fExpire em: §7" + (groupInfo.isPermanent() ? "Nunca" : DateUtils.getTime(sender.getLanguage(), groupInfo.getExpireTime()))
                  );
                  return;
               }

                group = CommonPlugin.getInstance().getPluginInfo().getGroupByName(args[2]);
               if (group == null) {
                  sender.sendMessage(sender.getLanguage().t("group-not-found", "%group%", args[2]));
                  return;
               }

               if (args.length == 2) {
                  sender.sendMessage("§eUse §b/group " + member.getPlayerName() + " add <group>§e para adicionar um grupo a alguém.");
                  sender.sendMessage("§eUse §b/group " + member.getPlayerName() + " remove <group>§e para remover um grupo de alguém.");
                  sender.sendMessage("§eUse §b/group " + member.getPlayerName() + " set <group>§e para setar um grupo a alguém.");
                  return;
               }

               String var9 = args[1].toLowerCase();
               switch(var9) {
                  case "add":
                     boolean temp = args.length >= 4;
                     long expireTime = temp ? DateUtils.getTime(args[3]) : -1L;
                     member.addServerGroup(group.getGroupName(), new GroupInfo(sender, expireTime));
                     member.setTag(member.getDefaultTag());
                     member.getMemberConfiguration().setStaffChat(false);
                     sender.sendMessage(
                        "§aVocê adicionou o cargo "
                           + group.getGroupName()
                           + " ao jogador "
                           + member.getPlayerName()
                           + " por tempo "
                           + (temp ? DateUtils.getTime(sender.getLanguage(), expireTime) : "indeterminado")
                           + "."
                     );
                     this.staffLog(
                        "O jogador "
                           + member.getPlayerName()
                           + " recebeu cargo "
                           + group.getRealPrefix()
                           + " §7por "
                           + (temp ? DateUtils.getTime(sender.getLanguage(), expireTime) : "indeterminado")
                           + " do "
                           + sender.getName(),
                        true
                     );
                     break;
                  case "remove":
                     if (member.hasGroup(group.getGroupName())) {
                        member.removeServerGroup(group.getGroupName());
                        member.setTag(member.getDefaultTag());
                        member.getMemberConfiguration().setStaffChat(false);
                        sender.sendMessage("§aVocê removeu o cargo " + group.getGroupName() + " do jogador " + member.getPlayerName() + ".");
                        this.staffLog(
                           "O jogador " + member.getPlayerName() + " teve o seu cargo " + group.getRealPrefix() + " §7removido pelo " + sender.getName(), true
                        );
                     } else {
                        sender.sendMessage("§cO player " + member.getPlayerName() + " não tem o grupo " + group.getGroupName() + ".");
                     }
                     break;
                  case "set":
                     member.setServerGroup(group.getGroupName(), new GroupInfo(sender, -1L));
                     member.setTag(member.getDefaultTag());
                     member.getMemberConfiguration().setStaffChat(false);
                     sender.sendMessage("§aVocê adicionou o cargo " + group.getGroupName() + " ao jogador " + member.getPlayerName() + ".");
                     this.staffLog(
                        "O jogador " + member.getPlayerName() + " teve o cargo alterado para " + group.getRealPrefix() + " §7pelo " + sender.getName(), true
                     );
                     break;
                  default:
                     sender.sendMessage("§eUse §b/group <player> add <group>§e para adicionar um grupo a alguém.");
                     sender.sendMessage("§eUse §b/group <player> remove <group>§e para remover um grupo de alguém.");
                     sender.sendMessage("§eUse §b/group <player> set <group>§e para setar um grupo a alguém.");
               }
         }
      }
   }

   @CommandFramework.Completer(
      name = "group"
   )
   public List<String> groupCompleter(CommandArgs cmdArgs) {
      List<String> returnList = new ArrayList<>();
      if (cmdArgs.getArgs().length == 1) {
         List<String> arguments = Arrays.asList("info", "list", "create", "manager", "delete");
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
      } else if (cmdArgs.getArgs().length == 2) {
         if (cmdArgs.getArgs()[0].equalsIgnoreCase("info")) {
            if (cmdArgs.getArgs()[1].isEmpty()) {
               for(Group group : CommonPlugin.getInstance().getPluginInfo().getGroupMap().values()) {
                  returnList.add(group.getGroupName());
               }
            } else {
               for(Group group : CommonPlugin.getInstance().getPluginInfo().getGroupMap().values()) {
                  if (group.getGroupName().toLowerCase().startsWith(cmdArgs.getArgs()[1].toLowerCase())) {
                     returnList.add(group.getGroupName());
                  }
               }
            }
         } else {
            List<String> arguments = Arrays.asList("add", "set", "remove");
            if (cmdArgs.getArgs()[1].isEmpty()) {
               for(String argument : arguments) {
                  returnList.add(argument);
               }
            } else {
               for(String argument : arguments) {
                  if (argument.toLowerCase().startsWith(cmdArgs.getArgs()[1].toLowerCase())) {
                     returnList.add(argument);
                  }
               }
            }
         }
      } else if (cmdArgs.getArgs().length == 3) {
         Player player = Bukkit.getPlayer(cmdArgs.getArgs()[0]);
         if (player != null) {
            List<String> arguments = Arrays.asList("add", "set", "remove");
            if (arguments.contains(cmdArgs.getArgs()[1])) {
               if (cmdArgs.getArgs()[2].isEmpty()) {
                  for(Group group : CommonPlugin.getInstance().getPluginInfo().getGroupMap().values()) {
                     returnList.add(group.getGroupName());
                  }
               } else {
                  for(Group group : CommonPlugin.getInstance().getPluginInfo().getGroupMap().values()) {
                     if (group.getGroupName().toLowerCase().startsWith(cmdArgs.getArgs()[2].toLowerCase())) {
                        returnList.add(group.getGroupName());
                     }
                  }
               }
            }
         }
      }

      return returnList;
   }

   public void set(Group group, int id) throws Exception {
      Field field = Group.class.getDeclaredField("id");
      field.setAccessible(true);
      field.set(group, id);
   }
}

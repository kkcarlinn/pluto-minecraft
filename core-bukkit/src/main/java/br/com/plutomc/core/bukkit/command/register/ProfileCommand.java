package br.com.plutomc.core.bukkit.command.register;

import br.com.plutomc.core.bukkit.menu.profile.PreferencesInventory;
import br.com.plutomc.core.bukkit.menu.profile.ProfileInventory;
import br.com.plutomc.core.bukkit.utils.player.PlayerAPI;
import br.com.plutomc.core.bukkit.BukkitConst;
import br.com.plutomc.core.bukkit.command.BukkitCommandSender;
import br.com.plutomc.core.common.punish.Punish;
import br.com.plutomc.core.common.punish.PunishType;
import com.comphenix.protocol.wrappers.WrappedSignedProperty;
import com.google.common.base.Joiner;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import br.com.plutomc.core.common.CommonConst;
import br.com.plutomc.core.common.CommonPlugin;
import br.com.plutomc.core.bukkit.member.BukkitMember;
import br.com.plutomc.core.common.command.CommandArgs;
import br.com.plutomc.core.common.command.CommandClass;
import br.com.plutomc.core.common.command.CommandFramework;
import br.com.plutomc.core.common.command.CommandSender;
import br.com.plutomc.core.common.member.Member;
import br.com.plutomc.core.common.member.Profile;
import br.com.plutomc.core.common.member.configuration.LoginConfiguration;
import br.com.plutomc.core.common.permission.Group;
import br.com.plutomc.core.common.permission.GroupInfo;
import br.com.plutomc.core.common.permission.Tag;
import br.com.plutomc.core.common.utils.DateUtils;
import br.com.plutomc.core.common.utils.skin.Skin;
import br.com.plutomc.core.common.utils.string.MessageBuilder;
import br.com.plutomc.core.common.utils.string.StringFormat;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.ClickEvent.Action;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class ProfileCommand implements CommandClass {
   @CommandFramework.Command(
      name = "block",
      console = false
   )
   public void blockCommand(CommandArgs cmdArgs) {
      Member sender = cmdArgs.getSenderAsMember();
      String[] args = cmdArgs.getArgs();
      if (args.length == 0) {
         sender.sendMessage("§eUse §b/" + cmdArgs.getLabel() + " <player>§e para bloquear um jogador.");
      } else {
         Member target = CommonPlugin.getInstance().getMemberManager().getMemberByName(cmdArgs.getArgs()[0]);
         if (target == null) {
            target = CommonPlugin.getInstance().getMemberData().loadMember(cmdArgs.getArgs()[0], true);
            if (target == null) {
               sender.sendMessage(sender.getLanguage().t("account-doesnt-exist", "%player%", cmdArgs.getArgs()[0]));
               return;
            }
         }

         if (sender.isUserBlocked(Profile.from(target))) {
            sender.sendMessage("§cO jogador " + target.getName() + " já está bloqueado.");
         } else {
            sender.sendMessage("§aVocê bloqueou o jogador " + target.getName() + ".");
            sender.block(Profile.from(target));
         }
      }
   }

   @CommandFramework.Command(
      name = "unblock",
      console = false
   )
   public void unblockCommand(CommandArgs cmdArgs) {
      Member sender = cmdArgs.getSenderAsMember();
      String[] args = cmdArgs.getArgs();
      if (args.length == 0) {
         sender.sendMessage("§eUse §b/" + cmdArgs.getLabel() + " <player>§e para desbloquear um jogador.");
      } else {
         Member target = CommonPlugin.getInstance().getMemberManager().getMemberByName(cmdArgs.getArgs()[0]);
         if (target == null) {
            target = CommonPlugin.getInstance().getMemberData().loadMember(cmdArgs.getArgs()[0], true);
            if (target == null) {
               sender.sendMessage(sender.getLanguage().t("account-doesnt-exist", "%player%", cmdArgs.getArgs()[0]));
               return;
            }
         }

         if (!sender.isUserBlocked(Profile.from(target))) {
            sender.sendMessage("§cO jogador " + target.getName() + " não está bloqueado.");
         } else {
            sender.sendMessage("§aVocê desbloqueou o jogador " + target.getName() + ".");
            sender.unblock(Profile.from(target));
         }
      }
   }

   @CommandFramework.Command(
      name = "ping",
      console = false
   )
   public void pingCommand(CommandArgs cmdArgs) {
      BukkitMember member = cmdArgs.getSenderAsMember(BukkitMember.class);
      member.sendMessage("§aSeu ping é " + ((CraftPlayer)member.getPlayer()).getHandle().ping + "ms.");
   }

   @CommandFramework.Command(
           name = "tell"
   )
   public void tellCommand(CommandArgs cmdArgs) {
      CommandSender sender = cmdArgs.getSender();
      String[] args = cmdArgs.getArgs();
      if (args.length <= 1) {
         sender.sendMessage("§eUse §b/" + cmdArgs.getLabel() + " <player> <message>§e para enviar uma mensagem para um jogador.");
      } else {
         String var4 = args[0].toLowerCase();
         switch(var4) {
            case "on":
               sender.setTellEnabled(true);
               sender.sendMessage("§aVocê agora receberá mensagens privadas.");
               break;
            case "off":
               sender.setTellEnabled(false);
               sender.sendMessage("§cVocê agora não receberá mais mensagens privadas.");
               break;
            default:
               if (sender instanceof Member) {
                  Member member = (Member)sender;
                  Punish punish = member.getPunishConfiguration().getActualPunish(PunishType.MUTE);
                  if (punish != null) {
                     member.sendMessage(
                             new MessageBuilder(punish.getMuteMessage(member.getLanguage()))
                                     .setHoverEvent(
                                             "§aPunido em: §7"
                                                     + CommonConst.DATE_FORMAT.format(punish.getCreatedAt())
                                                     + "\n§aExpire em: §7"
                                                     + (punish.isPermanent() ? "§cnunca" : DateUtils.formatDifference(member.getLanguage(), punish.getExpireAt() / 1000L))
                                     )
                                     .create()
                     );
                     return;
                  }
               }

               CommandSender target = (CommandSender)(args[0].equalsIgnoreCase("console")
                       ? new BukkitCommandSender(Bukkit.getConsoleSender())
                       : CommonPlugin.getInstance().getMemberManager().getMemberByName(args[0]));
               if (target == null) {
                  sender.sendMessage(sender.getLanguage().t("player-is-not-online", "%player%", args[0]));
                  return;
               }

               if (target.isUserBlocked(Profile.from(sender))) {
                  sender.sendMessage("§cO jogador " + target.getName() + " bloqueou você.");
                  return;
               }

               if (!target.isTellEnabled() && !sender.hasPermission("command.admin")) {
                  sender.sendMessage("§cO jogador " + target.getName() + " não está recebendo mensagens privadas no momento.");
                  return;
               }

               this.sendMessage(sender, target, Joiner.on(' ').join(Arrays.copyOfRange(args, 1, args.length)));
         }
      }
   }

   @CommandFramework.Command(
           name = "reply",
           aliases = {"r"}
   )
   public void replyCommand(CommandArgs cmdArgs) {
      CommandSender sender = cmdArgs.getSender();
      String[] args = cmdArgs.getArgs();
      if (args.length == 0) {
         sender.sendMessage("§eUse §b/" + cmdArgs.getLabel() + " <message>§e para enviar uma mensagem para um jogador.");
      } else if (!sender.hasReply()) {
         sender.sendMessage("§cVocê não possui mensagem para responder.");
      } else {
         if (sender instanceof Member) {
            Member member = (Member)sender;
            Punish punish = member.getPunishConfiguration().getActualPunish(PunishType.MUTE);
            if (punish != null) {
               member.sendMessage(
                       new MessageBuilder(punish.getMuteMessage(member.getLanguage()))
                               .setHoverEvent(
                                       "§aPunido em: §7"
                                               + CommonConst.DATE_FORMAT.format(punish.getCreatedAt())
                                               + "\n§aExpire em: §7"
                                               + (punish.isPermanent() ? "§cnunca" : DateUtils.formatDifference(member.getLanguage(), punish.getExpireAt() / 1000L))
                               )
                               .create()
               );
               return;
            }
         }

         CommandSender target = (CommandSender)(sender.getReplyId() == CommonConst.CONSOLE_ID
                 ? BukkitConst.CONSOLE_SENDER
                 : CommonPlugin.getInstance().getMemberManager().getMember(sender.getReplyId()));
         if (target == null) {
            sender.sendMessage("§cO último jogador que você te mandou mensagem não está mais online.");
         } else if (target.isUserBlocked(Profile.from(sender))) {
            sender.sendMessage("§cO jogador " + target.getName() + " bloqueou você.");
         } else {
            this.sendMessage(sender, target, Joiner.on(' ').join(Arrays.copyOfRange(args, 0, args.length)));
         }
      }
   }


   @CommandFramework.Command(
      name = "profile",
      aliases = {"perfil"},
      console = false
   )
   public void profileCommand(CommandArgs cmdArgs) {
      new ProfileInventory(cmdArgs.getSenderAsMember(BukkitMember.class).getPlayer());
   }

   @CommandFramework.Command(
      name = "preferences",
      aliases = {"pref", "prefs", "preferencias"},
      console = false
   )
   public void preferencesCommand(CommandArgs cmdArgs) {
      new PreferencesInventory(cmdArgs.getSenderAsMember(BukkitMember.class).getPlayer());
   }

   @CommandFramework.Command(
      name = "account",
      aliases = {"acc"},
      runAsync = true
   )
   public void accountCommand(CommandArgs cmdArgs) {
      CommandSender sender = cmdArgs.getSender();
      Member member = cmdArgs.isPlayer() ? cmdArgs.getSenderAsMember() : null;
      if (!cmdArgs.isPlayer() && cmdArgs.getArgs().length == 0) {
         sender.sendMessage("§eUse §b/" + cmdArgs.getLabel() + " <player>§e para ver o perfil de alguém.");
      } else {
         if (cmdArgs.getArgs().length >= 1) {
            if (!sender.hasPermission("command.admin")) {
               sender.sendMessage("§cVocê não tem permissão para visualizar o perfil de outros jogadores.");
               return;
            }

            member = CommonPlugin.getInstance().getMemberManager().getMemberByName(cmdArgs.getArgs()[0]);
            if (member == null) {
               member = CommonPlugin.getInstance().getMemberData().loadMember(cmdArgs.getArgs()[0], true);
               if (member == null) {
                  sender.sendMessage(sender.getLanguage().t("account-doesnt-exist", "%player%", cmdArgs.getArgs()[0]));
                  return;
               }
            }
         }

         Group actualGroup = member.getServerGroup();
         GroupInfo groupInfo = member.getServerGroup(actualGroup.getGroupName());
         sender.sendMessage(" ");
         sender.sendMessage("§a  " + (sender.getUniqueId() == member.getUniqueId() ? "Sua conta" : "Conta do " + member.getPlayerName()));
         sender.sendMessage("    §aPrimeiro login: §7" + CommonPlugin.getInstance().formatTime(member.getFirstLogin()));
         sender.sendMessage(
            "    §aUltimo login: §7"
               + CommonPlugin.getInstance().formatTime(member.getLastLogin())
               + (
                  member.isOnline()
                     ? ""
                     : " (há " + DateUtils.formatDifference(sender.getLanguage(), (System.currentTimeMillis() - member.getLastLogin()) / 1000L) + ")"
               )
         );
         sender.sendMessage("    §aTempo total de jogo: §7" + DateUtils.formatDifference(sender.getLanguage(), member.getOnlineTime() / 1000L));
         sender.sendMessage("    §aTipo de conta: §7" + StringFormat.formatString(member.getLoginConfiguration().getAccountType().name()));
         sender.sendMessage(" ");
         sender.sendMessage(
            new MessageBuilder("    §aGrupo principal: §7" + StringFormat.formatString(actualGroup.getGroupName()))
               .setHoverEvent(
                  "§aGrupo principal "
                     + StringFormat.formatString(actualGroup.getGroupName())
                     + "\n\n  §aAutor: §7"
                     + groupInfo.getAuthorName()
                     + "\n  §aData: §7"
                     + CommonConst.DATE_FORMAT.format(groupInfo.getGivenDate())
                     + "\n  §aExpire em: §7"
                     + (groupInfo.isPermanent() ? "Nunca" : DateUtils.getTime(sender.getLanguage(), groupInfo.getExpireTime()))
                     + "\n\n§eClique para ver informações do grupo."
               )
               .setClickEvent("/group info " + actualGroup.getGroupName())
               .create()
         );
         List<Group> list = member.getGroups()
            .keySet()
            .stream()
            .filter(groupName -> !actualGroup.getGroupName().equals(groupName))
            .map(groupName -> CommonPlugin.getInstance().getPluginInfo().getGroupByName(groupName))
            .collect(Collectors.toList());
         if (!list.isEmpty()) {
            MessageBuilder messageBuilder = new MessageBuilder("    §aGrupos adicionais: §7");
            Set<Entry<String, GroupInfo>> entrySet = new HashSet<>(member.getGroups().entrySet());
            entrySet.removeIf(entryx -> entryx.getKey().equalsIgnoreCase(actualGroup.getGroupName()));
            int i = 1;

            for(Entry<String, GroupInfo> entry : entrySet) {
               messageBuilder.extra(
                  new MessageBuilder("§7" + StringFormat.formatString(entry.getKey()) + (i == entrySet.size() ? "§a." : "§a, §7"))
                     .setHoverEvent(
                        "§aGrupo "
                           + StringFormat.formatString(entry.getKey())
                           + "\n\n  §aAutor: §7"
                           + entry.getValue().getAuthorName()
                           + "\n  §aData: §7"
                           + CommonConst.DATE_FORMAT.format(entry.getValue().getGivenDate())
                           + "\n  §aExpire em: §7"
                           + (entry.getValue().isPermanent() ? "Nunca" : DateUtils.getTime(sender.getLanguage(), entry.getValue().getExpireTime()))
                           + "\n\n§eClique para ver informações do grupo."
                     )
                     .setClickEvent("/group info " + (String)entry.getKey())
                     .create()
               );
               ++i;
            }

            sender.sendMessage(messageBuilder.create());
         }

         if (sender.isStaff() || sender.getUniqueId() == member.getUniqueId()) {
            if (!member.getPermissions().isEmpty()) {
               sender.sendMessage("    §aPermissões: §7" + Joiner.on(", ").join(member.getPermissions()));
            }

            if (member.getLastIpAddress() != null && sender.getServerGroup().getId() >= member.getServerGroup().getId()) {
               sender.sendMessage("");
               sender.sendMessage("    §aEndereço ip: §7" + member.getLastIpAddress());
            }
         }

         if (member.isOnline()) {
            if (member.getLastIpAddress() == null || sender.getServerGroup().getId() < member.getServerGroup().getId()) {
               sender.sendMessage("");
            }

            sender.sendMessage(
               new MessageBuilder("    §aServidor atual: §7" + member.getActualServerId())
                  .setClickEvent(Action.SUGGEST_COMMAND, "/connect " + member.getActualServerId())
                  .setHoverEvent("§aClique para ir ao servidor.")
                  .create()
            );
            sender.sendMessage("    §aTempo da sessão atual: §7" + DateUtils.formatDifference(sender.getLanguage(), member.getSessionTime() / 1000L));
            sender.sendMessage("    §aO jogador está online no momento.");
         } else {
            sender.sendMessage("    §cO jogador está offline no momento.");
         }

         sender.sendMessage("");

         String[] args = cmdArgs.getArgs();
         if(cmdArgs.getArgs()[1].equalsIgnoreCase("rank")) {
            Group group = CommonPlugin.getInstance().getPluginInfo().getGroupByName(args[2]);

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
         } else if(args[1].equalsIgnoreCase("remove")) {
            Group group = CommonPlugin.getInstance().getPluginInfo().getGroupByName(args[2]);
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
         }
      }
   }

   @CommandFramework.Command(
      name = "tag",
      runAsync = true,
      console = false
   )
   public void tagCommand(CommandArgs cmdArgs) {
      Member player = (BukkitMember)cmdArgs.getSender();
      String[] args = cmdArgs.getArgs();
      if (args.length == 0) {
         List<Tag> tags = CommonPlugin.getInstance()
            .getPluginInfo()
            .getTagMap()
            .values()
            .stream()
            .filter(tagx -> player.hasTag(tagx))
            .collect(Collectors.toList());
         if (tags.isEmpty()) {
            player.sendMessage("§cVocê não possui nenhuma tag.");
         } else {
            TextComponent message = new TextComponent("§aSelecione sua tag: ");
            int max = tags.size() * 2;
            int i = max - 1;

            for(Tag t : tags) {
               if (i < max - 1) {
                  message.addExtra(new TextComponent("§a, "));
                  --i;
               }

               message.addExtra(
                  new MessageBuilder(t.getStrippedColor().toUpperCase())
                     .setHoverEvent(
                        new HoverEvent(
                           HoverEvent.Action.SHOW_TEXT,
                           new TextComponent[]{new TextComponent("§aExemplo: " + t.getRealPrefix() + player.getPlayerName() + "\n\n§aClique para selecionar!")}
                        )
                     )
                     .setClickEvent(new ClickEvent(Action.RUN_COMMAND, "/tag " + t.getTagName()))
                     .create()
               );
               --i;
            }

            player.sendMessage(message);
         }
      } else if (!args[0].equalsIgnoreCase("default") && !args[0].equalsIgnoreCase("normal")) {
         Tag tag = CommonPlugin.getInstance().getPluginInfo().getTagByName(args[0]);
         if (tag == null) {
            player.sendMessage("§cA tag " + args[0] + " não existe!");
         } else {
            if (player.hasTag(tag)) {
               if (!player.getTag().equals(tag)) {
                  player.setTag(tag);
               }

               player.sendMessage("§aSua tag foi alterada para " + tag.getStrippedColor() + "§a.");
            } else {
               player.sendMessage("§cVocê não tem permissão para usar essa tag.");
            }
         }
      } else {
         if (player.setTag(player.getDefaultTag())) {
            player.sendMessage("§aVocê alterou sua tag para " + player.getDefaultTag().getStrippedColor() + "§a.");
         }
      }
   }

   @CommandFramework.Command(
      name = "fake.#",
      aliases = {"nick.#", "nickreset", "fakereset", "fake.reset", "nick.reset"},
      permission = "command.fake",
      console = false
   )
   public void fakeresetCommand(CommandArgs cmdArgs) {
      BukkitMember sender = cmdArgs.getSenderAsMember(BukkitMember.class);
      if (!sender.isUsingFake()) {
         sender.sendMessage("§cVocê não está usando fake.");
      } else {
         if (!sender.hasCustomSkin()) {
            PlayerAPI.changePlayerSkin(sender.getPlayer(), sender.getName(), sender.getUniqueId(), false);
         }

         Player player = sender.getPlayer();
         PlayerAPI.changePlayerSkin(player, player.getName(), player.getUniqueId(), false);
         PlayerAPI.changePlayerName(player, sender.getName(), true);
         sender.setFakeName(sender.getPlayerName());
         sender.setTag(sender.getDefaultTag());
         sender.sendMessage("§aSeu fake foi removido com sucesso.");
      }
   }

   @CommandFramework.Command(
      name = "fake",
      aliases = {"nick"},
      runAsync = true,
      permission = "command.fake",
      console = false
   )
   public void fakeCommand(CommandArgs cmdArgs) {
      BukkitMember sender = cmdArgs.getSenderAsMember(BukkitMember.class);
      String[] args = cmdArgs.getArgs();
      if (args.length == 0) {
         sender.sendMessage("§eUse §b/" + cmdArgs.getLabel() + " <player>§e para alterar sua skin.");
      } else if (sender.hasCooldown("fake.command") && !sender.hasPermission("command.admin")) {
         sender.sendMessage("§cVocê precisa esperar " + sender.getCooldownFormatted("fake.command") + " para usar eses comando novamente.");
      } else {
         String playerName = args[0].equals("random") ? BukkitConst.RANDOM.get(CommonConst.RANDOM.nextInt(BukkitConst.RANDOM.size())) : args[0];
         if (!PlayerAPI.validateName(playerName)) {
            sender.sendMessage("§cO nome inserido é inválido.");
         } else {
            UUID uniqueId = CommonPlugin.getInstance().getUuidFetcher().request(playerName);
            if (uniqueId != null) {
               sender.sendMessage("§cVocê não pode usar fake de uma conta registrada na mojang, tente outro nick.");
            } else if (CommonPlugin.getInstance().getMemberData().loadMember(playerName, true) != null) {
               sender.sendMessage("§cVocê não pode usar fake de uma conta já registrada no servidor, tente outro nick.");
            } else if (Bukkit.getPlayerExact(playerName) != null) {
               sender.sendMessage("§cVocê não pode usar o fake do " + playerName + ". 3");
            } else {
               if (!sender.isCustomSkin()) {
                  CommonPlugin.getInstance()
                     .getMemberManager()
                     .getMembers()
                     .stream()
                     .filter(member -> member.hasCustomSkin() || member.getLoginConfiguration().getAccountType() == LoginConfiguration.AccountType.PREMIUM)
                     .findFirst()
                     .ifPresent(member -> {
                        if (member.isCustomSkin()) {
                           sender.setSkin(member.getSkin());
                           PlayerAPI.changePlayerSkin(sender.getPlayer(), member.getSkin().getValue(), member.getSkin().getSignature(), false);
                        } else {
                           WrappedSignedProperty changePlayerSkin = PlayerAPI.changePlayerSkin(
                              sender.getPlayer(), member.getName(), member.getUniqueId(), false
                           );
                           sender.setSkin(new Skin(member.getName(), member.getUniqueId(), changePlayerSkin.getValue(), changePlayerSkin.getSignature()));
                        }
                     });
               }

               PlayerAPI.changePlayerName(sender.getPlayer(), playerName, true);
               sender.setFakeName(playerName);
               sender.setTag(CommonPlugin.getInstance().getPluginInfo().getDefaultTag());
               sender.sendMessage("§aSeu nick foi alterada para " + playerName + ".");
               sender.putCooldown("command.fake", 30L);
            }
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

   public void sendMessage(CommandSender sender, CommandSender target, String message) {
      sender.sendMessage("§7[" + sender.getName() + " -> " + target.getName() + "] " + message);
      target.sendMessage("§7[" + sender.getName() + " -> " + target.getName() + "] " + message);
      target.setReplyId(sender.getUniqueId());
   }
}

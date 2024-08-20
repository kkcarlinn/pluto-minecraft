package br.com.plutomc.core.bukkit.listener;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import br.com.plutomc.core.bukkit.BukkitCommon;
import br.com.plutomc.core.bukkit.BukkitConst;
import br.com.plutomc.core.common.CommonConst;
import br.com.plutomc.core.common.CommonPlugin;
import br.com.plutomc.core.common.language.Language;
import br.com.plutomc.core.common.medal.Medal;
import br.com.plutomc.core.common.member.Member;
import br.com.plutomc.core.common.member.Profile;
import br.com.plutomc.core.common.punish.Punish;
import br.com.plutomc.core.common.punish.PunishType;
import br.com.plutomc.core.common.utils.DateUtils;
import br.com.plutomc.core.common.utils.string.MessageBuilder;
import br.com.plutomc.core.common.utils.string.StringFormat;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.ClickEvent.Action;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class ChatListener implements Listener {
   public static final Pattern PATTERN = Pattern.compile("(§%question-(\\d+)%§)");
   private static Pattern urlFinderPattern = Pattern.compile("((https?):((//)|(\\\\))+[\\w\\d:#@%/;$()~_?\\+-=\\\\\\.&]*)", 2);

   @EventHandler(
           priority = EventPriority.LOWEST
   )
   public void onAsyncPlayerChatLW(AsyncPlayerChatEvent event) {
      if (BukkitCommon.getInstance().getChatManager().containsChat(event.getPlayer().getUniqueId())) {
         event.setCancelled(true);
         Player player = event.getPlayer();
         String message = event.getMessage();
         boolean cancel = message.contains("cancel") || Language.getLanguage(player.getUniqueId()).t("cancel").equalsIgnoreCase(message);
         boolean validate = BukkitCommon.getInstance().getChatManager().validate(player.getUniqueId(), message);
         if (validate || cancel) {
            String nextQuestion = BukkitCommon.getInstance().getChatManager().callback(player.getUniqueId(), event.getMessage(), cancel);
            if (nextQuestion != null) {
               String replace;
               String id;
               for(Matcher matcher = PATTERN.matcher(nextQuestion);
                   matcher.find();
                   nextQuestion = nextQuestion.replace(
                           replace, BukkitCommon.getInstance().getChatManager().getAnswers(player.getUniqueId(), StringFormat.parseInt(id).getAsInt() - 1)
                   )
               ) {
                  replace = matcher.group();
                  id = matcher.group(2).toLowerCase();
               }

               if (BukkitCommon.getInstance().getChatManager().isClearChat(player.getUniqueId())) {
                  for(int i = 0; i < 100; ++i) {
                     player.sendMessage(" ");
                  }
               }

               player.sendMessage(nextQuestion);
            }
         }
      }
   }

   @EventHandler(
           priority = EventPriority.LOW,
           ignoreCancelled = true
   )
   public void onAsyncPlayerChatL(AsyncPlayerChatEvent event) {
      Player player = event.getPlayer();
      Member member = CommonPlugin.getInstance().getMemberManager().getMember(player.getUniqueId());
      if (member == null) {
         event.setCancelled(true);
      } else {
         Punish punish = member.getPunishConfiguration().getActualPunish(PunishType.MUTE);
         if (punish != null) {
            member.sendMessage(
                    new MessageBuilder(punish.getMuteMessage(member.getLanguage()))
                            .setHoverEvent(
                                    "§fPunido em: §7"
                                            + CommonConst.DATE_FORMAT.format(punish.getCreatedAt())
                                            + "\n§fExpire em: §7"
                                            + (punish.isPermanent() ? "§cnunca" : DateUtils.formatDifference(member.getLanguage(), punish.getExpireAt() / 1000L))
                            )
                            .create()
            );
            event.setCancelled(true);
         } else {
            switch(BukkitCommon.getInstance().getChatState()) {
               case ENABLED:
               default:
                  break;
               case DISABLED:
                  if (!member.hasPermission("chat.disabled-say")) {
                     member.sendMessage("§cVocê não pode falar no chat no momento, somente membros da equipe.");
                     event.setCancelled(true);
                     return;
                  }
                  break;
               case YOUTUBER:
                  if (!member.hasPermission("chat.youtuber-say")) {
                     member.sendMessage("§cVocê não pode falar no chat no momento, somente celebridades do servidor.");
                     event.setCancelled(true);
                     return;
                  }
                  break;
               case PAYMENT:
                  if (!member.hasPermission("chat.payment-say")) {
                     member.sendMessage("§cVocê não pode falar no chat no momento, somente jogadores pagantes.");
                     event.setCancelled(true);
                     return;
                  }
            }

            if (member.hasCooldown("chat-cooldown") && !member.hasPermission("command.admin")) {
               member.sendActionBar("§cVocê precisa esperar " + member.getCooldownFormatted("chat-cooldown") + " para falar no chat novamente.");
               event.setCancelled(true);
            } else {
               member.putCooldown("chat-cooldown", 3.5);
               if (!member.hasPermission("command.admin")) {
                  for(String string : event.getMessage().split(" ")) {
                     if (BukkitConst.SWEAR_WORDS.contains(string.toLowerCase())) {
                        StringBuilder stringBuilder = new StringBuilder();

                        for(int x = 0; x < string.length(); ++x) {
                           stringBuilder.append('*');
                        }

                        event.setMessage(event.getMessage().replace(string, stringBuilder.toString().trim()));
                     }
                  }
               }
            }
         }
      }
   }

   @EventHandler(
           priority = EventPriority.HIGH,
           ignoreCancelled = true
   )
   public void onAsyncPlayerChatN(AsyncPlayerChatEvent event) {
      event.setCancelled(true);
      Player player = event.getPlayer();
      Member member = CommonPlugin.getInstance().getMemberManager().getMember(player.getUniqueId());
      String message = event.getMessage();
      MessageBuilder messageBuilder = new MessageBuilder("");
      Medal medal = member.getMedal();
      if (medal != null) {
         messageBuilder.extra(
                 new MessageBuilder(medal.getChatColor() + medal.getSymbol() + " ")
                         .setHoverEvent("§aMedalha: " + medal.getChatColor() + medal.getMedalName())
                         .create()
         );
      }

      messageBuilder.extra(
              new MessageBuilder(member.getTag().getRealPrefix() + player.getName())
                      .setHoverEvent(
                              "§a"
                                      + player.getName()
                                      + "\n\n§fMedalha: "
                                      + (medal == null ? "§7Nenhuma" : medal.getChatColor() + medal.getSymbol())
                                      + "\n§fTempo de sessão atual: §7"
                                      + DateUtils.formatDifference(member.getLanguage(), member.getSessionTime() / 1000L)
                                      + "\n\n§eMensagem enviada às "
                                      + CommonConst.TIME_FORMAT.format(System.currentTimeMillis())
                                      + "."
                      )
                      .create()
      );
      messageBuilder.extra("§7: §f");
      String[] split = event.getMessage().split(" ");
      String currentColor = "§f";

      for(int x = 0; x < split.length; ++x) {
         String msg = (x > 0 ? " " : "") + currentColor + split[x];
         List<String> links = extractUrls(msg);
         if (links.isEmpty()) {
            messageBuilder.extra(new MessageBuilder(msg).create());
         } else {
            String url = links.stream().findFirst().orElse(null).toLowerCase();
            if (!url.contains("you") && !url.contains("twitch") && !member.hasPermission("command.admin")) {
               member.sendMessage("§cNão é permitido enviar links.");
               event.setCancelled(true);
               return;
            }

            messageBuilder.extra(
                    new MessageBuilder(msg)
                            .setClickEvent(new ClickEvent(Action.OPEN_URL, url))
                            .setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(url)))
                            .create()
            );
         }

         currentColor = ChatColor.getLastColors(msg);
      }

      TextComponent textComponent = messageBuilder.create();
      event.getRecipients().removeIf(recipient -> {
         Member memberRecipient = CommonPlugin.getInstance().getMemberManager().getMember(recipient.getUniqueId());
         if (memberRecipient == null || memberRecipient.getUniqueId().equals(member.getUniqueId())) {
            return false;
         } else if (!memberRecipient.getMemberConfiguration().isSeeingChat() && !member.hasPermission("staff.seechat-ignore")) {
            return true;
         } else {
            return memberRecipient.isUserBlocked(Profile.from(member)) && !member.hasPermission("staff.seechat-ignore");
         }
      });
      event.getRecipients().forEach(recipient -> recipient.spigot().sendMessage(textComponent));
      Bukkit.getConsoleSender().sendMessage(member.getPlayerName() + ": " + message);
   }

   @EventHandler
   public void onPlayerQuit(PlayerQuitEvent event) {
      BukkitCommon.getInstance().getChatManager().remove(event.getPlayer().getUniqueId());
   }

   public static List<String> extractUrls(String text) {
      List<String> containedUrls = new ArrayList<>();
      Matcher urlMatcher = urlFinderPattern.matcher(text);

      while(urlMatcher.find()) {
         containedUrls.add(urlMatcher.group(1));
      }

      return containedUrls;
   }
}
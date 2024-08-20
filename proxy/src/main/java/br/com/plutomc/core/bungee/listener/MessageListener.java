package br.com.plutomc.core.bungee.listener;

import java.util.Arrays;
import java.util.UUID;

import br.com.plutomc.core.bungee.BungeeMain;
import br.com.plutomc.core.bungee.event.player.PlayerCommandEvent;
import br.com.plutomc.core.common.CommonConst;
import br.com.plutomc.core.common.CommonPlugin;
import br.com.plutomc.core.common.member.Member;
import br.com.plutomc.core.common.packet.types.StaffchatDiscordPacket;
import br.com.plutomc.core.common.punish.Punish;
import br.com.plutomc.core.common.punish.PunishType;
import br.com.plutomc.core.common.utils.DateUtils;
import br.com.plutomc.core.common.utils.mojang.UUIDParser;
import br.com.plutomc.core.common.utils.string.MessageBuilder;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class MessageListener implements Listener {
   @EventHandler(
           priority = 127
   )
   public void onChatTeleport(ChatEvent event) {
      if (event.getSender() instanceof ProxiedPlayer) {
         if (!event.isCancelled()) {
            ProxiedPlayer proxiedPlayer = (ProxiedPlayer)event.getSender();
            Member member = CommonPlugin.getInstance().getMemberManager().getMember(proxiedPlayer.getUniqueId());
            if (member == null) {
               event.setCancelled(true);
               proxiedPlayer.disconnect("§cSua conta não foi carregada. [BungeeCord: 01]");
            } else {
               boolean isCommand = event.isCommand();
               String message = event.getMessage();
               String[] split = message.trim().split(" ");
               String[] args = Arrays.copyOfRange(split, isCommand ? 1 : 0, split.length);
               String command = split[0].replace("/", "").toLowerCase();
               if (isCommand) {
                  PlayerCommandEvent callEvent = (PlayerCommandEvent)ProxyServer.getInstance()
                          .getPluginManager()
                          .callEvent(new PlayerCommandEvent(proxiedPlayer, command, args));
                  event.setCancelled(callEvent.isCancelled());
               } else if (member.getMemberConfiguration().isStaffChat()) {
                  if (!member.getMemberConfiguration().isSeeingStaffChat()) {
                     event.setCancelled(true);
                     member.sendMessage("§cAtiva a visualização do staffchat para poder falar no staffchat.");
                  } else {
                     String staffMessage = this.getStaffchatMessage(member, ChatColor.translateAlternateColorCodes('&', message));
                     CommonPlugin.getInstance()
                             .getMemberManager()
                             .getMembers()
                             .stream()
                             .filter(m -> m.isStaff() && m.getMemberConfiguration().isSeeingStaffChat())
                             .forEach(m -> m.sendMessage(staffMessage));
                     CommonPlugin.getInstance().getServerData().sendPacket(new StaffchatDiscordPacket(member.getUniqueId(), member.getServerGroup(), message));
                     event.setCancelled(true);
                  }
               } else {
                  if (event.getMessage().length() > 1) {
                     if (event.getMessage().startsWith("%") && member.hasPermission("command.staffchat")) {
                        if (!member.getMemberConfiguration().isSeeingStaffChat()) {
                           event.setCancelled(true);
                           member.sendMessage("§cAtiva a visualização do staffchat para poder falar no staffchat.");
                           return;
                        }

                        String staffMessage = this.getStaffchatMessage(member, event.getMessage().substring(1));
                        CommonPlugin.getInstance()
                                .getMemberManager()
                                .getMembers()
                                .stream()
                                .filter(m -> m.isStaff() && m.getMemberConfiguration().isSeeingStaffChat())
                                .forEach(m -> m.sendMessage(staffMessage));
                        CommonPlugin.getInstance()
                                .getServerData()
                                .sendPacket(new StaffchatDiscordPacket(member.getUniqueId(), member.getServerGroup(), message));
                        event.setCancelled(true);
                     } else if (event.getMessage().startsWith("@") && member.getParty() != null) {
                        Punish punish = member.getPunishConfiguration().getActualPunish(PunishType.MUTE);
                        if (punish != null) {
                           member.sendMessage(
                                   new MessageBuilder(punish.getMuteMessage(member.getLanguage()))
                                           .setHoverEvent(
                                                   "§fPunido em: §7"
                                                           + CommonConst.DATE_FORMAT.format(punish.getCreatedAt())
                                                           + "\n§fExpire em: §7"
                                                           + (punish.isPermanent() ? "§cnunca" : DateUtils.getTime(member.getLanguage(), punish.getExpireAt()))
                                           )
                                           .create()
                           );
                           event.setCancelled(true);
                           return;
                        }

                        member.getParty().chat(member, event.getMessage().substring(1));
                        event.setCancelled(true);
                     }
                  }
               }
            }
         }
      }
   }

   private String getStaffchatMessage(Member member, String message) {
      return "§e[STAFF] "
              + CommonPlugin.getInstance().getPluginInfo().getTagByGroup(member.getServerGroup()).getStrippedColor()
              + " "
              + member.getPlayerName()
              + "§7: §f"
              + ChatColor.translateAlternateColorCodes('&', message);
   }

   @EventHandler
   public void onPlayerCommand(PlayerCommandEvent event) {
      ProxiedPlayer player = event.getPlayer();
      String label = event.getCommand();
      String[] args = event.getArgs();
      if (!label.startsWith("teleport") && !label.startsWith("tp")) {
         Member member = CommonPlugin.getInstance().getMemberManager().getMember(player.getUniqueId());
         if (!member.getLoginConfiguration().isLogged() && !CommonConst.ALLOWED_COMMAND_LOGIN.contains(label)) {
            event.setCancelled(true);
            player.sendMessage(member.getLanguage().t("login.message.not-allowed"));
         }
      } else if (args.length == 1) {
         UUID uniqueId = UUIDParser.parse(args[0]);
         ProxiedPlayer target = uniqueId == null ? ProxyServer.getInstance().getPlayer(args[0]) : ProxyServer.getInstance().getPlayer(uniqueId);
         if (target != null
                 && target.getServer() != null
                 && target.getServer().getInfo() != null
                 && !target.getServer().getInfo().getName().equals(player.getServer().getInfo().getName())) {
            event.setCancelled(true);
            BungeeMain.getInstance().teleport(player, target);
            return;
         }

         return;
      }
   }
}
package br.com.plutomc.core.bungee.command.register;

import br.com.plutomc.core.common.CommonPlugin;
import br.com.plutomc.core.common.command.CommandArgs;
import br.com.plutomc.core.common.command.CommandClass;
import br.com.plutomc.core.common.command.CommandFramework;
import br.com.plutomc.core.common.member.Member;
import br.com.plutomc.core.common.permission.Tag;
import br.com.plutomc.core.common.utils.string.MessageBuilder;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ClickEvent.Action;

public class MediaCommand implements CommandClass {
   @CommandFramework.Command(
      name = "youtube"
   )
   public void youtubeCommand(CommandArgs cmdArgs) {
      Member sender = cmdArgs.getSenderAsMember();
      String[] args = cmdArgs.getArgs();
      if (args.length == 0) {
         sender.sendMessage("§eUse §b/" + cmdArgs.getLabel() + " <youtube>§e para alterar seu link do youtube.");
      } else {
         String youtubeLink = args[0];
         sender.setYoutubeUrl(youtubeLink);
         sender.sendMessage("§aVocê alterou seu link do youtube para §b" + youtubeLink + "§a.");
      }
   }

   @CommandFramework.Command(
      name = "twitch"
   )
   public void twitchCommand(CommandArgs cmdArgs) {
      Member sender = cmdArgs.getSenderAsMember();
      String[] args = cmdArgs.getArgs();
      if (args.length == 0) {
         sender.sendMessage("§eUse §b/" + cmdArgs.getLabel() + " <twitch>§e para alterar seu link da twitch.");
      } else {
         String twitchUrl = args[0];
         if (!twitchUrl.toLowerCase().contains("twitch.tv")) {
            twitchUrl = "twitch.tv/" + twitchUrl;
         }

         sender.setTwitchUrl(twitchUrl.toLowerCase());
         sender.sendMessage("§aVocê alterou seu link da twitch para §d" + twitchUrl.toLowerCase() + "§a.");
      }
   }

   @CommandFramework.Command(
      name = "stream",
      permission = "command.stream"
   )
   public void streamCommand(CommandArgs cmdArgs) {
      Member sender = cmdArgs.getSenderAsMember();
      if (sender.hasTwitch()) {
         if (sender.hasCooldown("command-stream") && !sender.hasPermission("staff.super")) {
            sender.sendMessage("§cVocê precisa esperar " + sender.getCooldownFormatted("command-stream") + " para usar esse comando novamente.");
            return;
         }

         Tag tag = CommonPlugin.getInstance().getPluginInfo().getTagByName(sender.getServerGroup().getGroupName());
         if (tag == null) {
            sender.sendMessage("§cO servidor não encontrou a sua tag.");
            return;
         }

         ProxyServer.getInstance().broadcast(" ");
         ProxyServer.getInstance()
            .broadcast(
               new MessageBuilder("§b§lPLUTO §6» §fO nosso " + tag.getRealPrefix() + sender.getName() + "§f está em live agora! §bClique aqui para acompanhar.")
                  .setHoverEvent("§eClique para abrir o link no navegador.")
                  .setClickEvent(
                     Action.OPEN_URL, sender.getTwitchUrl().toLowerCase().startsWith("http") ? sender.getTwitchUrl() : "https://" + sender.getTwitchUrl()
                  )
                  .create()
            );
         ProxyServer.getInstance().broadcast(" ");
         sender.putCooldown("command-stream", 300L);
      } else {
         sender.sendMessage("§cVocê ainda não registrou a sua Twitch. Use /twitch");
      }
   }

   @CommandFramework.Command(
      name = "record",
      aliases = {"record"},
      permission = "command.record"
   )
   public void recordCommand(CommandArgs cmdArgs) {
      Member sender = cmdArgs.getSenderAsMember();
      if (sender.hasCooldown("command-stream") && !sender.hasPermission("staff.super")) {
         sender.sendMessage("§cVocê precisa esperar " + sender.getCooldownFormatted("command-stream") + " para usar esse comando novamente.");
      } else {
         Tag tag = CommonPlugin.getInstance().getPluginInfo().getTagByName(sender.getServerGroup().getGroupName());
         if (tag == null) {
            sender.sendMessage("§cO servidor não encontrou a sua tag.");
         } else {
            ProxyServer.getInstance().broadcast(" ");
            ProxyServer.getInstance()
               .broadcast(
                  new MessageBuilder(
                        "§b§lPLUTO §6» §fO nosso "
                           + tag.getRealPrefix()
                           + sender.getName()
                           + "§f está gravando no "
                           + sender.getActualServerId()
                           + "! §bClique aqui para se conectar."
                     )
                     .setHoverEvent("§eClique aqui para se conectar.")
                     .setClickEvent(Action.RUN_COMMAND, "/connect " + sender.getActualServerId())
                     .create()
               );
            ProxyServer.getInstance().broadcast(" ");
            sender.putCooldown("command-stream", 300L);
         }
      }
   }
}

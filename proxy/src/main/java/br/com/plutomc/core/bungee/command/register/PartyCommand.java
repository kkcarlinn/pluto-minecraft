package br.com.plutomc.core.bungee.command.register;

import com.google.common.base.Joiner;
import java.util.HashMap;
import java.util.Map;
import java.util.OptionalInt;
import java.util.UUID;
import br.com.plutomc.core.common.CommonPlugin;
import br.com.plutomc.core.bungee.member.BungeeMember;
import br.com.plutomc.core.bungee.member.BungeeParty;
import br.com.plutomc.core.common.command.CommandArgs;
import br.com.plutomc.core.common.command.CommandClass;
import br.com.plutomc.core.common.command.CommandFramework;
import br.com.plutomc.core.common.manager.PartyManager;
import br.com.plutomc.core.common.member.Member;
import br.com.plutomc.core.common.member.Profile;
import br.com.plutomc.core.common.member.party.Party;
import br.com.plutomc.core.common.member.party.PartyRole;
import br.com.plutomc.core.common.utils.string.MessageBuilder;
import br.com.plutomc.core.common.utils.string.StringFormat;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.config.ServerInfo;

public class PartyCommand implements CommandClass {
   @CommandFramework.Command(
      name = "partychat",
      aliases = {"pc", "partychat", "party.chat"},
      console = false
   )
   public void partychatCommand(CommandArgs cmdArgs) {
      Member sender = (Member)cmdArgs.getSender();
      String[] args = cmdArgs.getArgs();
      if (args.length == 0) {
         sender.sendMessage("§eUse §b/" + cmdArgs.getLabel() + " <mensagem>§e para mandar uma mensagem na party.");
      } else if (sender.getParty() == null) {
         sender.sendMessage("§cVocê não está em uma party.");
      } else {
         sender.getParty().chat(sender, Joiner.on(' ').join(args));
      }
   }

   @CommandFramework.Command(
      name = "party",
      console = false
   )
   public void partyCommand(CommandArgs cmdArgs) {
      Member sender = (Member)cmdArgs.getSender();
      String[] args = cmdArgs.getArgs();
      if (args.length == 0) {
         sender.sendMessage("§eUse §b/" + cmdArgs.getLabel() + " <disband/acabar>§e para acabar com sua party.");
         sender.sendMessage("§eUse §b/" + cmdArgs.getLabel() + " convidar <player>§e para convidar algum jogador para sua party.");
         sender.sendMessage("§eUse §b/" + cmdArgs.getLabel() + " expulsar <player>§e para expulsar alguém da sua party.");
         sender.sendMessage("§eUse §b/" + cmdArgs.getLabel() + " abrir <máximo de players>§e para abrir sua party para uma quantidade de pessoas.");
         sender.sendMessage("§eUse §b/" + cmdArgs.getLabel() + " fechar§e para fechar sua party.");
         sender.sendMessage("");
         sender.sendMessage("§eUse §b/" + cmdArgs.getLabel() + " aceitar <player>§e para aceitar um convite de party.");
         sender.sendMessage("§eUse §b/" + cmdArgs.getLabel() + " entrar <player>§e para entrar numa party pública.");
         sender.sendMessage("§eUse §b/" + cmdArgs.getLabel() + " sair§e para sair de uma party.");
      } else {
         String var4 = args[0].toLowerCase();
         switch(var4) {
            case "close":
            case "fechar":
               Party party = sender.getParty();
               if (party == null) {
                  sender.sendMessage("§cVocê não tem uma party para fechar.");
                  return;
               }

               if (!party.closeParty()) {
                  sender.sendMessage("§cSua party não está aberta.");
               }

               return;
            case "abrir":
            case "open":
               int maxPlayers = 6;
               if (args.length >= 2) {
                  OptionalInt parseInt = StringFormat.parseInt(args[1]);
                  if (!parseInt.isPresent()) {
                     sender.sendMessage(sender.getLanguage().t("invalid-format-integer", "%value%"));
                     return;
                  }

                  maxPlayers = parseInt.getAsInt();
               }

               if (maxPlayers > 10) {
                  sender.sendMessage("§cVocê não pode abrir sua party para mais de 10 jogadores.");
                  return;
               }

               party = sender.getParty() == null ? this.createParty(sender) : sender.getParty();
               if (!party.openParty(maxPlayers)) {
                  sender.sendMessage("§cO valor inserido é menor que o número de membros da sua party.");
               }
               break;
            case "entrar":
               if (args.length < 2) {
                  sender.sendMessage("§eUse §b/" + cmdArgs.getLabel() + " entrar <player>§e para entrar numa party pública.");
                  return;
               }

               if (sender.getParty() != null) {
                  sender.sendMessage("§cVocê não pode entrar nessa party enquanto estiver em outra.");
                  return;
               }

               Member member = CommonPlugin.getInstance().getMemberManager().getMemberByName(args[1]);
               if (member == null) {
                  sender.sendMessage(sender.getLanguage().t("player-is-not-online", "%player%", args[1]));
                  return;
               }

               if (member.getParty() == null) {
                  sender.sendMessage("§cO jogador " + member.getPlayerName() + " não tem uma party.");
                  return;
               }

               if (member.getParty().getPartyPrivacy() == Party.PartyPrivacy.PRIVATE) {
                  sender.sendMessage("§cEsta party é privada, somente jogadores convidados podem entrar.");
                  return;
               }

               member.getParty().addMember(Profile.from(sender));
               sender.setParty(member.getParty());
               break;
            case "teleportar":
            case "warp":
               if (sender.getParty() == null) {
                  sender.sendMessage("§cVocê não tem party.");
                  return;
               }

               if (sender.getParty().hasRole(sender.getUniqueId(), PartyRole.OWNER)) {
                  BungeeMember bungeeMember = (BungeeMember)sender;
                  ServerInfo info = bungeeMember.getProxiedPlayer().getServer().getInfo();
                  sender.getParty().getMembers().stream().map(id -> ProxyServer.getInstance().getPlayer(id)).forEach(player -> {
                     if (player != null) {
                        player.connect(bungeeMember.getProxiedPlayer().getServer().getInfo());
                     }
                  });
                  sender.getParty().sendMessage("§aTodos os jogadores foram levados para a sala " + info.getName() + ".");
               } else {
                  sender.sendMessage("§cSomente o líder da party pode fazer isso.");
               }
               break;
            case "expulsar":
               party = sender.getParty();
               if (party == null) {
                  sender.sendMessage("§cVocê não tem uma party.");
                  return;
               }

               if (!party.hasRole(sender.getUniqueId(), PartyRole.ADMIN)) {
                  sender.sendMessage("§cVocê não tem permissão para expulsar alguém da sua party.");
                  return;
               }

               if (args.length == 2) {
                  sender.sendMessage("§eUse §b/" + cmdArgs.getLabel() + " expulsar <player>§e para expulsar alguém da sua party.");
                  return;
               }

               member = CommonPlugin.getInstance().getMemberManager().getMemberByName(args[1]);
               if (member == null) {
                  sender.sendMessage(sender.getLanguage().t("player-is-not-online", "%player%", args[1]));
                  return;
               }

               if (sender.getUniqueId().equals(member.getUniqueId())) {
                  sender.sendMessage("§cVocê não pode se expulsar da party.");
                  return;
               }

               if (party.hasRole(sender.getUniqueId(), PartyRole.OWNER)) {
                  sender.sendMessage("§cVocê não pode expulsar esse jogador.");
                  return;
               }

               party.kickMember(sender, member);
               break;
            case "acabar":
            case "disband":
               party = sender.getParty();
               if (party == null) {
                  sender.sendMessage("§cVocê não está em uma party.");
                  return;
               }

               if (!party.hasRole(sender.getUniqueId(), PartyRole.OWNER)) {
                  sender.sendMessage("§cVocê não pode acabar com essa party.");
                  return;
               }

               party.disband();
               break;
            case "sair":
               party = sender.getParty();
               if (party == null) {
                  sender.sendMessage("§cVocê não está em uma party.");
                  return;
               }

               if (party.hasRole(sender.getUniqueId(), PartyRole.OWNER)) {
                  sender.sendMessage("§cVocê não pode sair da party que você criou.");
                  sender.sendMessage("§cUse /party disband para sair da party.");
                  return;
               }

               party.removeMember(Profile.from(sender));
               sender.setParty(null);
               break;
            case "accept":
            case "aceitar":
               if (sender.getParty() != null) {
                  sender.sendMessage("§cSaia da sua party para entrar em outra.");
                  return;
               }

               if (!CommonPlugin.getInstance().getPartyManager().getPartyInvitesMap().containsKey(sender.getUniqueId())
                  || CommonPlugin.getInstance().getPartyManager().getPartyInvitesMap().get(sender.getUniqueId()).isEmpty()) {
                  sender.sendMessage("§cVocê não tem nenhum convite de party para ser aceito.");
                  return;
               }

               PartyManager.InviteInfo inviteInfo;
               if (args.length >= 2) {
                  if (CommonPlugin.getInstance().getPartyManager().getPartyInvitesMap().get(sender.getUniqueId()).size() > 1) {
                     sender.sendMessage("§eUse §b/" + cmdArgs.getLabel() + " aceitar <player>§e para aceitar um convite de party.");
                     return;
                  }

                  inviteInfo = CommonPlugin.getInstance()
                     .getPartyManager()
                     .getPartyInvitesMap()
                     .get(sender.getUniqueId())
                     .values()
                     .stream()
                     .findFirst()
                     .orElse(null);
               } else {
                  member = CommonPlugin.getInstance().getMemberManager().getMemberByName(args[1]);
                  if (member == null) {
                     sender.sendMessage(sender.getLanguage().t("player-is-not-online", "%player%", args[1]));
                     return;
                  }

                  inviteInfo = CommonPlugin.getInstance().getPartyManager().getPartyInvitesMap().get(sender.getUniqueId()).get(member.getUniqueId());
                  if (inviteInfo == null) {
                     sender.sendMessage("§cO jogador " + member.getPlayerName() + " não te convidou para party.");
                     return;
                  }
               }

               CommonPlugin.getInstance().getPartyManager().getPartyInvitesMap().remove(sender.getUniqueId());
               if (inviteInfo.getCreatedAt() + 180000L > System.currentTimeMillis()) {
                  party = CommonPlugin.getInstance().getPartyManager().getPartyById(inviteInfo.getPartyId());
                  if (party == null) {
                     sender.sendMessage("§cA party que convidou você não existe mais.");
                     return;
                  }

                  if (party.addMember(Profile.from(sender))) {
                     sender.setParty(party);
                  } else {
                     sender.sendMessage("§cA party está cheia.");
                  }

                  return;
               }

               sender.sendMessage("§cO convite para entrar na party expirou!");
               break;
            case "invite":
            case "convidar":
            default:
               boolean inviteArg = args[0].equalsIgnoreCase("convidar") || args[0].equalsIgnoreCase("invite");
               if (inviteArg && args.length <= 1) {
                  sender.sendMessage("§eUse §b/" + cmdArgs.getLabel() + " convidar <player>§e para convidar algum jogador para sua party.");
                  return;
               }

               member = CommonPlugin.getInstance().getMemberManager().getMemberByName(args[inviteArg ? 1 : 0]);
               if (member == null) {
                  sender.sendMessage(sender.getLanguage().t("player-is-not-online", "%player%", args[inviteArg ? 1 : 0]));
                  return;
               }

               if (sender.getUniqueId().equals(member.getUniqueId())) {
                  sender.sendMessage("§cVocê não pode convidar este jogador.");
                  return;
               }

               if (!member.getMemberConfiguration().isPartyInvites()) {
                  sender.sendMessage("§cO jogador " + member.getPlayerName() + " não está aceitando convites para party.");
                  return;
               }

               if (member.getParty() != null) {
                  sender.sendMessage("§cO jogador " + member.getPlayerName() + " já está em uma party.");
                  return;
               }

               party = sender.getParty() == null ? this.createParty(sender) : sender.getParty();
               if (!party.hasRole(sender.getUniqueId(), PartyRole.ADMIN)) {
                  sender.sendMessage("§cVocê não tem permissão para convidar alguém para sua party.");
                  return;
               }

               Map<UUID, PartyManager.InviteInfo> partyInvites = CommonPlugin.getInstance()
                  .getPartyManager()
                  .getPartyInvitesMap()
                  .computeIfAbsent(member.getUniqueId(), v -> new HashMap());
               if (partyInvites.containsKey(sender.getUniqueId())) {
                  inviteInfo = partyInvites.get(sender.getUniqueId());
                  if (inviteInfo.getCreatedAt() + 180000L > System.currentTimeMillis()) {
                     sender.sendMessage("§cVocê precisa esperar para enviar um novo invite para esse jogador.");
                     return;
                  }
               }

               CommonPlugin.getInstance().getPartyManager().invite(sender.getUniqueId(), member.getUniqueId(), party);
               sender.sendMessage("§aO convite para party foi enviado para " + member.getPlayerName() + ".");
               member.sendMessage("§aVocê foi convidado para party de " + sender.getPlayerName() + ".");
               member.sendMessage(
                       new MessageBuilder("§aClique ").create(),
                       new MessageBuilder("§a§lAQUI")
                          .setHoverEvent("§aClique aqui para aceitar o convite.")
                          .setClickEvent("/party aceitar " + sender.getPlayerName())
                          .create(),
                       new MessageBuilder("§e para aceitar o convite da party.").create());
         }
      }
   }

   public Party createParty(Member member) {
      UUID partyId = CommonPlugin.getInstance().getPartyData().getPartyId();
      Party party = new BungeeParty(partyId, member);
      member.setPartyId(partyId);
      member.sendMessage("§aSua party foi criada.");
      CommonPlugin.getInstance().getPartyData().createParty(party);
      CommonPlugin.getInstance().getPartyManager().loadParty(party);
      return party;
   }
}

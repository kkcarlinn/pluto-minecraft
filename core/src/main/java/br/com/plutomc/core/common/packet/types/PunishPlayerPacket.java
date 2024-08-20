package br.com.plutomc.core.common.packet.types;

import java.util.UUID;

import br.com.plutomc.core.common.CommonPlugin;
import br.com.plutomc.core.common.PluginInfo;
import br.com.plutomc.core.common.packet.Packet;
import br.com.plutomc.core.common.packet.PacketType;
import br.com.plutomc.core.common.punish.Punish;
import br.com.plutomc.core.common.punish.PunishType;
import br.com.plutomc.core.common.utils.DateUtils;
import br.com.plutomc.core.common.member.Member;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class PunishPlayerPacket extends Packet {
   private UUID playerId;
   private Punish punish;

   public PunishPlayerPacket(UUID playerId, Punish punish) {
      super(PacketType.PUNISH_PLAYER);
      this.bungeecord();
      this.playerId = playerId;
      this.punish = punish;
   }

   @Override
   public void receive() {
      Member member = CommonPlugin.getInstance().getMemberManager().getMember(this.playerId);
      if (member == null) {
         member = CommonPlugin.getInstance().getMemberData().loadMember(this.playerId);
         if (member == null) {
            CommonPlugin.getInstance().debug("Could not find member with UUID " + this.playerId.toString());
            return;
         }
      }

      member.getPunishConfiguration().punish(this.punish);
      member.saveConfig();
      ProxiedPlayer player = ProxyServer.getInstance().getPlayer(this.playerId);
      if (player != null && this.punish.getPunishType() == PunishType.BAN) {
         player.disconnect(
            PluginInfo.t(
               member,
               "ban-" + (this.punish.isPermanent() ? "permanent" : "temporary") + "-kick-message",
               "%reason%",
               this.punish.getPunishReason(),
               "%expireAt%",
               DateUtils.getTime(member.getLanguage(), this.punish.getExpireAt()),
               "%punisher%",
               this.punish.getPunisherName(),
               "%website%",
               CommonPlugin.getInstance().getPluginInfo().getWebsite(),
               "%store%",
               CommonPlugin.getInstance().getPluginInfo().getStore(),
               "%discord%",
               CommonPlugin.getInstance().getPluginInfo().getDiscord()
            )
         );
      }
   }

   public UUID getPlayerId() {
      return this.playerId;
   }

   public Punish getPunish() {
      return this.punish;
   }
}

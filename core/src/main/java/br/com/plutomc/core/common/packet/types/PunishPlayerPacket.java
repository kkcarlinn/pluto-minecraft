package br.com.plutomc.core.common.packet.types;

import java.util.UUID;

import br.com.plutomc.core.common.CommonPlugin;
import br.com.plutomc.core.common.PluginInfo;
import br.com.plutomc.core.common.packet.Packet;
import br.com.plutomc.core.common.packet.PacketType;
import br.com.plutomc.core.common.punish.Punish;
import br.com.plutomc.core.common.punish.PunishType;
import br.com.plutomc.core.common.utils.DateUtils;
import br.com.plutomc.core.common.account.Account;
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
      Account account = CommonPlugin.getInstance().getAccountManager().getAccount(this.playerId);
      if (account == null) {
         account = CommonPlugin.getInstance().getAccountData().loadAccount(this.playerId);
         if (account == null) {
            CommonPlugin.getInstance().debug("Could not find member with UUID " + this.playerId.toString());
            return;
         }
      }

      account.getPunishConfiguration().punish(this.punish);
      account.saveConfig();
      ProxiedPlayer player = ProxyServer.getInstance().getPlayer(this.playerId);
      if (player != null && this.punish.getPunishType() == PunishType.BAN) {
         player.disconnect(
            PluginInfo.t(
                    account,
               "ban-" + (this.punish.isPermanent() ? "permanent" : "temporary") + "-kick-message",
               "%reason%",
               this.punish.getPunishReason(),
               "%expireAt%",
               DateUtils.getTime(account.getLanguage(), this.punish.getExpireAt()),
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

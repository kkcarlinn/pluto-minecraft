package br.com.plutomc.core.common.packet.types;

import java.util.UUID;

import br.com.plutomc.core.common.CommonPlugin;
import br.com.plutomc.core.common.member.Member;
import br.com.plutomc.core.common.packet.Packet;
import br.com.plutomc.core.common.packet.PacketType;
import net.md_5.bungee.api.ChatColor;

public class StaffchatBungeePacket extends Packet {
   private UUID playerId;
   private String nickname;
   private String message;

   public StaffchatBungeePacket(UUID playerId, String nickname, String message) {
      super(PacketType.STAFFCHAT_BUNGEE);
      this.playerId = playerId;
      this.nickname = nickname;
      this.message = message;
      this.bungeecord();
   }

   @Override
   public void receive() {
      Member member = CommonPlugin.getInstance().getMemberManager().getMember(this.playerId);
      String staffMessage;
      if (member == null) {
         staffMessage = "§7[StaffChat] " + this.nickname + "§7: §f" + ChatColor.translateAlternateColorCodes('&', this.message);
      } else {
         staffMessage = "§3*§7[StaffChat] "
            + CommonPlugin.getInstance().getPluginInfo().getTagByGroup(member.getServerGroup()).getStrippedColor()
            + " "
            + member.getPlayerName()
            + "§7: §f"
            + ChatColor.translateAlternateColorCodes('&', this.message);
      }

      CommonPlugin.getInstance().getMemberManager().getMembers().stream().filter(Member::isStaff).forEach(m -> m.sendMessage(staffMessage));
   }

   public UUID getPlayerId() {
      return this.playerId;
   }

   public String getNickname() {
      return this.nickname;
   }

   public String getMessage() {
      return this.message;
   }
}

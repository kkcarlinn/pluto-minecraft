package br.com.plutomc.core.common.packet.types;

import java.util.UUID;

import br.com.plutomc.core.common.CommonPlugin;
import br.com.plutomc.core.common.member.Member;
import br.com.plutomc.core.common.packet.Packet;
import br.com.plutomc.core.common.packet.PacketType;
import net.md_5.bungee.api.chat.TextComponent;

public class PlayerMessagePacket extends Packet {
   private UUID uniqueId;
   private TextComponent[] components;

   public PlayerMessagePacket(UUID uniqueId, TextComponent... components) {
      super(PacketType.PLAYER_MESSAGE);
      this.uniqueId = uniqueId;
      this.components = components;
   }

   public PlayerMessagePacket(UUID uniqueId, String message) {
      this(uniqueId, new TextComponent(message));
   }

   @Override
   public void receive() {
      Member member = CommonPlugin.getInstance().getMemberManager().getMember(this.uniqueId);
      if (member != null) {
         member.sendMessage(this.components);
      }
   }

   public UUID getUniqueId() {
      return this.uniqueId;
   }

   public TextComponent[] getComponents() {
      return this.components;
   }
}

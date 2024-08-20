package br.com.plutomc.core.bukkit.utils;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.EnumWrappers.TitleAction;

public class PacketBuilder {
   private PacketContainer packetContainer;

   public PacketBuilder(PacketType packetType) {
      this.packetContainer = new PacketContainer(packetType);
   }

   public PacketBuilder writeTitleAction(int fieldIndex, TitleAction value) {
      this.packetContainer.getTitleActions().write(fieldIndex, value);
      return this;
   }

   public PacketBuilder writeChatComponents(int fieldIndex, WrappedChatComponent value) {
      this.packetContainer.getChatComponents().write(fieldIndex, value);
      return this;
   }

   public PacketBuilder writeInteger(int fieldIndex, int value) {
      this.packetContainer.getIntegers().write(fieldIndex, value);
      return this;
   }

   public PacketContainer build() {
      return this.packetContainer;
   }
}

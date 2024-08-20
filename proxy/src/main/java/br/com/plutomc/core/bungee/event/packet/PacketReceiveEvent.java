package br.com.plutomc.core.bungee.event.packet;

import lombok.NonNull;
import br.com.plutomc.core.common.packet.Packet;
import br.com.plutomc.core.common.packet.PacketType;
import net.md_5.bungee.api.plugin.Event;

public class PacketReceiveEvent extends Event {
   @NonNull
   private final Packet packet;

   public PacketType getPacketType() {
      return this.packet.getPacketType();
   }

   public PacketReceiveEvent(@NonNull Packet packet) {
      if (packet == null) {
         throw new NullPointerException("packet is marked non-null but is null");
      } else {
         this.packet = packet;
      }
   }

   @NonNull
   public Packet getPacket() {
      return this.packet;
   }
}

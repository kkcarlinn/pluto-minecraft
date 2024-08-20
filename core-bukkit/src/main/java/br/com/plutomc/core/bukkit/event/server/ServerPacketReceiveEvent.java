package br.com.plutomc.core.bukkit.event.server;

import br.com.plutomc.core.bukkit.event.NormalEvent;
import br.com.plutomc.core.common.packet.Packet;
import br.com.plutomc.core.common.packet.PacketType;

public class ServerPacketReceiveEvent extends NormalEvent {
   private PacketType packetType;
   private Packet packet;

   public ServerPacketReceiveEvent(PacketType packetType, Packet packet) {
      this.packetType = packetType;
      this.packet = packet;
   }

   public PacketType getPacketType() {
      return this.packetType;
   }

   public Packet getPacket() {
      return this.packet;
   }
}

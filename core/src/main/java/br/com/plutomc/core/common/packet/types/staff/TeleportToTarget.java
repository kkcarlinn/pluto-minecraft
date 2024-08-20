package br.com.plutomc.core.common.packet.types.staff;

import java.util.UUID;

import br.com.plutomc.core.common.packet.Packet;
import br.com.plutomc.core.common.packet.PacketType;

public class TeleportToTarget extends Packet {
   private UUID playerId;
   private UUID targetId;
   private String targetName;

   public TeleportToTarget(UUID playerId, UUID targetId, String targetName) {
      super(PacketType.TELEPORT_TO_TARGET);
      this.bungeecord();
      this.playerId = playerId;
      this.targetId = targetId;
      this.targetName = targetName;
   }

   @Override
   public void receive() {
      System.out.println(12312);
   }

   public UUID getPlayerId() {
      return this.playerId;
   }

   public UUID getTargetId() {
      return this.targetId;
   }

   public String getTargetName() {
      return this.targetName;
   }
}

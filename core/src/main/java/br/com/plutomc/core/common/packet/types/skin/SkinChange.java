package br.com.plutomc.core.common.packet.types.skin;

import java.util.UUID;

import br.com.plutomc.core.common.packet.Packet;
import br.com.plutomc.core.common.packet.PacketType;
import br.com.plutomc.core.common.account.Account;
import br.com.plutomc.core.common.utils.skin.Skin;

public class SkinChange extends Packet {
   private UUID playerId;
   private Skin skin;

   public SkinChange(UUID playerId, Skin skin) {
      super(PacketType.SKIN_CHANGE);
      this.bungeecord();
      this.playerId = playerId;
      this.skin = skin;
   }

   public SkinChange(Account account) {
      super(PacketType.SKIN_CHANGE);
      this.bungeecord();
      this.playerId = account.getUniqueId();
      this.skin = account.getSkin();
   }

   public UUID getPlayerId() {
      return this.playerId;
   }

   public Skin getSkin() {
      return this.skin;
   }
}

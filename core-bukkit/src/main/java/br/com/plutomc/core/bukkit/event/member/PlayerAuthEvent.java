package br.com.plutomc.core.bukkit.event.member;

import br.com.plutomc.core.bukkit.event.PlayerEvent;
import br.com.plutomc.core.common.member.Member;
import org.bukkit.entity.Player;

public class PlayerAuthEvent extends PlayerEvent {
   private Member member;

   public PlayerAuthEvent(Player player, Member member) {
      super(player);
      this.member = member;
   }

   public Member getMember() {
      return this.member;
   }
}

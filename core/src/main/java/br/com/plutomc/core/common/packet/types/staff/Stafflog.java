package br.com.plutomc.core.common.packet.types.staff;

import java.util.Arrays;

import br.com.plutomc.core.common.CommonPlugin;
import br.com.plutomc.core.common.packet.Packet;
import br.com.plutomc.core.common.packet.PacketType;
import br.com.plutomc.core.common.utils.string.MessageBuilder;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;

public class Stafflog extends Packet {
   private String message;
   private String hoverMessage;
   private String clickMessage;
   private boolean anticheat;

   public Stafflog(String message) {
      super(PacketType.STAFFLOG);
      this.bungeecord();
      this.message = message;
      this.hoverMessage = "";
      this.clickMessage = "";
   }

   public Stafflog anticheat() {
      this.anticheat = !this.anticheat;
      return this;
   }

   public Stafflog(TextComponent textComponent) {
      super(PacketType.STAFFLOG);
      this.bungeecord();
      this.message = textComponent.toLegacyText();
      if (textComponent.getHoverEvent() != null && textComponent.getHoverEvent().getValue() != null) {
         this.hoverMessage = Arrays.stream(textComponent.getHoverEvent().getValue())
            .map(xva$0 -> TextComponent.toLegacyText(new BaseComponent[]{xva$0}))
            .reduce("", (a, b) -> a + b);
      } else {
         this.hoverMessage = "";
      }

      if (textComponent.getClickEvent() != null && textComponent.getClickEvent().getValue() != null) {
         this.clickMessage = textComponent.getClickEvent().getValue();
      } else {
         this.clickMessage = "";
      }
   }

   @Override
   public void receive() {
      if (this.anticheat) {
         CommonPlugin.getInstance()
            .getMemberManager()
            .getMembers()
            .stream()
            .filter(member -> member.isStaff() && member.getMemberConfiguration().isAnticheatImportant())
            .forEach(member -> member.sendMessage(new MessageBuilder(this.message).setHoverEvent(this.hoverMessage).setClickEvent(this.clickMessage).create()));
      } else {
         CommonPlugin.getInstance()
            .getMemberManager()
            .staffLog(new MessageBuilder(this.message).setHoverEvent(this.hoverMessage).setClickEvent(this.clickMessage).create());
      }
   }
}

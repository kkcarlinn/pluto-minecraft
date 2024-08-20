package br.com.plutomc.core.common.packet.types;

import br.com.plutomc.core.common.CommonPlugin;
import br.com.plutomc.core.common.packet.Packet;
import br.com.plutomc.core.common.packet.PacketType;
import net.md_5.bungee.api.chat.TextComponent;

public class BroadcastPacket extends Packet {
   private String permission;
   private boolean staff;
   private TextComponent[] components;

   public BroadcastPacket(String permission, boolean staff, TextComponent... components) {
      super(PacketType.BROADCAST);
      this.permission = permission;
      this.staff = staff;
      this.components = components;
   }

   public BroadcastPacket(String permission, boolean staff, String string) {
      this(permission, staff, new TextComponent(string));
   }

   @Override
   public void receive() {
      CommonPlugin.getInstance()
         .getMemberManager()
         .getMembers()
         .stream()
         .filter(member -> this.staff ? member.hasPermission(this.permission) : member.isStaff())
         .forEach(member -> member.sendMessage(this.components));
   }

   public String getPermission() {
      return this.permission;
   }

   public boolean isStaff() {
      return this.staff;
   }

   public TextComponent[] getComponents() {
      return this.components;
   }
}

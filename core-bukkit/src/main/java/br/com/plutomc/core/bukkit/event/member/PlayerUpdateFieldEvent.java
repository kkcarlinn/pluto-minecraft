package br.com.plutomc.core.bukkit.event.member;

import br.com.plutomc.core.bukkit.event.PlayerCancellableEvent;
import br.com.plutomc.core.bukkit.member.BukkitMember;
import org.bukkit.entity.Player;

public class PlayerUpdateFieldEvent extends PlayerCancellableEvent {
   private BukkitMember bukkitMember;
   private String field;
   private Object oldObject;
   private Object object;

   public PlayerUpdateFieldEvent(Player p, BukkitMember player, String field, Object oldObject, Object object) {
      super(p);
      this.bukkitMember = player;
      this.field = field;
      this.oldObject = oldObject;
      this.object = object;
   }

   public BukkitMember getBukkitMember() {
      return this.bukkitMember;
   }

   public String getField() {
      return this.field;
   }

   public Object getOldObject() {
      return this.oldObject;
   }

   public Object getObject() {
      return this.object;
   }

   public void setOldObject(Object oldObject) {
      this.oldObject = oldObject;
   }

   public void setObject(Object object) {
      this.object = object;
   }
}

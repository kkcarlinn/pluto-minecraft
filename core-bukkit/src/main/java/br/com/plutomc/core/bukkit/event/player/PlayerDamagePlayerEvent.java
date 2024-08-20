package br.com.plutomc.core.bukkit.event.player;

import br.com.plutomc.core.bukkit.event.PlayerCancellableEvent;
import org.bukkit.entity.Player;

public class PlayerDamagePlayerEvent extends PlayerCancellableEvent {
   private Player damager;
   private double damage;
   private double finalDamage;

   public PlayerDamagePlayerEvent(Player entity, Player damager, boolean cancelled, double damage, double finalDamage) {
      super(entity);
      this.setCancelled(cancelled);
      this.damager = damager;
      this.damage = damage;
      this.finalDamage = finalDamage;
   }

   public void setDamage(double damage) {
      this.damage = damage;
   }

   public Player getDamager() {
      return this.damager;
   }

   public double getDamage() {
      return this.damage;
   }

   public double getFinalDamage() {
      return this.finalDamage;
   }
}

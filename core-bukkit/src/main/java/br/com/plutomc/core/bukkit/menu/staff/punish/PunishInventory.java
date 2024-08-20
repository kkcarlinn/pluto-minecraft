package br.com.plutomc.core.bukkit.menu.staff.punish;

import br.com.plutomc.core.bukkit.utils.menu.MenuInventory;
import br.com.plutomc.core.common.member.Member;
import br.com.plutomc.core.common.punish.Punish;
import org.bukkit.entity.Player;

public class PunishInventory extends MenuInventory {
   private Player player;
   private Member target;
   private Punish punish;
   private MenuInventory backInventory;

   public PunishInventory(Player player, Member target, Punish punish, MenuInventory backInventory) {
      super("§7Punição " + target.getName(), 3);
      this.player = player;
      this.target = target;
      this.punish = punish;
      this.backInventory = backInventory;
      this.open(player);
   }

   public void handleItems() {
   }

   public Player getPlayer() {
      return this.player;
   }

   public Member getTarget() {
      return this.target;
   }

   public Punish getPunish() {
      return this.punish;
   }

   public MenuInventory getBackInventory() {
      return this.backInventory;
   }
}

package br.com.plutomc.core.bukkit.menu.staff.punish;

import java.util.List;

import br.com.plutomc.core.bukkit.utils.item.ItemBuilder;
import br.com.plutomc.core.bukkit.utils.menu.MenuInventory;
import br.com.plutomc.core.common.member.Member;
import br.com.plutomc.core.common.punish.PunishType;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class PunishInfoInventory extends MenuInventory {
   public PunishInfoInventory(Player player, Member target) {
      super("§7Punições " + target.getName(), 3);
      this.setItem(
         10,
         new ItemBuilder()
            .name("§a" + target.getName())
            .lore("§7Total de punições: §a" + target.getPunishConfiguration().getPunishMap().values().stream().mapToInt(List::size).sum())
            .type(Material.SKULL_ITEM)
            .durability(3)
            .skin(target.getName())
            .build()
      );
      this.setItem(
         11,
         new ItemBuilder().name("§aTodos os banimentos").lore("§7Clique para Listar banimentos").type(Material.BOOK).build(),
         (p, inv, type, stack, slot) -> new PunishInfoListInventory(player, target, PunishType.BAN, 1, this)
      );
      this.setItem(
         12,
         new ItemBuilder().name("§aTodos os mutes").lore("§7Clique para Listar mutes").type(Material.BOOK).build(),
         (p, inv, type, stack, slot) -> new PunishInfoListInventory(player, target, PunishType.MUTE, 1, this)
      );
      this.setItem(
         13,
         new ItemBuilder().name("§aTodos os kicks").lore("§7Clique para Listar kicks").type(Material.BOOK).build(),
         (p, inv, type, stack, slot) -> new PunishInfoListInventory(player, target, PunishType.KICK, 1, this)
      );
      this.open(player);
   }
}

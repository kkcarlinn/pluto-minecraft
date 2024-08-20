package br.com.plutomc.core.bukkit.menu.group;

import java.util.List;

import br.com.plutomc.core.bukkit.utils.item.ItemBuilder;
import br.com.plutomc.core.bukkit.utils.menu.MenuInventory;
import br.com.plutomc.core.bukkit.utils.menu.MenuItem;
import br.com.plutomc.core.common.member.Member;
import br.com.plutomc.core.common.permission.Group;
import br.com.plutomc.core.common.utils.string.StringFormat;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class MemberGroupInventory {
   public MemberGroupInventory(Player player, Member target, Group group, List<MenuItem> items, int page) {
      MenuInventory menuInventory = new MenuInventory("§7" + target.getName(), 3);
      menuInventory.setItem(10, new ItemBuilder().name("§a" + target.getName()).type(Material.SKULL_ITEM).durability(3).skin(target.getName()).build());
      menuInventory.setItem(
         11, new ItemBuilder().name("§eTodos os grupos").lore("§7Clique para ver todos os grupos desse jogador.").type(Material.PAPER).build()
      );
      menuInventory.setItem(
         12, new ItemBuilder().name("§eTodos os grupos").lore("§7Clique para ver todas as permissões desse jogador.").type(Material.BOOK).build()
      );
      if (group.isDefaultGroup()) {
         menuInventory.setItem(
            15,
            new ItemBuilder()
               .name("§cRemover " + StringFormat.formatString(group.getGroupName()))
               .lore("§7Remova o grupo da conta desse jogador.")
               .type(Material.BARRIER)
               .build(),
            (p, inv, type, stack, slot) -> p.sendMessage("§cO grupo não pode ser removido!")
         );
      } else {
         menuInventory.setItem(
            15,
            new ItemBuilder()
               .name("§cRemover " + StringFormat.formatString(group.getGroupName()))
               .lore("§7Remova o grupo da conta desse jogador.")
               .type(Material.BARRIER)
               .build(),
            (p, inv, type, stack, slot) -> p.sendMessage("§cUse /group " + target.getName() + " remove " + group.getGroupName() + " para remover esse cargo.")
         );
      }

      menuInventory.setItem(
         16,
         new ItemBuilder().name("§7Voltar").type(Material.ARROW).build(),
         (p, inv, type, stack, slot) -> new MemberGroupListInventory(player, group, items, page)
      );
      menuInventory.open(player);
   }
}

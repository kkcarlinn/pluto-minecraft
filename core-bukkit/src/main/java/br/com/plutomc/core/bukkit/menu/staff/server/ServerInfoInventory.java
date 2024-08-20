package br.com.plutomc.core.bukkit.menu.staff.server;

import br.com.plutomc.core.bukkit.utils.item.ItemBuilder;
import br.com.plutomc.core.bukkit.utils.menu.MenuInventory;
import br.com.plutomc.core.common.server.loadbalancer.server.ProxiedServer;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class ServerInfoInventory extends MenuInventory {
   public ServerInfoInventory(Player player, ProxiedServer server, MenuInventory backInventory) {
      super("§7" + server.getServerId(), 3);
      this.setItem(10, new ItemBuilder().name("§a" + server.getServerId()).type(Material.BOOK).build());
      this.setItem(
         11,
         new ItemBuilder().name("§aAlterar detalhes").type(Material.PAPER).build(),
         (p, inv, type, stack, slot) -> new ServerDetailsInventory(player, server, this)
      );
      this.setItem(
         12,
         new ItemBuilder().name("§aExecutar ação").type(Material.IRON_CHESTPLATE).build(),
         (p, inv, type, stack, slot) -> new ServerActionsInventory(player, server, this)
      );
      this.setItem(
         13,
         new ItemBuilder().name("§aListar jogadores").type(Material.NAME_TAG).build(),
         (p, inv, type, stack, slot) -> new ServerPlayerListInventory(player, server, 1, this)
      );
      this.setItem(
         16,
         new ItemBuilder().name("§aVoltar").lore("§7Voltar para " + backInventory.getTitle()).type(Material.ARROW).build(),
         (p, inv, type, stack, slot) -> backInventory.open(player)
      );
      this.open(player);
   }
}

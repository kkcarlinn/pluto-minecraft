package br.com.plutomc.core.bukkit.menu.staff.server;

import br.com.plutomc.core.bukkit.utils.item.ItemBuilder;
import br.com.plutomc.core.bukkit.utils.menu.MenuInventory;
import br.com.plutomc.core.common.server.loadbalancer.server.ProxiedServer;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class ServerActionsInventory extends MenuInventory {
   public ServerActionsInventory(Player player, ProxiedServer server, MenuInventory backInventory) {
      super(server.getServerId(), 6);
      this.setItem(10, new ItemBuilder().type(Material.PAPER).name("§aDesligar servidor.").build());
      this.setItem(11, new ItemBuilder().type(Material.PAPER).name("§aVerificar atualização.").build());
      this.setItem(12, new ItemBuilder().type(Material.PAPER).name(server.isJoinEnabled() ? "§cDesativar jogadores." : "§aAtivar jogadores.").build());
      this.setItem(13, new ItemBuilder().type(Material.PAPER).name("§aExecutar comando.").build());
      this.setItem(14, new ItemBuilder().type(Material.PAPER).name("§aListar jogadores.").build());
      this.setItem(
         48,
         new ItemBuilder().name("§aVoltar").type(Material.ARROW).lore("§7Voltar para " + backInventory.getTitle()).build(),
         (p, inv, type, stack, slot) -> backInventory.open(player)
      );
      this.open(player);
   }
}

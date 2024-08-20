package br.com.plutomc.core.bukkit.menu.staff.server;

import br.com.plutomc.core.bukkit.BukkitCommon;
import br.com.plutomc.core.bukkit.utils.item.ItemBuilder;
import br.com.plutomc.core.bukkit.utils.menu.MenuInventory;
import br.com.plutomc.core.bukkit.manager.ChatManager;
import br.com.plutomc.core.common.CommonPlugin;
import br.com.plutomc.core.common.server.loadbalancer.server.ProxiedServer;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class ServerDetailsInventory extends MenuInventory {
   public ServerDetailsInventory(Player player, ProxiedServer server, MenuInventory backInventory) {
      super("§7Detalhes", 3);
      this.setItem(10, new ItemBuilder().name("§a" + server.getServerId()).type(Material.BOOK).build());
      this.setItem(
         11,
         new ItemBuilder().name("§eAlterar nome").lore("§7Clique para alterar o nome do servidor.").type(Material.BOOK).build(),
         (p, inv, type, stack, slot) -> {
            p.closeInventory();
            BukkitCommon.getInstance()
               .getChatManager()
               .loadChat(CommonPlugin.getInstance().getMemberManager().getMember(player.getUniqueId()), new ChatManager.Callback() {
                  @Override
                  public void callback(boolean cancel, String... asks) {
                     if (cancel) {
                        ServerDetailsInventory.this.open(player);
                     } else {
                        player.sendMessage("§aNome do servidor alterado de " + server.getServerId() + " para " + asks[0] + ".");
                        new ServerDetailsInventory(player, server, backInventory);
                     }
                  }
               }, "§aDigite o novo nome do servidor para altera-ló.");
         }
      );
      this.setItem(
         12,
         new ItemBuilder().name("§eAlterar tipo").lore("§7Clique para alterar o tipo do servidor.").type(Material.BOOK).build(),
         (p, inv, type, stack, slot) -> {
            player.sendMessage("§aO tipo do servidor foi alterado para " + server.getServerType().name() + ".");
            new ServerDetailsInventory(player, server, backInventory);
         }
      );
      this.setItem(
         16,
         new ItemBuilder().name("§aVoltar").type(Material.ARROW).lore("§7Voltar para " + backInventory.getTitle()).build(),
         (p, inv, type, stack, slot) -> backInventory.open(player)
      );
      this.open(player);
   }
}

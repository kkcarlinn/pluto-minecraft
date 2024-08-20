package br.com.plutomc.lobby.lobbyhost.menu;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import br.com.plutomc.core.common.CommonPlugin;
import br.com.plutomc.core.bukkit.BukkitCommon;
import br.com.plutomc.core.bukkit.event.server.ServerEvent;
import br.com.plutomc.lobby.lobbyhost.LobbyHost;
import br.com.plutomc.core.bukkit.utils.item.ItemBuilder;
import br.com.plutomc.core.bukkit.utils.menu.MenuInventory;
import br.com.plutomc.core.common.server.loadbalancer.server.ProxiedServer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

public class LobbyInventory {
   private static final MenuInventory MENU_INVENTORY = new MenuInventory("§7Lobbys", 3);
   private static final Map<String, Integer> SERVER_MAP = new HashMap<>();

   public LobbyInventory(Player player) {
      MENU_INVENTORY.open(player);
   }

   static {
      Bukkit.getPluginManager()
         .registerEvents(
            new Listener() {
               @EventHandler
               public void onServer(ServerEvent event) {
                  if (event.getServerType() == CommonPlugin.getInstance().getServerType()) {
                     switch(event.getAction()) {
                        case STOP:
                        case START:
                           LobbyInventory.MENU_INVENTORY.clear();
                           List<ProxiedServer> serverList = BukkitCommon.getInstance()
                              .getServerManager()
                              .getBalancer(CommonPlugin.getInstance().getServerType())
                              .getList()
                              .stream()
                              .sorted((s1, s2) -> s1.getServerId().compareTo(s2.getServerId()))
                              .collect(Collectors.toList());
      
                           for(int i = 0; i < serverList.size(); ++i) {
                              ProxiedServer server = serverList.get(i);
                              int slotx = 11 + i;
                              LobbyInventory.MENU_INVENTORY
                                 .setItem(
                                    slotx,
                                    new ItemBuilder()
                                       .name("§aLobby #" + LobbyHost.getInstance().getServerId(server.getServerId()))
                                       .lore("§7" + server.getOnlinePlayers() + " jogando.")
                                       .type(Material.STAINED_GLASS_PANE)
                                       .durability(5)
                                       .build(),
                                    (player, inventory, clickType, itemStackx, index) -> {
                                       if (player.hasPermission("lobby.join")) {
                                          BukkitCommon.getInstance().sendPlayerToServer(player, server.getServerId());
                                       } else {
                                          player.sendMessage("§cSomente jogadores pagantes podem transitar livremente entre os lobbies.");
                                       }
                                    }
                                 );
                              LobbyInventory.SERVER_MAP.put(server.getServerId(), slotx);
                           }
                           break;
                        case JOIN:
                        case LEAVE:
                           int slot = LobbyInventory.SERVER_MAP.get(event.getServerId());
                           ItemStack itemStack = LobbyInventory.MENU_INVENTORY.getInventory().getItem(slot);
                           ItemBuilder itemBuilder = ItemBuilder.fromStack(itemStack);
                           itemStack.setItemMeta(
                              itemBuilder.clearLore().lore("§7" + event.getProxiedServer().getOnlinePlayers() + " jogando.").build().getItemMeta()
                           );
                     }
                  }
               }
            },
            BukkitCommon.getInstance()
         );
      List<ProxiedServer> serverList = BukkitCommon.getInstance().getServerManager().getBalancer(CommonPlugin.getInstance().getServerType()).getList();

      for(int i = 0; i < serverList.size(); ++i) {
         ProxiedServer server = serverList.get(i);
         int slot = 11 + i;
         MENU_INVENTORY.setItem(
            slot,
            new ItemBuilder()
               .name("§aLobby #" + (i + 1))
               .lore("§7" + server.getOnlinePlayers() + " jogando.")
               .type(Material.STAINED_GLASS_PANE)
               .durability(5)
               .build(),
            (p, inv, type, stack, s) -> BukkitCommon.getInstance().sendPlayerToServer(p, server.getServerId())
         );
         SERVER_MAP.put(server.getServerId(), slot);
      }
   }
}

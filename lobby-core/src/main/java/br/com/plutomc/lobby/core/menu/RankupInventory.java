package br.com.plutomc.lobby.core.menu;

import java.util.HashSet;
import br.com.plutomc.core.common.CommonPlugin;
import br.com.plutomc.core.common.backend.data.DataServerMessage;
import br.com.plutomc.core.bukkit.BukkitCommon;
import br.com.plutomc.lobby.core.CoreMain;
import br.com.plutomc.lobby.core.server.ServerWatcher;
import br.com.plutomc.core.bukkit.utils.item.ItemBuilder;
import br.com.plutomc.core.bukkit.utils.menu.MenuInventory;
import br.com.plutomc.core.common.server.ServerType;
import br.com.plutomc.core.common.server.loadbalancer.server.ProxiedServer;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class RankupInventory {
   private static final MenuInventory MENU_INVENTORY = new MenuInventory("§7Rankup", 3);

   public RankupInventory(Player player) {
      MENU_INVENTORY.open(player);
   }

   private static void createRankup(RankupType rankupType, ProxiedServer server, DataServerMessage.Action action) {
      int slot = 11 + rankupType.ordinal() * 2;
      ItemBuilder itemBuilder = new ItemBuilder();
      itemBuilder.name("§%server.selector.rankup-server." + rankupType.name().toLowerCase() + "-name%§");
      itemBuilder.type(rankupType.getType());
      itemBuilder.lore("", "§7§%server.selector.rankup-server." + rankupType.name().toLowerCase() + "-description%§", "");
      switch(action) {
         case STOP:
            itemBuilder.lore("§cO servidor está indisponível no momento.");
            break;
         case JOIN_ENABLE:
            if (!server.isJoinEnabled()) {
               itemBuilder.lore("§cO servidor está em manutenção.");
               break;
            }
         default:
            itemBuilder.lore("§7" + server.getOnlinePlayers() + " jogando.");
      }

      MENU_INVENTORY.setItem(slot, itemBuilder.build(), (p, inv, type, stack, s) -> {
         if (server == null) {
            p.sendMessage("§cO servidor não está disponível no momento.");
         } else {
            if (!server.canBeSelected()) {
               if (server.isFull() && !p.hasPermission("server.full")) {
                  p.sendMessage("§cO servidor está cheio.");
                  return;
               }

               if (!server.isJoinEnabled() && !p.hasPermission("command.admin")) {
                  p.sendMessage("§cO servidor está em manutenção, estamos trabalhando para sua diversão.");
                  return;
               }
            }

            BukkitCommon.getInstance().sendPlayerToServer(p, server.getServerId());
         }
      });
   }

   static {
      CoreMain.getInstance().getServerWatcherManager().watch((new ServerWatcher() {
         @Override
         public void onServerUpdate(ProxiedServer server, DataServerMessage<?> data) {
            String serverId = data.getSource();
            RankupType rankupType = RankupType.getByServerId(serverId);
            if (rankupType == null) {
               CommonPlugin.getInstance().debug("The rankup type " + serverId + " not found " + data.getSource());
            } else {
               RankupInventory.createRankup(rankupType, server, data.getAction());
            }
         }
      }).server(ServerType.RANKUP));

      for(RankupType rankupType : RankupType.values()) {
         createRankup(
            rankupType,
            new ProxiedServer(
               rankupType.name().toLowerCase() + CommonPlugin.getInstance().getPluginInfo().getIp(), ServerType.RANKUP, new HashSet<>(), 80, false
            ),
            DataServerMessage.Action.START
         );
      }

      CoreMain.getInstance().getServerManager().getBalancer(ServerType.RANKUP).getList().forEach(server -> {
         RankupType rankupTypex = RankupType.getByServerId(server.getServerId());
         if (rankupTypex == null) {
            CommonPlugin.getInstance().debug("The rankup type " + rankupTypex + " not found");
         } else {
            createRankup(rankupTypex, server, DataServerMessage.Action.START);
         }
      });
   }

   public static enum RankupType {
      MITOLOGIC(Material.IRON_PICKAXE),
      ATLANTIC(Material.DIAMOND_PICKAXE),
      DARKNESS(Material.GOLD_PICKAXE);

      private Material type;

      public static RankupType getByServerId(String serverId) {
         for(RankupType type : values()) {
            if (serverId.toLowerCase().contains(type.name().toLowerCase())) {
               return type;
            }
         }

         return null;
      }

      private RankupType(Material type) {
         this.type = type;
      }

      public Material getType() {
         return this.type;
      }
   }
}

package br.com.plutomc.lobby.core.menu;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import br.com.plutomc.core.common.backend.data.DataServerMessage;
import br.com.plutomc.core.bukkit.BukkitCommon;
import br.com.plutomc.core.bukkit.event.server.ServerEvent;
import br.com.plutomc.core.bukkit.utils.item.ItemBuilder;
import br.com.plutomc.core.bukkit.utils.menu.MenuInventory;
import br.com.plutomc.core.common.server.ServerType;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

public class ServerInventory {
   private static final MenuInventory MENU_INVENTORY = new MenuInventory("§7§nEscolha um Modo de Jogo", 3);
   private static final Map<String, List<ServerType>> SERVER_MAP = new HashMap<>();

   public ServerInventory(Player player) {
      MENU_INVENTORY.open(player);
   }

   static {
      SERVER_MAP.put("bedwars", Arrays.asList(ServerType.values()).stream().filter(type -> type.name().contains("BW")).collect(Collectors.toList()));
      SERVER_MAP.put("skywars", Arrays.asList(ServerType.values()).stream().filter(type -> type.name().contains("SW")).collect(Collectors.toList()));
      SERVER_MAP.put("pvp", Arrays.asList(ServerType.ARENA, ServerType.LAVA, ServerType.FPS, ServerType.PVP_LOBBY));
      SERVER_MAP.put("hardcoregames", Arrays.asList(ServerType.HG, ServerType.HG_LOBBY));
      SERVER_MAP.put("duels", Arrays.asList(ServerType.values()).stream().filter(type -> type.name().contains("DUELS")).collect(Collectors.toList()));
      Bukkit.getPluginManager()
         .registerEvents(
            new Listener() {
               @EventHandler
               public void onServer(ServerEvent event) {
                  if (event.getAction() != DataServerMessage.Action.JOIN_ENABLE) {
                     ServerType serverType = event.getServerType();
                     String name = null;
                     List<ServerType> types = new ArrayList<>();
                     if (serverType.name().contains("BW")) {
                        name = "bedwars";
                     } else if (serverType.name().contains("SW")) {
                        name = "skywars";
                     } else if (serverType.isPvP()) {
                        name = "pvp";
                     } else if (serverType.isHG()) {
                        name = "hardcoregames";
                     } else if (serverType.name().contains("DUELS")) {
                        name = "duels";
                     }
      
                     if (name != null) {
                        types.addAll(ServerInventory.SERVER_MAP.get(name));
                        ItemStack itemStack = null;
      
                        for(int i = 11; i < ServerInventory.MENU_INVENTORY.getInventory().getContents().length; ++i) {
                           ItemStack item = ServerInventory.MENU_INVENTORY.getInventory().getContents()[i];
                           if (item != null && item.hasItemMeta() && item.getItemMeta().getDisplayName().toLowerCase().contains(name.toLowerCase())) {
                              itemStack = item;
                              break;
                           }
                        }
      
                        if (itemStack == null) {
                           return;
                        }
      
                        ItemBuilder itemBuilder = ItemBuilder.fromStack(itemStack);
                        itemStack.setItemMeta(
                           itemBuilder.clearLore()
                              .lore("§7" + BukkitCommon.getInstance().getServerManager().getTotalNumber(types) + " jogando.")
                              .build()
                              .getItemMeta()
                        );
                     }
                  }
               }
            },
            BukkitCommon.getInstance()
         );
      MENU_INVENTORY.setItem(
         11,
         new ItemBuilder()
            .name("§aPvP")
            .type(Material.IRON_CHESTPLATE)
            .lore("§7" + BukkitCommon.getInstance().getServerManager().getTotalNumber(SERVER_MAP.get("pvp")) + " jogando.")
            .build(),
         (p, inv, type, stack, slot) -> BukkitCommon.getInstance().sendPlayerToServer(p, ServerType.PVP_LOBBY)
      );
      MENU_INVENTORY.setItem(
         12,
         new ItemBuilder()
            .name("§aHardcoreGames")
            .type(Material.MUSHROOM_SOUP)
            .lore("§7" + BukkitCommon.getInstance().getServerManager().getTotalNumber(SERVER_MAP.get("hardcoregames")) + " jogando.")
            .build(),
         (p, inv, type, stack, slot) -> {
            BukkitCommon.getInstance().sendPlayerToServer(p, ServerType.HG_LOBBY);
            p.closeInventory();
         }
      );
      MENU_INVENTORY.setItem(
         13,
         new ItemBuilder()
            .name("§aSkyWars")
            .type(Material.EYE_OF_ENDER)
            .lore("§7" + BukkitCommon.getInstance().getServerManager().getTotalNumber(SERVER_MAP.get("skywars")) + " jogando.")
            .build(),
         (p, inv, type, stack, slot) -> {
            BukkitCommon.getInstance().sendPlayerToServer(p, ServerType.SW_LOBBY);
            p.closeInventory();
         }
      );
      MENU_INVENTORY.setItem(
         14,
         new ItemBuilder()
            .name("§aBedWars")
            .type(Material.BED)
            .lore("§7" + BukkitCommon.getInstance().getServerManager().getTotalNumber(SERVER_MAP.get("bedwars")) + " jogando.")
            .build(),
         (p, inv, type, stack, slot) -> {
            BukkitCommon.getInstance().sendPlayerToServer(p, ServerType.BW_LOBBY);
            p.closeInventory();
         }
      );
      MENU_INVENTORY.setItem(
         15,
         new ItemBuilder()
            .name("§aDuels")
            .type(Material.DIAMOND_SWORD)
            .lore("§7" + BukkitCommon.getInstance().getServerManager().getTotalNumber(SERVER_MAP.get("duels")) + " jogando.")
            .build(),
         (p, inv, type, stack, slot) -> {
            BukkitCommon.getInstance().sendPlayerToServer(p, ServerType.DUELS_LOBBY);
            p.closeInventory();
         }
      );
   }
}

package br.com.plutomc.core.bukkit.menu.staff.server;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import br.com.plutomc.core.bukkit.utils.item.ItemBuilder;
import br.com.plutomc.core.bukkit.utils.menu.MenuInventory;
import br.com.plutomc.core.bukkit.utils.menu.MenuItem;
import br.com.plutomc.core.common.CommonPlugin;
import br.com.plutomc.core.common.account.Account;
import br.com.plutomc.core.common.server.loadbalancer.server.ProxiedServer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ServerPlayerListInventory extends MenuInventory {
   public ServerPlayerListInventory(Player player, ProxiedServer server, int page, MenuInventory backInventory) {
      super("§7Lista de jogadores", 5);
      List<PlayerInfo> items = new ArrayList<>();

      for(UUID playerId : server.getPlayers()) {
         items.add(new PlayerInfo(playerId, CommonPlugin.getInstance().getAccountManager().getAccount(playerId)));
      }

      int pageStart = 0;
      int pageEnd = 21;
      if (page > 1) {
         pageStart = (page - 1) * 21;
         pageEnd = page * 21;
      }

      if (pageEnd > items.size()) {
         pageEnd = items.size();
      }

      int w = 10;

      for(int i = pageStart; i < pageEnd; ++i) {
         PlayerInfo playerInfo = items.get(i);
         playerInfo.setSlot(w);
         this.setItem(w, this.createItem(playerInfo));
         if (w % 9 == 7) {
            w += 3;
         } else {
            ++w;
         }
      }

      if (page == 1) {
         this.setItem(new MenuItem(new ItemBuilder().type(Material.ARROW).name("§aVoltar").build(), (p, inv, type, stack, s) -> backInventory.open(p)), 39);
      } else {
         this.setItem(
            new MenuItem(
               new ItemBuilder().type(Material.ARROW).name("§a§%page%§ " + (page - 1)).build(),
               (p, inv, type, stack, s) -> new ServerPlayerListInventory(player, server, page - 1, backInventory)
            ),
            39
         );
      }

      if (Math.ceil((double)(items.size() / 21)) + 1.0 > (double)page) {
         this.setItem(
            new MenuItem(
               new ItemBuilder().type(Material.ARROW).name("§a§%page%§ " + (page + 1)).build(),
               (p, inventory, clickType, item, slot) -> new ServerPlayerListInventory(player, server, page + 1, backInventory)
            ),
            41
         );
      } else {
         this.removeItem(41);
      }

      this.open(player);
   }

   private ItemStack createItem(PlayerInfo playerInfo) {
      ItemBuilder itemBuilder = new ItemBuilder()
         .name(playerInfo.getMember() == null ? "§e" + playerInfo.getPlayerId() : "§a" + playerInfo.getMember().getName())
         .type(Material.SKULL_ITEM)
         .durability(3);
      if (playerInfo.getMember() != null) {
         itemBuilder.skin(playerInfo.getMember().getName());
      }

      return itemBuilder.build();
   }

   public class PlayerInfo {
      private UUID playerId;
      private Account account;
      private int slot;
      private boolean has;

      public PlayerInfo(UUID playerId, Account account) {
         this.playerId = playerId;
         this.account = account;
      }

      public void setSlot(int slot) {
         this.slot = slot;
         this.has = true;
      }

      public void load() {
         if (this.has) {
            ServerPlayerListInventory.this.setItem(this.slot, ServerPlayerListInventory.this.createItem(this));
         }
      }

      public UUID getPlayerId() {
         return this.playerId;
      }

      public Account getMember() {
         return this.account;
      }

      public int getSlot() {
         return this.slot;
      }

      public boolean isHas() {
         return this.has;
      }
   }
}

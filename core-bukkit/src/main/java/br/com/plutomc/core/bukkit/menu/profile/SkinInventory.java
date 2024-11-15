package br.com.plutomc.core.bukkit.menu.profile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import br.com.plutomc.core.bukkit.utils.item.ItemBuilder;
import br.com.plutomc.core.bukkit.utils.menu.MenuInventory;
import br.com.plutomc.core.bukkit.utils.menu.MenuItem;
import br.com.plutomc.core.bukkit.utils.player.PlayerAPI;
import br.com.plutomc.core.common.CommonConst;
import br.com.plutomc.core.common.CommonPlugin;
import br.com.plutomc.core.common.account.Account;
import br.com.plutomc.core.common.packet.types.skin.SkinChange;
import br.com.plutomc.core.common.utils.skin.Skin;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class SkinInventory {
   private static final int ITEMS_PER_PAGE = 21;
   private static final List<SkinModel> SKIN_LIST = Arrays.asList(
      from("unidade", "Unidade"),
      from("reboting", "DaddyReboting"),
      from("luqinha", "Luqinha")
   );

   public SkinInventory(Player player) {
      this(player, InventoryType.PRINCIPAL, 1);
   }

   public SkinInventory(Player player, InventoryType type, int page) {
      Account account = CommonPlugin.getInstance().getAccountManager().getAccount(player.getUniqueId());
      MenuInventory menuInventory = new MenuInventory("§7Catálogo de skins", 5);
      if (type == InventoryType.PRINCIPAL) {
         menuInventory.setItem(
            13,
            new ItemBuilder()
               .name("§a" + account.getSkin().getPlayerName())
               .type(Material.SKULL_ITEM)
               .lore("", "§7Fonte: §a" + (account.isCustomSkin() ? "Customizada" : "Padrão"))
               .durability(3)
               .skin(account.getPlayerName())
               .build()
         );
         menuInventory.setItem(
            30,
            new ItemBuilder()
               .name("§aCostumizar sua skin")
               .type(Material.NAME_TAG)
               .lore("", "§7Escolha uma skin customizada", "§7baseada em um nickname.", "", "§cApenas para VIPs.", "", "§eClique para ver mais.")
               .build(),
            (p, inv, t, stack, slot) -> {
               p.closeInventory();
               p.performCommand("skin");
            }
         );
         menuInventory.setItem(
            32,
            new ItemBuilder()
               .name("§aBiblioteca")
               .type(Material.BOOK)
               .lore("", "§7Confira o pacote de ", "§7skins padrão disponíveis", "§7de graça.", "", "§eClique para ver mais.")
               .build(),
            (p, inv, t, stack, slot) -> new SkinInventory(player, InventoryType.LIBRARY, 1)
         );
         menuInventory.setItem(40, new ItemBuilder().name("§aVoltar").type(Material.ARROW).build(), (p, inv, t, stack, slot) -> new ProfileInventory(player));
      } else {
         List<MenuItem> items = new ArrayList<>();

         for(SkinModel skinModel : SKIN_LIST) {
            ItemBuilder itemBuilder = new ItemBuilder()
               .name("§a" + skinModel.getName())
               .type(Material.SKULL_ITEM)
               .durability(3)
               .lore("§eClique para selecionar.");
            if (skinModel.getSkin() != null && skinModel.getSkin().getValue() != null) {
               itemBuilder.skin(skinModel.getSkin().getValue(), skinModel.getSkin().getSignature() == null ? "" : skinModel.getSkin().getSignature());
            } else {
               itemBuilder.skin(skinModel.getSkin().getPlayerName());
            }

            items.add(new MenuItem(itemBuilder.build(), (p, inv, t, stack, slot) -> {
               player.closeInventory();
               PlayerAPI.changePlayerSkin(player, skinModel.getSkin().getValue(), skinModel.getSkin().getSignature(), true);
               account.setSkin(skinModel.getSkin(), true);
               CommonPlugin.getInstance().getServerData().sendPacket(new SkinChange(p.getUniqueId(), account.getSkin()));
            }));
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
            MenuItem item = items.get(i);
            menuInventory.setItem(item, w);
            if (w % 9 == 7) {
               w += 3;
            } else {
               ++w;
            }
         }

         if (page == 1) {
            menuInventory.setItem(
               new MenuItem(new ItemBuilder().type(Material.ARROW).name("§aVoltar").build(), (p, inv, t, stack, s) -> new SkinInventory(player)), 39
            );
         } else {
            menuInventory.setItem(
               new MenuItem(
                  new ItemBuilder().type(Material.ARROW).name("§a§%page%§ " + (page - 1)).build(),
                  (p, inv, t, stack, s) -> new SkinInventory(p, type, page - 1)
               ),
               39
            );
         }

         if (Math.ceil((double)(items.size() / 21)) + 1.0 > (double)page) {
            menuInventory.setItem(
               new MenuItem(
                  new ItemBuilder().type(Material.ARROW).name("§a§%page%§ " + (page + 1)).build(),
                  (p, inv, t, stack, s) -> new SkinInventory(p, type, page + 1)
               ),
               41
            );
         }
      }

      menuInventory.open(player);
   }

   public static SkinModel from(String name, String displayName) {
      return new SkinModel(
         displayName, CommonPlugin.getInstance().getSkinData().loadData(name).orElse(new Skin(name, CommonConst.CONSOLE_ID, "", ""))
      );
   }

   public static SkinModel from(String name) {
      return from(name, name);
   }

   public static enum InventoryType {
      PRINCIPAL,
      LIBRARY;
   }

   public static class SkinModel {
      private String name;
      private Skin skin;

      public SkinModel(String name, Skin skin) {
         this.name = name;
         this.skin = skin;
      }

      public String getName() {
         return this.name;
      }

      public Skin getSkin() {
         return this.skin;
      }
   }
}

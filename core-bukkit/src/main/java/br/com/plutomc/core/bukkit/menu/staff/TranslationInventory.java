package br.com.plutomc.core.bukkit.menu.staff;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import br.com.plutomc.core.bukkit.utils.item.ItemBuilder;
import br.com.plutomc.core.bukkit.utils.menu.MenuInventory;
import br.com.plutomc.core.bukkit.utils.menu.MenuItem;
import br.com.plutomc.core.common.CommonPlugin;
import br.com.plutomc.core.common.language.Language;
import br.com.plutomc.core.common.account.Account;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class TranslationInventory {
   private int itemsPerPage = 21;

   public TranslationInventory(Player player, Language language, int page) {
      MenuInventory menuInventory = new MenuInventory(
         Account.getLanguage(player.getUniqueId()).t("staff.inventory-translation", "%page%", page + "", "%language%", language.getLanguageName()), 5
      );
      List<MenuItem> items = new ArrayList<>();

      for(Entry<String, String> skin : CommonPlugin.getInstance()
         .getPluginInfo()
         .getLanguageMap()
         .get(language)
         .entrySet()
         .stream()
         .sorted((o1, o2) -> o1.getKey().compareTo(o2.getKey()))
         .collect(Collectors.toList())) {
         items.add(
            new MenuItem(
               new ItemBuilder()
                  .name("§a" + (String)skin.getKey())
                  .lore("\n§7" + (String)skin.getValue() + "\n§a\n§aClique para alterar.")
                  .type(Material.PAPER)
                  .build(),
               (p, inv, type, stack, s) -> {
                  p.closeInventory();
                  p.sendMessage("teste");
               }
            )
         );
      }

      int pageStart = 0;
      int pageEnd = this.itemsPerPage;
      if (page > 1) {
         pageStart = (page - 1) * this.itemsPerPage;
         pageEnd = page * this.itemsPerPage;
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
            39, new ItemBuilder().name("§a§%back%§").type(Material.ARROW).build(), (p, inv, type, stack, slot) -> new LanguageInventory(player)
         );
      } else {
         menuInventory.setItem(
            new MenuItem(
               new ItemBuilder().type(Material.ARROW).name("§a§%page%§ " + (page - 1)).build(),
               (p, inv, type, stack, s) -> new TranslationInventory(player, language, page - 1)
            ),
            39
         );
      }

      if (Math.ceil((double)(items.size() / this.itemsPerPage)) + 1.0 > (double)page) {
         menuInventory.setItem(
            new MenuItem(
               new ItemBuilder().type(Material.ARROW).name("§a§%page%§ " + (page + 1)).build(),
               (p, inventory, clickType, itemx, slot) -> new TranslationInventory(player, language, page + 1)
            ),
            41
         );
      }

      menuInventory.open(player);
   }
}

package br.com.plutomc.core.bukkit.menu.staff;

import br.com.plutomc.core.bukkit.utils.helper.SkullHelper;
import br.com.plutomc.core.bukkit.utils.item.ItemBuilder;
import br.com.plutomc.core.bukkit.utils.menu.MenuInventory;
import br.com.plutomc.core.bukkit.utils.menu.click.ClickType;
import br.com.plutomc.core.bukkit.utils.menu.click.MenuClickHandler;
import br.com.plutomc.core.common.CommonPlugin;
import br.com.plutomc.core.common.language.Language;
import br.com.plutomc.core.common.account.Account;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class LanguageInventory {
   public LanguageInventory(final Player player) {
      Account account = CommonPlugin.getInstance().getAccountManager().getAccount(player.getUniqueId());
      MenuInventory menuInventory = new MenuInventory("§7§%staff.inventory-language%§", 3);

      for(int i = 1; i <= Language.values().length; ++i) {
         final Language language = Language.values()[i - 1];
         menuInventory.setItem(
            9 + i,
            new ItemBuilder()
               .type(Material.SKULL_ITEM)
               .durability(3)
               .skin(SkullHelper.getLanguageSkin(language), "")
               .name("§a" + language.getLanguageName())
               .lore(
                  "§7"
                     + account.getLanguage().t("staff.inventory-language-" + language.name().toLowerCase() + "-description")
                     + "\n\n§e"
                     + account.getLanguage().t("staff.inventory-language-click-to-modify")
               )
               .build(),
            new MenuClickHandler() {
               @Override
               public void onClick(Player p, Inventory inv, ClickType type, ItemStack stack, int slot) {
                  new TranslationInventory(player, language, 1);
               }
            }
         );
      }

      menuInventory.open(player);
   }
}

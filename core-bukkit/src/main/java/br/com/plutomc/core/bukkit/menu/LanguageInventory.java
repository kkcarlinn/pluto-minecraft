package br.com.plutomc.core.bukkit.menu;

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
      final Account account = CommonPlugin.getInstance().getAccountManager().getAccount(player.getUniqueId());
      MenuInventory menuInventory = new MenuInventory("§7§%inventory-language%§", 3);

      for(int i = 1; i <= Language.values().length; ++i) {
         final Language language = Language.values()[i - 1];
         if (language == Language.PORTUGUESE || player.hasPermission("command.language")) {
            ItemBuilder itemBuilder = new ItemBuilder()
               .type(Material.SKULL_ITEM)
               .durability(3)
               .skin(SkullHelper.getLanguageSkin(language), "")
               .name("§a" + language.getLanguageName())
               .lore(
                  "§7"
                     + language.t("inventory-language-" + language.name().toLowerCase() + "-description")
                     + "\n\n§e"
                     + language.t("inventory-language-click-to-change")
               );
            if (account.getLanguage() == language) {
               itemBuilder.glow();
            }

            menuInventory.setItem(9 + i, itemBuilder.build(), new MenuClickHandler() {
               @Override
               public void onClick(Player p, Inventory inv, ClickType type, ItemStack stack, int slot) {
                  if (account.getLanguage() == language) {
                     player.sendMessage(language.t("inventory-language-already", "%language%", language.getLanguageName()));
                  } else {
                     account.setLanguage(language);
                     account.sendMessage(language.t("inventory-language-changed", "%language%", language.getLanguageName()));
                     new LanguageInventory(player);
                  }
               }
            });
         }
      }

      menuInventory.open(player);
   }
}

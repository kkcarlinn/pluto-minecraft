package br.com.plutomc.core.bukkit.menu.profile;

import java.util.Date;

import br.com.plutomc.core.bukkit.utils.item.ItemBuilder;
import br.com.plutomc.core.bukkit.utils.menu.MenuInventory;
import br.com.plutomc.core.bukkit.utils.menu.MenuUpdateHandler;
import br.com.plutomc.core.bukkit.menu.LanguageInventory;
import br.com.plutomc.core.common.CommonConst;
import br.com.plutomc.core.common.CommonPlugin;
import br.com.plutomc.core.common.language.Language;
import br.com.plutomc.core.common.account.Account;
import br.com.plutomc.core.common.utils.DateUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class ProfileInventory {
   public ProfileInventory(Player player) {
      final Account account = CommonPlugin.getInstance().getAccountManager().getAccount(player.getUniqueId());
      MenuInventory menuInventory = new MenuInventory(account.getLanguage().t("inventory-profile", "%player%", account.getPlayerName()), 5);
      this.updateProfileItem(menuInventory, account);
      menuInventory.setItem(
         29,
         new ItemBuilder().name("§a§%inventory-profile-your-stats%§").lore("§7§%inventory-profile-your-stats-description%§").type(Material.PAPER).build(),
         (p, inv, type, stack, slot) -> new StatisticsInventory(player, null)
      );
      menuInventory.setItem(
         30,
         new ItemBuilder().name("§a§%inventory-profile-your-medals%§").lore("§7§%inventory-profile-your-medals-description%§").type(Material.NAME_TAG).build(),
         (p, inv, type, stack, slot) -> {
            p.closeInventory();
            p.performCommand("medals");
         }
      );
      menuInventory.setItem(
         31,
         new ItemBuilder()
            .name("§a§%inventory-profile-select-language%§")
            .lore("§7§%inventory-profile-select-language-description%§")
            .type(Material.SKULL_ITEM)
            .durability(3)
            .skin(
               "e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTc5ZTIyNDhiZDc5OGViNjFmOTdhZWVlY2MyYWZkZGViYWQ1MmJmNDA1MmM3MjYxYjYxODBhNDU3N2Y4NjkzYSJ9fX0==",
               ""
            )
            .build(),
         (p, inv, type, stack, slot) -> new LanguageInventory(player)
      );
      menuInventory.setItem(
         32,
         new ItemBuilder()
            .name("§a§%inventory-profile-preferences%§")
            .lore("§7§%inventory-profile-preferences-description%§")
            .type(Material.REDSTONE_COMPARATOR)
            .build(),
         (p, inv, type, stack, slot) -> new PreferencesInventory(player)
      );
      menuInventory.setItem(
         33,
         new ItemBuilder().name("§a§%inventory-profile-skin%§").lore("§7§%inventory-profile-skin-description%§").type(Material.ITEM_FRAME).build(),
         (p, inv, type, stack, slot) -> new SkinInventory(player)
      );
      menuInventory.setUpdateHandler(new MenuUpdateHandler() {
         @Override
         public void onUpdate(Player player, MenuInventory menu) {
            if (menu.hasItem(13)) {
               ProfileInventory.this.updateProfileItem(menu, account);
            }
         }
      });
      menuInventory.open(player);
   }

   private void updateProfileItem(MenuInventory menuInventory, Account account) {
      Language language = account.getLanguage();
      if (account.isOnline()) {
         menuInventory.setItem(
            13,
            new ItemBuilder()
               .name("§a" + account.getPlayerName())
               .type(Material.SKULL_ITEM)
               .lore(
                  "",
                  "§7§%inventory-profile-first-login%§: §f" + CommonConst.DATE_FORMAT.format(new Date(account.getFirstLogin())),
                  "§7§%inventory-profile-last-login%§: §f" + CommonConst.DATE_FORMAT.format(new Date(account.getLastLogin())),
                  "§7§%inventory-profile-total-logged-time%§: §f" + DateUtils.formatDifference(language, account.getOnlineTime() / 1000L),
                  "§7§%inventory-profile-actual-logged-time%§: §f" + DateUtils.formatDifference(language, account.getSessionTime() / 1000L),
                  "",
                  "§a§%inventory-profile-user-online%§"
               )
               .durability(3)
               .skin(account.getPlayerName())
               .build()
         );
      } else {
         menuInventory.setItem(
            13,
            new ItemBuilder()
               .name("§a" + account.getPlayerName())
               .type(Material.SKULL_ITEM)
               .lore(
                  "",
                  "§7§%inventory-profile-first-login%§: §f" + CommonConst.DATE_FORMAT.format(new Date(account.getFirstLogin())),
                  "§7§%inventory-profile-last-login%§: §f" + CommonConst.DATE_FORMAT.format(new Date(account.getLastLogin())),
                  "§7§%inventory-profile-total-logged-time%§: §f" + DateUtils.formatDifference(language, account.getOnlineTime() / 1000L)
               )
               .durability(3)
               .skin(account.getPlayerName())
               .build()
         );
      }
   }
}

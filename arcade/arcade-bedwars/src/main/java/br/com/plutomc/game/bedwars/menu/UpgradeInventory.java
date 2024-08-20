package br.com.plutomc.game.bedwars.menu;

import br.com.plutomc.game.bedwars.GameMain;
import br.com.plutomc.game.bedwars.gamer.Gamer;
import br.com.plutomc.core.common.CommonPlugin;
import br.com.plutomc.game.engine.ArcadeCommon;
import br.com.plutomc.game.bedwars.island.Island;
import br.com.plutomc.game.bedwars.island.IslandUpgrade;
import br.com.plutomc.core.bukkit.utils.item.ItemBuilder;
import br.com.plutomc.core.bukkit.utils.menu.MenuInventory;
import br.com.plutomc.core.bukkit.utils.menu.click.ClickType;
import br.com.plutomc.core.bukkit.utils.menu.click.MenuClickHandler;
import br.com.plutomc.core.common.language.Language;
import br.com.plutomc.core.common.utils.string.StringFormat;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class UpgradeInventory {
   public UpgradeInventory(final Player player) {
      Island island = GameMain.getInstance().getIslandManager().getIsland(player.getUniqueId());
      if (island != null) {
         Gamer gamer = ArcadeCommon.getInstance().getGamerManager().getGamer(player.getUniqueId(), Gamer.class);
         if (gamer.isAlive()) {
            MenuInventory menuInventory = new MenuInventory("§7Loja do Time", 3);

            for(int i = 0; i < IslandUpgrade.values().length; ++i) {
               IslandUpgrade upgrade = IslandUpgrade.values()[i];
               this.handleUpgrade(player, CommonPlugin.getInstance().getPluginInfo().getDefaultLanguage(), island, 10 + i, menuInventory, upgrade);
            }

            menuInventory.open(player);
            (new BukkitRunnable() {
               @Override
               public void run() {
                  player.updateInventory();
               }
            }).runTaskLater(ArcadeCommon.getInstance(), 1L);
         }
      }
   }

   private void handleUpgrade(
      final Player player, final Language language, final Island island, int slot, MenuInventory menuInventory, final IslandUpgrade upgrade
   ) {
      String lore = "§7" + language.t("inventory-upgrade-" + upgrade.name().toLowerCase().replace("_", "-") + "-description");

      for(int k = 1; k <= upgrade.getMaxLevel(); ++k) {
         lore = lore.replace("%price-" + k + "%", "" + upgrade.getLevelsCost()[k - 1])
            .replace("%check-" + k + "%", island.getUpgradeLevel(upgrade) < k ? "§c✗" : "§a✓");
      }

      boolean maxLevel = island.getUpgradeLevel(upgrade) == upgrade.getMaxLevel();
      boolean enoughDiamonds = maxLevel
         ? true
         : player.getInventory()
            .contains(Material.DIAMOND, upgrade.getLevelsCost()[Math.min(island.getUpgradeLevel(upgrade), upgrade.getLevelsCost().length - 1)]);
      lore = lore.replace(
         "%buy%",
         maxLevel
            ? language.t("inventory-upgrade-max-level-reach-buy")
            : (
               player.getInventory().contains(Material.DIAMOND, upgrade.getLevelsCost()[island.getUpgradeLevel(upgrade)])
                  ? language.t("inventory-upgrade-diamond-enough")
                  : language.t("inventory-upgrade-not-diamond-enough")
            )
      );
      String level = upgrade.getMaxLevel() == 1 ? "" : "" + (maxLevel ? island.getUpgradeLevel(upgrade) : island.getUpgradeLevel(upgrade) + 1);
      menuInventory.setItem(
         slot,
         new ItemBuilder()
            .type(upgrade.getIcon())
            .name(
               (maxLevel ? "§a" : (enoughDiamonds ? "§e" : "§c"))
                  + language.t("inventory-upgrade-" + upgrade.name().toLowerCase().replace("_", "-"), "%level%", level)
            )
            .lore(lore)
            .build(),
         new MenuClickHandler() {
            @Override
            public void onClick(Player p, Inventory inv, ClickType type, ItemStack stack, int slot) {
               if (island.getUpgradeLevel(upgrade) == upgrade.getMaxLevel()) {
                  player.sendMessage(
                     language.t("inventory-upgrade-max-level-reach", "%upgrade%", StringFormat.formatToCamelCase(upgrade.name().replace("_", " ")))
                  );
                  player.playSound(player.getLocation(), Sound.NOTE_BASS, 1.0F, 1.0F);
               } else if (player.getInventory()
                  .contains(Material.DIAMOND, upgrade.getLevelsCost()[Math.min(island.getUpgradeLevel(upgrade), upgrade.getLevelsCost().length - 1)])) {
                  player.getInventory().removeItem(new ItemStack[]{new ItemStack(Material.DIAMOND, upgrade.getLevelsCost()[island.getUpgradeLevel(upgrade)])});
                  island.upgrade(player, upgrade);
                  player.playSound(player.getLocation(), Sound.LEVEL_UP, 1.0F, 1.0F);
                  new UpgradeInventory(player);
               } else {
                  player.sendMessage("§%inventory-upgrade-you-doesnt-have-enough-diamond%§");
                  player.playSound(player.getLocation(), Sound.NOTE_BASS, 1.0F, 1.0F);
               }
            }
         }
      );
   }
}

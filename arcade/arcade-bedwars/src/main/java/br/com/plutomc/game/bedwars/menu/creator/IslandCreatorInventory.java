package br.com.plutomc.game.bedwars.menu.creator;

import br.com.plutomc.game.bedwars.island.Island;
import br.com.plutomc.core.bukkit.utils.Location;
import br.com.plutomc.core.bukkit.utils.item.ActionItemStack;
import br.com.plutomc.core.bukkit.utils.item.ItemBuilder;
import br.com.plutomc.core.bukkit.utils.menu.MenuInventory;
import br.com.plutomc.core.bukkit.utils.menu.click.ClickType;
import br.com.plutomc.core.bukkit.utils.menu.confirm.ConfirmInventory;
import br.com.plutomc.core.common.language.Language;
import br.com.plutomc.core.common.utils.string.StringFormat;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class IslandCreatorInventory {
   public IslandCreatorInventory(Player player, Island island) {
      Language language = Language.getLanguage(player.getUniqueId());
      MenuInventory menuInventory = new MenuInventory(
         language.t("bedwars.inventory.island-creator.name", "%island%", StringFormat.formatString(island.getIslandColor().name())), 3
      );
      menuInventory.setItem(
         10,
         new ItemBuilder()
            .name("§aSpawn Location")
            .type(Material.PAPER)
            .lore(
               "§fWorld: §7" + island.getSpawnLocation().getWorldName(),
               "§fLocation: §7"
                  + StringFormat.formatString(", ", island.getSpawnLocation().getX(), island.getSpawnLocation().getY(), island.getSpawnLocation().getZ()),
               "§fYaw: §7" + island.getSpawnLocation().getYaw(),
               "§fPitch: §7" + island.getSpawnLocation().getPitch()
            )
            .build(),
         (p, inv, type, stack, slot) -> {
            if (type == ClickType.RIGHT) {
               p.teleport(island.getSpawnLocation().getAsLocation());
            } else {
               this.handle(p, island, stack, menuInventory, () -> island.setSpawnLocation(Location.fromLocation(p.getLocation())));
            }
         }
      );
      menuInventory.setItem(
         11,
         new ItemBuilder()
            .name("§aBed Location")
            .type(Material.BED)
            .lore(
               "§fWorld: §7" + island.getBedLocation().getWorldName(),
               "§fLocation: §7"
                  + StringFormat.formatString(", ", island.getBedLocation().getX(), island.getBedLocation().getY(), island.getBedLocation().getZ()),
               "§fYaw: §7" + island.getBedLocation().getYaw(),
               "§fPitch: §7" + island.getBedLocation().getPitch()
            )
            .build(),
         (p, inv, type, stack, slot) -> {
            if (type == ClickType.RIGHT) {
               p.teleport(island.getBedLocation().getAsLocation());
            } else {
               this.handle(p, island, stack, menuInventory, () -> island.setBedLocation(Location.fromLocation(p.getLocation())));
            }
         }
      );
      menuInventory.setItem(
         12,
         new ItemBuilder()
            .name("§aShop Location")
            .type(Material.EMERALD)
            .lore(
               "§fWorld: §7" + island.getShopLocation().getWorldName(),
               "§fLocation: §7"
                  + StringFormat.formatString(", ", island.getShopLocation().getX(), island.getShopLocation().getY(), island.getShopLocation().getZ()),
               "§fYaw: §7" + island.getShopLocation().getYaw(),
               "§fPitch: §7" + island.getShopLocation().getPitch()
            )
            .build(),
         (p, inv, type, stack, slot) -> {
            if (type == ClickType.RIGHT) {
               p.teleport(island.getShopLocation().getAsLocation());
            } else {
               this.handle(p, island, stack, menuInventory, () -> island.setShopLocation(Location.fromLocation(p.getLocation())));
            }
         }
      );
      menuInventory.setItem(
         13,
         new ItemBuilder()
            .name("§aUpgrade Location")
            .type(Material.DIAMOND)
            .lore(
               "§fWorld: §7" + island.getUpgradeLocation().getWorldName(),
               "§fLocation: §7"
                  + StringFormat.formatString(", ", island.getUpgradeLocation().getX(), island.getUpgradeLocation().getY(), island.getUpgradeLocation().getZ()),
               "§fYaw: §7" + island.getUpgradeLocation().getYaw(),
               "§fPitch: §7" + island.getUpgradeLocation().getPitch()
            )
            .build(),
         (p, inv, type, stack, slot) -> {
            if (type == ClickType.RIGHT) {
               p.teleport(island.getUpgradeLocation().getAsLocation());
            } else {
               this.handle(p, island, stack, menuInventory, () -> island.setUpgradeLocation(Location.fromLocation(p.getLocation())));
            }
         }
      );
      menuInventory.setItem(
         14,
         new ItemBuilder().name("§aGenerators").type(Material.FURNACE).build(),
         (p, inv, type, stack, slot) -> new IslandCreatorGeneratorInventory(player, island)
      );
      menuInventory.setItem(16, new ItemBuilder().name("§aSave").type(Material.ENCHANTED_BOOK).build(), (p, inv, type, stack, slot) -> {
         p.performCommand("config bedwars save");
         p.closeInventory();
         if (p.getInventory().getItemInHand().getType() == Material.BARRIER) {
            p.getInventory().removeItem(new ItemStack[]{p.getInventory().getItemInHand()});
         }
      });
      menuInventory.open(player);
   }

   private void handle(
      Player player, final Island island, final ItemStack itemStack, MenuInventory menuInventory, final ConfirmHandler confirmHandler
   ) {
      player.getInventory()
         .addItem(
            new ItemStack[]{
               new ActionItemStack(
                     new ItemBuilder().type(itemStack.getType()).name(itemStack.getItemMeta().getDisplayName()).build(), new ActionItemStack.Interact() {
                        @Override
                        public boolean onInteract(Player player, Entity entity, Block block, ItemStack item, ActionItemStack.ActionType action) {
                           new ConfirmInventory(player, "§7" + ChatColor.stripColor(itemStack.getItemMeta().getDisplayName()), b -> {
                              if (b) {
                                 player.getInventory().remove(item);
                                 ActionItemStack.unregisterHandler(this);
                                 confirmHandler.confirm();
                                 new IslandCreatorInventory(player, island);
                              }
                           }, null);
                           return false;
                        }
                     }
                  )
                  .getItemStack()
            }
         );
      player.closeInventory();
   }

   public interface ConfirmHandler {
      void confirm();
   }
}

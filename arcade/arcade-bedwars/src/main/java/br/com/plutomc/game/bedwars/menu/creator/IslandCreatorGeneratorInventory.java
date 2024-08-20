package br.com.plutomc.game.bedwars.menu.creator;

import java.util.ArrayList;
import java.util.List;
import br.com.plutomc.game.bedwars.island.Island;
import br.com.plutomc.core.bukkit.utils.Location;
import br.com.plutomc.core.bukkit.utils.item.ActionItemStack;
import br.com.plutomc.core.bukkit.utils.item.ItemBuilder;
import br.com.plutomc.core.bukkit.utils.menu.MenuInventory;
import br.com.plutomc.core.bukkit.utils.menu.click.ClickType;
import br.com.plutomc.core.bukkit.utils.menu.confirm.ConfirmInventory;
import br.com.plutomc.core.common.language.Language;
import br.com.plutomc.core.common.utils.string.StringFormat;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class IslandCreatorGeneratorInventory {
   public IslandCreatorGeneratorInventory(Player player, Island island) {
      Language language = Language.getLanguage(player.getUniqueId());
      MenuInventory menuInventory = new MenuInventory(language.t("bedwars.inventory.island-generator.title"), 4);
      this.handleItems(player, menuInventory, island);
      menuInventory.setItem(
         27, new ItemBuilder().type(Material.ARROW).name("§%back%§").build(), (p, inv, type, stack, slot) -> new IslandCreatorInventory(player, island)
      );
      menuInventory.open(player);
   }

   private void handleItems(Player player, MenuInventory menuInventory, Island island) {
      List<Location> ironList = island.getGeneratorMap().computeIfAbsent(Material.IRON_INGOT, v -> new ArrayList());
      List<Location> goldList = island.getGeneratorMap().computeIfAbsent(Material.GOLD_INGOT, v -> new ArrayList());

      for(int i = 0; i < 7; ++i) {
         Location ironLocation = i >= ironList.size() ? null : ironList.get(i);
         int index = i;
         if (ironLocation == null) {
            menuInventory.setItem(
               10 + i,
               new ItemBuilder().name("§c§%empty-slot%§").lore("§7Click to create new generator").type(Material.STAINED_GLASS_PANE).durability(14).build(),
               (p, inv, type, stack, slot) -> {
                  ironList.add(Location.fromLocation(player.getLocation()));
                  this.handleItems(player, menuInventory, island);
               }
            );
         } else {
            menuInventory.setItem(10 + i, this.createItem("§7Iron " + (i + 1), ironLocation), (p, inv, type, stack, slot) -> {
               if (type == ClickType.RIGHT) {
                  p.teleport(ironLocation.getAsLocation());
               } else if (type == ClickType.SHIFT) {
                  ironList.remove(index);
                  this.handleItems(player, menuInventory, island);
               } else {
                  this.handle(player, stack, ironLocation, island);
               }
            });
         }

         Location goldLocation = i >= goldList.size() ? null : goldList.get(i);
         if (goldLocation == null) {
            menuInventory.setItem(
               19 + i,
               new ItemBuilder().name("§c§%empty-slot%§").lore("§7Click to create new generator").type(Material.STAINED_GLASS_PANE).durability(14).build(),
               (p, inv, type, stack, slot) -> {
                  goldList.add(Location.fromLocation(player.getLocation()));
                  this.handleItems(player, menuInventory, island);
               }
            );
         } else {
            menuInventory.setItem(19 + i, this.createItem("§7Gold " + (i + 1), goldLocation), (p, inv, type, stack, slot) -> {
               if (type == ClickType.RIGHT) {
                  p.teleport(goldLocation.getAsLocation());
               } else if (type == ClickType.SHIFT) {
                  goldList.remove(index);
                  this.handleItems(player, menuInventory, island);
               } else {
                  this.handle(player, stack, goldLocation, island);
               }
            });
         }
      }
   }

   private ItemStack createItem(String name, Location location) {
      return new ItemBuilder()
         .name(name)
         .type(name.contains("Gold") ? Material.GOLD_INGOT : Material.IRON_INGOT)
         .lore(
            "§fWorld: §7" + location.getWorldName(),
            "§fLocation: §7" + StringFormat.formatString(", ", location.getX(), location.getY(), location.getZ()),
            "§fYaw: §7" + location.getYaw(),
            "§fPitch: §7" + location.getPitch()
         )
         .build();
   }

   public void handle(Player player, final ItemStack itemStack, final Location location, final Island island) {
      player.getInventory().addItem(new ItemStack[]{new ActionItemStack(itemStack, new ActionItemStack.Interact() {
         @Override
         public boolean onInteract(Player player, Entity entity, Block block, ItemStack item, ActionItemStack.ActionType action) {
            new ConfirmInventory(player, itemStack.getItemMeta().getDisplayName(), b -> {
               if (b) {
                  org.bukkit.Location newLocation;
                  if (block == null) {
                     newLocation = player.getLocation();
                  } else {
                     newLocation = block.getLocation();
                  }

                  location.set(newLocation);
               }

               while(player.getInventory().contains(itemStack)) {
                  player.getInventory().remove(itemStack);
               }

               ActionItemStack.unregisterHandler(this);
               new IslandCreatorGeneratorInventory(player, island);
            }, null);
            return false;
         }
      }).getItemStack()});
   }
}

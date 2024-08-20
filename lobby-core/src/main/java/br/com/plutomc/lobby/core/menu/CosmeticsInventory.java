package br.com.plutomc.lobby.core.menu;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import br.com.plutomc.lobby.core.gamer.Gamer;
import br.com.plutomc.lobby.core.wadgets.Heads;
import br.com.plutomc.lobby.core.wadgets.Particles;
import br.com.plutomc.lobby.core.wadgets.Wadget;
import br.com.plutomc.lobby.core.wadgets.Wings;
import br.com.plutomc.lobby.core.CoreMain;
import br.com.plutomc.core.bukkit.utils.item.ItemBuilder;
import br.com.plutomc.core.bukkit.utils.menu.MenuInventory;
import br.com.plutomc.core.bukkit.utils.menu.MenuItem;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class CosmeticsInventory {
   private int itemsPerPage = 21;

   public CosmeticsInventory(Player player, Wadget wadget, int page) {
      Gamer gamer = CoreMain.getInstance().getGamerManager().getGamer(player.getUniqueId());
      MenuInventory menuInventory = new MenuInventory("§7Cosméticos", wadget == null ? 3 : 6);
      if (wadget == null) {
         for(int i = 0; i < Wadget.values().length; ++i) {
            Wadget actual = Wadget.values()[i];
            List<Enum<?>> list = Arrays.asList(
               (Enum<?>[])(actual == Wadget.HEADS ? Heads.values() : (actual == Wadget.CAPES ? Wings.values() : Particles.values()))
            );
            long totalOwned = list.stream()
               .filter(o -> player.hasPermission("lobby.wadgets") || player.hasPermission(actual.name().toLowerCase() + "." + o.name().toLowerCase()))
               .count();
            menuInventory.setItem(
               11 + i * 2,
               new ItemBuilder().name("§a" + actual.getName()).type(actual.getType()).lore("", "§7Disponível: §f" + totalOwned + "/" + list.size()).build(),
               (p, inv, type, stack, slot) -> new CosmeticsInventory(player, actual, 1)
            );
         }
      } else {
         List<MenuItem> items = new ArrayList<>();
         switch(wadget) {
            case HEADS:
               Heads[] var15 = Heads.values();
               int var19 = var15.length;
               int var23 = 0;

               for(; var23 < var19; ++var23) {
                  Heads heads = var15[var23];
                  if (!player.hasPermission("lobby.cosmetics") && !player.hasPermission(wadget.name().toLowerCase() + "." + heads.name().toLowerCase())) {
                     items.add(
                        new MenuItem(
                           new ItemBuilder()
                              .type(Material.INK_SACK)
                              .durability(8)
                              .name("§a" + heads.getHeadName())
                              .lore("", "§7Exclusivo para §aVIP", "", "§cVocê não possui esse item.")
                              .skin(heads.getValue(), "")
                              .build()
                        )
                     );
                  } else {
                     items.add(
                        new MenuItem(
                           new ItemBuilder()
                              .type(Material.SKULL_ITEM)
                              .durability(3)
                              .name("§a" + heads.getHeadName())
                              .lore("", "§eClique aqui para selecionar.")
                              .skin(heads.getValue(), "")
                              .build(),
                           (p, inv, type, stack, slot) -> {
                              p.getInventory().setHelmet(stack);
                              p.closeInventory();
                              p.sendMessage("§aColetável ativado: Chapéu do " + heads.getHeadName());
                           }
                        )
                     );
                  }
               }

               menuInventory.setItem(new MenuItem(new ItemBuilder().type(Material.BARRIER).name("§cRemover cabeça").build(), (p, inv, type, stack, s) -> {
                  p.getInventory().setHelmet(null);
                  p.closeInventory();
               }), 49);
               break;
            case CAPES:
               for(Wings wing : Wings.values()) {
                  if (!player.hasPermission("lobby.cosmetics") && !player.hasPermission(wadget.name().toLowerCase() + "." + wing.name().toLowerCase())) {
                     items.add(
                        new MenuItem(
                           wing.getItem()
                              .name(wing.getName())
                              .type(Material.INK_SACK)
                              .durability(8)
                              .lore("", "§7Exclusivo para §aVIP", "", "§cVocê não possui esse item.")
                              .build()
                        )
                     );
                  } else {
                     items.add(
                        new MenuItem(
                           new ItemBuilder().type(Material.INK_SACK).durability(10).name("§a" + wing.getName()).build(), (p, inv, type, stack, slot) -> {
                              gamer.setUsingParticle(true);
                              gamer.setWing(wing);
                              gamer.setCape(true);
                              gamer.setParticle(null);
                              player.closeInventory();
                              player.sendMessage("§aColetável ativado: " + ChatColor.stripColor(wing.getName()) + "!");
                           }
                        )
                     );
                  }
               }

               menuInventory.setItem(new MenuItem(new ItemBuilder().type(Material.BARRIER).name("§cRemover cabeça").build(), (p, inv, type, stack, s) -> {
                  gamer.setUsingParticle(false);
                  gamer.setCape(false);
                  gamer.setWing(null);
                  gamer.setParticle(null);
                  player.closeInventory();
               }), 49);
               break;
            case PARTICLES:
               for(Particles particle : Particles.values()) {
                  if (!player.hasPermission("lobby.cosmetics") && !player.hasPermission(wadget.name().toLowerCase() + "." + particle.name().toLowerCase())) {
                     items.add(
                        new MenuItem(
                           particle.getItem()
                              .name(particle.getName())
                              .type(Material.INK_SACK)
                              .durability(8)
                              .lore("", "§7Exclusivo para §aVIP", "", "§cVocê não possui esse item.")
                              .build()
                        )
                     );
                  } else {
                     items.add(
                        new MenuItem(
                           new ItemBuilder().type(Material.INK_SACK).durability(10).name("§a" + particle.getName()).build(), (p, inv, type, stack, slot) -> {
                              gamer.setUsingParticle(true);
                              gamer.setCape(false);
                              gamer.setWing(null);
                              gamer.setParticle(particle);
                              player.closeInventory();
                              player.sendMessage("§aColetável ativado: " + ChatColor.stripColor(particle.getName()) + "!");
                           }
                        )
                     );
                  }
               }

               menuInventory.setItem(new MenuItem(new ItemBuilder().type(Material.BARRIER).name("§cRemover cabeça").build(), (p, inv, type, stack, s) -> {
                  gamer.setUsingParticle(false);
                  gamer.setCape(false);
                  gamer.setParticle(null);
                  player.closeInventory();
               }), 49);
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
               47, new ItemBuilder().name("§cVoltar").type(Material.ARROW).build(), (p, inv, type, stack, slot) -> new CosmeticsInventory(player, null)
            );
         } else {
            menuInventory.setItem(
               new MenuItem(
                  new ItemBuilder().type(Material.ARROW).name("§aPágina " + (page - 1)).build(),
                  (p, inv, type, stack, s) -> new CosmeticsInventory(player, wadget, page - 1)
               ),
               47
            );
         }

         if (Math.ceil((double)(items.size() / this.itemsPerPage)) + 1.0 > (double)page) {
            menuInventory.setItem(
               new MenuItem(
                  new ItemBuilder().type(Material.ARROW).name("§aPágina " + (page + 1)).build(),
                  (p, inventory, clickType, itemx, slot) -> new CosmeticsInventory(player, wadget, page + 1)
               ),
               51
            );
         }
      }

      menuInventory.open(player);
   }

   public CosmeticsInventory(Player player, Wadget wadget) {
      this(player, wadget, 1);
   }

   public CosmeticsInventory(Player player) {
      this(player, null, 1);
   }
}

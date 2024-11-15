package br.com.plutomc.core.bukkit.menu.staff.punish;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import br.com.plutomc.core.bukkit.utils.item.ItemBuilder;
import br.com.plutomc.core.bukkit.utils.menu.MenuInventory;
import br.com.plutomc.core.bukkit.utils.menu.MenuItem;
import br.com.plutomc.core.bukkit.utils.menu.click.ClickType;
import br.com.plutomc.core.common.CommonConst;
import br.com.plutomc.core.common.language.Language;
import br.com.plutomc.core.common.account.Account;
import br.com.plutomc.core.common.punish.Punish;
import br.com.plutomc.core.common.punish.PunishType;
import br.com.plutomc.core.common.utils.DateUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class PunishInfoListInventory extends MenuInventory {
   private Player player;
   private Account target;
   private PunishType punishType;
   private int page;
   private PunishOrdenator ordenator = PunishOrdenator.ALPHABETIC;
   private boolean asc = true;
   private long wait;
   private MenuInventory backInventory;

   public PunishInfoListInventory(Player player, Account target, PunishType punishType, int page, MenuInventory backInventory) {
      super("§7Listando " + punishType.name().toLowerCase() + "s", 5);
      this.player = player;
      this.target = target;
      this.punishType = punishType;
      this.page = page;
      this.backInventory = backInventory;
      this.handleItems();
      this.open(player);
   }

   private void handleItems() {
      List<MenuItem> items = new ArrayList<>();

      for(Punish punish : this.target
         .getPunishConfiguration()
         .getPunish(this.punishType)
         .stream()
         .sorted((o1, o2) -> this.ordenator.compare(o1, o2) * (this.asc ? 1 : -1))
         .collect(Collectors.toList())) {
         ItemBuilder itemBuilder = new ItemBuilder()
            .name("§a" + punish.getPunisherName())
            .type(Material.SKULL_ITEM)
            .durability(3)
            .skin(punish.getPunisherName());
         itemBuilder.lore(
            "§fAutor: §7" + punish.getPunisherName(),
            "§fMotivo: §7" + punish.getPunishReason(),
            "§fCriado às: §7" + CommonConst.DATE_FORMAT.format(punish.getCreatedAt())
         );
         if (punish.isPermanent()) {
            itemBuilder.lore("§cEssa punição não tem prazo de expiração.");
         } else if (punish.hasExpired()) {
            itemBuilder.lore("§aEssa punição já expirou.");
         } else {
            itemBuilder.lore("§fExpira em: §7" + DateUtils.getTime(Language.getLanguage(this.player.getUniqueId()), punish.getExpireAt()) + "§f.");
         }

         if (punish.isUnpunished()) {
            itemBuilder.lore("", "§aEssa punição foi revogada pelo " + punish.getUnpunisherName() + ".");
         }

         items.add(new MenuItem(itemBuilder.build(), (p, inv, type, stack, s) -> new PunishInventory(this.player, this.target, punish, this)));
      }

      int pageStart = 0;
      int pageEnd = 21;
      if (this.page > 1) {
         pageStart = (this.page - 1) * 21;
         pageEnd = this.page * 21;
      }

      if (pageEnd > items.size()) {
         pageEnd = items.size();
      }

      int w = 10;

      for(int i = pageStart; i < pageEnd; ++i) {
         MenuItem item = items.get(i);
         this.setItem(item, w);
         if (w % 9 == 7) {
            w += 3;
         } else {
            ++w;
         }
      }

      this.setItem(
         40,
         new ItemBuilder()
            .name("§a§%punish.order." + this.ordenator.name().toLowerCase().replace("_", "-") + "-name%§")
            .type(Material.ITEM_FRAME)
            .lore(
               "§7§%punish.order." + this.ordenator.name().toLowerCase().replace("_", "-") + "-description%§",
               this.asc ? "§7Ordem crescente." : "§7Ordem decrescente."
            )
            .build(),
         (p, inv, type, stack, s) -> {
            if (this.wait > System.currentTimeMillis()) {
               p.sendMessage("§cAguarde para mudar a ordenação novamente.");
            } else {
               this.wait = System.currentTimeMillis() + 500L;
               if (type != ClickType.RIGHT && type != ClickType.SHIFT) {
                  this.ordenator = PunishOrdenator.values()[this.ordenator.ordinal() == PunishOrdenator.values().length - 1 ? 0 : this.ordenator.ordinal() + 1];
               } else {
                  this.asc = !this.asc;
               }
   
               this.handleItems();
            }
         }
      );
      if (this.page == 1) {
         if (this.backInventory == null) {
            this.removeItem(39);
         } else {
            this.setItem(
               new MenuItem(
                  new ItemBuilder().type(Material.ARROW).name("§a§%back%§").lore("§7Voltar para " + this.backInventory.getTitle()).build(),
                  (p, inv, type, stack, s) -> this.backInventory.open(p)
               ),
               39
            );
         }
      } else {
         this.setItem(new MenuItem(new ItemBuilder().type(Material.ARROW).name("§a§%page%§ " + (this.page - 1)).build(), (p, inv, type, stack, s) -> {
            --this.page;
            this.handleItems();
         }), 39);
      }

      if (Math.ceil((double)(items.size() / 21)) + 1.0 > (double)this.page) {
         this.setItem(
            new MenuItem(new ItemBuilder().type(Material.ARROW).name("§a§%page%§ " + (this.page + 1)).build(), (p, inventory, clickType, itemx, slot) -> {
               ++this.page;
               this.handleItems();
            }), 41
         );
      } else {
         this.removeItem(41);
      }
   }
}

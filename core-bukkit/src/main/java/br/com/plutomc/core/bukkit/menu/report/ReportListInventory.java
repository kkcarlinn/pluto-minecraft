package br.com.plutomc.core.bukkit.menu.report;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import br.com.plutomc.core.bukkit.utils.item.ItemBuilder;
import br.com.plutomc.core.bukkit.utils.menu.MenuInventory;
import br.com.plutomc.core.bukkit.utils.menu.MenuItem;
import br.com.plutomc.core.bukkit.utils.menu.click.ClickType;
import br.com.plutomc.core.bukkit.utils.menu.confirm.ConfirmInventory;
import br.com.plutomc.core.common.CommonPlugin;
import br.com.plutomc.core.common.language.Language;
import br.com.plutomc.core.common.packet.types.staff.TeleportToTarget;
import br.com.plutomc.core.common.report.Report;
import br.com.plutomc.core.common.report.ReportInfo;
import br.com.plutomc.core.common.utils.DateUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class ReportListInventory extends MenuInventory {
   private static final int ITEMS_PER_PAGE = 21;
   private Language language;
   private Ordenator ordenator;
   private boolean asc;
   private int page;
   private long wait;

   public ReportListInventory(Player player, Ordenator ordenator, boolean asc, int page) {
      super("§7Reports", 5);
      this.language = Language.getLanguage(player.getUniqueId());
      this.ordenator = ordenator;
      this.asc = asc;
      this.page = page;
      this.handleItems();
      this.setUpdateHandler((p, menu) -> this.handleItems());
      this.open(player);
   }

   public ReportListInventory(Player player, int page) {
      this(player, Ordenator.values()[0], true, page);
   }

   private void handleItems() {
      List<MenuItem> items = new ArrayList<>();

      for(Report report : CommonPlugin.getInstance()
         .getReportManager()
         .getReports()
         .stream()
         .sorted((o1, o2) -> this.ordenator.compare(o1, o2) * (this.asc ? 1 : -1))
         .collect(Collectors.toList())) {
         if (report.hasExpired()) {
            report.deleteReport();
            break;
         }

         ReportInfo lastReport = report.getLastReport();
         items.add(
            new MenuItem(
               new ItemBuilder()
                  .name("§a" + report.getPlayerName())
                  .lore(
                     Arrays.asList(
                        "",
                        "§eUltima denúncia:",
                        "§f  Autor: §7" + lastReport.getPlayerName(),
                        "§f  Motivo: §7" + lastReport.getReason(),
                        "§f  Criado há: §7" + DateUtils.formatDifference(this.language, (System.currentTimeMillis() - lastReport.getCreatedAt()) / 1000L),
                        "",
                        "§fExpira em: §7" + DateUtils.getTime(this.language, report.getExpiresAt()),
                        report.isOnline() ? "§aO jogador está online no momento." : ""
                     )
                  )
                  .type(Material.SKULL_ITEM)
                  .durability(3)
                  .skin(report.getPlayerName())
                  .build(),
               (p, inv, type, stack, s) -> {
                  if (type == ClickType.RIGHT) {
                     new ConfirmInventory(p, "§7Deletar report " + report.getPlayerName(), confirm -> {
                        if (confirm) {
                           report.deleteReport();
                           new ReportListInventory(p, this.page);
                        }
                     }, this);
                  } else {
                     if (report.isOnline() ? type != ClickType.LEFT : type != ClickType.SHIFT) {
                        new ReportInventory(p, report, this);
                     } else {
                        CommonPlugin.getInstance()
                           .getServerData()
                           .sendPacket(new TeleportToTarget(p.getUniqueId(), report.getReportId(), report.getPlayerName()));
                     }
                  }
               }
            )
         );
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

      for(int i = pageStart; i < this.page * 21; ++i) {
         if (i < pageEnd) {
            MenuItem item = items.get(i);
            this.setItem(item, w);
         } else {
            this.removeItem(w);
         }

         if (w % 9 == 7) {
            w += 3;
         } else {
            ++w;
         }
      }

      this.setItem(
         40,
         new ItemBuilder()
            .name("§a§%report.order." + this.ordenator.name().toLowerCase().replace("_", "-") + "-name%§")
            .type(Material.ITEM_FRAME)
            .lore(
               "§7§%report.order." + this.ordenator.name().toLowerCase().replace("_", "-") + "-description%§",
               this.asc ? "§7Ordem crescente." : "§7Ordem decrescente."
            )
            .build(),
         (p, inv, type, stack, s) -> {
            if (this.wait > System.currentTimeMillis()) {
               p.sendMessage("§cAguarde para mudar a ordenação novamente.");
            } else {
               this.wait = System.currentTimeMillis() + 500L;
               if (type != ClickType.RIGHT && type != ClickType.SHIFT) {
                  this.ordenator = Ordenator.values()[this.ordenator.ordinal() == Ordenator.values().length - 1
                     ? 0
                     : this.ordenator.ordinal() + 1];
               } else {
                  this.asc = !this.asc;
               }
   
               this.handleItems();
            }
         }
      );
      if (this.page == 1) {
         this.removeItem(39);
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

   public static enum Ordenator implements Comparator<Report> {
      ALPHABETIC {
         public int compare(Report o1, Report o2) {
            return o1.getPlayerName().compareTo(o2.getPlayerName());
         }
      },
      EXPIRE_TIME {
         public int compare(Report o1, Report o2) {
            return Long.compare(o1.getExpiresAt(), o2.getExpiresAt()) * -1;
         }
      },
      CREATION_TIME {
         public int compare(Report o1, Report o2) {
            return Long.compare(o1.getCreatedAt(), o2.getCreatedAt());
         }
      },
      ONLINE {
         public int compare(Report o1, Report o2) {
            return Boolean.compare(o1.isOnline(), o2.isOnline());
         }
      };

      private Ordenator() {
      }
   }
}

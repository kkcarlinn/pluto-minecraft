package br.com.plutomc.core.bukkit.menu.report;

import java.util.ArrayList;
import java.util.List;

import br.com.plutomc.core.bukkit.utils.item.ItemBuilder;
import br.com.plutomc.core.bukkit.utils.menu.MenuInventory;
import br.com.plutomc.core.bukkit.utils.menu.MenuItem;
import br.com.plutomc.core.common.language.Language;
import br.com.plutomc.core.common.report.Report;
import br.com.plutomc.core.common.report.ReportInfo;
import br.com.plutomc.core.common.utils.DateUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class ReportInfoInventory {
   public ReportInfoInventory(Player player, Report report) {
      this(player, report, new ReportListInventory(player, 1), 1);
   }

   public ReportInfoInventory(Player player, Report report, MenuInventory backInventory, int page) {
      MenuInventory menuInventory = new MenuInventory("§7Report " + report.getPlayerName(), 5);
      Language language = Language.getLanguage(player.getUniqueId());
      List<MenuItem> items = new ArrayList<>();

      for(ReportInfo reportInfo : report.getReportMap().values()) {
         items.add(
            new MenuItem(
               new ItemBuilder()
                  .name("§a" + reportInfo.getPlayerName())
                  .lore(
                     "",
                     "§eInformações:",
                     "§f  Autor: §7" + reportInfo.getPlayerName(),
                     "§f  Motivo: §7" + reportInfo.getReason(),
                     "§f  Criado há: §7" + DateUtils.formatDifference(language, (System.currentTimeMillis() - reportInfo.getCreatedAt()) / 1000L)
                  )
                  .type(Material.SKULL_ITEM)
                  .durability(3)
                  .skin(reportInfo.getPlayerName())
                  .build()
            )
         );
      }

      int pageStart = 0;
      int pageEnd = 21;
      if (page > 1) {
         pageStart = (page - 1) * 21;
         pageEnd = page * 21;
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
            new MenuItem(
               new ItemBuilder().type(Material.ARROW).name("§aVoltar").build(), (p, inv, type, stack, s) -> new ReportInventory(player, report, backInventory)
            ),
            39
         );
      } else {
         menuInventory.setItem(
            new MenuItem(
               new ItemBuilder().type(Material.ARROW).name("§a§%page%§ " + (page - 1)).build(),
               (p, inv, type, stack, s) -> new ReportInfoInventory(player, report, backInventory, page - 1)
            ),
            39
         );
      }

      if (Math.ceil((double)(items.size() / 21)) + 1.0 > (double)page) {
         menuInventory.setItem(
            new MenuItem(
               new ItemBuilder().type(Material.ARROW).name("§a§%page%§ " + (page + 1)).build(),
               (p, inventory, clickType, itemx, slot) -> new ReportInfoInventory(player, report, backInventory, page + 1)
            ),
            41
         );
      }

      menuInventory.open(player);
   }
}

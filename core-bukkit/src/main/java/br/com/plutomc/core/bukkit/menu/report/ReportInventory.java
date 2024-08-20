package br.com.plutomc.core.bukkit.menu.report;

import java.util.Arrays;

import br.com.plutomc.core.bukkit.utils.item.ItemBuilder;
import br.com.plutomc.core.bukkit.utils.menu.MenuInventory;
import br.com.plutomc.core.bukkit.utils.menu.click.ClickType;
import br.com.plutomc.core.bukkit.utils.menu.confirm.ConfirmInventory;
import br.com.plutomc.core.common.language.Language;
import br.com.plutomc.core.common.report.Report;
import br.com.plutomc.core.common.report.ReportInfo;
import br.com.plutomc.core.common.utils.DateUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class ReportInventory {
   public ReportInventory(Player player, Report report) {
      this(player, report, new ReportListInventory(player, 1));
   }

   public ReportInventory(Player player, Report report, MenuInventory backInventory) {
      MenuInventory menuInventory = new MenuInventory("§7Report " + report.getPlayerName(), 3);
      Language language = Language.getLanguage(player.getUniqueId());
      ReportInfo lastReport = report.getLastReport();
      menuInventory.setItem(
         10,
         new ItemBuilder()
            .name("§a" + report.getPlayerName())
            .lore(
               Arrays.asList(
                  "",
                  "§eUltima denúncia:",
                  "§f  Autor: §7" + lastReport.getPlayerName(),
                  "§f  Motivo: §7" + lastReport.getReason(),
                  "§f  Criado há: §7" + DateUtils.formatDifference(language, (System.currentTimeMillis() - lastReport.getCreatedAt()) / 1000L),
                  "",
                  "§fExpira em: §7" + DateUtils.getTime(language, report.getExpiresAt())
               )
            )
            .type(Material.SKULL_ITEM)
            .durability(3)
            .skin(report.getPlayerName())
            .build()
      );
      menuInventory.setItem(
         11,
         new ItemBuilder().name("§eTodas as denúncias").lore("§7Clique para ver todas as denúncias feitas a esse jogador.").type(Material.BOOK).build(),
         (p, inv, type, stack, slot) -> new ReportInfoInventory(player, report, backInventory, 1)
      );
      menuInventory.setItem(
         15, new ItemBuilder().name("§cDeletar report").lore("§7Clique para excluir o report").type(Material.BARRIER).build(), (p, inv, type, stack, slot) -> {
            if (type == ClickType.SHIFT) {
               report.deleteReport();
               backInventory.open(p);
            } else {
               new ConfirmInventory(player, "§7Deletar report " + report.getPlayerName(), confirm -> {
                  if (confirm) {
                     report.deleteReport();
                     backInventory.open(p);
                  }
               }, menuInventory);
            }
         }
      );
      menuInventory.setItem(
         16,
         new ItemBuilder().name("§a§%back%§").lore("§7Voltar para " + backInventory.getTitle()).type(Material.ARROW).build(),
         (p, inv, type, stack, slot) -> backInventory.open(p)
      );
      menuInventory.open(player);
   }
}

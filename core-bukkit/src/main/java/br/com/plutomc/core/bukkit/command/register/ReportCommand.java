package br.com.plutomc.core.bukkit.command.register;

import java.util.Optional;

import br.com.plutomc.core.bukkit.BukkitCommon;
import br.com.plutomc.core.bukkit.menu.report.ReportInventory;
import br.com.plutomc.core.bukkit.menu.report.ReportListInventory;
import br.com.plutomc.core.common.CommonPlugin;
import br.com.plutomc.core.bukkit.member.BukkitMember;
import br.com.plutomc.core.common.command.CommandArgs;
import br.com.plutomc.core.common.command.CommandClass;
import br.com.plutomc.core.common.command.CommandFramework;
import br.com.plutomc.core.common.report.Report;
import org.bukkit.entity.Player;

public class ReportCommand implements CommandClass {
   @CommandFramework.Command(
      name = "report",
      aliases = {"reports"},
      permission = "command.report",
      console = false
   )
   public void reportCommand(CommandArgs cmdArgs) {
      Player sender = cmdArgs.getSenderAsMember(BukkitMember.class).getPlayer();
      String[] args = cmdArgs.getArgs();
      if (args.length == 0) {
         new ReportListInventory(sender, 1);
      } else {
         Optional<Player> optional = BukkitCommon.getPlayer(args[0], false);
         if (!optional.isPresent()) {
            sender.sendMessage(cmdArgs.getSender().getLanguage().t("player-is-not-online", "%player%", args[0]));
         } else {
            Player target = optional.get();
            Report report = CommonPlugin.getInstance().getReportManager().getReportById(target.getUniqueId());
            if (report == null) {
               new ReportListInventory(sender, 1);
            } else {
               new ReportInventory(target, report, new ReportListInventory(sender, 1));
            }
         }
      }
   }
}

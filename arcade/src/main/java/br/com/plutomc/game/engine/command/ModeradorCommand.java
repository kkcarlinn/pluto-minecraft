package br.com.plutomc.game.engine.command;

import br.com.plutomc.game.engine.ArcadeCommon;
import br.com.plutomc.core.common.command.CommandArgs;
import br.com.plutomc.core.common.command.CommandClass;
import br.com.plutomc.core.common.command.CommandFramework;
import br.com.plutomc.core.common.command.CommandSender;
import br.com.plutomc.core.common.utils.DateUtils;
import br.com.plutomc.core.common.utils.string.StringFormat;

public class ModeradorCommand implements CommandClass {
   @CommandFramework.Command(
      name = "time",
      aliases = {"tempo"},
      permission = "command.time"
   )
   public void timeCommand(CommandArgs cmdArgs) {
      CommandSender sender = cmdArgs.getSender();
      String[] args = cmdArgs.getArgs();
      if (args.length == 0) {
         sender.sendMessage(sender.getLanguage().t("command-time-usage", "%label%", cmdArgs.getLabel()));
      } else if (args[0].equalsIgnoreCase("stop")) {
         ArcadeCommon.getInstance().setTimer(!ArcadeCommon.getInstance().isTimer());
         ArcadeCommon.getInstance().setConsoleControl(false);
         sender.sendMessage("§%command-time-timer-" + (ArcadeCommon.getInstance().isTimer() ? "enabled" : "disabled") + "%§");
      } else {
         long time;
         try {
            time = DateUtils.parseDateDiff(args[0], true);
         } catch (Exception var7) {
            sender.sendMessage(sender.getLanguage().t("number-format-invalid", "%number%", args[0]));
            return;
         }

         int seconds = (int)Math.floor((double)((time - System.currentTimeMillis()) / 1000L));
         if (seconds >= 7200) {
            seconds = 7200;
         }

         sender.sendMessage(sender.getLanguage().t("command-time-changed", "%time%", StringFormat.formatTime(seconds, StringFormat.TimeFormat.NORMAL)));
         ArcadeCommon.getInstance().setTime(seconds);
      }
   }
}

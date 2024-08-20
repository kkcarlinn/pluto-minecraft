package br.com.plutomc.core.bukkit.listener;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import br.com.plutomc.core.bukkit.event.player.PlayerCommandEvent;
import br.com.plutomc.core.common.language.Language;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class CommandListener implements Listener {
   private Map<String, Command> knownCommands;

   public CommandListener() {
      try {
         Field field = Bukkit.getServer().getClass().getDeclaredField("commandMap");
         field.setAccessible(true);
         CommandMap commandMap = (CommandMap)field.get(Bukkit.getServer());
         Field secondField = commandMap.getClass().getDeclaredField("knownCommands");
         secondField.setAccessible(true);
         this.knownCommands = (HashMap)secondField.get(commandMap);
      } catch (Exception var4) {
         var4.printStackTrace();
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
      if (event.getMessage().startsWith("/")) {
         if (!event.getMessage().startsWith("//")) {
            String command = event.getMessage().split(" ")[0];
            String commandLabel = command.substring(1, command.length());
            PlayerCommandEvent playerCommandEvent = new PlayerCommandEvent(event.getPlayer(), commandLabel);
            Bukkit.getPluginManager().callEvent(playerCommandEvent);
            event.setCancelled(playerCommandEvent.isCancelled());
         }
      }
   }

   @EventHandler
   public void onPlayerCommand(PlayerCommandEvent event) {
      if (!event.getCommandLabel().startsWith("//")) {
         String command = event.getCommandLabel();
         if (command.contains(":")) {
            event.getPlayer().sendMessage(Language.getLanguage(event.getPlayer().getUniqueId()).t("command-not-found", "%command%", command));
            event.setCancelled(true);
         } else {
            if (!this.knownCommands.containsKey(command.toLowerCase())) {
               event.getPlayer().sendMessage(Language.getLanguage(event.getPlayer().getUniqueId()).t("command-not-found", "%command%", command));
               event.setCancelled(true);
            }
         }
      }
   }
}

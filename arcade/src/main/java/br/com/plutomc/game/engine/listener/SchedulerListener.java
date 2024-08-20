package br.com.plutomc.game.engine.listener;

import br.com.plutomc.game.engine.ArcadeCommon;
import br.com.plutomc.core.bukkit.event.UpdateEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class SchedulerListener implements Listener {
   @EventHandler
   public void update(UpdateEvent event) {
      if (event.getType() == UpdateEvent.UpdateType.SECOND) {
         ArcadeCommon.getInstance().getSchedulerManager().pulse();
      }
   }
}

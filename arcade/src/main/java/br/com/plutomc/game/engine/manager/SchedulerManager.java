package br.com.plutomc.game.engine.manager;

import br.com.plutomc.game.engine.ArcadeCommon;
import br.com.plutomc.game.engine.scheduler.Scheduler;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import br.com.plutomc.game.engine.event.SchedulePulseEvent;
import org.bukkit.Bukkit;

public class SchedulerManager {
   private Map<String, Scheduler> schedulerMap = new HashMap<>();

   public void loadScheduler(Scheduler scheduler) {
      this.schedulerMap.put(scheduler.toString().toLowerCase(), scheduler);
   }

   public void loadScheduler(String identifier, Scheduler scheduler) {
      this.schedulerMap.put(identifier, scheduler);
   }

   public void unloadSchedulers() {
      this.schedulerMap.clear();
   }

   public Collection<Scheduler> getSchedulers() {
      return ImmutableList.copyOf(this.schedulerMap.values());
   }

   public void unloadScheduler(Scheduler scheduler) {
      String identifier = null;

      for(Entry<String, Scheduler> entry : this.schedulerMap.entrySet()) {
         if (entry.getValue() == scheduler) {
            identifier = entry.getKey();
            break;
         }
      }

      if (identifier != null) {
         this.schedulerMap.remove(identifier);
      }
   }

   public void unloadScheduler(String identifier) {
      this.schedulerMap.remove(identifier);
   }

   public void pulse() {
      boolean pulse = false;

      for(Iterator<Scheduler> iterator = ImmutableMap.copyOf(this.schedulerMap).values().iterator(); iterator.hasNext(); pulse = true) {
         iterator.next().pulse();
      }

      if (pulse) {
         Bukkit.getPluginManager().callEvent(new SchedulePulseEvent());
      }

      if (ArcadeCommon.getInstance().isTimer()) {
         ArcadeCommon.getInstance().setTime(ArcadeCommon.getInstance().getTime() + (ArcadeCommon.getInstance().getState().isDecrementTime() ? -1 : 1));
      }
   }
}

package br.com.plutomc.game.engine;

import br.com.plutomc.game.engine.scheduler.Scheduler;
import br.com.plutomc.core.common.CommonPlugin;
import br.com.plutomc.core.bukkit.BukkitCommon;
import br.com.plutomc.game.engine.backend.GamerData;
import br.com.plutomc.game.engine.backend.impl.GamerDataImpl;
import br.com.plutomc.game.engine.event.GameStateChangeEvent;
import br.com.plutomc.game.engine.gamer.Gamer;
import br.com.plutomc.game.engine.listener.GamerListener;
import br.com.plutomc.game.engine.listener.SchedulerListener;
import br.com.plutomc.game.engine.manager.GamerManager;
import br.com.plutomc.game.engine.manager.SchedulerManager;
import br.com.plutomc.core.common.server.loadbalancer.server.MinigameState;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

@Getter
public abstract class ArcadeCommon extends BukkitCommon {

   @Getter
   private static ArcadeCommon instance;
   private GamerManager gamerManager;
   private SchedulerManager schedulerManager;
   @Setter
   private boolean unloadGamer = true;
   @Setter
   private Class<? extends Gamer> gamerClass;
   @Setter
   private String collectionName;
   @Setter
   private boolean timer;
   @Setter
   private boolean consoleControl = true;
   private GamerData gamerData;

   @Override
   public void onLoad() {
      super.onLoad();
      this.setServerLog(false);
      this.setRemovePlayerDat(false);
      instance = this;
   }

   @Override
   public void onEnable() {
      super.onEnable();
      this.gamerManager = new GamerManager();
      this.schedulerManager = new SchedulerManager();
      this.gamerData = new GamerDataImpl();
      Bukkit.getPluginManager().registerEvents(new GamerListener(), this);
      Bukkit.getPluginManager().registerEvents(new SchedulerListener(), this);
   }

   public void startScheduler(Scheduler scheduler) {
      this.getSchedulerManager().loadScheduler(scheduler);
      if (Listener.class.isAssignableFrom(scheduler.getClass())) {
         Bukkit.getPluginManager().registerEvents((Listener)scheduler, this);
      }
   }

   public void stopScheduler(Scheduler scheduler) {
      this.getSchedulerManager().unloadScheduler(scheduler);
      if (Listener.class.isAssignableFrom(scheduler.getClass())) {
         HandlerList.unregisterAll((Listener)scheduler);
      }
   }

   public void setMap(String mapName) {
      CommonPlugin.getInstance().setMap(mapName);
      CommonPlugin.getInstance().getServerData().updateStatus(this.getState(), this.getMapName(), this.getTime());
   }

   public void setTime(int time) {
      CommonPlugin.getInstance().setServerTime(time);
      CommonPlugin.getInstance().getServerData().updateStatus(this.getState(), this.getMapName(), this.getTime());
   }

   public void setState(MinigameState state) {
      MinigameState oldState = CommonPlugin.getInstance().getMinigameState();
      if (oldState != state) {
         CommonPlugin.getInstance().setMinigameState(state);
         CommonPlugin.getInstance().getServerData().updateStatus(this.getState(), this.getMapName(), this.getTime());
         System.out.println(oldState + " > " + state);
         Bukkit.getPluginManager().callEvent(new GameStateChangeEvent(oldState, state));
      }
   }

   public String getMapName() {
      return CommonPlugin.getInstance().getMap();
   }

   public int getTime() {
      return CommonPlugin.getInstance().getServerTime();
   }

   public MinigameState getState() {
      return CommonPlugin.getInstance().getMinigameState();
   }

}

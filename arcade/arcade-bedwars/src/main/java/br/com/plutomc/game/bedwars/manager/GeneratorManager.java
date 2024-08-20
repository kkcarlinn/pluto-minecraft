package br.com.plutomc.game.bedwars.manager;

import br.com.plutomc.game.bedwars.GameMain;
import br.com.plutomc.game.bedwars.generator.Generator;
import br.com.plutomc.game.bedwars.generator.GeneratorType;
import br.com.plutomc.game.bedwars.generator.impl.DiamondGenerator;
import br.com.plutomc.game.bedwars.generator.impl.EmeraldGenerator;
import com.google.common.collect.ImmutableSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import br.com.plutomc.core.bukkit.event.UpdateEvent;
import br.com.plutomc.game.engine.ArcadeCommon;
import br.com.plutomc.core.bukkit.utils.Location;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class GeneratorManager {
   private Map<GeneratorType, List<Generator>> generatorMap = new HashMap<>();

   public GeneratorManager() {
      for(GeneratorType generatorType : GeneratorType.values()) {
         if (generatorType != GeneratorType.NORMAL) {
            for(Location location : GameMain.getInstance()
               .getConfiguration()
               .getList(generatorType.getConfigFieldName(), new ArrayList<>(), true, Location.class)) {
               this.createGenerator(generatorType, location, false);
            }
         }
      }
   }

   public void createGenerator(GeneratorType generatorType, Location location, boolean save) {
      Generator generator = (Generator)(generatorType == GeneratorType.DIAMOND
         ? new DiamondGenerator(location.getAsLocation())
         : new EmeraldGenerator(location.getAsLocation()));
      this.generatorMap.computeIfAbsent(generatorType, v -> new ArrayList<>()).add(generator);
      if (save) {
         GameMain.getInstance().getConfiguration().addElementToList(generatorType.getConfigFieldName(), location);
      }
   }

   public boolean setLocation(GeneratorType generatorType, int index, Location fromLocation, boolean save) {
      Generator generator = this.generatorMap.computeIfAbsent(generatorType, v -> new ArrayList<>()).get(index);
      if (generator == null) {
         return false;
      } else {
         generator.setLocation(fromLocation.getAsLocation());
         return save ? GameMain.getInstance().getConfiguration().setElementToList(generatorType.getConfigFieldName(), index, fromLocation) : true;
      }
   }

   public void startGenerators() {
      this.generatorMap.values().forEach(list -> list.forEach(Generator::handleHologram));
      Bukkit.getPluginManager().registerEvents(new GeneratorListener(), ArcadeCommon.getInstance());
   }

   public Generator getGenerator(GeneratorType generatorType, int asInt) {
      return this.generatorMap.containsKey(generatorType) ? this.generatorMap.get(generatorType).get(asInt) : null;
   }

   public List<Generator> getGenerators(GeneratorType generatorType) {
      return this.generatorMap.get(generatorType);
   }

   public List<Generator> getGenerators() {
      List<Generator> generator = new ArrayList<>();

      for(GeneratorType generatorType : GeneratorType.values()) {
         if (this.generatorMap.containsKey(generatorType)) {
            generator.addAll(this.generatorMap.get(generatorType));
         }
      }

      return generator;
   }

   public void addGenerator(Generator createGenerator) {
      this.generatorMap.computeIfAbsent(createGenerator.getGeneratorType(), v -> new ArrayList()).add(createGenerator);
   }

   public class GeneratorListener implements Listener {
      public GeneratorListener() {
         Bukkit.getPluginManager().registerEvents(this, ArcadeCommon.getInstance());
      }

      @EventHandler
      public void onUpdate(UpdateEvent event) {
         if (event.getCurrentTick() % 3L == 0L) {
            for(Entry<GeneratorType, List<Generator>> entry : ImmutableSet.copyOf(GeneratorManager.this.generatorMap.entrySet())) {
               for(Generator generator : entry.getValue()) {
                  generator.animate();
                  generator.updateHologram();
                  if (generator.getLastGenerate() + generator.getGenerateTime() <= System.currentTimeMillis()) {
                     generator.generate();
                  }
               }
            }
         }
      }
   }
}

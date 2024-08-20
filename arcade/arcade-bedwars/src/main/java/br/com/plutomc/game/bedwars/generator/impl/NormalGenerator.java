package br.com.plutomc.game.bedwars.generator.impl;

import br.com.plutomc.game.bedwars.generator.Generator;
import br.com.plutomc.game.bedwars.generator.GeneratorType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class NormalGenerator extends Generator {
   public NormalGenerator(Location location, Material material) {
      super(location, GeneratorType.NORMAL, new ItemStack(material));
   }

   @Override
   public Location getDropLocation() {
      if (this.getDropsLocation().isEmpty()) {
         return this.getLocation();
      } else {
         return this.getDropsLocation().size() <= 1
            ? this.getDropsLocation().stream().findFirst().orElse(null)
            : this.getDropsLocation().get(++this.dropIndex >= this.getDropsLocation().size() ? (this.dropIndex = 0) : this.dropIndex);
      }
   }
}

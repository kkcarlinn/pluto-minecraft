package br.com.plutomc.game.bedwars.generator.impl;

import br.com.plutomc.game.bedwars.generator.GeneratorType;
import br.com.plutomc.game.bedwars.generator.Generator;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class EmeraldGenerator extends Generator {
   public EmeraldGenerator(Location location) {
      super(location, GeneratorType.EMERALD, new ItemStack(Material.EMERALD));
   }
}

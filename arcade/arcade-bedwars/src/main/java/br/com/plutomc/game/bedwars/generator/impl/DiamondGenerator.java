package br.com.plutomc.game.bedwars.generator.impl;

import br.com.plutomc.game.bedwars.generator.Generator;
import br.com.plutomc.game.bedwars.generator.GeneratorType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class DiamondGenerator extends Generator {
   public DiamondGenerator(Location location) {
      super(location, GeneratorType.DIAMOND, new ItemStack(Material.DIAMOND));
   }
}

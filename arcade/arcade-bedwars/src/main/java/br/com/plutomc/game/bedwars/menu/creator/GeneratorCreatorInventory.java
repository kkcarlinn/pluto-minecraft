package br.com.plutomc.game.bedwars.menu.creator;

import br.com.plutomc.core.bukkit.utils.menu.MenuInventory;
import org.bukkit.entity.Player;

public class GeneratorCreatorInventory {
   public GeneratorCreatorInventory(Player player) {
      MenuInventory menuInventory = new MenuInventory("§7Criar geradores", 3);
      menuInventory.open(player);
   }
}

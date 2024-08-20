package br.com.plutomc.lobby.lobbyhost.wadgets;

import org.bukkit.Material;

public enum Wadget {
   HEADS("Cabeças", Material.GOLD_HELMET),
   CAPES("Capas", Material.ENCHANTMENT_TABLE),
   PARTICLES("Partículas", Material.NETHER_STAR);

   private String name;
   private Material type;

   public String getName() {
      return this.name;
   }

   public Material getType() {
      return this.type;
   }

   private Wadget(String name, Material type) {
      this.name = name;
      this.type = type;
   }
}

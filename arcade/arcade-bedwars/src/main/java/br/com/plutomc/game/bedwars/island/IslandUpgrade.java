package br.com.plutomc.game.bedwars.island;

import org.bukkit.Material;

public enum IslandUpgrade {
   SHARPNESS(Material.IRON_SWORD, 4),
   ARMOR_REINFORCEMENT(Material.IRON_CHESTPLATE, 2, 4, 8, 16),
   HASTE(Material.GOLD_PICKAXE, 2, 4),
   FORGE(Material.FURNACE, 4, 8, 12, 16),
   REGENERATION(Material.BEACON, 1),
   TRAP(Material.TRIPWIRE_HOOK, 1);

   private Material icon;
   private int maxLevel;
   private int[] levelsCost;

   private IslandUpgrade(Material icon, int... cost) {
      this.icon = icon;
      this.maxLevel = cost.length;
      this.levelsCost = new int[cost.length];

      for(int i = 0; i < this.levelsCost.length; ++i) {
         this.levelsCost[i] = cost[i] * this.getMultiplier();
      }
   }

   public int getMultiplier() {
      return 1;
   }

   public Material getIcon() {
      return this.icon;
   }

   public int getMaxLevel() {
      return this.maxLevel;
   }

   public int[] getLevelsCost() {
      return this.levelsCost;
   }
}

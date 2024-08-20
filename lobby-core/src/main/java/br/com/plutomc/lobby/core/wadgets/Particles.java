package br.com.plutomc.lobby.core.wadgets;

import br.com.plutomc.core.bukkit.utils.item.ItemBuilder;
import net.minecraft.server.v1_8_R3.EnumParticle;
import org.bukkit.Material;

public enum Particles {
   HEART("§6Partículas de Corações", EnumParticle.HEART, new ItemBuilder().type(Material.INK_SACK).durability(10).name("§6Partículas de Corações")),
   FOGUETE("§6Partículas de Anjo", EnumParticle.FIREWORKS_SPARK, new ItemBuilder().type(Material.INK_SACK).durability(10).name("§6Partículas de Anjo")),
   FOGO("§6Partículas de Fogo", EnumParticle.FLAME, new ItemBuilder().type(Material.INK_SACK).durability(10).name("§6Partículas de Fogo"));

   private String name;
   private EnumParticle particle;
   private ItemBuilder item;

   public static Particles getParticleByName(String nameOfParticle) {
      for(Particles p : values()) {
         if (p.getName().equalsIgnoreCase(nameOfParticle)) {
            return p;
         }
      }

      return null;
   }

   private Particles(String name, EnumParticle particle, ItemBuilder item) {
      this.name = name;
      this.particle = particle;
      this.item = item;
   }

   public String getName() {
      return this.name;
   }

   public EnumParticle getParticle() {
      return this.particle;
   }

   public ItemBuilder getItem() {
      return this.item;
   }
}

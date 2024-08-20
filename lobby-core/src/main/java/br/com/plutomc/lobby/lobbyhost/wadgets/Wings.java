package br.com.plutomc.lobby.lobbyhost.wadgets;

import br.com.plutomc.core.bukkit.utils.item.ItemBuilder;
import net.minecraft.server.v1_8_R3.EnumParticle;
import org.bukkit.Material;

public enum Wings {
   FOGUETE("§6Asas de Anjo", EnumParticle.FIREWORKS_SPARK, new ItemBuilder().type(Material.FIREWORK).name("§6Asas de Anjo")),
   FOGO("§6Asas de Fogo", EnumParticle.FLAME, new ItemBuilder().type(Material.LAVA_BUCKET).name("§6Asas de Fogo"));

   private String name;
   private EnumParticle particle;
   private ItemBuilder item;

   public static Wings getWingsByName(String nameOfParticle) {
      for(Wings p : values()) {
         if (p.getName().equalsIgnoreCase(nameOfParticle)) {
            return p;
         }
      }

      return null;
   }

   private Wings(String name, EnumParticle particle, ItemBuilder item) {
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

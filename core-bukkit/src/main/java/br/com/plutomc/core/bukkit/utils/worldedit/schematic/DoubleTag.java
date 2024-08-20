package br.com.plutomc.core.bukkit.utils.worldedit.schematic;

public final class DoubleTag extends Tag {
   private final double value;

   public DoubleTag(String name, double value) {
      super(name);
      this.value = value;
   }

   public Double getValue() {
      return this.value;
   }

   @Override
   public String toString() {
      String name = this.getName();
      String append = "";
      if (name != null && !name.equals("")) {
         append = "(\"" + this.getName() + "\")";
      }

      return "TAG_Double" + append + ": " + this.value;
   }
}

package br.com.plutomc.core.bukkit.utils.worldedit.schematic;

public final class LongTag extends Tag {
   private final long value;

   public LongTag(String name, long value) {
      super(name);
      this.value = value;
   }

   public Long getValue() {
      return this.value;
   }

   @Override
   public String toString() {
      String name = this.getName();
      String append = "";
      if (name != null && !name.equals("")) {
         append = "(\"" + this.getName() + "\")";
      }

      return "TAG_Long" + append + ": " + this.value;
   }
}

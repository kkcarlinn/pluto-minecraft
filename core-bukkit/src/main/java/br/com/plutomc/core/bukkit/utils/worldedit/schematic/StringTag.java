package br.com.plutomc.core.bukkit.utils.worldedit.schematic;

public final class StringTag extends Tag {
   private final String value;

   public StringTag(String name, String value) {
      super(name);
      this.value = value;
   }

   public String getValue() {
      return this.value;
   }

   @Override
   public String toString() {
      String name = this.getName();
      String append = "";
      if (name != null && !name.equals("")) {
         append = "(\"" + this.getName() + "\")";
      }

      return "TAG_String" + append + ": " + this.value;
   }
}

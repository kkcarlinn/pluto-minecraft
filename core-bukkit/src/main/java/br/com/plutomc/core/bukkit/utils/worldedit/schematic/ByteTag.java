package br.com.plutomc.core.bukkit.utils.worldedit.schematic;

public final class ByteTag extends Tag {
   private final byte value;

   public ByteTag(String name, byte value) {
      super(name);
      this.value = value;
   }

   public Byte getValue() {
      return this.value;
   }

   @Override
   public String toString() {
      String name = this.getName();
      String append = "";
      if (name != null && !name.equals("")) {
         append = "(\"" + this.getName() + "\")";
      }

      return "TAG_Byte" + append + ": " + this.value;
   }
}

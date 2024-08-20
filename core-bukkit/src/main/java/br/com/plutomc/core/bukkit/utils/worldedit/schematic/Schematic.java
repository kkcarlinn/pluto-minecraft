package br.com.plutomc.core.bukkit.utils.worldedit.schematic;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;

public class Schematic {
   private static Schematic instance = new Schematic();
   private short[] blocks;
   private byte[] data;
   private short width;
   private short lenght;
   private short height;

   private Schematic(short[] blocks, byte[] data, short width, short lenght, short height) {
      this.blocks = blocks;
      this.data = data;
      this.width = width;
      this.lenght = lenght;
      this.height = height;
   }

   public Schematic loadSchematic(File file) throws IOException, DataException {
      FileInputStream stream = new FileInputStream(file);
      NBTInputStream nbtStream = new NBTInputStream(stream);
      CompoundTag schematicTag = (CompoundTag)nbtStream.readTag();
      nbtStream.close();
      if (!schematicTag.getName().equalsIgnoreCase("Schematic")) {
         throw new IllegalArgumentException("Tag \"Schematic\" does not exist or is not first");
      } else {
         Map schematic;
         if (!(schematic = schematicTag.getValue()).containsKey("Blocks")) {
            throw new IllegalArgumentException("Schematic file is missing a \"Blocks\" tag");
         } else {
            short width = ((ShortTag)this.getChildTag(schematic, "Width", ShortTag.class)).getValue();
            short length = ((ShortTag)this.getChildTag(schematic, "Length", ShortTag.class)).getValue();
            short height = ((ShortTag)this.getChildTag(schematic, "Height", ShortTag.class)).getValue();
            byte[] blockId = ((ByteArrayTag)this.getChildTag(schematic, "Blocks", ByteArrayTag.class)).getValue();
            byte[] blockData = ((ByteArrayTag)this.getChildTag(schematic, "Data", ByteArrayTag.class)).getValue();
            byte[] addId = new byte[0];
            short[] blocks = new short[blockId.length];
            if (schematic.containsKey("AddBlocks")) {
               addId = ((ByteArrayTag)this.getChildTag(schematic, "AddBlocks", ByteArrayTag.class)).getValue();
            }

            for(int index = 0; index < blockId.length; ++index) {
               if (index >> 1 >= addId.length) {
                  blocks[index] = (short)(blockId[index] & 255);
               } else if ((index & 1) == 0) {
                  blocks[index] = (short)(((addId[index >> 1] & 15) << 8) + (blockId[index] & 255));
               } else {
                  blocks[index] = (short)(((addId[index >> 1] & 240) << 4) + (blockId[index] & 255));
               }
            }

            return new Schematic(blocks, blockData, width, length, height);
         }
      }
   }

   private <T extends Tag> Tag getChildTag(Map<String, Tag> items, String key, Class<T> expected) throws DataException {
      if (!items.containsKey(key)) {
         throw new DataException("Schematic file is missing a \"" + key + "\" tag");
      } else {
         Tag tag = items.get(key);
         if (!expected.isInstance(tag)) {
            throw new DataException(key + " tag is not of tag type " + expected.getName());
         } else {
            return expected.cast(tag);
         }
      }
   }

   public static Schematic getInstance() {
      return instance;
   }

   public short[] getBlocks() {
      return this.blocks;
   }

   public byte[] getData() {
      return this.data;
   }

   public short getWidth() {
      return this.width;
   }

   public short getLenght() {
      return this.lenght;
   }

   public short getHeight() {
      return this.height;
   }

   public Schematic() {
   }
}

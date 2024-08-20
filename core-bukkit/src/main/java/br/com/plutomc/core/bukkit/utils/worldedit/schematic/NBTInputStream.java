package br.com.plutomc.core.bukkit.utils.worldedit.schematic;

import java.io.Closeable;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

public final class NBTInputStream implements Closeable {
   private final DataInputStream is;

   public NBTInputStream(InputStream is) throws IOException {
      this.is = new DataInputStream(new GZIPInputStream(is));
   }

   public Tag readTag() throws IOException {
      return this.readTag(0);
   }

   private Tag readTag(int depth) throws IOException {
      int type = this.is.readByte() & 255;
      String name;
      if (type != 0) {
         int nameLength = this.is.readShort() & '\uffff';
         byte[] nameBytes = new byte[nameLength];
         this.is.readFully(nameBytes);
         name = new String(nameBytes, NBTConstants.CHARSET);
      } else {
         name = "";
      }

      return this.readTagPayload(type, name, depth);
   }

   private Tag readTagPayload(int type, String name, int depth) throws IOException {
      switch(type) {
         case 0:
            if (depth == 0) {
               throw new IOException("TAG_End found without a TAG_Compound/TAG_List tag preceding it.");
            }

            return new EndTag();
         case 1:
            return new ByteTag(name, this.is.readByte());
         case 2:
            return new ShortTag(name, this.is.readShort());
         case 3:
            return new IntTag(name, this.is.readInt());
         case 4:
            return new LongTag(name, this.is.readLong());
         case 5:
            return new FloatTag(name, this.is.readFloat());
         case 6:
            return new DoubleTag(name, this.is.readDouble());
         case 7: {
            int length = this.is.readInt();
            byte[] bytes = new byte[length];
            this.is.readFully(bytes);
            return new ByteArrayTag(name, bytes);
         }
         case 8: {
            int length = this.is.readShort();
            byte[] bytes = new byte[length];
            this.is.readFully(bytes);
            return new StringTag(name, new String(bytes, NBTConstants.CHARSET));
         }
         case 9: {
            int childType = this.is.readByte();
            int length = this.is.readInt();
            List<Tag> tagList = new ArrayList<>();

            for(int i = 0; i < length; ++i) {
               Tag tag = this.readTagPayload(childType, "", depth + 1);
               if (tag instanceof EndTag) {
                  throw new IOException("TAG_End not permitted in a list.");
               }

               tagList.add(tag);
            }

            return new ListTag(name, NBTUtils.getTypeClass(childType), tagList);
         }
         case 10:
            Map<String, Tag> tagMap = new HashMap<>();

            while(true) {
               Tag tag = this.readTag(depth + 1);
               if (tag instanceof EndTag) {
                  return new CompoundTag(name, tagMap);
               }

               tagMap.put(tag.getName(), tag);
            }
         default:
            throw new IOException("Invalid tag type: " + type + ".");
      }
   }

   @Override
   public void close() throws IOException {
      this.is.close();
   }
}

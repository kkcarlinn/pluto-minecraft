package br.com.plutomc.core.common.utils.configuration;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import lombok.NonNull;
import br.com.plutomc.core.common.utils.FileCreator;
import br.com.plutomc.core.common.utils.NumberConversions;

public interface Configuration {
   FileCreator FILE_CREATOR = new DefaultFileCreator();

   Configuration loadConfig() throws FileNotFoundException, Exception;

   boolean saveConfig() throws FileNotFoundException, Exception;

   String getFileName();

   String getFilePath();

   Configuration defaultSave(boolean var1);

   Map<String, Object> getValues();

   <T extends Configuration> T watch() throws Exception;

   <T> boolean set(@NonNull String var1, T var2);

   Object getAsObject(@NonNull String var1, Object var2, boolean var3);

   <T> T get(@NonNull String var1, T var2, boolean var3);

   default <T> T get(@NonNull String fieldName, T defaultValue) {
      if (fieldName == null) {
         throw new NullPointerException("fieldName is marked non-null but is null");
      } else {
         return this.get(fieldName, defaultValue, false);
      }
   }

   default Object get(@NonNull String fieldName) {
      if (fieldName == null) {
         throw new NullPointerException("fieldName is marked non-null but is null");
      } else {
         return this.get(fieldName, null, false);
      }
   }

   default String getString(@NonNull String fieldName, String defaultValue, boolean saveDefaultValue) {
      if (fieldName == null) {
         throw new NullPointerException("fieldName is marked non-null but is null");
      } else {
         return String.class.cast(this.get(fieldName, defaultValue, saveDefaultValue));
      }
   }

   default String getString(@NonNull String fieldName) {
      if (fieldName == null) {
         throw new NullPointerException("fieldName is marked non-null but is null");
      } else {
         return this.getString(fieldName, null, false);
      }
   }

   default String getString(@NonNull String fieldName, String defaultValue) {
      if (fieldName == null) {
         throw new NullPointerException("fieldName is marked non-null but is null");
      } else {
         return this.getString(fieldName, defaultValue, false);
      }
   }

   default <T> T getAs(@NonNull String fieldName, T defaultValue, boolean saveDefaultValue, Class<T> clazz) {
      if (fieldName == null) {
         throw new NullPointerException("fieldName is marked non-null but is null");
      } else {
         return clazz.cast(this.get(fieldName, defaultValue, saveDefaultValue));
      }
   }

   default <T> T getAs(@NonNull String fieldName, T defaultValue, Class<T> clazz) {
      if (fieldName == null) {
         throw new NullPointerException("fieldName is marked non-null but is null");
      } else {
         return this.getAs(fieldName, defaultValue, false, clazz);
      }
   }

   default <T> T getAs(@NonNull String fieldName, Class<T> clazz) {
      if (fieldName == null) {
         throw new NullPointerException("fieldName is marked non-null but is null");
      } else {
         return this.getAs(fieldName, (T)null, false, clazz);
      }
   }

   default OptionalInt getInt(@NonNull String fieldName, int defaultValue, boolean saveDefaultValue) {
      if (fieldName == null) {
         throw new NullPointerException("fieldName is marked non-null but is null");
      } else {
         Object object = this.getAsObject(fieldName, defaultValue, saveDefaultValue);
         return OptionalInt.of(object instanceof Number ? NumberConversions.toInt(object) : 0);
      }
   }

   default OptionalInt getInt(@NonNull String fieldName) {
      if (fieldName == null) {
         throw new NullPointerException("fieldName is marked non-null but is null");
      } else {
         return this.getInt(fieldName, 0, false);
      }
   }

   default OptionalInt getInt(@NonNull String fieldName, int defaultValue) {
      if (fieldName == null) {
         throw new NullPointerException("fieldName is marked non-null but is null");
      } else {
         return this.getInt(fieldName, defaultValue, false);
      }
   }

   default OptionalDouble getDouble(@NonNull String fieldName, double defaultValue, boolean saveDefaultValue) {
      if (fieldName == null) {
         throw new NullPointerException("fieldName is marked non-null but is null");
      } else {
         Object object = this.getAsObject(fieldName, defaultValue, saveDefaultValue);
         return OptionalDouble.of(object instanceof Number ? NumberConversions.toDouble(object) : 0.0);
      }
   }

   default OptionalDouble getDouble(@NonNull String fieldName, Double defaultValue) {
      if (fieldName == null) {
         throw new NullPointerException("fieldName is marked non-null but is null");
      } else {
         return this.getDouble(fieldName, defaultValue, false);
      }
   }

   default OptionalDouble getDouble(@NonNull String fieldName) {
      if (fieldName == null) {
         throw new NullPointerException("fieldName is marked non-null but is null");
      } else {
         return this.getDouble(fieldName, 0.0, false);
      }
   }

   default OptionalLong getLong(@NonNull String fieldName, long defaultValue, boolean saveDefaultValue) {
      if (fieldName == null) {
         throw new NullPointerException("fieldName is marked non-null but is null");
      } else {
         Object object = this.getAsObject(fieldName, defaultValue, saveDefaultValue);
         return OptionalLong.of(object instanceof Number ? NumberConversions.toLong(object) : 0L);
      }
   }

   default OptionalLong getLong(@NonNull String fieldName, Long defaultValue) {
      if (fieldName == null) {
         throw new NullPointerException("fieldName is marked non-null but is null");
      } else {
         return this.getLong(fieldName, defaultValue, false);
      }
   }

   default OptionalLong getLong(@NonNull String fieldName) {
      if (fieldName == null) {
         throw new NullPointerException("fieldName is marked non-null but is null");
      } else {
         return this.getLong(fieldName, 0L, false);
      }
   }

   default float getFloat(@NonNull String fieldName, float defaultValue, boolean saveDefaultValue) {
      if (fieldName == null) {
         throw new NullPointerException("fieldName is marked non-null but is null");
      } else {
         Object object = this.getAsObject(fieldName, defaultValue, saveDefaultValue);
         return object instanceof Number ? NumberConversions.toFloat(object) : 0.0F;
      }
   }

   default float getFloat(@NonNull String fieldName, float defaultValue) {
      if (fieldName == null) {
         throw new NullPointerException("fieldName is marked non-null but is null");
      } else {
         return this.getFloat(fieldName, defaultValue, false);
      }
   }

   default float getFloat(@NonNull String fieldName) {
      if (fieldName == null) {
         throw new NullPointerException("fieldName is marked non-null but is null");
      } else {
         return this.getFloat(fieldName, 0.0F, false);
      }
   }

   default boolean getBoolean(@NonNull String fieldName, Boolean defaultValue, boolean saveDefaultValue) {
      if (fieldName == null) {
         throw new NullPointerException("fieldName is marked non-null but is null");
      } else {
         return Boolean.class.cast(this.getAs(fieldName, defaultValue, saveDefaultValue, Boolean.class));
      }
   }

   default boolean getBoolean(@NonNull String fieldName, boolean defaultValue) {
      if (fieldName == null) {
         throw new NullPointerException("fieldName is marked non-null but is null");
      } else {
         return this.getBoolean(fieldName, defaultValue, false);
      }
   }

   default boolean getBoolean(@NonNull String fieldName) {
      if (fieldName == null) {
         throw new NullPointerException("fieldName is marked non-null but is null");
      } else {
         return this.getBoolean(fieldName, false, false);
      }
   }

   <T> List<T> getList(String var1, List<T> var2, boolean var3, Class<T> var4);

   default <T> List<T> getList(String fieldName, Class<T> clazz) {
      return this.getList(fieldName, new ArrayList<>(), false, clazz);
   }

   <T> boolean addElementToList(String var1, T var2);

   <T> boolean setElementToList(String var1, int var2, T var3);
}

package br.com.plutomc.core.common.utils.configuration.impl;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.stream.JsonReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import lombok.NonNull;
import br.com.plutomc.core.common.CommonConst;
import br.com.plutomc.core.common.CommonPlugin;
import br.com.plutomc.core.common.utils.FileWatcher;
import br.com.plutomc.core.common.utils.configuration.Configuration;

public class JsonConfiguration implements Configuration {
   private final String fileName;
   private final String filePath;
   private boolean defaultSave;
   private Runnable watchChanges;
   private Map<String, Object> map;
   private Set<String> verifySet = new HashSet<>();

   public static void main(String[] args) {
      Configuration jsonConfiguration = new JsonConfiguration("bedwars.json", "C:\\Users\\ALLAN\\Desktop\\high\\Server\\Bedwars 2\\plugins\\GameAPI")
         .defaultSave(true);

      try {
         jsonConfiguration.loadConfig();
      } catch (Exception var3) {
         var3.printStackTrace();
      }

      System.out.println(jsonConfiguration.getInt("maxPlayers", 8));
      jsonConfiguration.set("maxPlayers", 8);
   }

   public JsonConfiguration(String fileName, String filePath, boolean defaultSave) {
      this(fileName, filePath);
      this.defaultSave = defaultSave;
   }

   @Override
   public Configuration loadConfig() throws FileNotFoundException, Exception {
      FileInputStream fileInputStream = new FileInputStream(FILE_CREATOR.createFile(this.fileName, this.filePath));
      InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, "UTF-8");
      JsonReader jsonReader = new JsonReader(inputStreamReader);
      this.map = CommonConst.GSON_PRETTY.fromJson(jsonReader, Map.class);

      for(Entry<String, Object> entry : this.map.entrySet()) {
         Object object = entry.getValue();
         if (object instanceof LinkedTreeMap) {
            JsonElement jsonElement = CommonConst.GSON.toJsonTree(object);
            entry.setValue(jsonElement);
         }
      }

      jsonReader.close();
      inputStreamReader.close();
      fileInputStream.close();
      this.verifySet.clear();
      return this;
   }

   @Override
   public boolean saveConfig() throws FileNotFoundException, Exception {
      String json = CommonConst.GSON_PRETTY.toJson(this.map);
      FileOutputStream fileOutputStream = new FileOutputStream(FILE_CREATOR.createFile(this.fileName, this.filePath));
      OutputStreamWriter outputStreamReader = new OutputStreamWriter(fileOutputStream, "UTF-8");
      BufferedWriter bufferedWriter = new BufferedWriter(outputStreamReader);
      bufferedWriter.write(json);
      bufferedWriter.flush();
      bufferedWriter.close();
      fileOutputStream.close();
      outputStreamReader.close();
      return true;
   }

   @Override
   public Configuration defaultSave(boolean defaultSave) {
      this.defaultSave = defaultSave;
      return this;
   }

   @Override
   public Map<String, Object> getValues() {
      return ImmutableMap.copyOf(this.map);
   }

   @Override
   public <T> boolean set(@NonNull String fieldName, T value) {
      if (fieldName == null) {
         throw new NullPointerException("fieldName is marked non-null but is null");
      } else {
         this.map.put(fieldName, value);
         if (this.defaultSave) {
            try {
               return this.saveConfig();
            } catch (Exception var4) {
               var4.printStackTrace();
            }
         }

         return true;
      }
   }

   @Override
   public Object getAsObject(@NonNull String fieldName, Object defaultValue, boolean saveDefaultValue) {
      if (fieldName == null) {
         throw new NullPointerException("fieldName is marked non-null but is null");
      } else if (this.map.containsKey(fieldName)) {
         return this.map.get(fieldName);
      } else {
         if (saveDefaultValue && defaultValue != null) {
            this.set(fieldName, defaultValue);
         }

         return defaultValue;
      }
   }

   @Override
   public <T> T get(@NonNull String fieldName, T defaultValue, boolean saveDefaultValue) {
      if (fieldName == null) {
         throw new NullPointerException("fieldName is marked non-null but is null");
      } else if (this.map.containsKey(fieldName)) {
         return (T)this.map.get(fieldName);
      } else {
         if (saveDefaultValue && defaultValue != null) {
            this.set(fieldName, defaultValue);
         }

         return defaultValue;
      }
   }

   @Override
   public <T> List<T> getList(String fieldName, List<T> defaultValue, boolean saveDefaultValue, Class<T> clazz) {
      if (this.map.containsKey(fieldName)) {
         Object object = this.map.get(fieldName);
         if (object instanceof List) {
            if (!this.verifySet.contains(fieldName)) {
               List<T> list = (List)object;
               int index = 0;

               for(T t : list) {
                  if (t instanceof LinkedTreeMap) {
                     JsonElement jsonElement = CommonConst.GSON_PRETTY.toJsonTree(t);
                     list.set(index, CommonConst.GSON_PRETTY.fromJson(jsonElement, clazz));
                  }

                  ++index;
               }

               this.verifySet.add(fieldName);
            }

            return List.class.cast(object);
         }
      }

      if (saveDefaultValue && defaultValue != null) {
         this.set(fieldName, defaultValue);
      }

      return defaultValue;
   }

   @Override
   public <T> boolean addElementToList(String fieldName, T value) {
      ((List)this.map.computeIfAbsent(fieldName, v -> new ArrayList())).add(value);
      if (this.defaultSave) {
         try {
            return this.saveConfig();
         } catch (Exception var4) {
            var4.printStackTrace();
         }
      }

      return true;
   }

   @Override
   public <T> boolean setElementToList(String fieldName, int index, T value) {
      ((List)this.map.computeIfAbsent(fieldName, v -> new ArrayList())).set(index, value);
      if (this.defaultSave) {
         try {
            return this.saveConfig();
         } catch (Exception var5) {
            var5.printStackTrace();
         }
      }

      return true;
   }

   public JsonConfiguration watch() throws Exception {
      if (this.watchChanges == null) {
         CommonPlugin.getInstance().getPluginPlatform().run(this.watchChanges = new FileWatcher(FILE_CREATOR.createFile(this.fileName, this.filePath)) {
            @Override
            public void onChange() {
               try {
                  JsonConfiguration.this.loadConfig();
               } catch (Exception var2) {
                  var2.printStackTrace();
               }
            }
         }, 100L, 100L);
         return this;
      } else {
         throw new Exception("Cannot disable watch file");
      }
   }

   @Override
   public String getFileName() {
      return this.fileName;
   }

   @Override
   public String getFilePath() {
      return this.filePath;
   }

   public boolean isDefaultSave() {
      return this.defaultSave;
   }

   public Runnable getWatchChanges() {
      return this.watchChanges;
   }

   public Map<String, Object> getMap() {
      return this.map;
   }

   public Set<String> getVerifySet() {
      return this.verifySet;
   }

   public JsonConfiguration(String fileName, String filePath) {
      this.fileName = fileName;
      this.filePath = filePath;
   }
}

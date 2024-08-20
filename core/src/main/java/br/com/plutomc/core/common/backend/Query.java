package br.com.plutomc.core.common.backend;

import java.util.Collection;

import br.com.plutomc.core.common.utils.Callback;

public interface Query<T> {
   Collection<T> find();

   Collection<T> find(String var1);

   <GenericType> Collection<T> find(String var1, GenericType var2);

   <GenericType> Collection<T> find(String var1, String var2, GenericType var3);

   <GenericType> T findOne(String var1, GenericType var2);

   <GenericType> T findOne(String var1, String var2, GenericType var3);

   void create(String[] var1);

   void create(String var1, String[] var2);

   <GenericType> void deleteOne(String var1, GenericType var2);

   <GenericType> void deleteOne(String var1, String var2, GenericType var3);

   <GenericType> void updateOne(String var1, GenericType var2, T var3);

   <GenericType> void updateOne(String var1, String var2, GenericType var3, T var4);

   <GenericType> Collection<T> ranking(String var1, GenericType var2, int var3);

   public static class QueryResponse<T> {
      private long startTime = System.currentTimeMillis();
      private long durationTime;
      private Callback<T> callback;

      public QueryResponse(Callback<T> callback) {
         this.callback = callback;
      }

      public void callback(T t) {
         this.callback.callback(t);
      }

      public long getStartTime() {
         return this.startTime;
      }

      public long getDurationTime() {
         return this.durationTime;
      }

      public Callback<T> getCallback() {
         return this.callback;
      }
   }
}

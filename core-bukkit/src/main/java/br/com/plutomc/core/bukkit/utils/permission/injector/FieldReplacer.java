package br.com.plutomc.core.bukkit.utils.permission.injector;

import java.lang.reflect.Field;

public class FieldReplacer<Instance, Type> {
   private final Class<Type> requiredType;
   private final Field field;

   public FieldReplacer(Class<? extends Instance> clazz, String fieldName, Class<Type> requiredType) {
      this.requiredType = requiredType;
      this.field = getField(clazz, fieldName);
      if (this.field == null) {
         throw new ExceptionInInitializerError("No such field " + fieldName + " in class " + clazz);
      } else {
         this.field.setAccessible(true);
         if (!requiredType.isAssignableFrom(this.field.getType())) {
            throw new ExceptionInInitializerError("Field of wrong type");
         }
      }
   }

   public Type get(Instance instance) {
      try {
         return this.requiredType.cast(this.field.get(instance));
      } catch (IllegalAccessException var3) {
         throw new Error(var3);
      }
   }

   public void set(Instance instance, Type newValue) {
      try {
         this.field.set(instance, newValue);
      } catch (IllegalAccessException var4) {
         throw new Error(var4);
      }
   }

   private static Field getField(Class<?> clazz, String fieldName) {
      while(clazz != null && clazz != Object.class) {
         try {
            return clazz.getDeclaredField(fieldName);
         } catch (NoSuchFieldException var3) {
            clazz = clazz.getSuperclass();
         }
      }

      return null;
   }
}

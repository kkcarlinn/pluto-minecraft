package br.com.plutomc.core.bukkit.utils.permission.injector.regexperms;

import java.lang.reflect.Field;
import java.util.List;
import java.util.logging.Logger;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.PermissibleBase;

public abstract class PermissibleInjector {
   protected final String clazzName;
   protected final String fieldName;
   protected final boolean copyValues;

   public PermissibleInjector(String clazzName, String fieldName, boolean copyValues) {
      this.clazzName = clazzName;
      this.fieldName = fieldName;
      this.copyValues = copyValues;
   }

   public Permissible inject(Player player, Permissible permissible) throws NoSuchFieldException, IllegalAccessException {
      Field permField = this.getPermissibleField(player);
      if (permField == null) {
         return null;
      } else {
         Permissible oldPerm = (Permissible)permField.get(player);
         if (this.copyValues && permissible instanceof PermissibleBase) {
            PermissibleBase newBase = (PermissibleBase)permissible;
            PermissibleBase oldBase = (PermissibleBase)oldPerm;
            this.copyValues(oldBase, newBase);
         }

         permField.set(player, permissible);
         return oldPerm;
      }
   }

   public Permissible getPermissible(Player player) throws NoSuchFieldException, IllegalAccessException {
      return (Permissible)this.getPermissibleField(player).get(player);
   }

   private Field getPermissibleField(Player player) throws NoSuchFieldException {
      Class<?> humanEntity;
      try {
         humanEntity = Class.forName(this.clazzName);
      } catch (ClassNotFoundException var4) {
         Logger.getLogger("yUtils").warning("[yUtils] Instance nao encontrada");
         return null;
      }

      if (!humanEntity.isAssignableFrom(player.getClass())) {
         Logger.getLogger("yUtils").warning("[yUtils] Erro estranho enquanto injeta permissivel");
         return null;
      } else {
         Field permField = humanEntity.getDeclaredField(this.fieldName);
         permField.setAccessible(true);
         return permField;
      }
   }

   private void copyValues(PermissibleBase old, PermissibleBase newPerm) throws NoSuchFieldException, IllegalAccessException {
      Field attachmentField = PermissibleBase.class.getDeclaredField("attachments");
      attachmentField.setAccessible(true);
      List<Object> attachmentPerms = (List)attachmentField.get(newPerm);
      attachmentPerms.clear();
      attachmentPerms.addAll((List)attachmentField.get(old));
      newPerm.recalculatePermissions();
   }

   public abstract boolean isApplicable(Player var1);

   public static class ClassNameRegexPermissibleInjector extends PermissibleInjector {
      private final String regex;

      public ClassNameRegexPermissibleInjector(String clazz, String field, boolean copyValues, String regex) {
         super(clazz, field, copyValues);
         this.regex = regex;
      }

      @Override
      public boolean isApplicable(Player player) {
         return player.getClass().getName().matches(this.regex);
      }
   }

   public static class ClassPresencePermissibleInjector extends PermissibleInjector {
      public ClassPresencePermissibleInjector(String clazzName, String fieldName, boolean copyValues) {
         super(clazzName, fieldName, copyValues);
      }

      @Override
      public boolean isApplicable(Player player) {
         try {
            return Class.forName(this.clazzName).isInstance(player);
         } catch (ClassNotFoundException var3) {
            return false;
         }
      }
   }

   public static class ServerNamePermissibleInjector extends PermissibleInjector {
      protected final String serverName;

      public ServerNamePermissibleInjector(String clazz, String field, boolean copyValues, String serverName) {
         super(clazz, field, copyValues);
         this.serverName = serverName;
      }

      @Override
      public boolean isApplicable(Player player) {
         return player.getServer().getName().equalsIgnoreCase(this.serverName);
      }
   }
}

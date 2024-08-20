package br.com.plutomc.core.common.command;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import br.com.plutomc.core.common.CommonPlugin;
import br.com.plutomc.core.common.utils.ClassGetter;

public interface CommandFramework {
   Class<?> getJarClass();

   void registerCommands(CommandClass var1);

   default CommandFramework loadCommands(String packageName) {
      for(Class<?> commandClass : ClassGetter.getClassesForPackage(this.getJarClass(), packageName)) {
         if (CommandClass.class != commandClass && CommandClass.class.isAssignableFrom(commandClass)) {
            try {
               this.registerCommands((CommandClass)commandClass.newInstance());
            } catch (Exception var5) {
               CommonPlugin.getInstance().getLogger().warning("Error when loading command from " + commandClass.getSimpleName() + "!");
               var5.printStackTrace();
            }
         }
      }

      return this;
   }

   default CommandFramework loadCommands(Class<?> jarClass, String packageName) {
      for(Class<?> commandClass : ClassGetter.getClassesForPackage(jarClass, packageName)) {
         if (CommandClass.class.isAssignableFrom(commandClass)) {
            try {
               this.registerCommands((CommandClass)commandClass.newInstance());
            } catch (Exception var6) {
               CommonPlugin.getInstance().getLogger().warning("Error when loading command from " + commandClass.getSimpleName() + "!");
               var6.printStackTrace();
            }
         }
      }

      return this;
   }

   @Target({ElementType.METHOD})
   @Retention(RetentionPolicy.RUNTIME)
   public @interface Command {
      String name();

      String permission() default "";

      String[] aliases() default {};

      String description() default "";

      String usage() default "";

      boolean runAsync() default false;

      boolean console() default true;
   }

   @Target({ElementType.METHOD})
   @Retention(RetentionPolicy.RUNTIME)
   public @interface Completer {
      String name();

      String[] aliases() default {};
   }
}

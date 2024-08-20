package br.com.plutomc.core.bukkit.utils.permission.injector;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.regex.Pattern;

import br.com.plutomc.core.bukkit.utils.permission.injector.loader.LoaderNormal;

public class RegExpMatcher implements PermissionMatcher {
   public static final String RAW_REGEX_CHAR = "$";
   protected static Pattern rangeExpression = Pattern.compile("(\\d+)-(\\d+)");
   private Object patternCache;

   public RegExpMatcher() {
      Class<?> cacheBuilder = this.getClassGuava("com.google.common.cache.CacheBuilder");
      Class<?> cacheLoader = this.getClassGuava("com.google.common.cache.CacheLoader");

      try {
         Object obj = cacheBuilder.getMethod("newBuilder").invoke(null);
         Method maximumSize = obj.getClass().getMethod("maximumSize", Long.TYPE);
         Object obj2 = maximumSize.invoke(obj, 500);
         Object loader = new LoaderNormal();
         Method build = obj2.getClass().getMethod("build", cacheLoader);
         this.patternCache = build.invoke(obj2, loader);
      } catch (Exception var8) {
         var8.printStackTrace();
      }
   }

   @Override
   public boolean isMatches(String expression, String permission) {
      try {
         Method get = this.patternCache.getClass().getMethod("get", Object.class);
         get.setAccessible(true);
         Object obj = get.invoke(this.patternCache, expression);
         return ((Pattern)obj).matcher(permission).matches();
      } catch (IllegalArgumentException var5) {
         var5.printStackTrace();
      } catch (IllegalAccessException var6) {
         var6.printStackTrace();
      } catch (InvocationTargetException var7) {
         var7.printStackTrace();
      } catch (NoSuchMethodException var8) {
         var8.printStackTrace();
      } catch (SecurityException var9) {
         var9.printStackTrace();
      }

      return false;
   }

   private Class<?> getClassGuava(String str) {
      Class<?> clasee = null;

      try {
         if (this.hasNetUtil()) {
            str = "net.minecraft.util." + str;
         }

         clasee = Class.forName(str);
      } catch (ClassNotFoundException var4) {
         var4.printStackTrace();
      }

      return clasee;
   }

   private boolean hasNetUtil() {
      try {
         Class.forName("net.minecraft.util.com.google.common.cache.LoadingCache");
         return true;
      } catch (ClassNotFoundException var2) {
         return false;
      }
   }
}

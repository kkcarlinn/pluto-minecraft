package br.com.plutomc.core.bukkit.utils.helper;

import java.lang.reflect.Field;
import br.com.plutomc.core.common.language.Language;

public class SkullHelper {
   public static final String EARTH_SKULL = "e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTc5ZTIyNDhiZDc5OGViNjFmOTdhZWVlY2MyYWZkZGViYWQ1MmJmNDA1MmM3MjYxYjYxODBhNDU3N2Y4NjkzYSJ9fX0==";
   public static final String PORTUGUESE_SKULL = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTY2OGExZmI2YWY4MWIyMzFiYmNjNGRlNWY3Zjk1ODAzYmJkMTk0ZjU4MjdkYTAyN2ZhNzAzMjFjZjQ3YyJ9fX0=";
   public static final String ENGLISH_SKULL = "ewogICJ0aW1lc3RhbXAiIDogMTY2NDQyMzQ5NzcyMSwKICAicHJvZmlsZUlkIiA6ICJhOWI5ZWZiYmY0ODM0M2ZlYjNmYjcwZjA0MDQ5MDMzZiIsCiAgInByb2ZpbGVOYW1lIiA6ICJGYXBpIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2I0ODg3ZmY1ODhjOWU2ZDc2MjBmMjRmM2NlZWQ0YjEwM2JkMzczOWMxZDBiOGU2N2IwZWFhM2Q2MzQ3Mzg0MTMiCiAgICB9CiAgfQp9";

   public static String getLanguageSkin(Language language) {
      try {
         Field declaredField = SkullHelper.class.getDeclaredField(language.name() + "_SKULL");
         return (String)declaredField.get(null);
      } catch (Exception var2) {
         var2.printStackTrace();
         return "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTY2OGExZmI2YWY4MWIyMzFiYmNjNGRlNWY3Zjk1ODAzYmJkMTk0ZjU4MjdkYTAyN2ZhNzAzMjFjZjQ3YyJ9fX0=";
      }
   }
}

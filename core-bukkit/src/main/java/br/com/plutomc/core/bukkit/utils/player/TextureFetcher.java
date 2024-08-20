package br.com.plutomc.core.bukkit.utils.player;

import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import com.comphenix.protocol.wrappers.WrappedSignedProperty;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.TimeUnit;
import br.com.plutomc.core.common.CommonPlugin;

public class TextureFetcher {
   public static final LoadingCache<WrappedGameProfile, WrappedSignedProperty> TEXTURE = CacheBuilder.newBuilder()
      .expireAfterWrite(30L, TimeUnit.MINUTES)
      .build(
         new CacheLoader<WrappedGameProfile, WrappedSignedProperty>() {
            public WrappedSignedProperty load(WrappedGameProfile profile) throws Exception {
               try {
                  Object minecraftServer = MinecraftReflection.getMinecraftServerClass().getMethod("getServer").invoke(null);
                  ((MinecraftSessionService)minecraftServer.getClass().getMethod("aD").invoke(minecraftServer))
                     .fillProfileProperties((GameProfile)profile.getHandle(), true);
               } catch (IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException | IllegalAccessException var3) {
                  var3.printStackTrace();
               }
      
               if (profile.getProperties().containsKey("textures")) {
                  return (WrappedSignedProperty)profile.getProperties().get("textures").stream().findFirst().orElse(null);
               } else {
                  String[] properties = CommonPlugin.getInstance().getSkinData().loadSkinById(profile.getUUID());
                  return properties == null ? null : new WrappedSignedProperty(profile.getName(), properties[0], properties[1]);
               }
            }
         }
      );

   public static WrappedSignedProperty loadTexture(WrappedGameProfile wrappedGameProfile) {
      return (WrappedSignedProperty)TEXTURE.getUnchecked(wrappedGameProfile);
   }
}

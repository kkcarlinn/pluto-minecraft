package br.com.plutomc.core.bukkit.utils.player;

import br.com.plutomc.core.bukkit.BukkitCommon;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.PacketType.Play.Server;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.reflect.FieldAccessException;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.PlayerInfoData;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import com.comphenix.protocol.wrappers.WrappedSignedProperty;
import com.comphenix.protocol.wrappers.EnumWrappers.Difficulty;
import com.comphenix.protocol.wrappers.EnumWrappers.NativeGameMode;
import com.comphenix.protocol.wrappers.EnumWrappers.PlayerInfoAction;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import br.com.plutomc.core.common.CommonConst;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerAPI {
   public static void changePlayerName(Player player, String name) {
      changePlayerName(player, name, true);
   }

   public static void changePlayerName(Player player, String name, boolean respawn) {
      if (respawn) {
         removeFromTab(player);
      }

      try {
         Object minecraftServer = MinecraftReflection.getMinecraftServerClass().getMethod("getServer").invoke(null);
         Object playerList = minecraftServer.getClass().getMethod("getPlayerList").invoke(minecraftServer);
         Field f = playerList.getClass().getSuperclass().getDeclaredField("playersByName");
         f.setAccessible(true);
         Map<String, Object> playersByName = (Map)f.get(playerList);
         playersByName.remove(player.getName());
         WrappedGameProfile profile = WrappedGameProfile.fromPlayer(player);
         Field field = profile.getHandle().getClass().getDeclaredField("name");
         field.setAccessible(true);
         field.set(profile.getHandle(), name);
         field.setAccessible(false);
         playersByName.put(name, MinecraftReflection.getCraftPlayerClass().getMethod("getHandle").invoke(player));
         f.setAccessible(false);
      } catch (Exception var9) {
         var9.printStackTrace();
      }

      if (respawn) {
         respawnPlayer(player);
      }
   }

   public void addToTab(Player player, Collection<? extends Player> players) {
      PacketContainer packet = new PacketContainer(Server.PLAYER_INFO);
      packet.getPlayerInfoAction().write(0, PlayerInfoAction.ADD_PLAYER);
      if (player.getGameMode() != null) {
         try {
            Object entityPlayer = MinecraftReflection.getCraftPlayerClass().getMethod("getHandle").invoke(player);
            Object getDisplayName = MinecraftReflection.getEntityPlayerClass().getMethod("getPlayerListName").invoke(entityPlayer);
            packet.getPlayerInfoDataLists()
               .write(
                  0,
                  Arrays.asList(
                     new PlayerInfoData(
                        WrappedGameProfile.fromPlayer(player),
                        0,
                        NativeGameMode.fromBukkit(player.getGameMode()),
                        getDisplayName != null ? WrappedChatComponent.fromHandle(getDisplayName) : null
                     )
                  )
               );
         } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException | FieldAccessException var8) {
            var8.printStackTrace();
         }
      }

      for(Player online : players) {
         if (online.canSee(player)) {
             ProtocolLibrary.getProtocolManager().sendServerPacket(online, packet);
         }
      }
   }

   public static void removeFromTab(Player player) {
      PacketContainer packet = new PacketContainer(Server.PLAYER_INFO);
      packet.getPlayerInfoAction().write(0, PlayerInfoAction.REMOVE_PLAYER);
      if (player.getGameMode() != null) {
         try {
            Object entityPlayer = MinecraftReflection.getCraftPlayerClass().getMethod("getHandle").invoke(player);
            Object getDisplayName = MinecraftReflection.getEntityPlayerClass().getMethod("getPlayerListName").invoke(entityPlayer);
            packet.getPlayerInfoDataLists()
               .write(
                  0,
                  Arrays.asList(
                     new PlayerInfoData(
                        WrappedGameProfile.fromPlayer(player),
                        0,
                        NativeGameMode.fromBukkit(player.getGameMode()),
                        getDisplayName != null ? WrappedChatComponent.fromHandle(getDisplayName) : null
                     )
                  )
               );
         } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException | FieldAccessException var6) {
            var6.printStackTrace();
         }
      }

      for(Player online : Bukkit.getOnlinePlayers()) {
         if (online.canSee(player)) {
             ProtocolLibrary.getProtocolManager().sendServerPacket(online, packet);
         }
      }
   }

   public static void respawnPlayer(final Player player) {
      respawnSelf(player);
      (new BukkitRunnable() {
            @Override
            public void run() {
               Bukkit.getOnlinePlayers()
                  .stream()
                  .filter(onlinePlayer -> !onlinePlayer.equals(player))
                  .filter(onlinePlayer -> onlinePlayer.canSee(player))
                  .forEach(onlinePlayer -> {
                     onlinePlayer.hidePlayer(player);
                     onlinePlayer.showPlayer(player);
                  });
            }
         })
         .runTaskLater(BukkitCommon.getInstance(), 2L);
   }

   public static void respawnSelf(final Player player) {
      List<PlayerInfoData> data = new ArrayList();
      if (player.getGameMode() != null) {
         try {
            Object entityPlayer = MinecraftReflection.getCraftPlayerClass().getMethod("getHandle").invoke(player);
            Object getDisplayName = MinecraftReflection.getEntityPlayerClass().getMethod("getPlayerListName").invoke(entityPlayer);
            int ping = (int) MinecraftReflection.getEntityPlayerClass().getField("ping").get(entityPlayer);
            data.add(
               new PlayerInfoData(
                  WrappedGameProfile.fromPlayer(player),
                  ping,
                  NativeGameMode.fromBukkit(player.getGameMode()),
                  getDisplayName != null ? WrappedChatComponent.fromHandle(getDisplayName) : null
               )
            );
         } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException | NoSuchFieldException | FieldAccessException var6) {
            var6.printStackTrace();
         }
      }

      final PacketContainer addPlayerInfo = new PacketContainer(Server.PLAYER_INFO);
      addPlayerInfo.getPlayerInfoAction().write(0, PlayerInfoAction.ADD_PLAYER);
      addPlayerInfo.getPlayerInfoDataLists().write(0, data);
      final PacketContainer removePlayerInfo = new PacketContainer(Server.PLAYER_INFO);
      removePlayerInfo.getPlayerInfoAction().write(0, PlayerInfoAction.REMOVE_PLAYER);
      removePlayerInfo.getPlayerInfoDataLists().write(0, data);
      final PacketContainer respawnPlayer = new PacketContainer(Server.RESPAWN);
      respawnPlayer.getIntegers().write(0, player.getWorld().getEnvironment().getId());
      respawnPlayer.getDifficulties().write(0, Difficulty.valueOf(player.getWorld().getDifficulty().name()));
      if (player.getGameMode() != null) {
         respawnPlayer.getGameModes().write(0, NativeGameMode.fromBukkit(player.getGameMode()));
      }

      respawnPlayer.getWorldTypeModifier().write(0, player.getWorld().getWorldType());
      final boolean flying = player.isFlying();
      (new BukkitRunnable() {
         @Override
         public void run() {
             ProtocolLibrary.getProtocolManager().sendServerPacket(player, removePlayerInfo);
             ProtocolLibrary.getProtocolManager().sendServerPacket(player, addPlayerInfo);
             ProtocolLibrary.getProtocolManager().sendServerPacket(player, respawnPlayer);
             player.getInventory().setHeldItemSlot(player.getInventory().getHeldItemSlot());
             player.teleport(player.getLocation());
             player.setFlying(flying);
             player.setWalkSpeed(player.getWalkSpeed());
             player.setMaxHealth(player.getMaxHealth());
             player.setHealthScale(player.getHealthScale());
             player.setExp(player.getExp());
             player.setLevel(player.getLevel());
             player.updateInventory();
         }
      }).runTask(BukkitCommon.getInstance());
   }

   public static WrappedSignedProperty changePlayerSkin(Player player, String value, String signature, boolean respawn) {
      return changePlayerSkin(player, new WrappedSignedProperty("textures", value, signature));
   }

   public static WrappedSignedProperty changePlayerSkin(Player player, String name, UUID uuid, boolean respawn) {
      WrappedSignedProperty property = null;
      WrappedGameProfile gameProfile = WrappedGameProfile.fromPlayer(player);
      gameProfile.getProperties().clear();
      gameProfile.getProperties().put("textures", property = TextureFetcher.loadTexture(new WrappedGameProfile(uuid, name)));
      if (respawn) {
         respawnPlayer(player);
      }

      return property;
   }

   public static WrappedSignedProperty changePlayerSkin(Player player, WrappedSignedProperty wrappedSignedProperty) {
      WrappedGameProfile gameProfile = WrappedGameProfile.fromPlayer(player);
      gameProfile.getProperties().clear();
      gameProfile.getProperties().put("textures", wrappedSignedProperty);
      respawnPlayer(player);
      return wrappedSignedProperty;
   }

   public static void changePlayerSkin(Player player, WrappedSignedProperty property, boolean respawn) {
      WrappedGameProfile gameProfile = WrappedGameProfile.fromPlayer(player);
      gameProfile.getProperties().clear();
      gameProfile.getProperties().put("textures", property);
      if (respawn) {
         respawnPlayer(player);
      }
   }

   public static void removePlayerSkin(Player player) {
      removePlayerSkin(player, true);
   }

   public static void removePlayerSkin(Player player, boolean respawn) {
      WrappedGameProfile gameProfile = WrappedGameProfile.fromPlayer(player);
      gameProfile.getProperties().clear();
      if (respawn) {
         respawnPlayer(player);
      }
   }

   public static boolean validateName(String username) {
      return CommonConst.NAME_PATTERN.matcher(username).matches();
   }
}

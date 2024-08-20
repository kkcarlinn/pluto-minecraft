package br.com.plutomc.core.bukkit.utils.character;

import br.com.plutomc.core.bukkit.BukkitCommon;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import br.com.plutomc.core.common.CommonPlugin;
import br.com.plutomc.core.common.utils.skin.Skin;
import br.com.plutomc.core.common.utils.string.CodeCreator;
import net.minecraft.server.v1_8_R3.DataWatcher;
import net.minecraft.server.v1_8_R3.EntityBat;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.MinecraftServer;
import net.minecraft.server.v1_8_R3.PacketPlayOutAttachEntity;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityHeadRotation;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityMetadata;
import net.minecraft.server.v1_8_R3.PacketPlayOutNamedEntitySpawn;
import net.minecraft.server.v1_8_R3.PacketPlayOutPlayerInfo;
import net.minecraft.server.v1_8_R3.PacketPlayOutSpawnEntityLiving;
import net.minecraft.server.v1_8_R3.PlayerConnection;
import net.minecraft.server.v1_8_R3.PlayerInteractManager;
import net.minecraft.server.v1_8_R3.WorldServer;
import net.minecraft.server.v1_8_R3.PacketPlayOutPlayerInfo.EnumPlayerInfoAction;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class NPC {
   public static final CodeCreator CODE_CREATOR = new CodeCreator(10).setSpecialCharacters(false).setUpperCase(false).setNumbers(true);
   private GameProfile gameProfile;
   private Location location;
   private EntityPlayer entityPlayer;
   private EntityBat entityBat;
   private Set<UUID> showing = new HashSet<>();

   public NPC(Location location, String skinName) {
      this(location, CommonPlugin.getInstance().getSkinData().loadData(skinName).orElse(null));
   }

   public NPC(Location location, Skin skin) {
      this.location = new Location(location.getWorld(), location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
      MinecraftServer server = ((CraftServer)Bukkit.getServer()).getServer();
      WorldServer world = ((CraftWorld)location.getWorld()).getHandle();
      this.gameProfile = new GameProfile(UUID.randomUUID(), "§8[" + CODE_CREATOR.random() + "]");
      this.entityPlayer = new EntityPlayer(server, world, this.gameProfile, new PlayerInteractManager(world));
      if (skin != null) {
         this.gameProfile.getProperties().clear();
         PropertyMap propertyMap = new PropertyMap();
         propertyMap.put("textures", new Property("textures", skin.getValue(), skin.getSignature()));
         this.gameProfile.getProperties().putAll(propertyMap);
      }

      this.entityPlayer.getBukkitEntity().setRemoveWhenFarAway(false);
      this.entityPlayer.setLocation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
      this.entityPlayer.setInvisible(false);
      this.entityBat = new EntityBat(world);
      this.entityBat.setLocation(location.getX() * 32.0, location.getY() * 32.0, location.getZ() * 32.0, 0.0F, 0.0F);
      this.entityBat.setInvisible(true);
      Bukkit.getOnlinePlayers().forEach(player -> this.show(player));
   }

   public void teleport(Location location) {
      this.location = location;
      this.entityPlayer.setLocation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
      this.entityBat.setLocation(location.getX() * 32.0, location.getY() * 32.0, location.getZ() * 32.0, 0.0F, 0.0F);

      for(Player player : Bukkit.getOnlinePlayers()) {
         this.hide(player);
         this.show(player);
      }
   }

   public void show(Player player) {
      if (!this.showing.contains(player.getUniqueId())) {
         this.showing.add(player.getUniqueId());
         PlayerConnection connection = ((CraftPlayer)player).getHandle().playerConnection;
         connection.sendPacket(new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.ADD_PLAYER, this.entityPlayer));
         connection.sendPacket(new PacketPlayOutNamedEntitySpawn(this.entityPlayer));
         connection.sendPacket(new PacketPlayOutEntityMetadata(this.entityPlayer.getId(), this.entityPlayer.getDataWatcher(), true));
         connection.sendPacket(new PacketPlayOutEntityHeadRotation(this.entityPlayer, (byte)((int)(this.location.getYaw() * 256.0F / 360.0F))));
         connection.sendPacket(new PacketPlayOutSpawnEntityLiving(this.entityBat));
         connection.sendPacket(new PacketPlayOutAttachEntity(0, this.entityBat, this.entityPlayer));
         DataWatcher watcher = this.entityPlayer.getDataWatcher();
         watcher.watch(10, (byte)127);
         connection.sendPacket(new PacketPlayOutEntityMetadata(this.entityPlayer.getId(), watcher, true));
         Bukkit.getScheduler()
            .scheduleSyncDelayedTask(
               BukkitCommon.getInstance(), () -> connection.sendPacket(new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.REMOVE_PLAYER, this.entityPlayer)), 85L
            );
      }
   }

   public void hide(Player player) {
      if (this.showing.contains(player.getUniqueId())) {
         this.showing.remove(player.getUniqueId());
         PlayerConnection playerConnection = ((CraftPlayer)player).getHandle().playerConnection;
         playerConnection.sendPacket(new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.REMOVE_PLAYER, this.entityPlayer));
         playerConnection.sendPacket(new PacketPlayOutEntityDestroy(this.entityPlayer.getId()));
         playerConnection.sendPacket(new PacketPlayOutEntityDestroy(this.entityBat.getId()));
      }
   }

   public void remove() {
      Bukkit.getOnlinePlayers().forEach(player -> this.hide(player));
   }

   public GameProfile getGameProfile() {
      return this.gameProfile;
   }

   public Location getLocation() {
      return this.location;
   }

   public EntityPlayer getEntityPlayer() {
      return this.entityPlayer;
   }

   public EntityBat getEntityBat() {
      return this.entityBat;
   }

   public Set<UUID> getShowing() {
      return this.showing;
   }
}

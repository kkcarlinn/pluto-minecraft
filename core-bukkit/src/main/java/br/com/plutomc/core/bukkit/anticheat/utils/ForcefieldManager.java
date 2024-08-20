package br.com.plutomc.core.bukkit.anticheat.utils;

import br.com.plutomc.core.bukkit.BukkitCommon;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.PacketType.Play.Client;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers.EntityUseAction;
import com.mojang.authlib.GameProfile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import br.com.plutomc.core.common.CommonConst;
import br.com.plutomc.core.bukkit.protocol.PacketInjector;
import br.com.plutomc.core.common.utils.supertype.FutureCallback;
import net.minecraft.server.v1_8_R3.DataWatcher;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityMetadata;
import net.minecraft.server.v1_8_R3.PacketPlayOutNamedEntitySpawn;
import net.minecraft.server.v1_8_R3.PacketPlayOutPlayerInfo;
import net.minecraft.server.v1_8_R3.PlayerConnection;
import net.minecraft.server.v1_8_R3.PlayerInteractManager;
import net.minecraft.server.v1_8_R3.PacketPlayOutPlayerInfo.EnumPlayerInfoAction;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class ForcefieldManager {
   private Map<Player, ForcefieldChecker> forcefieldCheckers = new HashMap<>();

   public ForcefieldManager() {
      (new PacketInjector() {
         @Override
         public void inject(Plugin plugin) {
            ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(BukkitCommon.getInstance(), ListenerPriority.LOWEST, Client.USE_ENTITY) {
               public void onPacketReceiving(PacketEvent event) {
                  if (event.getPlayer() != null) {
                     PacketContainer packetContainer = event.getPacket();
                     int entityId = packetContainer.getIntegers().read(0);
                     EntityUseAction action = (EntityUseAction)packetContainer.getEntityUseActions().read(0);
                     if (action == EntityUseAction.ATTACK && ForcefieldManager.this.forcefieldCheckers.containsKey(event.getPlayer())) {
                        ForcefieldChecker checker = ForcefieldManager.this.forcefieldCheckers.get(event.getPlayer());
                        if (checker.getEntitiesHit().containsKey(entityId)) {
                           checker.kill(entityId, true);
                        }
                     }
                  }
               }
            });
         }
      }).inject(BukkitCommon.getInstance());
   }

   public void check(Player player, FutureCallback<Result> callback) {
      this.check(player, 2, 5, 6, true, callback);
   }

   public void check(Player player, int entityCount, FutureCallback<Result> callback) {
      this.check(player, 2, 5, entityCount, true, callback);
   }

   public void check(Player player, int entityCount, boolean invisible, FutureCallback<Result> callback) {
      this.check(player, 2, 5, entityCount, invisible, callback);
   }

   public void check(Player player, boolean invisible, FutureCallback<Result> callback) {
      this.check(player, 2, 5, 6, invisible, callback);
   }

   public void check(
      final Player player, int ticks, int ticksToDestroy, int entityCount, boolean invisible, final FutureCallback<Result> callback
   ) {
      final ForcefieldChecker checker = new ForcefieldChecker(player, entityCount, ticksToDestroy, invisible);
      List<Location> locations = this.getLocationList(player, checker.getEntityCount());

      for(int i = 0; i < checker.getEntityCount(); ++i) {
         final Location location = locations.get(i);
         (new BukkitRunnable() {
            @Override
            public void run() {
               checker.spawn(location);
            }
         }).runTaskLater(BukkitCommon.getInstance(), (long)(i * ticks));
      }

      this.forcefieldCheckers.put(player, checker);
      (new BukkitRunnable() {
            @Override
            public void run() {
               callback.result(
                  ForcefieldManager.this.new Result((int)checker.getEntitiesHit().values().stream().filter(b -> b).count(), checker.getEntityCount()), null
               );
               ForcefieldManager.this.forcefieldCheckers.remove(player);
            }
         })
         .runTaskLater(BukkitCommon.getInstance(), (long)(ticks * checker.getEntityCount() + checker.getTicksToDestroy()));
   }

   public List<Location> getLocationList(Player player, int entityCount) {
      List<Location> locations = new ArrayList<>();
      locations.add(player.getLocation().add(1.5, 0.5, 1.5));
      locations.add(player.getLocation().add(-1.5, 0.5, 1.5));
      locations.add(player.getLocation().add(1.5, 0.5, -1.5));
      locations.add(player.getLocation().add(-1.5, 0.5, -1.5));
      locations.add(player.getLocation().add(0.5, 0.5, 1.5));
      locations.add(player.getLocation().add(1.5, 0.5, 0.5));
      locations.add(player.getLocation().add(-1.5, 0.5, 0.5));
      locations.add(player.getLocation().add(0.5, 0.5, -1.5));
      return locations;
   }

   public class FakePlayer extends EntityPlayer {
      public FakePlayer(String playerName, Location loc) {
         super(
            ((CraftServer)Bukkit.getServer()).getServer(),
            ((CraftWorld)loc.getWorld()).getHandle(),
            new GameProfile(UUID.randomUUID(), playerName),
            new PlayerInteractManager(((CraftWorld)loc.getWorld()).getHandle())
         );
         this.setInvisible(false);
         this.setHealth(20.0F);
         this.setLocation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
      }

      @Override
      public boolean isBlocking() {
         return true;
      }
   }

   public class ForcefieldChecker {
      private Player player;
      private int entityCount;
      private int ticksToDestroy;
      private boolean invisible;
      private Map<Integer, Boolean> entitiesHit;

      public ForcefieldChecker(Player player, int entityCount, int ticksToDestroy, boolean invisible) {
         this.player = player;
         this.entityCount = entityCount;
         this.ticksToDestroy = ticksToDestroy;
         this.invisible = invisible;
         this.entitiesHit = new HashMap<>();
      }

      public void spawn(Location location) {
         final FakePlayer fakePlayer = ForcefieldManager.this.new FakePlayer("§7" + CommonConst.RANDOM.nextInt(999999), location);
         final PlayerConnection connection = this.getConnection();
         connection.sendPacket(new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.ADD_PLAYER, fakePlayer));
         connection.sendPacket(new PacketPlayOutNamedEntitySpawn(fakePlayer));
         connection.sendPacket(new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.REMOVE_PLAYER, fakePlayer));
         if (this.invisible) {
            DataWatcher dataWatcher = new DataWatcher(fakePlayer);
            dataWatcher.a(0, (byte)32);
            connection.sendPacket(new PacketPlayOutEntityMetadata(fakePlayer.getId(), dataWatcher, true));
         }

         (new BukkitRunnable() {
            @Override
            public void run() {
               connection.sendPacket(new PacketPlayOutEntityDestroy(fakePlayer.getId()));
            }
         }).runTaskLater(BukkitCommon.getInstance(), (long)this.ticksToDestroy);
         this.entitiesHit.put(fakePlayer.getId(), false);
      }

      public void kill(int entityId, boolean hit) {
         if (hit) {
            this.entitiesHit.put(entityId, true);
         }

         this.getConnection().sendPacket(new PacketPlayOutEntityDestroy(entityId));
      }

      public PlayerConnection getConnection() {
         return ((CraftPlayer)this.player).getHandle().playerConnection;
      }

      public Player getPlayer() {
         return this.player;
      }

      public int getEntityCount() {
         return this.entityCount;
      }

      public int getTicksToDestroy() {
         return this.ticksToDestroy;
      }

      public boolean isInvisible() {
         return this.invisible;
      }

      public Map<Integer, Boolean> getEntitiesHit() {
         return this.entitiesHit;
      }
   }

   public class Result {
      private int hits;
      private int entityCount;

      public Result(int hits, int entityCount) {
         this.hits = hits;
         this.entityCount = entityCount;
      }

      public int getHits() {
         return this.hits;
      }

      public int getEntityCount() {
         return this.entityCount;
      }
   }
}

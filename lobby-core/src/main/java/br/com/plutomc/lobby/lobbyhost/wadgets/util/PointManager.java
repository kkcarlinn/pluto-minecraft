package br.com.plutomc.lobby.lobbyhost.wadgets.util;

import br.com.plutomc.lobby.lobbyhost.gamer.Gamer;
import br.com.plutomc.lobby.lobbyhost.LobbyHost;
import net.minecraft.server.v1_8_R3.EnumParticle;
import net.minecraft.server.v1_8_R3.PacketPlayOutWorldParticles;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class PointManager {
   private static PointManager instance = new PointManager();
   private Point3D[] outline;
   private Point3D[] fill;
   private int x = 0;
   private int B = 0;

   public PointManager() {
      this.outline = new Point3D[]{
         new Point3D(0.0F, 0.0F, -0.5F),
         new Point3D(0.1F, 0.01F, -0.5F),
         new Point3D(0.3F, 0.03F, -0.5F),
         new Point3D(0.4F, 0.04F, -0.5F),
         new Point3D(0.6F, 0.1F, -0.5F),
         new Point3D(0.61F, 0.2F, -0.5F),
         new Point3D(0.62F, 0.4F, -0.5F),
         new Point3D(0.63F, 0.6F, -0.5F),
         new Point3D(0.635F, 0.7F, -0.5F),
         new Point3D(0.7F, 0.7F, -0.5F),
         new Point3D(0.9F, 0.75F, -0.5F),
         new Point3D(1.2F, 0.8F, -0.5F),
         new Point3D(1.4F, 0.9F, -0.5F),
         new Point3D(1.6F, 1.0F, -0.5F),
         new Point3D(1.8F, 1.1F, -0.5F),
         new Point3D(1.85F, 0.9F, -0.5F),
         new Point3D(1.9F, 0.7F, -0.5F),
         new Point3D(1.85F, 0.5F, -0.5F),
         new Point3D(1.8F, 0.3F, -0.5F),
         new Point3D(1.75F, 0.1F, -0.5F),
         new Point3D(1.7F, -0.1F, -0.5F),
         new Point3D(1.65F, -0.3F, -0.5F),
         new Point3D(1.55F, -0.5F, -0.5F),
         new Point3D(1.45F, -0.7F, -0.5F),
         new Point3D(1.3F, -0.75F, -0.5F),
         new Point3D(1.15F, -0.8F, -0.5F),
         new Point3D(1.0F, -0.85F, -0.5F),
         new Point3D(0.8F, -0.87F, -0.5F),
         new Point3D(0.6F, -0.7F, -0.5F),
         new Point3D(0.5F, -0.5F, -0.5F),
         new Point3D(0.4F, -0.3F, -0.5F),
         new Point3D(0.3F, -0.3F, -0.5F),
         new Point3D(0.15F, -0.3F, -0.5F),
         new Point3D(0.0F, -0.3F, -0.5F),
         new Point3D(0.9F, 0.55F, -0.5F),
         new Point3D(1.2F, 0.6F, -0.5F),
         new Point3D(1.4F, 0.7F, -0.5F),
         new Point3D(1.6F, 0.9F, -0.5F),
         new Point3D(0.9F, 0.35F, -0.5F),
         new Point3D(1.2F, 0.4F, -0.5F),
         new Point3D(1.4F, 0.5F, -0.5F),
         new Point3D(1.6F, 0.7F, -0.5F),
         new Point3D(0.9F, 0.15F, -0.5F),
         new Point3D(1.2F, 0.2F, -0.5F),
         new Point3D(1.4F, 0.3F, -0.5F),
         new Point3D(1.6F, 0.5F, -0.5F),
         new Point3D(0.9F, -0.05F, -0.5F),
         new Point3D(1.2F, 0.0F, -0.5F),
         new Point3D(1.4F, 0.1F, -0.5F),
         new Point3D(1.6F, 0.3F, -0.5F),
         new Point3D(0.7F, -0.25F, -0.5F),
         new Point3D(1.0F, -0.2F, -0.5F),
         new Point3D(1.2F, -0.1F, -0.5F),
         new Point3D(1.4F, 0.1F, -0.5F),
         new Point3D(0.7F, -0.45F, -0.5F),
         new Point3D(1.0F, -0.4F, -0.5F),
         new Point3D(1.2F, -0.3F, -0.5F),
         new Point3D(1.4F, -0.1F, -0.5F),
         new Point3D(1.3F, -0.55F, -0.5F),
         new Point3D(1.15F, -0.6F, -0.5F),
         new Point3D(1.0F, -0.65F, -0.5F)
      };
      this.fill = new Point3D[]{
         new Point3D(1.2F, 0.6F, -0.5F),
         new Point3D(1.4F, 0.7F, -0.5F),
         new Point3D(1.1F, 0.2F, -0.5F),
         new Point3D(1.3F, 0.3F, -0.5F),
         new Point3D(1.0F, -0.2F, -0.5F),
         new Point3D(1.2F, -0.1F, -0.5F)
      };
      instance = this;
   }

   public void sendPacket(Player player, EnumParticle particle) {
      if (this.x > 500) {
         this.x = 0;
      }

      if (this.x % 5 == 0) {
         ++this.B;
         if (this.B % 25 == 0) {
            return;
         }

         Location playerLocation = player.getEyeLocation();
         float x = (float)playerLocation.getX();
         float y = (float)playerLocation.getY() - 0.2F;
         float z = (float)playerLocation.getZ();
         float rot = -playerLocation.getYaw() * ((float) (Math.PI / 180.0));
         Point3D rotated = null;

         for(Point3D point : this.outline) {
            rotated = point.rotate(rot);
            PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles(
               particle, true, rotated.x + x, rotated.y + y, rotated.z + z, 0.0F, 0.0F, 0.0F, 0.0F, 1
            );
            point.z *= -1.0F;
            rotated = point.rotate(rot + 3.1415F);
            point.z *= -1.0F;
            PacketPlayOutWorldParticles packet2 = new PacketPlayOutWorldParticles(
               particle, true, rotated.x + x, rotated.y + y, rotated.z + z, 0.0F, 0.0F, 0.0F, 0.0F, 1
            );

            for(Player online : Bukkit.getOnlinePlayers()) {
               if (online.canSee(player)) {
                  ((CraftPlayer)online).getHandle().playerConnection.sendPacket(packet);
                  ((CraftPlayer)online).getHandle().playerConnection.sendPacket(packet2);
               }
            }
         }

         for(Point3D point : this.fill) {
            rotated = point.rotate(rot);
            PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles(
               particle, true, rotated.x + x, rotated.y + y, rotated.z + z, 0.0F, 0.0F, 0.0F, 0.0F, 1
            );
            point.z *= -1.0F;
            rotated = point.rotate(rot + 3.1415F);
            point.z *= -1.0F;
            PacketPlayOutWorldParticles packet2 = new PacketPlayOutWorldParticles(
               particle, true, rotated.x + x, rotated.y + y, rotated.z + z, 0.0F, 0.0F, 0.0F, 0.0F, 1
            );

            for(Player online : Bukkit.getOnlinePlayers()) {
               if (online.canSee(player)) {
                  ((CraftPlayer)online).getHandle().playerConnection.sendPacket(packet);
                  ((CraftPlayer)online).getHandle().playerConnection.sendPacket(packet2);
               }
            }
         }
      }

      ++this.x;
   }

   public void sendPacket(Player player) {
      Gamer gamer = LobbyHost.getInstance().getGamerManager().getGamer(player.getUniqueId());
      gamer.setAlpha(gamer.getAlpha() + (Math.PI / 16));
      double alpha = gamer.getAlpha();
      Location loc = player.getLocation();
      Location firstLocation = loc.clone().add(Math.cos(alpha), Math.sin(alpha) + 1.0, Math.sin(alpha));
      Location secondLocation = loc.clone().add(Math.cos(alpha + Math.PI), Math.sin(alpha) + 1.0, Math.sin(alpha + Math.PI));
      PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles(
         gamer.getParticle().getParticle(),
         true,
         (float)firstLocation.getX(),
         (float)firstLocation.getY(),
         (float)firstLocation.getZ(),
         0.0F,
         0.0F,
         0.0F,
         0.0F,
         1
      );
      PacketPlayOutWorldParticles packet2 = new PacketPlayOutWorldParticles(
         gamer.getParticle().getParticle(),
         true,
         (float)secondLocation.getX(),
         (float)secondLocation.getY(),
         (float)secondLocation.getZ(),
         0.0F,
         0.0F,
         0.0F,
         0.0F,
         1
      );

      for(Player online : Bukkit.getOnlinePlayers()) {
         if (online.canSee(player)) {
            ((CraftPlayer)online).getHandle().playerConnection.sendPacket(packet);
            ((CraftPlayer)online).getHandle().playerConnection.sendPacket(packet2);
         }
      }
   }

   public static PointManager getInstance() {
      return instance;
   }
}

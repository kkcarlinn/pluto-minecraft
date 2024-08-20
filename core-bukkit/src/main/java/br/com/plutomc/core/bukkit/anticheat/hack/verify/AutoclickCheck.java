package br.com.plutomc.core.bukkit.anticheat.hack.verify;

import br.com.plutomc.core.bukkit.BukkitCommon;
import br.com.plutomc.core.bukkit.anticheat.gamer.UserData;
import br.com.plutomc.core.bukkit.anticheat.hack.HackType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.PacketType.Play.Client;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import br.com.plutomc.core.bukkit.anticheat.hack.Clicks;
import br.com.plutomc.core.bukkit.anticheat.hack.Verify;
import net.minecraft.server.v1_8_R3.MinecraftServer;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class AutoclickCheck implements Verify {
   private Map<Player, Clicks> clicksPerSecond = new HashMap<>();
   private Map<Player, Long> cooldownMap = new HashMap<>();

   public AutoclickCheck() {
      ProtocolLibrary.getProtocolManager()
         .addPacketListener(
            new PacketAdapter(BukkitCommon.getInstance(), Client.ARM_ANIMATION) {
               public void onPacketReceiving(PacketEvent event) {
                  Player player = event.getPlayer();
                  UserData userData = AutoclickCheck.this.getUserData(player);
                  if (player != null && userData != null) {
                     if (player.getGameMode() != GameMode.CREATIVE
                        && player.getGameMode() != GameMode.SPECTATOR
                        && userData.getPing() <= 90
                        && !(MinecraftServer.getServer().recentTps[0] < 19.95)) {
                        if (!AutoclickCheck.this.cooldownMap.containsKey(player) || AutoclickCheck.this.cooldownMap.get(player) <= System.currentTimeMillis()) {
                           try {
                              if (player.getTargetBlock((Set<Material>)null, 4).getType() != Material.AIR) {
                                 return;
                              }
                           } catch (IllegalStateException var5) {
                              return;
                           }
      
                           AutoclickCheck.this.handle(player);
                        }
                     }
                  }
               }
            }
         );
   }

   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void onPlayerInteract(BlockDamageEvent event) {
      this.clicksPerSecond.remove(event.getPlayer());
      this.cooldownMap.put(event.getPlayer(), System.currentTimeMillis() + 1000L);
   }

   @EventHandler
   public void onPlayerQuit(PlayerQuitEvent event) {
      this.clicksPerSecond.remove(event.getPlayer());
      this.cooldownMap.remove(event.getPlayer());
   }

   public void handle(Player player) {
      Clicks click = this.clicksPerSecond.computeIfAbsent(player, v -> new Clicks());
      if (click.getExpireTime() < System.currentTimeMillis()) {
         if (click.getClicks() >= 25) {
            this.alert(player, "" + click.getClicks() + " cps, max: 25");
         }

         this.clicksPerSecond.remove(player);
      } else {
         click.addClick();
      }
   }

   @Override
   public HackType getHackType() {
      return HackType.AUTOCLICK;
   }
}

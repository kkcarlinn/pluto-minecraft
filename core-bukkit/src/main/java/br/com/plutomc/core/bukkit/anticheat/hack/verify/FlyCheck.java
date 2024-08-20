package br.com.plutomc.core.bukkit.anticheat.hack.verify;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import br.com.plutomc.core.bukkit.anticheat.gamer.UserData;
import br.com.plutomc.core.bukkit.utils.MoveTypes;
import br.com.plutomc.core.bukkit.anticheat.hack.HackType;
import br.com.plutomc.core.bukkit.anticheat.hack.Verify;
import net.minecraft.server.v1_8_R3.MinecraftServer;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class FlyCheck implements Verify {
   private Map<UUID, Integer> ticks;
   private Map<Player, Long> cooldownMap = new HashMap<>();

   public FlyCheck() {
      this.ticks = new HashMap<>();
   }

   public static boolean isSwimming(Location location) {
      String type = location.getBlock().getType().name();
      return type.contains("WATER") || type.contains("LAVA");
   }

   @EventHandler
   public void onPlayerMove(PlayerMoveEvent event) {
      Player player = event.getPlayer();
      UserData userData = this.getUserData(player);
      if (!player.isInsideVehicle()
         && userData.getPing() <= 150
         && !(MinecraftServer.getServer().recentTps[0] < 19.9)
         && !MoveTypes.isClimbing(player.getLocation())
         && !player.getAllowFlight()) {
         Integer tick = this.ticks.computeIfAbsent(player.getUniqueId(), v -> 0);
         if (MoveTypes.hasSurrondingBlocks(player.getLocation().getBlock()) || isSwimming(player.getLocation())) {
            tick = 0;
         } else if (!userData.isFalling()) {
            tick = tick + 1;
         } else {
            tick = 0;
         }

         this.ticks.put(player.getUniqueId(), tick);
         if (tick >= 30 && (!this.cooldownMap.containsKey(player) || this.cooldownMap.get(player) < System.currentTimeMillis())) {
            this.alert(player, "times: " + tick);
            this.cooldownMap.put(player, System.currentTimeMillis() + 750L);
         }
      }
   }

   @EventHandler
   public void onPlayerQuit(PlayerQuitEvent event) {
      this.ticks.remove(event.getPlayer().getUniqueId());
      this.cooldownMap.remove(event.getPlayer());
   }

   @Override
   public HackType getHackType() {
      return HackType.FLY;
   }
}

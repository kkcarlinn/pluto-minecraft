package br.com.plutomc.core.bukkit.anticheat.hack.verify;

import java.util.HashMap;
import java.util.Map;

import br.com.plutomc.core.bukkit.anticheat.gamer.UserData;
import br.com.plutomc.core.bukkit.anticheat.hack.HackType;
import br.com.plutomc.core.bukkit.anticheat.hack.Verify;
import net.minecraft.server.v1_8_R3.MinecraftServer;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class GlideCheck implements Verify {
   private Map<Player, Long> cooldownMap = new HashMap<>();

   @EventHandler
   public void onPlayerMove(PlayerMoveEvent event) {
      Player player = event.getPlayer();
      UserData userData = this.getUserData(player);
      if (!this.isIgnore(player)
         && !player.getAllowFlight()
         && !player.getGameMode().equals(GameMode.CREATIVE)
         && userData.getPing() <= 150
         && !(MinecraftServer.getServer().recentTps[0] < 19.9)) {
         if (!this.cooldownMap.containsKey(player) || this.cooldownMap.get(player) <= System.currentTimeMillis()) {
            if (event.getTo().getY() - event.getFrom().getY() == -0.125
               && event.getTo().clone().subtract(0.0, 1.0, 0.0).getBlock().getType().equals(Material.AIR)) {
               this.alert(player);
               this.cooldownMap.put(player, System.currentTimeMillis() + 500L);
            }
         }
      }
   }

   @EventHandler
   public void onPlayerQuit(PlayerQuitEvent event) {
      if (this.cooldownMap.containsKey(event.getPlayer())) {
         this.cooldownMap.remove(event.getPlayer());
      }
   }

   @Override
   public HackType getHackType() {
      return HackType.GLIDE;
   }
}

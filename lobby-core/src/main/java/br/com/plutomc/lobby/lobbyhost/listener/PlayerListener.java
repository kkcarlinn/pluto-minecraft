package br.com.plutomc.lobby.lobbyhost.listener;

import br.com.plutomc.lobby.lobbyhost.gamer.Gamer;
import br.com.plutomc.core.common.CommonPlugin;
import br.com.plutomc.core.bukkit.BukkitCommon;
import br.com.plutomc.lobby.lobbyhost.LobbyHost;
import br.com.plutomc.lobby.lobbyhost.LobbyConst;
import br.com.plutomc.core.bukkit.utils.player.PlayerHelper;
import br.com.plutomc.core.common.account.Account;
import br.com.plutomc.core.common.server.ServerType;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class PlayerListener implements Listener {
   @EventHandler(
      priority = EventPriority.HIGHEST
   )
   public void onPlayerLogin(PlayerLoginEvent event) {
      if (event.getResult() == Result.ALLOWED) {
         Player player = event.getPlayer();
         Account account = CommonPlugin.getInstance().getAccountManager().getAccount(player.getUniqueId());
         if (account == null) {
            event.disallow(Result.KICK_OTHER, "§cNão foi possível carregar sua conta, tente novamente.");
         } else {
            LobbyHost.getInstance().getGamerManager().loadGamer(account.getUniqueId(), new Gamer(account));
         }
      }
   }

   @EventHandler
   public void onPlayerJoin(PlayerJoinEvent event) {
      Player player = event.getPlayer();
      Account account = CommonPlugin.getInstance().getAccountManager().getAccount(player.getUniqueId());
      player.teleport(BukkitCommon.getInstance().getLocationManager().getLocation("spawn"));
      player.setGameMode(GameMode.ADVENTURE);
      if (player.hasPermission("command.fly")) {
         player.setAllowFlight(true);
         player.setFlying(true);
         player.teleport(player.getLocation().add(0.0, 2.0, 0.0));
      }

      player.getInventory().clear();
      player.getInventory().setArmorContents(new ItemStack[4]);
      player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 100, 4));
      player.setHealth(player.getMaxHealth());
      player.setHealthScale(1.0);
      player.setFoodLevel(20);
      player.setExp(0.0F);
      player.setLevel(0);
      player.setTotalExperience(0);
      LobbyHost.getInstance().getPlayerInventory().handle(event.getPlayer());
      if (CommonPlugin.getInstance().getServerType() == ServerType.LOBBY && account.getSessionTime() <= 10000L) {
         PlayerHelper.title(player, "§b§lPLUTO", "§eSeja bem-vindo!");
      }

      PlayerHelper.setHeaderAndFooter(player, "\n§b§lPLUTO\n", LobbyConst.TAB_FOOTER);
   }

   @EventHandler
   public void onPlayerQuit(PlayerQuitEvent event) {
      LobbyHost.getInstance().getGamerManager().unloadGamer(event.getPlayer().getUniqueId());
   }

   @EventHandler
   public void onPlayerMove(PlayerMoveEvent event) {
      if (event.getTo().getBlockY() < 1) {
         event.getPlayer().teleport(BukkitCommon.getInstance().getLocationManager().getLocation("spawn"));
      } else if (event.getPlayer().getLocation().subtract(0.0, 1.0, 0.0).getBlock().getType() == Material.SLIME_BLOCK) {
         event.getPlayer().setVelocity(event.getPlayer().getLocation().getDirection().multiply(2).setY(0.6));
      }
   }
}

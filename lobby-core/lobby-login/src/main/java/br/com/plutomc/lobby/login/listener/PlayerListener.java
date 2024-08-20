package br.com.plutomc.lobby.login.listener;

import br.com.plutomc.lobby.login.captcha.Captcha;
import br.com.plutomc.lobby.login.captcha.impl.ItemCaptcha;
import br.com.plutomc.lobby.login.captcha.impl.MoveCaptcha;
import com.google.common.collect.ImmutableSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import br.com.plutomc.core.common.CommonPlugin;
import br.com.plutomc.core.bukkit.BukkitCommon;
import br.com.plutomc.core.bukkit.event.UpdateEvent;
import br.com.plutomc.core.bukkit.event.member.PlayerAuthEvent;
import br.com.plutomc.lobby.login.event.CaptchaSuccessEvent;
import br.com.plutomc.core.common.member.Member;
import br.com.plutomc.core.common.member.configuration.LoginConfiguration;
import br.com.plutomc.core.common.server.ServerType;
import br.com.plutomc.core.common.utils.Callback;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerListener implements Listener {
   private List<Captcha> captchaList = new ArrayList<>();
   private Map<UUID, Long> timeMap = new HashMap<>();

   public PlayerListener() {
      this.captchaList.add(new MoveCaptcha());
      this.captchaList.add(new ItemCaptcha());
   }

   @EventHandler
   public void update(UpdateEvent event) {
      if (event.getType() == UpdateEvent.UpdateType.SECOND) {
         for(Entry<UUID, Long> next : ImmutableSet.copyOf(this.timeMap.entrySet())) {
            Player player = Bukkit.getPlayer(next.getKey());
            Member member = CommonPlugin.getInstance().getMemberManager().getMember(player.getUniqueId());
            boolean needCaptcha = this.needCaptcha(member);
            if (next.getValue() > System.currentTimeMillis()) {
               if (!needCaptcha) {
                  int time = (int)(next.getValue() - System.currentTimeMillis()) / 1000 + 1;
                  if (time % 10 == 0) {
                     member.sendMessage(
                        member.getLoginConfiguration().isRegistered()
                           ? "§aVocê precisa se autenticar usando o /login <sua senha>§f."
                           : "§aVocê precisa se autenticar usando o /register <sua senha> <repita sua senha>§f."
                     );
                  }

                  member.sendActionBar("§c" + time + " segundos restantes.");
               }
            } else {
               if (needCaptcha) {
                  this.deletePlayer(player);
               }

               player.kickPlayer(needCaptcha ? "§cVocê precisa fazer o captcha para se autenticar." : "§cVocê demorou demais para se logar.");
            }
         }
      }
   }

   @EventHandler
   public void onPlayerAuth(final PlayerAuthEvent event) {
      this.timeMap.remove(event.getPlayer().getUniqueId());
      (new BukkitRunnable() {
         @Override
         public void run() {
            BukkitCommon.getInstance().sendPlayerToServer(event.getPlayer(), true, ServerType.LOBBY);
         }
      }).runTaskTimer(BukkitCommon.getInstance(), 20L, 60L);
   }

   @EventHandler(
      priority = EventPriority.HIGHEST
   )
   public void onPlayerJoin(PlayerJoinEvent event) {
      Player player = event.getPlayer();
      Member member = CommonPlugin.getInstance().getMemberManager().getMember(player.getUniqueId());
      if (member.getLoginConfiguration().getAccountType() == LoginConfiguration.AccountType.PREMIUM) {
         if (!member.hasPermission("staff.super")) {
            BukkitCommon.getInstance().sendPlayerToServer(event.getPlayer(), true, ServerType.LOBBY);
         }
      } else {
         boolean captcha = this.needCaptcha(member);
         this.loadTime(player);
         if (captcha) {
            player.sendMessage("§aComplete o captcha para ter o acesso liberado.");
            this.captcha(player, this.captchaList.stream().findFirst().orElse(null), 0);
         }

         new BukkitRunnable() {
            @Override
            public void run() {
            }
         };
      }
   }

   @EventHandler(
      priority = EventPriority.HIGHEST
   )
   public void onPlayerQuit(PlayerQuitEvent event) {
      this.timeMap.remove(event.getPlayer().getUniqueId());
   }

   private void captcha(final Player player, Captcha orElse, final int i) {
      if (this.captchaList.size() == i) {
         Member member = CommonPlugin.getInstance().getMemberManager().getMember(player.getUniqueId());
         CommonPlugin.getInstance().debug("The ip " + member.getIpAddress() + " pass in the captcha.");
         member.getLoginConfiguration().setCaptcha(true);
         this.loadTime(player);
         Bukkit.getPluginManager().callEvent(new CaptchaSuccessEvent(player));
      } else {
         orElse.verify(
            player,
            new Callback<Boolean>() {
               public void callback(Boolean t) {
                  if (t) {
                     PlayerListener.this.captcha(
                        player, i + 1 >= PlayerListener.this.captchaList.size() ? null : PlayerListener.this.captchaList.get(i + 1), i + 1
                     );
                  } else {
                     PlayerListener.this.deletePlayer(player);
                     player.kickPlayer("§cVocê falhou no captcha.");
                  }
               }
            }
         );
      }
   }

   public boolean needCaptcha(Member member) {
      return !member.getLoginConfiguration().isCaptcha() && member.getLoginConfiguration().getAccountType() != LoginConfiguration.AccountType.PREMIUM;
   }

   public void loadTime(Player player) {
      this.timeMap.put(player.getUniqueId(), System.currentTimeMillis() + 30000L - 1L);
   }

   public void deletePlayer(Player player) {
      Member member = CommonPlugin.getInstance().getMemberManager().getMember(player.getUniqueId());
      CommonPlugin.getInstance().debug("The ip " + member.getIpAddress() + " was kicked from captcha failure.");
      if (member.getOnlineTime() <= 600000L) {
         CommonPlugin.getInstance().getPluginPlatform().runAsync(() -> CommonPlugin.getInstance().getMemberData().deleteMember(player.getUniqueId()));
      }
   }
}

package br.com.plutomc.core.bukkit.listener.member;

import br.com.plutomc.core.bukkit.BukkitCommon;
import br.com.plutomc.core.bukkit.event.member.PlayerChangedTagEvent;
import br.com.plutomc.core.bukkit.event.member.PlayerLanguageChangeEvent;
import br.com.plutomc.core.bukkit.utils.player.PlayerHelper;
import br.com.plutomc.core.common.CommonPlugin;
import br.com.plutomc.core.bukkit.member.BukkitMember;
import br.com.plutomc.core.bukkit.utils.scoreboard.ScoreboardAPI;
import br.com.plutomc.core.common.language.Language;
import br.com.plutomc.core.common.member.Member;
import br.com.plutomc.core.common.permission.Tag;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.PluginDisableEvent;

public class TagListener implements Listener {
   private static char[] chars = "abcdefghijklmnopqrstuv".toCharArray();

   @EventHandler
   public void onPluginDisable(PluginDisableEvent event) {
      if (BukkitCommon.getInstance().isTagControl()) {
         for(Player p : Bukkit.getServer().getOnlinePlayers()) {
            ScoreboardAPI.leaveCurrentTeamForOnlinePlayers(p);
         }
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onPlayerQuit(PlayerQuitEvent event) {
      ScoreboardAPI.leaveCurrentTeamForOnlinePlayers(event.getPlayer());
   }

   @EventHandler
   public void onPlayerLanguageChange(PlayerLanguageChangeEvent event) {
      if (BukkitCommon.getInstance().isTagControl()) {
         Player player = event.getPlayer();

         for(Player o : Bukkit.getOnlinePlayers()) {
            Member bp = CommonPlugin.getInstance().getMemberManager().getMember(o.getUniqueId());
            Tag tag = bp.getTag();
            String id = getTeamName(tag);
            String prefix = PlayerHelper.translate(Language.getLanguage(player.getUniqueId()), tag.getRealPrefix());
            ScoreboardAPI.setTeamPrefixAndSuffix(ScoreboardAPI.createTeamIfNotExistsToPlayer(player, id, prefix, ""), prefix, "");
         }
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void onPlayerChangeTag(PlayerChangedTagEvent event) {
      if (BukkitCommon.getInstance().isTagControl()) {
         Player p = event.getPlayer();
         Member player = (BukkitMember)event.getMember();
         if (player != null) {
            String id = getTeamName(event.getNewTag());
            String oldId = getTeamName(event.getOldTag());
            String tag = PlayerHelper.translate(Language.getLanguage(player.getUniqueId()), event.getNewTag().getRealPrefix());

            for(Player o : Bukkit.getOnlinePlayers()) {
               ScoreboardAPI.leaveTeamToPlayer(o, oldId, p);
               ScoreboardAPI.joinTeam(ScoreboardAPI.createTeamIfNotExistsToPlayer(o, id, tag, ""), p);
            }
         }
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onPlayerJoin(PlayerJoinEvent e) {
      if (BukkitCommon.getInstance().isTagControl()) {
         Player p = e.getPlayer();
         Member player = CommonPlugin.getInstance().getMemberManager().getMember(e.getPlayer().getUniqueId(), BukkitMember.class);
         Tag tag = player.getTag();
         String id = getTeamName(tag);

         for(Player o : Bukkit.getOnlinePlayers()) {
            ScoreboardAPI.joinTeam(
               ScoreboardAPI.createTeamIfNotExistsToPlayer(o, id, PlayerHelper.translate(Language.getLanguage(o.getUniqueId()), tag.getRealPrefix()), ""), p
            );
         }

         for(Player o : Bukkit.getOnlinePlayers()) {
            if (!o.getUniqueId().equals(p.getUniqueId())) {
               BukkitMember bp = (BukkitMember)CommonPlugin.getInstance().getMemberManager().getMember(o.getUniqueId());
               if (bp == null) {
                  o.kickPlayer("§cSua conta não foi carregada.");
                  return;
               }

               tag = bp.getTag();
               id = getTeamName(tag);
               ScoreboardAPI.joinTeam(
                  ScoreboardAPI.createTeamIfNotExistsToPlayer(p, id, PlayerHelper.translate(player.getLanguage(), tag.getRealPrefix()), ""), o
               );
            }
         }
      }
   }

   public static String getTeamName(Tag tag) {
      return chars[tag.getTagId()] + "";
   }
}

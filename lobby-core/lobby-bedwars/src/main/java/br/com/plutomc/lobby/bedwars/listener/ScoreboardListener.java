package br.com.plutomc.lobby.bedwars.listener;

import br.com.plutomc.core.bukkit.BukkitCommon;
import br.com.plutomc.core.bukkit.event.UpdateEvent;
import br.com.plutomc.core.bukkit.event.member.PlayerGroupChangeEvent;
import br.com.plutomc.core.bukkit.event.member.PlayerLanguageChangeEvent;
import br.com.plutomc.core.bukkit.event.server.PlayerChangeEvent;
import br.com.plutomc.core.bukkit.utils.scoreboard.ScoreHelper;
import br.com.plutomc.core.bukkit.utils.scoreboard.Scoreboard;
import br.com.plutomc.core.common.CommonPlugin;
import br.com.plutomc.core.common.member.Member;
import br.com.plutomc.core.common.member.status.Status;
import br.com.plutomc.core.common.member.status.StatusType;
import br.com.plutomc.core.common.member.status.types.BedwarsCategory;
import br.com.plutomc.core.common.permission.Tag;
import br.com.plutomc.core.common.server.ServerType;
import br.com.plutomc.core.common.utils.string.WaveAnimation;
import br.com.plutomc.lobby.bedwars.LobbyMain;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class ScoreboardListener implements Listener {

    private static final WaveAnimation animation = new WaveAnimation("BEDWARS", "§b§l", "§6§l", "§e§l");
    @EventHandler
    public void onUpdate(UpdateEvent event) {
        if (event.getCurrentTick() % 2 == 0) {
            String next = animation.next();
            for(Scoreboard score : ScoreHelper.getInstance().scoreMap.values()) {
                score.setDisplayName(next);
            }
        }
    }

    @EventHandler(
            priority = EventPriority.HIGHEST
    )
    public void onPlayerJoin(PlayerJoinEvent event) {
        this.handleScoreboard(event.getPlayer());
        this.updateScoreboard(event.getPlayer());
        this.updatePlayers();
    }

    @EventHandler(
            priority = EventPriority.HIGHEST
    )
    public void onPlayerQuit(PlayerQuitEvent event) {
        this.updatePlayers();
    }

    @EventHandler(
            priority = EventPriority.HIGHEST
    )
    public void onPlayerChange(PlayerChangeEvent event) {
        this.updatePlayers();
    }

    @EventHandler
    public void onPlayerLanguageChange(PlayerLanguageChangeEvent event) {
        ScoreHelper.getInstance().removeScoreboard(event.getPlayer());
        this.handleScoreboard(event.getPlayer());
        this.updateScoreboard(event.getPlayer());
    }

    @EventHandler
    public void onPlayerGroupChange(PlayerGroupChangeEvent event) {
        this.updateScoreboard(event.getPlayer());
    }

    private void handleScoreboard(Player player) {
        Member member = CommonPlugin.getInstance().getMemberManager().getMember(player.getUniqueId());
        Status status = CommonPlugin.getInstance().getStatusManager().loadStatus(player.getUniqueId(), StatusType.BEDWARS);

        Scoreboard scoreboard = new Scoreboard(player, "§b§lBEDWARS");
        scoreboard.add(14, "");
        scoreboard.add(13, "§fSeu nível: " + LobbyMain.getInstance().getColorByLevelPlusBrackets(status.getInteger(BedwarsCategory.BEDWARS_LEVEL)));
        scoreboard.add(12, "§8[" + LobbyMain.getInstance().createProgressBar('❚', 'a', '7', 6, status.getInteger(BedwarsCategory.BEDWARS_POINTS), LobbyMain.getInstance().getMaxPoints(status.getInteger(BedwarsCategory.BEDWARS_LEVEL))) + "§8]§r §7(" + status.getInteger(BedwarsCategory.BEDWARS_POINTS) + "/" + LobbyMain.getInstance().getMaxPoints(status.getInteger(BedwarsCategory.BEDWARS_LEVEL)) + ")");
        scoreboard.add(11, "");
        scoreboard.add(10, "§eSolo:");
        scoreboard.add(9, " §fVitórias: §a" + status.getInteger(BedwarsCategory.BEDWARS_WINS.getSpecialServer(ServerType.BW_SOLO)));
        scoreboard.add(8, " §fWinstreak: §a" + status.getInteger(BedwarsCategory.BEDWARS_WINSTREAK.getSpecialServer(ServerType.BW_SOLO)));
        scoreboard.add(7, "§eDuplas:");
        scoreboard.add(6, " §fVitórias: §a" + status.getInteger(BedwarsCategory.BEDWARS_WINS.getSpecialServer(ServerType.BW_DUOS)));
        scoreboard.add(5, " §fWinstreak: §a" + status.getInteger(BedwarsCategory.BEDWARS_WINSTREAK.getSpecialServer(ServerType.BW_DUOS)));
        scoreboard.add(4, "");
        scoreboard.add(3, "§f§%scoreboard-players%§: §b" + BukkitCommon.getInstance().getServerManager().getTotalMembers());
        scoreboard.add(2, "§a");
        scoreboard.add(1, "§awww." + CommonPlugin.getInstance().getPluginInfo().getWebsite());
        ScoreHelper.getInstance().setScoreboard(player, scoreboard);
    }

    private void updateScoreboard(Player player) {
        Member member = CommonPlugin.getInstance().getMemberManager().getMember(player.getUniqueId());
        Tag tag = CommonPlugin.getInstance().getPluginInfo().getTagByGroup(member.getServerGroup());
    }

    private void updatePlayers() {
        ScoreHelper.getInstance().updateScoreboard(3, "§f§%scoreboard-players%§: §b" + BukkitCommon.getInstance().getServerManager().getTotalMembers());
    }
}
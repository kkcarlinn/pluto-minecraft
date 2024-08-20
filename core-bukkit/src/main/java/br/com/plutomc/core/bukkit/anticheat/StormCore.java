package br.com.plutomc.core.bukkit.anticheat;

import br.com.plutomc.core.bukkit.BukkitCommon;
import br.com.plutomc.core.bukkit.anticheat.hack.HackType;
import br.com.plutomc.core.bukkit.anticheat.hack.verify.*;
import br.com.plutomc.core.bukkit.anticheat.listener.PlayerListener;
import br.com.plutomc.core.bukkit.anticheat.utils.ForcefieldManager;
import br.com.plutomc.core.bukkit.event.UpdateEvent;
import br.com.plutomc.core.bukkit.member.BukkitMember;
import br.com.plutomc.core.common.CommonConst;
import br.com.plutomc.core.common.CommonPlugin;
import br.com.plutomc.core.common.member.Member;
import br.com.plutomc.core.common.packet.types.PunishPlayerPacket;
import br.com.plutomc.core.common.punish.Punish;
import br.com.plutomc.core.common.punish.PunishType;
import br.com.plutomc.core.common.utils.string.StringFormat;
import com.google.common.collect.ImmutableList;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class StormCore {
   private static StormCore instance;
   private final Plugin plugin;
   private ForcefieldManager forcefieldManager;
   private Map<UUID, Long> banPlayerMap = new HashMap<>();

   public StormCore(Plugin plugin) {
      this.plugin = plugin;
      instance = this;
   }

   public void onLoad() {
   }

   public void onEnable() {
      this.forcefieldManager = new ForcefieldManager();
      Bukkit.getPluginManager().registerEvents(new AutoclickCheck(), this.plugin);
      Bukkit.getPluginManager().registerEvents(new AutosoupCheck(), this.plugin);
      Bukkit.getPluginManager().registerEvents(new FlyCheck(), this.plugin);
      Bukkit.getPluginManager().registerEvents(new GlideCheck(), this.plugin);
      Bukkit.getPluginManager().registerEvents(new KillauraCheck(), this.plugin);
      Bukkit.getPluginManager().registerEvents(new MacroCheck(), this.plugin);
      Bukkit.getPluginManager().registerEvents(new ReachCheck(), this.plugin);
      Bukkit.getPluginManager().registerEvents(new SpeedCheck(), this.plugin);
      Bukkit.getPluginManager().registerEvents(new PlayerListener(), this.plugin);
      Bukkit.getPluginManager()
         .registerEvents(
            new Listener() {
               @EventHandler
               public void onUpdate(UpdateEvent event) {
                  if (event.getType() == UpdateEvent.UpdateType.SECOND) {
                     ImmutableList.copyOf(StormCore.this.banPlayerMap.entrySet())
                        .forEach(
                           entry -> {
                              Player player = Bukkit.getPlayer(entry.getKey());
                              if (player == null) {
                                 CommonPlugin.getInstance()
                                    .getMemberManager()
                                    .getMembers()
                                    .stream()
                                    .filter(m -> m.isStaff() && m.getMemberConfiguration().isSeeingLogs())
                                    .forEach(m -> m.sendMessage("§cO jogador " + entry.getKey() + " foi desconectado antes de ser banido!"));
                                 StormCore.this.banPlayerMap.remove(entry.getKey());
                              } else {
                                 BukkitMember member = CommonPlugin.getInstance().getMemberManager().getMember(player.getUniqueId(), BukkitMember.class);
                                 if (member != null) {
                                    int seconds = (int)((entry.getValue() - System.currentTimeMillis()) / 1000L);
                                    HackType hackType = member.getUserData()
                                       .getHackMap()
                                       .entrySet()
                                       .stream()
                                       .sorted((o1, o2) -> Integer.compare(o1.getValue().getTimes(), o2.getValue().getTimes()))
                                       .findFirst()
                                       .orElse(null)
                                       .getKey();
                                    if (seconds <= 0) {
                                       this.banPlayer(player, member, hackType);
                                    } else {
                                       if (seconds <= 30 && seconds % 10 == 0 || seconds % 15 == 0) {
                                          CommonPlugin.getInstance()
                                             .getMemberManager()
                                             .getMembers()
                                             .stream()
                                             .filter(m -> m.isStaff() && m.getMemberConfiguration().isSeeingLogs())
                                             .forEach(
                                                m -> m.sendMessage(
                                                      "§cO jogador "
                                                         + player.getName()
                                                         + " será banido por uso de "
                                                         + StringFormat.formatString(hackType.name())
                                                         + " em "
                                                         + seconds
                                                         + " segundos!"
                                                   )
                                             );
                                       }
                                    }
                                 }
                              }
                           }
                        );
                  }
               }
      
               private void banPlayer(Player player, Member member, HackType hackType) {
                  Punish punish = null;
                  if (hackType.isPermanent()) {
                     punish = new Punish(member, CommonConst.CONSOLE_ID, "STORM", "Uso de " + StringFormat.formatString(hackType.name()), -1L, PunishType.BAN);
                  } else {
                     punish = new Punish(
                        member,
                        CommonConst.CONSOLE_ID,
                        "STORM",
                        "Uso de " + StringFormat.formatString(hackType.name()),
                        System.currentTimeMillis()
                           + (member.getPunishConfiguration().getPunish(PunishType.BAN).stream().filter(p -> p.getPunisherName().equals("STORM")).count() + 1L)
                              * 21600000L,
                        PunishType.BAN
                     );
                  }
      
                  CommonPlugin.getInstance().getServerData().sendPacket(new PunishPlayerPacket(player.getUniqueId(), punish));
                  StormCore.this.banPlayerMap.remove(player.getUniqueId());
               }
            },
            this.plugin
         );
   }

   public void onDisable() {
   }

   public void ignore(Player player, double seconds) {
      player.setMetadata("anticheat-ignore", BukkitCommon.getInstance().createMeta(System.currentTimeMillis() + (long)(seconds * 1000.0)));
   }

   public void autoban(UUID playerId) {
      this.banPlayerMap.put(playerId, System.currentTimeMillis() + 30500L);
   }

   public Plugin getPlugin() {
      return this.plugin;
   }

   public ForcefieldManager getForcefieldManager() {
      return this.forcefieldManager;
   }

   public Map<UUID, Long> getBanPlayerMap() {
      return this.banPlayerMap;
   }

   public static StormCore getInstance() {
      return instance;
   }
}

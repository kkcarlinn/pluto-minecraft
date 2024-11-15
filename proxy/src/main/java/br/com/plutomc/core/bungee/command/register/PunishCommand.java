package br.com.plutomc.core.bungee.command.register;

import br.com.plutomc.core.bungee.event.player.PlayerPardonedEvent;
import br.com.plutomc.core.bungee.event.player.PlayerPunishEvent;
import br.com.plutomc.core.bungee.account.BungeeAccount;
import br.com.plutomc.core.common.CommonPlugin;
import br.com.plutomc.core.common.PluginInfo;
import br.com.plutomc.core.common.command.CommandArgs;
import br.com.plutomc.core.common.command.CommandClass;
import br.com.plutomc.core.common.command.CommandFramework;
import br.com.plutomc.core.common.command.CommandSender;
import br.com.plutomc.core.common.account.Account;
import br.com.plutomc.core.common.punish.Punish;
import br.com.plutomc.core.common.punish.PunishType;
import br.com.plutomc.core.common.utils.DateUtils;
import br.com.plutomc.core.common.utils.string.StringFormat;
import com.google.common.base.Joiner;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class PunishCommand implements CommandClass {
    @CommandFramework.Command(
            name = "pardon",
            aliases = {"unpunish"},
            permission = "command.pardon",
            runAsync = true
    )
    public void pardonCommand(CommandArgs cmdArgs) {
        CommandSender sender = cmdArgs.getSender();
        String[] args = cmdArgs.getArgs();
        if (args.length <= 1) {
            sender.sendMessage(PluginInfo.t(sender, "command.pardon.usage", "%label%", cmdArgs.getLabel()));
        } else {
            this.pardon(sender, args, null);
        }
    }

    @CommandFramework.Command(
            name = "unban",
            permission = "command.pardon"
    )
    public void unbanCommand(CommandArgs cmdArgs) {
        CommandSender sender = cmdArgs.getSender();
        String[] args = cmdArgs.getArgs();
        if (args.length == 0) {
            sender.sendMessage("§eUse §b/" + cmdArgs.getLabel() + " <player>§e para desbanir o jogador.");
        } else {
            this.pardon(sender, args, PunishType.BAN);
        }
    }

    @CommandFramework.Command(
            name = "unmute",
            permission = "command.unmute"
    )
    public void unmuteCommand(CommandArgs cmdArgs) {
        CommandSender sender = cmdArgs.getSender();
        String[] args = cmdArgs.getArgs();
        if (args.length == 0) {
            sender.sendMessage("§eUse §b/" + cmdArgs.getLabel() + " <player>§e para desmutar o jogador.");
        } else {
            this.pardon(sender, args, PunishType.MUTE);
        }
    }

    @CommandFramework.Command(
            name = "punish",
            aliases = {"punir"},
            permission = "command.punish",
            runAsync = true
    )
    public void punishCommand(CommandArgs cmdArgs) {
        CommandSender sender = cmdArgs.getSender();
        String[] args = cmdArgs.getArgs();
        if (args.length <= 2) {
            sender.sendMessage(PluginInfo.t(sender, "command.punish.usage", "%label%", cmdArgs.getLabel()));
        } else {
            PunishType punishType = null;

            try {
                punishType = PunishType.valueOf(args[1].toUpperCase());
            } catch (Exception var6) {
                sender.sendMessage(PluginInfo.t(sender, "command.punish.type-not-exist", "%type%", args[1]));
                return;
            }

            String reason;
            if (args.length == 3) {
                reason = sender.getLanguage().t(punishType == PunishType.KICK ? "no-reason" : "defaut-ban");
            } else {
                reason = Joiner.on(' ').join(Arrays.copyOfRange(args, 3, args.length));
            }

            this.punish(sender, args[0], punishType, reason, !args[2].equals("0") && !args[2].equals("never") ? DateUtils.getTime(args[2]) : -1L);
        }
    }

    @CommandFramework.Command(
            name = "mute",
            aliases = {"ban", "tempban", "tempmute", "kick"},
            permission = "command.mute",
            runAsync = true
    )
    public void banMuteCommand(CommandArgs cmdArgs) {
        CommandSender sender = cmdArgs.getSender();
        String[] args = cmdArgs.getArgs();
        if (args.length <= 1) {
            sender.sendMessage("§eUse §b/" + cmdArgs.getLabel() + " <player> <tempo:never> <motivo>§e para punir alguém.");
        } else {
           boolean temp = !args[1].equalsIgnoreCase("never");
            this.punish(
                    sender,
                    args[0],
                    PunishType.valueOf(cmdArgs.getLabel().toUpperCase().replace("TEMP", "")),
                    args.length == (temp ? 2 : 1)
                            ? sender.getLanguage().t(cmdArgs.getLabel().equalsIgnoreCase("kick") ? "no-reason" : "defaut-ban")
                            : Joiner.on(' ').join(Arrays.copyOfRange(args, temp ? 2 : 1, args.length)),
                    temp ? DateUtils.getTime(args[1]) : -1L
            );
        }
    }

    public void punish(CommandSender sender, String playerName, PunishType punishType, String reason, long expireTime) {
        Account target = CommonPlugin.getInstance().getAccountManager().getAccountByName(playerName);
        if (target == null) {
            target = CommonPlugin.getInstance().getAccountData().loadAccount(playerName, true);
            if (target == null) {
                sender.sendMessage(sender.getLanguage().t("player-not-found", "%player%", playerName));
                return;
            }
        }

        if (target.getServerGroup().getId() >= sender.getServerGroup().getId() && sender.isPlayer()) {
            sender.sendMessage("§cVocê não pode banir cargo superior.");
        } else if (punishType != PunishType.KICK && target.getPunishConfiguration().getActualPunish(punishType) != null) {
            sender.sendMessage(
                    PluginInfo.t(sender, "command.punish.already-punished", "%type%", StringFormat.formatString(punishType.name()), "%player%", target.getName())
            );
        } else {
            boolean permanent = expireTime == -1L;
            Punish punish = new Punish(target, sender, reason, expireTime, punishType);
            switch (punish.getPunishType()) {
                case BAN:
                    this.banPlayer(target, punish);
                    break;
                case KICK:
                    if (!target.isOnline()) {
                        sender.sendMessage(PluginInfo.t(sender, "player-is-not-online", "%player%", target.getPlayerName()));
                        return;
                    }

                    this.kickPlayer(target, punish);
                    break;
                case MUTE:
                    target.sendMessage(
                            PluginInfo.t(
                                    sender,
                                    "mute-message",
                                    "%reason%",
                                    punish.getPunishReason(),
                                    "%expireAt%",
                                    DateUtils.getTime(target.getLanguage(), punish.getExpireAt()),
                                    "%punisher%",
                                    punish.getPunisherName(),
                                    "%website%",
                                    CommonPlugin.getInstance().getPluginInfo().getWebsite(),
                                    "%store%",
                                    CommonPlugin.getInstance().getPluginInfo().getStore(),
                                    "%discord%",
                                    CommonPlugin.getInstance().getPluginInfo().getDiscord(),
                                    "%id%",
                                    punish.getId()
                            )
                    );
            }

            target.getPunishConfiguration().punish(punish);
            target.saveConfig();
            sender.sendMessage(
                    PluginInfo.t(
                            sender,
                            "command.punish.success-" + punishType.name().toLowerCase() + "-" + (permanent ? "permanent" : "temporary"),
                            "%player%",
                            target.getName(),
                            "%reason%",
                            reason,
                            "%time%",
                            DateUtils.getTime(sender.getLanguage(), expireTime)
                    )
            );
            ProxyServer.getInstance().getPluginManager().callEvent(new PlayerPunishEvent(target, punish, sender));
        }
    }

    @CommandFramework.Completer(
            name = "ban",
            aliases = {"mute", "tempban", "tempmute", "tempbanir", "tempmutar", "punish", "pardon", "punir", "perdoar"}
    )
    public List<String> punishCompleter(CommandArgs cmdArgs) {
        switch (cmdArgs.getArgs().length) {
            case 1:
                return ProxyServer.getInstance()
                        .getPlayers()
                        .stream()
                        .filter(player -> player.getName().toLowerCase().startsWith(cmdArgs.getArgs()[0].toLowerCase()))
                        .<String>map(net.md_5.bungee.api.CommandSender::getName)
                        .collect(Collectors.toList());
            case 2:
                if (cmdArgs.getLabel().equalsIgnoreCase("punish")) {
                    return Arrays.asList(PunishType.values())
                            .stream()
                            .filter(type -> type.name().toLowerCase().startsWith(cmdArgs.getArgs()[0].toLowerCase()))
                            .map(Enum::name)
                            .collect(Collectors.toList());
                }
            default:
                return new ArrayList<>();
        }
    }

    private void kickPlayer(Account target, Punish punish) {
        if (target instanceof BungeeAccount) {
            BungeeAccount bungeeMember = (BungeeAccount) target;
            ProxiedPlayer player = bungeeMember.getProxiedPlayer();
            if (player != null) {
                player.disconnect(
                        PluginInfo.t(
                                bungeeMember,
                                "kick-message",
                                "%reason%",
                                punish.getPunishReason(),
                                "%punisher%",
                                punish.getPunisherName(),
                                "%website%",
                                CommonPlugin.getInstance().getPluginInfo().getWebsite(),
                                "%store%",
                                CommonPlugin.getInstance().getPluginInfo().getStore(),
                                "%discord%",
                                CommonPlugin.getInstance().getPluginInfo().getDiscord(),
                                "%id%",
                                punish.getId()
                        )
                );
            }
        }
    }

    public void banPlayer(Account target, Punish punish) {
        CommonPlugin.getInstance().getReportManager().notify(target.getUniqueId());
        if (target.isOnline()) {
            BungeeAccount bungeeMember = (BungeeAccount) target;
            ProxiedPlayer player = bungeeMember.getProxiedPlayer();
            if (player != null) {
                player.disconnect(
                        PluginInfo.t(
                                bungeeMember,
                                "ban-" + (punish.isPermanent() ? "permanent" : "temporary") + "-kick-message",
                                "%reason%",
                                punish.getPunishReason(),
                                "%expireAt%",
                                DateUtils.getTime(bungeeMember.getLanguage(), punish.getExpireAt()),
                                "%punisher%",
                                punish.getPunisherName(),
                                "%website%",
                                CommonPlugin.getInstance().getPluginInfo().getWebsite(),
                                "%store%",
                                CommonPlugin.getInstance().getPluginInfo().getStore(),
                                "%discord%",
                                CommonPlugin.getInstance().getPluginInfo().getDiscord(),
                                "%id%",
                                punish.getId()
                        )
                );
            }
        }
    }

    private void pardon(CommandSender sender, String[] args, PunishType punishType) {
        Account target = CommonPlugin.getInstance().getAccountManager().getAccountByName(args[0]);
        if (target == null) {
            target = CommonPlugin.getInstance().getAccountData().loadAccount(args[0], true);
            if (target == null) {
                sender.sendMessage(sender.getLanguage().t("player-not-found", "%player%", args[0]));
                return;
            }
        }

        if (punishType == null) {
            try {
                punishType = PunishType.valueOf(args[1].toUpperCase());
            } catch (Exception var6) {
            }
        }

        if (punishType != null && punishType != PunishType.KICK) {
            Punish actualPunish = target.getPunishConfiguration().getActualPunish(punishType);
            if (actualPunish == null) {
                sender.sendMessage(
                        PluginInfo.t(sender, "command.pardon.no-punish-found", "%type%", StringFormat.formatString(punishType.name()), "%player%", target.getName())
                );
            } else if (target.getPunishConfiguration().pardon(actualPunish, sender)) {
                sender.sendMessage(
                        PluginInfo.t(sender, "command.pardon.success", "%type%", StringFormat.formatString(punishType.name()), "%player%", target.getName())
                );
                target.saveConfig();
                ProxyServer.getInstance().getPluginManager().callEvent(new PlayerPardonedEvent(target, actualPunish, sender));
            } else {
                sender.sendMessage(
                        PluginInfo.t(sender, "command.pardon.failed", "%type%", StringFormat.formatString(punishType.name()), "%player%", target.getName())
                );
            }
        } else {
            sender.sendMessage(PluginInfo.t(sender, "command.punish.type-not-exist", "%type%", args[1]));
        }
    }
}

package br.com.plutomc.core.bungee.listener;

import br.com.plutomc.core.bungee.event.player.PlayerPardonedEvent;
import br.com.plutomc.core.bungee.event.player.PlayerPunishEvent;
import br.com.plutomc.core.common.CommonConst;
import br.com.plutomc.core.common.CommonPlugin;
import br.com.plutomc.core.common.command.CommandSender;
import br.com.plutomc.core.common.account.Account;
import br.com.plutomc.core.common.punish.Punish;
import br.com.plutomc.core.common.utils.DateUtils;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class LogListener implements Listener {
    @EventHandler
    public void onPlayerPunish(PlayerPunishEvent event) {
        Account target = event.getPunished();
        CommandSender sender = event.getSender();
        Punish punish = event.getPunish();
        if (punish.getPunisherId().equals(CommonConst.CONSOLE_ID)) {
            CommonPlugin.getInstance()
                    .getAccountManager()
                    .getAccounts()
                    .stream()
                    .filter(Account::isStaff)
                    .forEach(
                            member -> member.sendMessage(
                                    "§cO jogador "
                                            + target.getName()
                                            + " foi "
                                            + punish.getPunishType().getDescriminator()
                                            + " do servidor por "
                                            + punish.getPunishReason()
                                            + " pelo CONSOLE."
                            )
                    );
        } else {
            switch(punish.getPunishType()) {
                case KICK:
                    CommonPlugin.getInstance()
                            .getAccountManager()
                            .staffLog(
                                    "§cO jogador " + target.getPlayerName() + " foi expulso do servidor por " + punish.getPunishReason() + " pelo " + sender.getName() + ".",
                                    false
                            );
                    break;
                default:
                    CommonPlugin.getInstance()
                            .getAccountManager()
                            .getAccounts()
                            .stream()
                            .forEach(
                                    member -> {
                                        if (member.isStaff()) {
                                            member.sendMessage(
                                                    "§cO jogador "
                                                            + target.getPlayerName()
                                                            + " foi "
                                                            + punish.getPunishType().getDescriminator().toLowerCase()
                                                            + " "
                                                            + (
                                                            punish.isPermanent()
                                                                    ? "permanentemente"
                                                                    : " temporariamente com duração de "
                                                                    + DateUtils.formatDifference(
                                                                    CommonPlugin.getInstance().getPluginInfo().getDefaultLanguage(), punish.getExpireTime() / 1000L
                                                            )
                                                    )
                                                            + " por "
                                                            + punish.getPunishReason()
                                                            + " pelo "
                                                            + sender.getName()
                                                            + "."
                                            );
                                        } else {
                                            member.sendMessage(
                                                    "§cO jogador "
                                                            + target.getName()
                                                            + " foi banido "
                                                            + (punish.isPermanent() ? "permanentemente" : "temporariamente")
                                                            + " do servidor."
                                            );
                                        }
                                    }
                            );
            }
        }
    }

    @EventHandler
    public void onPlayerPardoned(PlayerPardonedEvent event) {
        Account target = event.getPunished();
        CommandSender sender = event.getSender();
        Punish punish = event.getPunish();
        CommonPlugin.getInstance()
                .getAccountManager()
                .staffLog(
                        "§eO jogador " + target.getPlayerName() + " foi des" + punish.getPunishType().getDescriminator().toLowerCase() + " pelo " + sender.getName() + ".",
                        false
                );
    }
}
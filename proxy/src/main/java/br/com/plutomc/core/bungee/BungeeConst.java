package br.com.plutomc.core.bungee;

import br.com.plutomc.core.bungee.command.BungeeCommandSender;
import br.com.plutomc.core.common.command.CommandSender;
import net.md_5.bungee.api.ProxyServer;

public class BungeeConst {
   public static final int MAX_PLAYERS = 3000;
   public static final CommandSender CONSOLE_SENDER = new BungeeCommandSender(ProxyServer.getInstance().getConsole());
   public static final String BROADCAST_PREFIX = "§b§lPLUTO §6» ";
}

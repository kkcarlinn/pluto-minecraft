package br.com.plutomc.core.bukkit;

import java.util.Arrays;
import java.util.List;

import br.com.plutomc.core.bukkit.command.BukkitCommandSender;
import br.com.plutomc.core.common.command.CommandSender;
import org.bukkit.Bukkit;

public class BukkitConst {
   public static final List<String> RANDOM = Arrays.asList("BeicolaPvP__", "PernasAbertasPvP", "_InternetPvP_", "_XablauKits_");
   public static final List<String> SWEAR_WORDS = Arrays.asList(
      "merda",
      "loser",
      "cu",
      "porra",
      "buceta",
      "lixo",
      "random",
      "bct",
      "caralho",
      "fdp",
      "vsf",
      "vsfd",
      "tnc",
      "vtnc",
      "crl",
      "klux",
      "arrombado",
      "krl",
      "hypemc",
      "hype",
      "mushmc",
      "prismamc",
      "mush",
      "prisma",
      "prismamc.com.br",
      "hypemc.com.br",
      "weaven",
      "weavenmc",
      "weavenhg",
      "mc-weaven.com.br",
      "weaven-network.com.br",
      "weaven-network",
      "logicmc.com.br",
      "empire-network.com.br",
      "empire",
      "empiremc",
      "empire-network",
      "wayzemc.com.br",
      "wayze",
      "wayzemc",
      "macaco",
      "macacos",
      "mushmc.com.br",
      "seu negro",
      "seu preto"
   );
   public static final boolean CREATE_MEMBER = false;
   public static final double TPS = 20.0;
   public static final int ITEMS_PER_PAGE = 21;
   public static final CommandSender CONSOLE_SENDER = new BukkitCommandSender(Bukkit.getConsoleSender());
   public static final String PERMISION_ADMIN_MODE = "command.admin";
   public static final String ANTICHEAT_BYPASS = "anticheat.bypass";
   public static final String PERMISSION_CHAT_DISABLED = "chat.disabled-say";
   public static final String PERMISSION_CHAT_PAYMENT = "chat.payment-say";
   public static final String PERMISSION_CHAT_YOUTUBER = "chat.youtuber-say";
   public static final String ANTICHEAT_IGNORE_METADATA = "anticheat-ignore";
}

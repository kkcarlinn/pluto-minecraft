package br.com.plutomc.core.bukkit.utils.player;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.PacketType.Play.Server;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.EnumWrappers.TitleAction;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import br.com.plutomc.core.common.CommonPlugin;
import br.com.plutomc.core.bukkit.utils.PacketBuilder;
import br.com.plutomc.core.bukkit.utils.ProtocolVersion;
import br.com.plutomc.core.bukkit.utils.StringLoreUtils;
import br.com.plutomc.core.common.language.Language;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class PlayerHelper {
   public static void broadcastHeader(String header) {
      broadcastHeaderAndFooter(header, null);
   }

   public static void broadcastFooter(String footer) {
      broadcastHeaderAndFooter(null, footer);
   }

   public static void broadcastHeaderAndFooter(String header, String footer) {
      for(Player player : Bukkit.getOnlinePlayers()) {
         setHeaderAndFooter(player, header, footer);
      }
   }

   public static void setHeader(Player p, String header) {
      setHeaderAndFooter(p, header, null);
   }

   public static void setFooter(Player p, String footer) {
      setHeaderAndFooter(p, null, footer);
   }

   public static void setHeaderAndFooter(Player p, String rawHeader, String rawFooter) {
      PacketContainer packet = new PacketContainer(Server.PLAYER_LIST_HEADER_FOOTER);
      packet.getChatComponents().write(0, WrappedChatComponent.fromText(rawHeader));
      packet.getChatComponents().write(1, WrappedChatComponent.fromText(rawFooter));

      try {
         ProtocolLibrary.getProtocolManager().sendServerPacket(p, packet);
      } catch (InvocationTargetException var5) {
         var5.printStackTrace();
      }
   }

   public static void title(Player player, String title, String subTitle) {
      if (ProtocolVersion.getProtocolVersion(player).getId() >= 47) {
         sendPacket(
            player, new PacketBuilder(Server.TITLE).writeTitleAction(0, TitleAction.TITLE).writeChatComponents(0, WrappedChatComponent.fromText(title)).build()
         );
         sendPacket(
            player,
            new PacketBuilder(Server.TITLE).writeTitleAction(0, TitleAction.SUBTITLE).writeChatComponents(0, WrappedChatComponent.fromText(subTitle)).build()
         );
         sendPacket(
            player, new PacketBuilder(Server.TITLE).writeTitleAction(0, TitleAction.TIMES).writeInteger(0, 10).writeInteger(1, 20).writeInteger(2, 20).build()
         );
      }
   }

   public static void subtitle(Player player, String subTitle) {
      if (ProtocolVersion.getProtocolVersion(player).getId() >= 47) {
         sendPacket(
            player,
            new PacketBuilder(Server.TITLE).writeTitleAction(0, TitleAction.SUBTITLE).writeChatComponents(0, WrappedChatComponent.fromText(subTitle)).build()
         );
      }
   }

   public static void title(Player player, String title, String subTitle, int fadeIn, int stayIn, int fadeOut) {
      if (ProtocolVersion.getProtocolVersion(player).getId() >= 47) {
         sendPacket(
            player, new PacketBuilder(Server.TITLE).writeTitleAction(0, TitleAction.TITLE).writeChatComponents(0, WrappedChatComponent.fromText(title)).build()
         );
         sendPacket(
            player,
            new PacketBuilder(Server.TITLE).writeTitleAction(0, TitleAction.SUBTITLE).writeChatComponents(0, WrappedChatComponent.fromText(subTitle)).build()
         );
         sendPacket(
            player,
            new PacketBuilder(Server.TITLE)
               .writeTitleAction(0, TitleAction.TIMES)
               .writeInteger(0, fadeIn)
               .writeInteger(1, stayIn)
               .writeInteger(2, fadeOut)
               .build()
         );
      }
   }

   public static void actionbar(Player player, String text) {
      PacketContainer packet = new PacketContainer(Server.CHAT);
      packet.getChatComponents().write(0, WrappedChatComponent.fromJson("{\"text\":\"" + text + " \"}"));
      packet.getBytes().write(0, (byte)2);
      sendPacket(player, packet);
   }

   public static void broadcastActionBar(String text) {
      Bukkit.getOnlinePlayers().forEach(player -> actionbar(player, text));
   }

   public static void sendPacket(Player player, PacketContainer packet) {
      try {
         ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet);
      } catch (InvocationTargetException var3) {
         var3.printStackTrace();
      }
   }

   public static String translate(Language lang, String string) {
      return CommonPlugin.getInstance().getPluginInfo().findAndTranslate(lang, string);
   }

   public static ItemStack translate(Language lang, ItemStack item) {
      if (item != null && item.getType() != Material.AIR && item.hasItemMeta()) {
         ItemMeta meta = item.getItemMeta();
         if (meta.hasDisplayName()) {
            String name = meta.getDisplayName();
            meta.setDisplayName(translate(lang, name));
         }

         if (meta.hasLore()) {
            List<String> lore = new ArrayList<>();

            for(String line : meta.getLore()) {
               line = translate(lang, line);
               if (line.contains("\n")) {
                  String[] split = line.split("\n");

                  for(int i = 0; i < split.length; ++i) {
                     lore.addAll(StringLoreUtils.formatForLore(split[i]));
                  }
               } else {
                  lore.addAll(StringLoreUtils.formatForLore(line));
               }
            }

            meta.setLore(lore);
         }

         item.setItemMeta(meta);
      }

      return item;
   }
}

package br.com.plutomc.core.bukkit.protocol.impl;

import br.com.plutomc.core.bukkit.BukkitCommon;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.PacketType.Play.Client;
import com.comphenix.protocol.PacketType.Play.Server;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import io.netty.buffer.ByteBuf;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import br.com.plutomc.core.common.CommonPlugin;
import br.com.plutomc.core.bukkit.protocol.PacketInjector;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class LimiterInjector implements PacketInjector {
   private final Pattern pattern = Pattern.compile(".*\\$\\{[^}]*\\}.*");

   @Override
   public void inject(Plugin plugin) {
      ProtocolLibrary.getProtocolManager()
         .addPacketListener(
            new PacketAdapter(BukkitCommon.getInstance(), ListenerPriority.LOWEST, Client.WINDOW_CLICK, Client.CUSTOM_PAYLOAD) {
               public void onPacketReceiving(PacketEvent event) {
                  if (event.getPlayer() != null) {
                     if (event.getPacketType() == Client.WINDOW_CLICK) {
                        if ((int)event.getPacket().getModifier().getValues().get(1) >= 100) {
                           event.setCancelled(true);
                           LimiterInjector.this.disconnect(event.getPlayer(), "§cYou are sending too many packets.");
                           CommonPlugin.getInstance().debug("The player " + event.getPlayer().getName() + " is trying to crash the server (WindowClick)");
                        }
                     } else {
                        String packetName = (String)event.getPacket().getStrings().getValues().get(0);
                        if ((packetName.equals("MC|BEdit") || packetName.equals("MC|BSign"))
                           && ((ByteBuf)event.getPacket().getModifier().getValues().get(1)).capacity() > 7500) {
                           event.setCancelled(true);
                           LimiterInjector.this.disconnect(event.getPlayer(), "§cYou are sending too many packets.");
                           CommonPlugin.getInstance().debug("The player " + event.getPlayer().getName() + " is trying to crash the server (CustomPayload)");
                        }
                     }
                  }
               }
            }
         );
      ProtocolLibrary.getProtocolManager()
         .addPacketListener(new PacketAdapter(BukkitCommon.getInstance(), ListenerPriority.LOWEST, Server.CHAT, Client.CHAT, Client.WINDOW_CLICK) {
            public void onPacketSending(PacketEvent event) {
               if (event.getPacketType() == Server.CHAT) {
                  PacketContainer packetContainer = event.getPacket();
                  WrappedChatComponent wrappedChatComponent = (WrappedChatComponent)packetContainer.getChatComponents().getValues().get(0);
                  if (wrappedChatComponent == null) {
                     return;
                  }
   
                  String jsonMessage = wrappedChatComponent.getJson();
                  if (jsonMessage.indexOf(36) == -1) {
                     return;
                  }
   
                  if (LimiterInjector.this.matches(jsonMessage)) {
                     event.setCancelled(true);
                     packetContainer.getChatComponents().write(0, WrappedChatComponent.fromText(""));
                  }
               }
            }
   
            public void onPacketReceiving(PacketEvent event) {
               if (event.getPacketType() == Client.CHAT) {
                  PacketContainer packetContainer = event.getPacket();
                  String message = (String)packetContainer.getStrings().read(0);
                  if (message.indexOf(36) == -1) {
                     return;
                  }
   
                  if (LimiterInjector.this.matches(message)) {
                     event.setCancelled(true);
                     packetContainer.getStrings().write(0, "");
                     CommonPlugin.getInstance().debug("The player " + event.getPlayer().getName() + " is trying to crash the server (Chat)");
                  }
               }
            }
         });
   }

   private void disconnect(Player player, String string) {
   }

   private boolean matches(String message) {
      Matcher matcher = this.pattern.matcher(message.replaceAll("[^\\x00-\\x7F]", "").toLowerCase(Locale.ROOT));
      return matcher.find();
   }
}

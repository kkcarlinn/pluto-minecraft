package br.com.plutomc.core.bukkit.protocol.impl;

import br.com.plutomc.core.bukkit.utils.player.PlayerHelper;
import br.com.plutomc.core.bukkit.protocol.PacketInjector;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.PacketType.Play.Server;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;
import com.google.common.base.Splitter;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.Iterator;
import java.util.regex.Matcher;
import br.com.plutomc.core.common.CommonConst;
import br.com.plutomc.core.common.CommonPlugin;
import br.com.plutomc.core.common.language.Language;
import br.com.plutomc.core.common.account.Account;
import org.bukkit.ChatColor;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public class TranslationInjector implements PacketInjector {
   @Override
   public void inject(Plugin plugin) {
      ProtocolLibrary.getProtocolManager()
         .addPacketListener(
            new PacketAdapter(
               plugin,
               ListenerPriority.NORMAL,
               Server.CHAT,
               Server.WINDOW_ITEMS,
               Server.SET_SLOT,
               Server.OPEN_WINDOW,
               Server.UPDATE_SIGN,
               Server.SCOREBOARD_OBJECTIVE,
               Server.SCOREBOARD_TEAM,
               Server.SCOREBOARD_SCORE,
               Server.PLAYER_LIST_HEADER_FOOTER,
               Server.SPAWN_ENTITY_LIVING,
               Server.ENTITY_METADATA,
               Server.TITLE
            ) {
               public void onPacketSending(PacketEvent event) {
                  if (event.getPlayer() != null) {
                     if (event.getPlayer().getUniqueId() != null) {
                        if (event.getPacket() != null) {
                           if (!event.isReadOnly()) {
                              Language lang = Account.getLanguage(event.getPlayer().getUniqueId());
                              if (event.getPacketType() == Server.CHAT) {
                                 PacketContainer packet = event.getPacket().deepClone();
      
                                 for(int i = 0; i < packet.getChatComponents().size(); ++i) {
                                    WrappedChatComponent chatComponent = (WrappedChatComponent)packet.getChatComponents().read(i);
                                    if (chatComponent != null) {
                                       packet.getChatComponents()
                                          .write(i, WrappedChatComponent.fromJson(PlayerHelper.translate(lang, chatComponent.getJson())));
                                    }
                                 }
      
                                 event.setPacket(packet);
                              } else if (event.getPacketType() == Server.WINDOW_ITEMS) {
                                 PacketContainer packet = event.getPacket().deepClone();
      
                                 for(ItemStack item : (ItemStack[])packet.getItemArrayModifier().read(0)) {
                                    if (item != null) {
                                       PlayerHelper.translate(lang, item);
                                    }
                                 }
      
                                 event.setPacket(packet);
                              } else if (event.getPacketType() == Server.SET_SLOT) {
                                 PacketContainer packet = event.getPacket().deepClone();
                                 ItemStack item = (ItemStack)packet.getItemModifier().read(0);
                                 packet.getItemModifier().write(0, PlayerHelper.translate(lang, item));
                                 event.setPacket(packet);
                              } else if (event.getPacketType() == Server.TITLE) {
                                 PacketContainer packet = event.getPacket().deepClone();
                                 WrappedChatComponent component = (WrappedChatComponent)event.getPacket().getChatComponents().read(0);
                                 if (component == null) {
                                    return;
                                 }
      
                                 packet.getChatComponents().write(0, WrappedChatComponent.fromJson(PlayerHelper.translate(lang, component.getJson())));
                                 event.setPacket(packet);
                              } else if (event.getPacketType() == Server.SCOREBOARD_SCORE) {
                                 PacketContainer packet = event.getPacket().deepClone();
                                 String message = (String)event.getPacket().getStrings().read(0);
                                 packet.getStrings().write(0, PlayerHelper.translate(lang, message));
                                 event.setPacket(packet);
                              } else if (event.getPacketType() == Server.OPEN_WINDOW) {
                                 PacketContainer packet = event.getPacket().deepClone();
                                 WrappedChatComponent component = (WrappedChatComponent)event.getPacket().getChatComponents().read(0);
                                 JsonElement element = JsonParser.parseString(component.getJson());
                                 if (!(element instanceof JsonObject) || !((JsonObject)element).has("translate")) {
                                    String message = PlayerHelper.translate(lang, element.getAsString());
                                    message = message.substring(0, message.length() > 32 ? 32 : message.length());
                                    packet.getChatComponents().write(0, WrappedChatComponent.fromText(message));
                                    event.setPacket(packet);
                                 }
                              } else if (event.getPacketType() == Server.SCOREBOARD_OBJECTIVE) {
                                 PacketContainer packet = event.getPacket().deepClone();
                                 String message = (String)event.getPacket().getStrings().read(1);
                                 packet.getStrings().write(1, PlayerHelper.translate(lang, message));
                                 event.setPacket(packet);
                              } else if (event.getPacketType() == Server.SCOREBOARD_TEAM) {
                                 PacketContainer packet = event.getPacket().deepClone();
                                 String pre = (String)packet.getStrings().read(2);
                                 String su = (String)packet.getStrings().read(3);
                                 boolean matched = false;
      
                                 for(Matcher matcher = CommonConst.TRANSLATE_PATTERN.matcher(pre); matcher.find(); matched = true) {
                                    pre = pre.replace(matcher.group(), CommonPlugin.getInstance().getPluginInfo().translate(lang, matcher.group(2)));
                                 }
      
                                 for(Matcher var49 = CommonConst.TRANSLATE_PATTERN.matcher(su); var49.find(); matched = true) {
                                    su = su.replace(var49.group(), CommonPlugin.getInstance().getPluginInfo().translate(lang, var49.group(2)));
                                 }
      
                                 if (matched && pre.length() <= 16 && su.length() <= 16) {
                                    packet.getStrings().write(2, pre);
                                    packet.getStrings().write(3, su);
                                    event.setPacket(packet);
                                    return;
                                 }
      
                                 String text = (String)packet.getStrings().read(2) + (String)packet.getStrings().read(3);
                                 Matcher var50 = CommonConst.TRANSLATE_PATTERN.matcher(text);
      
                                 for(matched = false; var50.find(); matched = true) {
                                    text = text.replace(var50.group(), CommonPlugin.getInstance().getPluginInfo().translate(lang, var50.group(2)));
                                 }
      
                                 if (!matched) {
                                    return;
                                 }
      
                                 Iterator<String> iterator = Splitter.fixedLength(16).split(text).iterator();
                                 String str = iterator.next();
                                 String prefix;
                                 String suffix;
                                 if (str.endsWith("§")) {
                                    str = str.substring(0, str.length() - 1);
                                    prefix = str;
                                    if (iterator.hasNext()) {
                                       String next = iterator.next();
                                       if (!next.startsWith("§")) {
                                          String str2 = "§" + next;
                                          if (str2.length() > 16) {
                                             str2 = str2.substring(0, 16);
                                          }
      
                                          suffix = str2;
                                       } else {
                                          suffix = next;
                                       }
                                    } else {
                                       suffix = "";
                                    }
                                 } else if (iterator.hasNext()) {
                                    String next = iterator.next();
                                    if (!next.startsWith("§")) {
                                       String colors = ChatColor.getLastColors(str);
                                       String str3 = colors + next;
                                       if (str3.length() > 16) {
                                          str3 = str3.substring(0, 16);
                                       }
      
                                       prefix = str;
                                       suffix = str3;
                                    } else {
                                       prefix = str;
                                       suffix = next;
                                    }
                                 } else {
                                    prefix = str;
                                    suffix = "";
                                 }
      
                                 packet.getStrings().write(2, prefix);
                                 packet.getStrings().write(3, suffix);
                                 event.setPacket(packet);
                              } else if (event.getPacketType() == Server.PLAYER_LIST_HEADER_FOOTER) {
                                 PacketContainer packet = event.getPacket().deepClone();
                                 WrappedChatComponent header = (WrappedChatComponent)packet.getChatComponents().read(0);
                                 WrappedChatComponent footer = (WrappedChatComponent)packet.getChatComponents().read(1);
                                 if (header != null) {
                                    packet.getChatComponents().write(0, WrappedChatComponent.fromJson(PlayerHelper.translate(lang, header.getJson())));
                                 }
      
                                 if (footer != null) {
                                    packet.getChatComponents().write(1, WrappedChatComponent.fromJson(PlayerHelper.translate(lang, footer.getJson())));
                                 }
      
                                 event.setPacket(packet);
                              } else if (event.getPacketType() == Server.ENTITY_METADATA) {
                                 PacketContainer packet = event.getPacket().deepClone();
      
                                 for(WrappedWatchableObject obj : packet.getWatchableCollectionModifier().read(0)) {
                                    if (obj.getIndex() == 2) {
                                       String str = (String)obj.getRawValue();
                                       str = PlayerHelper.translate(lang, str);
                                       obj.setValue(str);
                                       break;
                                    }
                                 }
      
                                 event.setPacket(packet);
                              } else if (event.getPacketType() == Server.SPAWN_ENTITY_LIVING) {
                                 PacketContainer packet = event.getPacket();
                                 int type = packet.getIntegers().read(1);
                                 if (type != EntityType.ARMOR_STAND.getTypeId()) {
                                    return;
                                 }
      
                                 PacketContainer packetClone = event.getPacket().deepClone();
      
                                 for(WrappedWatchableObject obj : ((WrappedDataWatcher)packetClone.getDataWatcherModifier().read(0)).getWatchableObjects()) {
                                    if (obj.getIndex() == 2) {
                                       String str = (String)obj.getRawValue();
                                       str = PlayerHelper.translate(lang, str);
                                       obj.setValue(str);
                                       break;
                                    }
                                 }
      
                                 event.setPacket(packetClone);
                              }
                           }
                        }
                     }
                  }
               }
            }
         );
   }
}

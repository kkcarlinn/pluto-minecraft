package br.com.plutomc.core.bungee.account;

import br.com.plutomc.core.common.CommonPlugin;
import br.com.plutomc.core.common.account.Account;
import br.com.plutomc.core.common.account.configuration.LoginConfiguration;
import br.com.plutomc.core.common.packet.types.ActionBar;
import net.md_5.bungee.BungeeTitle;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.UUID;

public class BungeeAccount extends Account {
   private transient ProxiedPlayer proxiedPlayer;

   public BungeeAccount(UUID uniqueId, String playerName, LoginConfiguration.AccountType accountType) {
      super(uniqueId, playerName, accountType);
   }

   @Override
   public boolean hasPermission(String permission) {
      return this.proxiedPlayer != null && this.proxiedPlayer.hasPermission(permission.toLowerCase()) ? true : super.hasPermission(permission);
   }

   @Override
   public void sendMessage(String message) {
      if (this.proxiedPlayer != null) {
         this.proxiedPlayer.sendMessage(CommonPlugin.getInstance().getPluginInfo().findAndTranslate(this.getLanguage(), message));
      }
   }

   @Override
   public void sendMessage(BaseComponent str) {
      if (this.proxiedPlayer != null) {
         this.proxiedPlayer.sendMessage(str);
      }
   }

   @Override
   public void sendMessage(BaseComponent... fromLegacyText) {
      if (this.proxiedPlayer != null) {
         this.proxiedPlayer.sendMessage(fromLegacyText);
      }
   }

   @Override
   public void sendTitle(String title, String subTitle, int fadeIn, int stayIn, int fadeOut) {
      if (this.proxiedPlayer != null) {
         BungeeTitle packet = new BungeeTitle();
         packet.title(TextComponent.fromLegacyText(title));
         packet.subTitle(TextComponent.fromLegacyText(subTitle));
         packet.fadeIn(fadeIn);
         packet.fadeOut(fadeOut);
         packet.stay(stayIn);
         packet.send(this.proxiedPlayer);
      }
   }

   @Override
   public void sendActionBar(String message) {
      if (this.proxiedPlayer != null) {
         CommonPlugin.getInstance().getServerData().sendPacket(new ActionBar(this.getUniqueId(), message).server(new String[]{this.getActualServerId()}));
      }
   }

   @Override
   public void saveConfig() {
      super.saveConfig();
      this.save("ipAddress", "lastIpAddress", "firstLogin", "lastLogin", "joinTime", "onlineTime", "online");
   }

   public ProxiedPlayer getProxiedPlayer() {
      return this.proxiedPlayer;
   }

   public void setProxiedPlayer(ProxiedPlayer proxiedPlayer) {
      this.proxiedPlayer = proxiedPlayer;
   }
}

package br.com.plutomc.core.bungee.command;

import java.util.UUID;
import br.com.plutomc.core.common.CommonConst;
import br.com.plutomc.core.common.CommonPlugin;
import br.com.plutomc.core.common.command.CommandSender;
import br.com.plutomc.core.common.language.Language;
import br.com.plutomc.core.common.account.Account;
import br.com.plutomc.core.common.account.Profile;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class BungeeCommandSender implements CommandSender {
   private final net.md_5.bungee.api.CommandSender sender;
   private UUID replyId;
   private boolean tellEnabled;

   @Override
   public UUID getUniqueId() {
      return this.sender instanceof ProxiedPlayer ? ((ProxiedPlayer)this.sender).getUniqueId() : CommonConst.CONSOLE_ID;
   }

   @Override
   public String getName() {
      return this.sender instanceof ProxiedPlayer ? ((ProxiedPlayer)this.sender).getName() : "CONSOLE";
   }

   @Override
   public void sendMessage(String str) {
      this.sender.sendMessage(TextComponent.fromLegacyText(this.translate(this.getLanguage(), str)));
   }

   @Override
   public void sendMessage(BaseComponent baseComponent) {
      this.sender.sendMessage(baseComponent);
   }

   @Override
   public void sendMessage(BaseComponent... fromLegacyText) {
      this.sender.sendMessage(fromLegacyText);
   }

   @Override
   public boolean isPlayer() {
      return this.sender instanceof ProxiedPlayer;
   }

   @Override
   public String getSenderName() {
      return this.sender.getName();
   }

   @Override
   public boolean hasPermission(String permission) {
      return this.sender instanceof ProxiedPlayer
         ? CommonPlugin.getInstance().getAccountManager().getAccount(this.getUniqueId()).hasPermission(permission)
         : true;
   }

   @Override
   public Language getLanguage() {
      return this.isPlayer() ? ((Account)this.getSender()).getLanguage() : CommonPlugin.getInstance().getPluginInfo().getDefaultLanguage();
   }

   public String translate(Language language, String string) {
      return CommonPlugin.getInstance().getPluginInfo().findAndTranslate(language, string);
   }

   @Override
   public boolean isStaff() {
      if (this.sender instanceof ProxiedPlayer) {
         Account account = CommonPlugin.getInstance().getAccountManager().getAccount(((ProxiedPlayer)this.sender).getUniqueId());
         return account.getServerGroup().isStaff();
      } else {
         return true;
      }
   }

   @Override
   public boolean isUserBlocked(Profile profile) {
      return this.isPlayer() ? ((Account)this.getSender()).isUserBlocked(profile) : false;
   }

   public BungeeCommandSender(net.md_5.bungee.api.CommandSender sender) {
      this.sender = sender;
   }

   public net.md_5.bungee.api.CommandSender getSender() {
      return this.sender;
   }

   @Override
   public UUID getReplyId() {
      return this.replyId;
   }

   @Override
   public boolean isTellEnabled() {
      return this.tellEnabled;
   }

   @Override
   public void setReplyId(UUID replyId) {
      this.replyId = replyId;
   }

   @Override
   public void setTellEnabled(boolean tellEnabled) {
      this.tellEnabled = tellEnabled;
   }
}

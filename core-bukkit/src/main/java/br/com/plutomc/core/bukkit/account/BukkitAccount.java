package br.com.plutomc.core.bukkit.account;

import java.util.UUID;

import br.com.plutomc.core.bukkit.anticheat.gamer.UserData;
import br.com.plutomc.core.bukkit.event.member.PlayerChangeTagEvent;
import br.com.plutomc.core.bukkit.event.member.PlayerChangedTagEvent;
import br.com.plutomc.core.bukkit.event.member.PlayerGroupChangeEvent;
import br.com.plutomc.core.bukkit.event.member.PlayerLanguageChangeEvent;
import br.com.plutomc.core.bukkit.utils.player.PlayerHelper;
import br.com.plutomc.core.common.language.Language;
import br.com.plutomc.core.common.account.Account;
import br.com.plutomc.core.common.account.configuration.LoginConfiguration;
import br.com.plutomc.core.common.permission.GroupInfo;
import br.com.plutomc.core.common.permission.Tag;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class BukkitAccount extends Account {
   private transient Player player;
   private transient boolean buildEnabled;
   private transient UserData userData;
   private boolean anticheatBypass;

   public BukkitAccount(UUID uniqueId, String playerName) {
      super(uniqueId, playerName, LoginConfiguration.AccountType.NONE);
   }

   public void setAnticheatBypass(boolean anticheatBypass) {
      this.anticheatBypass = anticheatBypass;
      this.save(new String[]{"anticheatBypass"});
   }

   public UserData getUserData() {
      if (this.userData == null) {
         this.userData = new UserData(this.getUniqueId(), this.getPlayerName());
      }

      return this.userData;
   }

   @Override
   public void setServerGroup(String groupName, GroupInfo groupInfo) {
      super.setServerGroup(groupName, groupInfo);
      Bukkit.getPluginManager()
         .callEvent(new PlayerGroupChangeEvent(this.player, this, groupName, groupInfo.getExpireTime(), PlayerGroupChangeEvent.Action.SET));
   }

   @Override
   public void removeServerGroup(String groupName) {
      super.removeServerGroup(groupName);
      Bukkit.getPluginManager().callEvent(new PlayerGroupChangeEvent(this.player, this, groupName, 0L, PlayerGroupChangeEvent.Action.REMOVE));
   }

   @Override
   public void addServerGroup(String groupName, GroupInfo groupInfo) {
      super.addServerGroup(groupName, groupInfo);
      Bukkit.getPluginManager()
         .callEvent(new PlayerGroupChangeEvent(this.player, this, groupName, groupInfo.getExpireTime(), PlayerGroupChangeEvent.Action.ADD));
   }

   @Override
   public void setLanguage(Language language) {
      super.setLanguage(language);
      Bukkit.getPluginManager().callEvent(new PlayerLanguageChangeEvent(this.player, language));
   }

   @Override
   public void sendMessage(String str) {
      if (this.player != null) {
         this.player.sendMessage(PlayerHelper.translate(this.getLanguage(), str));
      }
   }

   @Override
   public void sendMessage(BaseComponent str) {
      if (this.player != null) {
         this.player.spigot().sendMessage(str);
      }
   }

   @Override
   public void sendMessage(BaseComponent... fromLegacyText) {
      if (this.player != null) {
         this.player.spigot().sendMessage(fromLegacyText);
      }
   }

   @Override
   public boolean hasPermission(String permission) {
      if (this.player == null) {
         return super.hasPermission(permission);
      } else {
         return this.player.hasPermission(permission) || super.hasPermission(permission);
      }
   }

   @Override
   public void sendTitle(String title, String subTitle, int fadeIn, int stayIn, int fadeOut) {
      if (this.player != null) {
         PlayerHelper.title(this.player, title, subTitle, fadeIn, stayIn, fadeOut);
      }
   }

   @Override
   public void sendActionBar(String message) {
      if (this.player != null) {
         PlayerHelper.actionbar(this.player, message);
      }
   }

   @Override
   public boolean setTag(Tag tag) {
      return this.setTag(tag, false);
   }

   public boolean setTag(Tag tag, boolean forcetag) {
      PlayerChangeTagEvent event = new PlayerChangeTagEvent(this.player, this.getTag(), tag, forcetag);
      Bukkit.getPluginManager().callEvent(event);
      tag = event.getNewTag();
      if (!event.isCancelled() && !forcetag) {
         PlayerChangedTagEvent change = new PlayerChangedTagEvent(this.player, this, this.getTag(), tag, forcetag);
         Bukkit.getPluginManager().callEvent(change);
         tag = change.getNewTag();
         super.setTag(tag);
      }

      return !event.isCancelled();
   }

   @Override
   public boolean isOnline() {
      return super.isOnline();
   }

   public Player getPlayer() {
      return this.player;
   }

   public boolean isBuildEnabled() {
      return this.buildEnabled;
   }

   public boolean isAnticheatBypass() {
      return this.anticheatBypass;
   }

   public void setPlayer(Player player) {
      this.player = player;
   }

   public void setBuildEnabled(boolean buildEnabled) {
      this.buildEnabled = buildEnabled;
   }
}

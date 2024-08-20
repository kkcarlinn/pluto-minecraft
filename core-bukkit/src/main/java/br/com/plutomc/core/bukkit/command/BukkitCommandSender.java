package br.com.plutomc.core.bukkit.command;

import br.com.plutomc.core.bukkit.utils.player.PlayerHelper;
import com.google.common.base.Joiner;
import java.util.Arrays;
import java.util.UUID;
import java.util.stream.Collectors;
import br.com.plutomc.core.common.CommonConst;
import br.com.plutomc.core.common.CommonPlugin;
import br.com.plutomc.core.common.command.CommandSender;
import br.com.plutomc.core.common.language.Language;
import br.com.plutomc.core.common.member.Member;
import br.com.plutomc.core.common.member.Profile;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.entity.Player;

public class BukkitCommandSender implements CommandSender {
   private final org.bukkit.command.CommandSender sender;
   private UUID replyId;
   private boolean tellEnabled;

   @Override
   public UUID getUniqueId() {
      return this.sender instanceof Player ? ((Player)this.sender).getUniqueId() : CommonConst.EMPTY_UNIQUE_ID;
   }

   @Override
   public String getSenderName() {
      return this.sender instanceof Player ? this.sender.getName() : "CONSOLE";
   }

   @Override
   public void sendMessage(String message) {
      this.sender.sendMessage(PlayerHelper.translate(CommonPlugin.getInstance().getPluginInfo().getDefaultLanguage(), message));
   }

   @Override
   public void sendMessage(BaseComponent baseComponent) {
      if (this.sender instanceof Player) {
         ((Player)this.sender).spigot().sendMessage(baseComponent);
      } else {
         this.sender.sendMessage(PlayerHelper.translate(CommonPlugin.getInstance().getPluginInfo().getDefaultLanguage(), baseComponent.toLegacyText()));
      }
   }

   @Override
   public void sendMessage(BaseComponent... baseComponent) {
      if (this.sender instanceof Player) {
         ((Player)this.sender).spigot().sendMessage(baseComponent);
      } else {
         this.sender
            .sendMessage(
               Joiner.on("")
                  .join(
                     Arrays.asList(baseComponent)
                        .stream()
                        .map(str -> PlayerHelper.translate(CommonPlugin.getInstance().getPluginInfo().getDefaultLanguage(), str.toLegacyText()))
                        .collect(Collectors.toList())
                  )
            );
      }
   }

   public Player getPlayer() {
      return (Player)this.sender;
   }

   @Override
   public boolean isPlayer() {
      return this.sender instanceof Player;
   }

   @Override
   public boolean hasPermission(String permission) {
      return this.sender.hasPermission(permission);
   }

   @Override
   public Language getLanguage() {
      return this.isPlayer() ? ((Member)this.getSender()).getLanguage() : CommonPlugin.getInstance().getPluginInfo().getDefaultLanguage();
   }

   @Override
   public boolean isStaff() {
      if (this.sender instanceof Player) {
         Member member = CommonPlugin.getInstance().getMemberManager().getMember(((Player)this.sender).getUniqueId());
         return member.getServerGroup().isStaff();
      } else {
         return true;
      }
   }

   @Override
   public boolean isUserBlocked(Profile profile) {
      return this.isPlayer() ? ((Member)this.getSender()).isUserBlocked(profile) : false;
   }

   public BukkitCommandSender(org.bukkit.command.CommandSender sender) {
      this.sender = sender;
   }

   public org.bukkit.command.CommandSender getSender() {
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

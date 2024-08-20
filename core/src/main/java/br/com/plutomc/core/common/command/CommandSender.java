package br.com.plutomc.core.common.command;

import java.util.UUID;
import br.com.plutomc.core.common.CommonPlugin;
import br.com.plutomc.core.common.language.Language;
import br.com.plutomc.core.common.member.Profile;
import br.com.plutomc.core.common.permission.Group;
import net.md_5.bungee.api.chat.BaseComponent;

public interface CommandSender {
   UUID getUniqueId();

   String getSenderName();

   void sendMessage(String var1);

   void sendMessage(BaseComponent var1);

   void sendMessage(BaseComponent... var1);

   boolean hasPermission(String var1);

   void setTellEnabled(boolean var1);

   boolean isTellEnabled();

   void setReplyId(UUID var1);

   UUID getReplyId();

   boolean isPlayer();

   boolean isStaff();

   boolean isUserBlocked(Profile var1);

   default Group getServerGroup() {
      return this.isPlayer() ? null : CommonPlugin.getInstance().getPluginInfo().getHighGroup();
   }

   default String getName() {
      return this.getSenderName();
   }

   default boolean hasReply() {
      return this.getReplyId() != null;
   }

   Language getLanguage();
}

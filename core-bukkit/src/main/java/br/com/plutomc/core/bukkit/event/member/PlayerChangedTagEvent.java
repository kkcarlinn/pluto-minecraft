package br.com.plutomc.core.bukkit.event.member;

import br.com.plutomc.core.bukkit.event.PlayerCancellableEvent;
import br.com.plutomc.core.common.account.Account;
import br.com.plutomc.core.common.permission.Tag;
import org.bukkit.entity.Player;

public class PlayerChangedTagEvent extends PlayerCancellableEvent {
   private Account account;
   private Tag oldTag;
   private Tag newTag;
   private boolean forced;

   public PlayerChangedTagEvent(Player player, Account account, Tag oldTag, Tag newTag, boolean forced) {
      super(player);
      this.account = account;
      this.oldTag = oldTag;
      this.newTag = newTag;
      this.forced = forced;
   }

   public Account getMember() {
      return this.account;
   }

   public Tag getOldTag() {
      return this.oldTag;
   }

   public Tag getNewTag() {
      return this.newTag;
   }

   public boolean isForced() {
      return this.forced;
   }

   public void setNewTag(Tag newTag) {
      this.newTag = newTag;
   }
}

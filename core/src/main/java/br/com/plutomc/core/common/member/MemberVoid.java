package br.com.plutomc.core.common.member;

import java.util.UUID;

import br.com.plutomc.core.common.member.configuration.LoginConfiguration;
import net.md_5.bungee.api.chat.BaseComponent;

public class MemberVoid extends Member {
   public MemberVoid(UUID uniqueId, String playerName) {
      super(uniqueId, playerName, LoginConfiguration.AccountType.NONE);
   }

   public MemberVoid(UUID uniqueId, String playerName, LoginConfiguration.AccountType accountType) {
      super(uniqueId, playerName, accountType);
   }

   @Override
   public void sendMessage(String message) {
   }

   @Override
   public void sendMessage(BaseComponent str) {
   }

   @Override
   public void sendMessage(BaseComponent... fromLegacyText) {
   }

   @Override
   public void sendTitle(String title, String subTitle, int fadeIn, int stayIn, int fadeOut) {
   }

   @Override
   public void sendActionBar(String message) {
   }
}

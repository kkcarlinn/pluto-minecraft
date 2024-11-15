package br.com.plutomc.core.common.account;

import java.util.UUID;

import br.com.plutomc.core.common.account.configuration.LoginConfiguration;
import net.md_5.bungee.api.chat.BaseComponent;

public class AccountVoid extends Account {
   public AccountVoid(UUID uniqueId, String playerName) {
      super(uniqueId, playerName, LoginConfiguration.AccountType.NONE);
   }

   public AccountVoid(UUID uniqueId, String playerName, LoginConfiguration.AccountType accountType) {
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

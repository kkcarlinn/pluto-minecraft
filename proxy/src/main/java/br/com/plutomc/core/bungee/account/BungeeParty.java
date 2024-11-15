package br.com.plutomc.core.bungee.account;

import java.util.UUID;
import br.com.plutomc.core.common.account.Account;
import br.com.plutomc.core.common.account.party.Party;

public class BungeeParty extends Party {
   public BungeeParty(UUID partyId, Account account) {
      super(partyId, account);
   }
}

package br.com.plutomc.core.bukkit.account.party;

import java.util.UUID;
import br.com.plutomc.core.common.account.Account;
import br.com.plutomc.core.common.account.party.Party;

public class BukkitParty extends Party {
   public BukkitParty(UUID partyId, Account account) {
      super(partyId, account);
   }
}

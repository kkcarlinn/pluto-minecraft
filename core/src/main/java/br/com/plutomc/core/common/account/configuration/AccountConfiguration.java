package br.com.plutomc.core.common.account.configuration;

import br.com.plutomc.core.common.account.Account;

public class AccountConfiguration {
   private transient Account account;
   private boolean seeingChat = true;
   private boolean partyInvites = true;
   private boolean tellEnabled = true;
   private boolean staffChat;
   private boolean seeingStaffChat;
   private boolean seeingLogs;
   private boolean spectatorsEnabled;
   private boolean reportsEnabled = false;
   private CheatState cheatState = CheatState.ENABLED;
   private int adminModeJoin;
   private boolean adminMode;
   private boolean adminRemoveItems;

   public AccountConfiguration(Account account) {
      this.account = account;
   }

   public void setCheatState(CheatState cheatState) {
      this.cheatState = cheatState;
      this.save();
   }

   public boolean isAnticheatEnabled() {
      return this.cheatState == CheatState.ENABLED;
   }

   public boolean isAnticheatImportant() {
      return this.cheatState == CheatState.IMPORTANT || this.cheatState == CheatState.ENABLED;
   }

   public void setReportsEnabled(boolean reportsEnabled) {
      this.reportsEnabled = reportsEnabled;
      this.save();
   }

   public void setSpectatorsEnabled(boolean spectatorsEnabled) {
      this.spectatorsEnabled = spectatorsEnabled;
      this.save();
   }

   public void setSeeingLogs(boolean seeingLogs) {
      this.seeingLogs = seeingLogs;
      this.save();
   }

   public void setTellEnabled(boolean tellEnabled) {
      this.tellEnabled = tellEnabled;
      this.save();
   }

   public boolean isAdminOnJoin() {
      return this.adminModeJoin == 1 || this.adminModeJoin == 2 && this.isAdminMode();
   }

   public void setAdminModeJoin(int adminModeJoin) {
      this.adminModeJoin = adminModeJoin;
      this.save();
   }

   public void setAdminRemoveItems(boolean adminRemoveItems) {
      this.adminRemoveItems = adminRemoveItems;
      this.save();
   }

   public void setAdminMode(boolean adminMode) {
      this.adminMode = adminMode;
      this.save();
   }

   public void setSeeingStaffChat(boolean seeingStaffChat) {
      this.seeingStaffChat = seeingStaffChat;
      this.save();
   }

   public void setStaffChat(boolean staffChat) {
      this.staffChat = staffChat;
      this.save();
   }

   public void setSeeingChat(boolean seeingChat) {
      this.seeingChat = seeingChat;
      this.save();
   }

   public void setPartyInvites(boolean partyInvites) {
      this.partyInvites = partyInvites;
      this.save();
   }

   public void loadConfiguration(Account account) {
      this.account = account;
   }

   public void save() {
      if (this.account != null) {
         this.account.save("accountConfiguration");
      }
   }

   public Account getAccount() {
      return this.account;
   }

   public boolean isSeeingChat() {
      return this.seeingChat;
   }

   public boolean isPartyInvites() {
      return this.partyInvites;
   }

   public boolean isTellEnabled() {
      return this.tellEnabled;
   }

   public boolean isStaffChat() {
      return this.staffChat;
   }

   public boolean isSeeingStaffChat() {
      return this.seeingStaffChat;
   }

   public boolean isSeeingLogs() {
      return this.seeingLogs;
   }

   public boolean isSpectatorsEnabled() {
      return this.spectatorsEnabled;
   }

   public boolean isReportsEnabled() {
      return this.reportsEnabled;
   }

   public CheatState getCheatState() {
      return this.cheatState;
   }

   public int getAdminModeJoin() {
      return this.adminModeJoin;
   }

   public boolean isAdminMode() {
      return this.adminMode;
   }

   public boolean isAdminRemoveItems() {
      return this.adminRemoveItems;
   }

   public static enum CheatState {
      ENABLED,
      DISABLED,
      IMPORTANT;
   }
}

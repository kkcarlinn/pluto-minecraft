package br.com.plutomc.core.common.account.configuration;

import java.nio.charset.Charset;
import java.util.Base64;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import br.com.plutomc.core.common.account.Account;

public class LoginConfiguration {
   private transient Account account;
   private AccountType accountType;
   private String password;
   private boolean logged;
   private boolean captcha;
   private Map<String, Long> sessionMap;
   private Map<String, Long> timeoutMap;
   private Map<String, Integer> attempsMap;

   public LoginConfiguration(Account account, AccountType accountType) {
      this.account = account;
      this.accountType = accountType;
      this.password = "";
      this.logged = false;
      this.sessionMap = new HashMap<>();
      this.timeoutMap = new HashMap<>();
      this.attempsMap = new HashMap<>();
   }

   public LoginConfiguration(Account account) {
      this(account, AccountType.PREMIUM);
   }

   public void setCaptcha(boolean captcha) {
      this.captcha = captcha;
      this.save();
   }

   public boolean isLogged() {
      return this.accountType == AccountType.PREMIUM ? true : this.logged;
   }

   public boolean reloadSession() {
      if (this.account.getIpAddress() != null && this.sessionMap.containsKey(this.account.getIpAddress())) {
         if (this.sessionMap.get(this.account.getIpAddress()) > System.currentTimeMillis()) {
            this.logged = true;
            this.account.sendMessage(this.account.getLanguage().t("login.message.session-restored"));
            this.account
               .sendTitle(
                  this.account.getLanguage().t("login.title.session-restored"), this.account.getLanguage().t("login.subtitle.session-restored"), 10, 40, 10
               );
            this.save();
            return true;
         }

         this.logged = false;
         this.account.sendMessage(this.account.getLanguage().t("login.message.session-expired"));
         this.sessionMap.remove(this.account.getIpAddress());
         this.save();
      }

      return false;
   }

   public void startSession() {
      if (this.account.getIpAddress() != null) {
         this.sessionMap.put(this.account.getIpAddress(), System.currentTimeMillis() + 604800000L);
         this.account.sendMessage(this.account.getLanguage().t("login.message.session-created"));
         this.save();
      }
   }

   public void stopSession() {
      if (this.account.getIpAddress() != null && this.sessionMap.containsKey(this.account.getIpAddress())) {
         this.sessionMap.remove(this.account.getIpAddress());
         this.account.sendMessage(this.account.getLanguage().t("login.message.session-stopped"));
         this.save();
      }
   }

   public void logIn() {
      if (this.account.getIpAddress() != null) {
         this.timeoutMap.remove(this.account.getIpAddress());
         this.attempsMap.remove(this.account.getIpAddress());
      }

      this.logged = true;
      this.save();
   }

   public void logOut() {
      this.logged = false;
      this.save();
   }

   public void register(String password) {
      this.password = encode(password);
      this.logIn();
      this.save();
   }

   public boolean isTimeouted() {
      return this.account.getIpAddress() != null
         ? this.timeoutMap.containsKey(this.account.getIpAddress()) && this.timeoutMap.get(this.account.getIpAddress()) > System.currentTimeMillis()
         : false;
   }

   public long getTimeoutTime() {
      return this.account.getIpAddress() != null ? this.timeoutMap.get(this.account.getIpAddress()) : 0L;
   }

   public Integer attemp() {
      if (this.account.getIpAddress() != null) {
         Integer integer = this.attempsMap.computeIfAbsent(this.account.getIpAddress(), v -> 0);
         Integer var2;
         this.attempsMap.put(this.account.getIpAddress(), var2 = integer + 1);
         if (var2 >= 5) {
            this.attempsMap.remove(this.account.getIpAddress());
            this.timeoutMap.put(this.account.getIpAddress(), System.currentTimeMillis() + 900000L);
         }

         this.save();
         return var2;
      } else {
         return 0;
      }
   }

   public boolean isPassword(String string) {
      return this.isRegistered() ? decode(this.password).equals(string) : false;
   }

   public boolean isRegistered() {
      return this.password != null && !this.password.isEmpty();
   }

   public void loadConfiguration(Account account) {
      this.account = account;
      Iterator<Entry<String, Long>> iterator = this.timeoutMap.entrySet().iterator();
      boolean save = false;

      while(iterator.hasNext()) {
         Entry<String, Long> next = iterator.next();
         if (next.getValue() < System.currentTimeMillis()) {
            iterator.remove();
            save = true;
         }
      }

      if (save) {
         this.save();
      }
   }

   public void setAccountType(AccountType accountType) {
      if (this.accountType == AccountType.NONE) {
         this.accountType = accountType;
      }
   }

   public void save() {
      if (this.account != null) {
         this.account.save("loginConfiguration");
      }
   }

   public static String encode(String paramString) {
      byte[] encode = Base64.getEncoder().encode(paramString.getBytes());
      return new String(encode, Charset.forName("UTF-8"));
   }

   public static String decode(String paramString) {
      byte[] decode = Base64.getDecoder().decode(paramString.getBytes());
      return new String(decode, Charset.forName("UTF-8"));
   }

   public Account getMember() {
      return this.account;
   }

   public AccountType getAccountType() {
      return this.accountType;
   }

   public String getPassword() {
      return this.password;
   }

   public boolean isCaptcha() {
      return this.captcha;
   }

   public Map<String, Long> getSessionMap() {
      return this.sessionMap;
   }

   public Map<String, Long> getTimeoutMap() {
      return this.timeoutMap;
   }

   public Map<String, Integer> getAttempsMap() {
      return this.attempsMap;
   }

   public static enum AccountType {
      PREMIUM,
      CRACKED,
      NONE;
   }
}

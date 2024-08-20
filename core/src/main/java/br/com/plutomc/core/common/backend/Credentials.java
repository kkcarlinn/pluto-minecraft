package br.com.plutomc.core.common.backend;

public class Credentials {
   private String hostName;
   private String userName;
   private String passWord;
   private String database;
   private int port;

   public String getHostName() {
      return this.hostName;
   }

   public String getUserName() {
      return this.userName;
   }

   public String getPassWord() {
      return this.passWord;
   }

   public String getDatabase() {
      return this.database;
   }

   public int getPort() {
      return this.port;
   }

   public Credentials(String hostName, String userName, String passWord, String database, int port) {
      this.hostName = hostName;
      this.userName = userName;
      this.passWord = passWord;
      this.database = database;
      this.port = port;
   }
}

package br.com.plutomc.core.common.backend.mongodb;

import br.com.plutomc.core.common.backend.Credentials;
import br.com.plutomc.core.common.backend.Database;
import com.mongodb.MongoClientURI;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class MongoConnection implements Database {
   private static final String PATTERN = "([01]?[0-9]{1,2}|2[0-4][0-9]|25[0-5])";
   private static final Pattern IP_PATTERN = Pattern.compile(
      "([01]?[0-9]{1,2}|2[0-4][0-9]|25[0-5])\\.([01]?[0-9]{1,2}|2[0-4][0-9]|25[0-5])\\.([01]?[0-9]{1,2}|2[0-4][0-9]|25[0-5])\\.([01]?[0-9]{1,2}|2[0-4][0-9]|25[0-5])"
   );
   private MongoClient client;
   private MongoDatabase db;
   private String url;
   private String dataBase;
   private int port;

   public MongoConnection(String url) {
      this.url = url;
   }

   public MongoConnection(String hostName, String userName, String passWord, String dataBase, int port) {
      this(
         IP_PATTERN.matcher(hostName).matches()
            ? "mongodb://" + (userName.isEmpty() ? "" : userName + ":" + passWord + "@") + hostName + "/" + dataBase + "?retryWrites=true&w=majority"
            : "mongodb+srv://" + (userName.isEmpty() ? "" : userName + ":" + passWord + "@") + hostName + "/" + dataBase + "?retryWrites=true&w=majority"
      );
   }

   public MongoConnection(Credentials credentials) {
      this(credentials.getHostName(), credentials.getUserName(), credentials.getPassWord(), credentials.getDatabase(), credentials.getPort());
   }

   public MongoConnection(String hostName, String userName, String passWord, String dataBase) {
      this(hostName, userName, passWord, dataBase, 27017);
   }

   @Override
   public void connect() {
      MongoClientURI uri = new MongoClientURI(this.getUrl());
      this.dataBase = uri.getDatabase();
      this.client = new MongoClient(new MongoClientURI(this.getUrl()));
      Logger.getLogger("uri").setLevel(Level.SEVERE);
      this.db = this.client.getDatabase(this.dataBase);
   }

   public MongoDatabase getDefaultDatabase() {
      return this.client.getDatabase(this.getDataBase());
   }

   public MongoDatabase getDatabase(String database) {
      return this.client.getDatabase(database);
   }

   @Override
   public boolean isConnected() {
      return this.client != null;
   }

   @Override
   public void close() {
      this.client.close();
   }

   public String getUrl() {
      return this.url;
   }

   public String getDataBase() {
      return this.dataBase;
   }

   public int getPort() {
      return this.port;
   }

   public MongoClient getClient() {
      return this.client;
   }

   public MongoDatabase getDb() {
      return this.db;
   }
}

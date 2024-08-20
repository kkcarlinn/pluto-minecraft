package br.com.plutomc.core.common.backend.mongodb;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import br.com.plutomc.core.common.backend.Query;
import br.com.plutomc.core.common.utils.json.JsonUtils;
import org.bson.Document;
import org.bson.json.JsonWriterSettings;

public class MongoQuery implements Query<JsonElement> {
   private static final JsonWriterSettings SETTINGS = JsonWriterSettings.builder()
      .int64Converter((value, writer) -> writer.writeNumber(value.toString()))
      .build();
   private MongoDatabase database;
   private MongoCollection<Document> collection;

   public MongoQuery(MongoConnection mongoConnection, String collectionName) {
      this.database = mongoConnection.getDb();
      this.collection = this.database.getCollection(collectionName);
   }

   public MongoQuery(MongoConnection mongoConnection, String databaseName, String collectionName) {
      this.database = mongoConnection.getDatabase(databaseName);
      this.collection = this.database.getCollection(collectionName);
   }

   @Override
   public Collection<JsonElement> find() {
      MongoCursor<Document> mongoCursor = this.collection.find().iterator();
      List<JsonElement> documentList = new ArrayList<>();

      while(mongoCursor.hasNext()) {
         documentList.add(JsonParser.parseString(mongoCursor.next().toJson(SETTINGS)));
      }

      return documentList;
   }

   @Override
   public Collection<JsonElement> find(String collection) {
      MongoCursor<Document> mongoCursor = this.database.getCollection(collection).find().iterator();
      List<JsonElement> documentList = new ArrayList<>();

      while(mongoCursor.hasNext()) {
         documentList.add(JsonParser.parseString(mongoCursor.next().toJson(SETTINGS)));
      }

      return documentList;
   }

   @Override
   public <GenericType> Collection<JsonElement> find(String key, GenericType value) {
      MongoCursor<Document> mongoCursor = this.collection.find(Filters.eq(key, value)).iterator();
      List<JsonElement> documentList = new ArrayList<>();

      while(mongoCursor.hasNext()) {
         documentList.add(JsonParser.parseString(mongoCursor.next().toJson(SETTINGS)));
      }

      return documentList;
   }

   @Override
   public <GenericType> Collection<JsonElement> find(String collection, String key, GenericType value) {
      MongoCursor<Document> mongoCursor = this.database.getCollection(collection).find(Filters.eq(key, value)).iterator();
      List<JsonElement> documentList = new ArrayList<>();

      while(mongoCursor.hasNext()) {
         documentList.add(JsonParser.parseString(mongoCursor.next().toJson(SETTINGS)));
      }

      return documentList;
   }

   public <GenericType> JsonElement findOne(String key, GenericType value) {
      JsonElement json = null;
      Document document = this.collection.find(Filters.eq(key, value)).first();
      if (document != null) {
         json = JsonParser.parseString(document.toJson(SETTINGS));
      }

      return json;
   }

   public <GenericType> JsonElement findOne(String collection, String key, GenericType value) {
      JsonElement json = null;
      Document document = this.database.getCollection(collection).find(Filters.eq(key, value)).first();
      if (document != null) {
         json = JsonParser.parseString(document.toJson(SETTINGS));
      }

      return json;
   }

   @Override
   public void create(String[] jsons) {
      for(String json : jsons) {
         this.collection.insertOne(Document.parse(json));
      }
   }

   @Override
   public void create(String collection, String[] jsons) {
      for(String json : jsons) {
         this.database.getCollection(collection).insertOne(Document.parse(json));
      }
   }

   @Override
   public <GenericType> void deleteOne(String key, GenericType value) {
      this.collection.deleteOne(Filters.eq(key, value));
   }

   @Override
   public <GenericType> void deleteOne(String collection, String key, GenericType value) {
      this.database.getCollection(collection).deleteOne(Filters.eq(key, value));
   }

   public <GenericType> void updateOne(String key, GenericType value, JsonElement t) {
      JsonObject jsonObject = (JsonObject)t;
      if (jsonObject.has("fieldName") && jsonObject.has("value")) {
         Object object = JsonUtils.elementToBson(jsonObject.get("value"));
         if (object != null && !jsonObject.get("value").isJsonNull()) {
            this.collection.updateOne(Filters.eq(key, value), new Document("$set", new Document(jsonObject.get("fieldName").getAsString(), object)));
         } else {
            this.collection.updateOne(Filters.eq(key, value), new Document("$unset", new Document(jsonObject.get("fieldName").getAsString(), "")));
         }
      }
   }

   public <GenericType> void updateOne(String collection, String key, GenericType value, JsonElement t) {
      JsonObject jsonObject = (JsonObject)t;
      if (jsonObject.has("fieldName") && jsonObject.has("value")) {
         Object object = JsonUtils.elementToBson(jsonObject.get("value"));
         this.database
            .getCollection(collection)
            .updateOne(Filters.eq(key, value), new Document("$set", new Document(jsonObject.get("fieldName").getAsString(), object)));
      } else {
         this.database.getCollection(collection).updateOne(Filters.eq(key, value), Document.parse(t.toString()));
      }
   }

   @Override
   public <GenericType> Collection<JsonElement> ranking(String key, GenericType value, int limit) {
      MongoCursor<Document> mongoCursor = this.collection.find().sort(Filters.eq(key, value)).limit(limit).iterator();
      List<JsonElement> documentList = new ArrayList<>();

      while(mongoCursor.hasNext()) {
         documentList.add(JsonParser.parseString(mongoCursor.next().toJson(SETTINGS)));
      }

      return documentList;
   }

   public static MongoQuery createDefault(MongoConnection mongoConnection, String databaseName, String collectionName) {
      return new MongoQuery(mongoConnection, databaseName, collectionName);
   }

   public static MongoQuery createDefault(MongoConnection mongoConnection, String collectionName) {
      return new MongoQuery(mongoConnection, mongoConnection.getDataBase(), collectionName);
   }

   public MongoDatabase getDatabase() {
      return this.database;
   }

   public MongoCollection<Document> getCollection() {
      return this.collection;
   }
}

package br.com.plutomc.core.common.utils.skin;

import java.util.UUID;

public class Skin {
   private String playerName;
   private UUID uniqueId;
   private String value;
   private String signature;
   private long createdAt;

   public Skin(String playerName, UUID uniqueId, String value, String signature) {
      this(playerName, uniqueId, value, signature, System.currentTimeMillis());
   }

   public Skin(String playerName, String value, String signature) {
      this(playerName, null, value, signature, System.currentTimeMillis());
   }

   public Skin(String playerName, UUID uniqueId, String value) {
      this(playerName, uniqueId, value, "", System.currentTimeMillis());
   }

   public String getPlayerName() {
      return this.playerName;
   }

   public UUID getUniqueId() {
      return this.uniqueId;
   }

   public String getValue() {
      return this.value;
   }

   public String getSignature() {
      return this.signature;
   }

   public long getCreatedAt() {
      return this.createdAt;
   }

   public Skin(String playerName, UUID uniqueId, String value, String signature, long createdAt) {
      this.playerName = playerName;
      this.uniqueId = uniqueId;
      this.value = value;
      this.signature = signature;
      this.createdAt = createdAt;
   }
}

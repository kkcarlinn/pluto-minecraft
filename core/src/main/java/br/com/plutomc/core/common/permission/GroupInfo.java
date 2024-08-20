package br.com.plutomc.core.common.permission;

import java.util.UUID;

import br.com.plutomc.core.common.command.CommandSender;

public class GroupInfo {
   private String authorName;
   private UUID authorId;
   private long givenDate;
   private long expireTime;

   public GroupInfo() {
      this("CONSOLE", UUID.randomUUID(), System.currentTimeMillis(), -1L);
   }

   public GroupInfo(String authorName, UUID authorId) {
      this(authorName, authorId, System.currentTimeMillis(), -1L);
   }

   public GroupInfo(CommandSender sender) {
      this(sender.getName(), sender.getUniqueId(), System.currentTimeMillis(), -1L);
   }

   public GroupInfo(CommandSender sender, long expireTime) {
      this(sender.getName(), sender.getUniqueId(), System.currentTimeMillis(), expireTime);
   }

   public GroupInfo(String authorName, long expireTime) {
      this(authorName, UUID.randomUUID(), System.currentTimeMillis(), expireTime);
   }

   public GroupInfo(String authorName, UUID authorId, long expireTime) {
      this(authorName, authorId, System.currentTimeMillis(), expireTime);
   }

   public GroupInfo(String authorName, UUID authorId, long givenDate, long expireTime) {
      this.authorName = authorName;
      this.authorId = authorId;
      this.givenDate = givenDate;
      this.expireTime = expireTime;
   }

   public boolean hasExpired() {
      return System.currentTimeMillis() > this.expireTime;
   }

   public boolean isPermanent() {
      return this.expireTime <= 0L;
   }

   public String getAuthorName() {
      return this.authorName;
   }

   public UUID getAuthorId() {
      return this.authorId;
   }

   public long getGivenDate() {
      return this.givenDate;
   }

   public long getExpireTime() {
      return this.expireTime;
   }
}

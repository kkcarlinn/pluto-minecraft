package br.com.plutomc.core.common.permission;

import java.util.List;

import br.com.plutomc.core.common.utils.string.StringFormat;
import net.md_5.bungee.api.ChatColor;

public class Tag {
   private int tagId;
   private String tagName;
   private String tagPrefix;
   private String score;
   private List<String> aliases;
   private boolean exclusive;
   private boolean defaultTag;

   public String getRealPrefix() {
      return this.tagPrefix + (ChatColor.stripColor(this.tagPrefix).trim().length() > 0 ? " " : "");
   }

   public String getStrippedColor() {
      return this.tagPrefix.length() > 2 ? this.tagPrefix : this.tagPrefix + StringFormat.formatString(this.tagName) + "";
   }

   public String getColor() {
      return this.tagPrefix.length() > 2 ? this.tagPrefix.substring(0, 2) : this.tagPrefix;
   }

   public Tag(int tagId, String tagName, String tagPrefix, String score, List<String> aliases, boolean exclusive, boolean defaultTag) {
      this.tagId = tagId;
      this.tagName = tagName;
      this.tagPrefix = tagPrefix;
      this.score = score;
      this.aliases = aliases;
      this.exclusive = exclusive;
      this.defaultTag = defaultTag;
   }

   public String getScore() {
      return score;
   }

   public int getTagId() {
      return this.tagId;
   }

   public String getTagName() {
      return this.tagName;
   }

   public String getTagPrefix() {
      return this.tagPrefix;
   }

   public List<String> getAliases() {
      return this.aliases;
   }

   public boolean isExclusive() {
      return this.exclusive;
   }

   public boolean isDefaultTag() {
      return this.defaultTag;
   }

   public void setTagId(int tagId) {
      this.tagId = tagId;
   }

   public void setTagName(String tagName) {
      this.tagName = tagName;
   }

   public void setTagPrefix(String tagPrefix) {
      this.tagPrefix = tagPrefix;
   }

   public void setAliases(List<String> aliases) {
      this.aliases = aliases;
   }

   public void setExclusive(boolean exclusive) {
      this.exclusive = exclusive;
   }

   public void setDefaultTag(boolean defaultTag) {
      this.defaultTag = defaultTag;
   }

   public void setTagScore(String score) {
      this.score = score;
   }
}

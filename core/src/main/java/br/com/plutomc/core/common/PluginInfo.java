package br.com.plutomc.core.common;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import br.com.plutomc.core.common.backend.Credentials;
import br.com.plutomc.core.common.command.CommandSender;
import br.com.plutomc.core.common.language.Language;
import br.com.plutomc.core.common.medal.Medal;
import br.com.plutomc.core.common.permission.Group;
import br.com.plutomc.core.common.permission.Tag;
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.ChatColor;

public class PluginInfo {
   @Getter
   private String website;
   @Getter
   private String discord;
   @Getter
   private String store;
   @Getter
   private String ip;
   @Setter
   @Getter
   private Language defaultLanguage;
   @Getter
   private boolean piratePlayersEnabled;
   @Getter
   private boolean redisDebugEnabled;
   @Getter
   private boolean debug;
   @Getter
   private Credentials mongoCredentials;
   @Getter
   private Credentials redisCredentials;
   @Getter
   private LinkedHashMap<String, Group> groupMap;
   @Getter
   private LinkedHashMap<String, Tag> tagMap;
   @Getter
   private Map<Language, Map<String, String>> languageMap;
   @Getter
   private Map<String, Medal> medalMap;
   private transient Tag defaultTag;
   private transient Group defaultGroup;

   public String getWebsiteUrl() {
      return "https://" + this.getWebsite() + "/";
   }

   public void loadMedal(Medal medal) {
      this.medalMap.put(medal.getMedalName().toLowerCase(), medal);
      CommonPlugin.getInstance().saveConfig("medalMap");
   }

   public void loadGroup(Group group) {
      this.groupMap.put(group.getGroupName().toLowerCase(), group);
      CommonPlugin.getInstance().saveConfig("groupMap");
   }

   public void loadTag(Tag tag) {
      this.tagMap.put(tag.getTagName().toLowerCase(), tag);
      CommonPlugin.getInstance().saveConfig("tagMap");
   }

   public Tag getTagById(int id) {
      return this.tagMap.values().stream().filter(tag -> tag.getTagId() == id).findFirst().orElse(null);
   }

   public Group filterGroup(Predicate<? super Group> filter) {
      return this.groupMap.values().stream().filter(filter).findFirst().orElse(null);
   }

   public Group filterGroup(Predicate<? super Group> filter, Group orElse) {
      return this.groupMap.values().stream().filter(filter).findFirst().orElse(orElse);
   }

   public Group getFirstLowerGroup(int id) {
      return this.groupMap.values().stream().filter(group -> group.getId() < id).findFirst().orElse(null);
   }

   public Group getGroupById(int id) {
      return this.groupMap.values().stream().filter(group -> group.getId() == id).findFirst().orElse(null);
   }

   public Group getGroupByName(String string) {
      Group group = this.groupMap.get(string.toLowerCase());
      if (group == null) {
         group = this.groupMap.values().stream().filter(g -> g.getGroupName().equalsIgnoreCase(string)).findFirst().orElse(null);
      }

      return group;
   }

   public String translate(Language language, String key, String... replaces) {
      Map<String, String> map = this.languageMap.computeIfAbsent(language, v -> new HashMap());
      String translate = "[NOT FOUND: " + key + "]";
      if (map.containsKey(key)) {
         translate = this.languageMap.get(language).get(key);
         if (replaces.length > 0 && replaces.length % 2 == 0) {
            for(int i = 0; i < replaces.length; i += 2) {
               translate = translate.replace(replaces[i], replaces[i + 1]);
            }
         }
      } else {
         if (this.defaultLanguage == language) {
            map.put(key, translate);
         } else {
            translate = this.translate(this.defaultLanguage, key, replaces) + " §0§l*";
            map.put(key, translate);
         }

         CommonPlugin.getInstance().saveConfig("languageMap");
      }

      return ChatColor.translateAlternateColorCodes('&', translate);
   }

   public String findAndTranslate(Language lang, String string) {
      String replace;
      String id;
      if (string != null && !string.isEmpty()) {
         for(Matcher matcher = CommonConst.TRANSLATE_PATTERN.matcher(string); matcher.find(); string = string.replace(replace, this.translate(lang, id))) {
            replace = matcher.group();
            id = matcher.group(2).toLowerCase();
         }
      }

      return string;
   }

   public String translate(String key) {
      return this.translate(this.getDefaultLanguage(), key);
   }

   public void addTranslate(Language language, String translateKey, String translate) {
      this.languageMap.computeIfAbsent(language, v -> new HashMap()).put(translateKey, translate);
      CommonPlugin.getInstance().saveConfig("languageMap");
   }

   public static String t(CommandSender sender, String translate) {
      return sender.getLanguage().t(translate);
   }

   public static String t(CommandSender sender, String translate, String... replaces) {
      return sender.getLanguage().t(translate, replaces);
   }

   public void sort() {
      for(Language language : this.languageMap.keySet()) {
         Map<String, String> map = this.languageMap.get(language);
         List<Entry<String, String>> list = map.entrySet().stream().sorted((o1, o2) -> o1.getKey().compareTo(o2.getKey())).collect(Collectors.toList());
         map.clear();

         for(Entry<String, String> entry : list) {
            map.put(entry.getKey(), entry.getValue());
         }
      }

      this.sortGroup();
      this.sortTag();
   }

   public Group getHighGroup() {
      return this.getSortGroup().findFirst().orElse(null);
   }

   public Stream<Group> getSortGroup() {
      return this.groupMap.values().stream().sorted((o1, o2) -> o2.getId() - o1.getId());
   }

   public Collection<Tag> getTags() {
      return this.tagMap.values();
   }

   public Tag getTagByGroup(Group serverGroup) {
      Tag tag = this.tagMap.get(serverGroup.getGroupName().toLowerCase());
      return tag == null ? this.getDefaultTag() : tag;
   }

   public Tag getTagByName(String string) {
      return this.tagMap.containsKey(string.toLowerCase())
         ? this.tagMap.get(string.toLowerCase())
         : this.tagMap
            .values()
            .stream()
            .filter(t -> t.getTagName().equalsIgnoreCase(string) || t.getAliases().contains(string.toLowerCase()))
            .findFirst()
            .orElse(null);
   }

   public Tag getDefaultTag() {
      return this.defaultTag == null
         ? (
            this.defaultTag = this.tagMap
               .values()
               .stream()
               .filter(tag -> tag.isDefaultTag())
               .findFirst()
               .orElse(new Tag(20, "membro", "§7", "§7Membro", new ArrayList<>(), false, true))
         )
         : this.defaultTag;
   }

   public Group getDefaultGroup() {
      return this.defaultGroup == null
         ? (this.defaultGroup = this.groupMap.values().stream().filter(tag -> tag.isDefaultGroup()).findFirst().orElse(null))
         : this.defaultGroup;
   }

   public Medal getMedalByName(String medalName) {
      return this.medalMap.containsKey(medalName.toLowerCase())
         ? this.medalMap.get(medalName.toLowerCase())
         : this.medalMap
            .values()
            .stream()
            .filter(medal -> medal.getMedalName().equalsIgnoreCase(medalName) || medal.getAliases().contains(medalName.toLowerCase()))
            .findFirst()
            .orElse(null);
   }

   public void sortGroup() {
      List<Entry<String, Group>> list = this.groupMap
         .entrySet()
         .stream()
         .sorted((o1, o2) -> o2.getValue().getId() - o1.getValue().getId())
         .collect(Collectors.toList());
      this.groupMap.clear();

      for(Entry<String, Group> entry : list) {
         this.groupMap.put(entry.getKey(), entry.getValue());
      }
   }

   public void sortTag() {
      List<Entry<String, Tag>> list = this.tagMap
         .entrySet()
         .stream()
         .sorted((o1, o2) -> o1.getValue().getTagId() - o2.getValue().getTagId())
         .collect(Collectors.toList());
      this.tagMap.clear();

      for(Entry<String, Tag> entry : list) {
         this.tagMap.put(entry.getKey(), entry.getValue());
      }
   }

}

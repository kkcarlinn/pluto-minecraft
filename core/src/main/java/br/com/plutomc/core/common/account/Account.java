package br.com.plutomc.core.common.account;

import br.com.plutomc.core.common.CommonPlugin;
import br.com.plutomc.core.common.command.CommandSender;
import br.com.plutomc.core.common.language.Language;
import br.com.plutomc.core.common.medal.Medal;
import br.com.plutomc.core.common.account.configuration.LoginConfiguration;
import br.com.plutomc.core.common.account.configuration.AccountConfiguration;
import br.com.plutomc.core.common.account.configuration.PunishConfiguration;
import br.com.plutomc.core.common.account.party.Party;
import br.com.plutomc.core.common.permission.Group;
import br.com.plutomc.core.common.permission.GroupInfo;
import br.com.plutomc.core.common.permission.Tag;
import br.com.plutomc.core.common.server.ServerType;
import br.com.plutomc.core.common.utils.DateUtils;
import br.com.plutomc.core.common.utils.skin.Skin;
import com.google.common.collect.ImmutableSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import lombok.NonNull;

public abstract class Account implements CommandSender {
   private final UUID uniqueId;
   private String playerName;
   private Map<String, GroupInfo> groups;
   private transient String serverGroup;
   private String tagName;
   private String medalName;
   private List<String> permissions;
   private transient List<String> cachedPermissions;
   private List<String> medals;
   private Map<String, Long> cooldownMap;
   private List<Profile> blockedList;
   private List<Profile> friendList;
   private LoginConfiguration loginConfiguration;
   private AccountConfiguration accountConfiguration;
   private PunishConfiguration punishConfiguration;
   private Skin skin;
   private boolean customSkin;
   private String fakeName;
   private String twitterUrl;
   private String youtubeUrl;
   private String twitchUrl;
   private String discordId;
   private UUID partyId;
   private String ipAddress;
   private String lastIpAddress;
   private long firstLogin;
   private long lastLogin;
   private long joinTime;
   private long onlineTime;
   private boolean online;
   private String actualServerId;
   private ServerType actualServerType;
   private String lastServerId;
   private ServerType lastServerType;
   private Language language;
   private transient UUID replyId;

   public Account(UUID uniqueId, String playerName, LoginConfiguration.AccountType accountType) {
      this.uniqueId = uniqueId;
      this.playerName = playerName;
      this.groups = new HashMap<>();
      this.cooldownMap = new HashMap<>();
      this.blockedList = new ArrayList<>();
      this.friendList = new ArrayList<>();
      this.loginConfiguration = new LoginConfiguration(this, accountType);
      this.accountConfiguration = new AccountConfiguration(this);
      this.punishConfiguration = new PunishConfiguration(this);
      this.permissions = new ArrayList<>();
      this.cachedPermissions = new ArrayList<>();
      this.medals = new ArrayList<>();
      this.firstLogin = System.currentTimeMillis();
      this.joinTime = System.currentTimeMillis();
      this.lastLogin = System.currentTimeMillis();
      this.onlineTime = -1L;
      this.online = true;
      this.language = CommonPlugin.getInstance().getPluginInfo().getDefaultLanguage();
      this.handleDefaultGroup();
   }

   public boolean block(Profile profile) {
      if (this.blockedList.contains(profile)) {
         return false;
      } else {
         this.blockedList.add(profile);
         this.save("blockedList");
         return true;
      }
   }

   public boolean unblock(Profile profile) {
      if (!this.blockedList.contains(profile)) {
         return false;
      } else {
         this.blockedList.remove(profile);
         this.save("blockedList");
         return true;
      }
   }

   @Override
   public boolean isUserBlocked(Profile profile) {
      if (this.blockedList == null) {
         this.blockedList = new ArrayList<>();
      }

      return profile == null ? false : this.blockedList.contains(profile);
   }

   public boolean hasCooldown(String cooldownId) {
      if (this.cooldownMap == null) {
         this.cooldownMap = new HashMap<>();
      }

      if (this.cooldownMap.containsKey(cooldownId.toLowerCase())) {
         if (this.cooldownMap.get(cooldownId.toLowerCase()) > System.currentTimeMillis()) {
            return true;
         }

         this.cooldownMap.remove(cooldownId.toLowerCase());
         this.save("cooldownMap");
      }

      return false;
   }

   public String getCooldownFormatted(String cooldownId) {
      return DateUtils.getTime(this.getLanguage(), this.cooldownMap.get(cooldownId.toLowerCase()));
   }

   public void putCooldown(String cooldownId, long cooldown) {
      if (this.cooldownMap == null) {
         this.cooldownMap = new HashMap<>();
      }

      this.cooldownMap.put(cooldownId.toLowerCase(), cooldown);
      this.save("cooldownMap");
   }

   public void putCooldown(String cooldownId, double seconds) {
      this.putCooldown(cooldownId, System.currentTimeMillis() + (long)(1000.0 * seconds));
   }

   public boolean isUsingFake() {
      return this.fakeName != null && !this.fakeName.equals(this.playerName);
   }

   public void setFakeName(String fakeName) {
      this.fakeName = fakeName;
      this.save("fakeName");
   }

   public boolean hasCustomSkin() {
      return this.skin != null && !this.skin.getPlayerName().equals(this.playerName);
   }

   public void setSkin(Skin skin) {
      this.setSkin(skin, false);
   }

   public void setSkin(Skin skin, boolean customSkin) {
      this.skin = skin;
      this.customSkin = customSkin;
      this.save("skin", "customSkin");
   }

   public boolean hasMedal(Medal medal) {
      return this.medals.contains(medal.getMedalName().toLowerCase());
   }

   public boolean removeMedal(Medal medal) {
      if (this.medals.contains(medal.getMedalName().toLowerCase())) {
         this.medals.remove(medal.getMedalName().toLowerCase());
         this.save("medals");
         return true;
      } else {
         return false;
      }
   }

   public boolean addMedal(Medal medal) {
      if (!this.medals.contains(medal.getMedalName().toLowerCase())) {
         this.medals.add(medal.getMedalName().toLowerCase());
         this.save("medals");
         return true;
      } else {
         return false;
      }
   }

   public void setMedal(Medal medal) {
      if (medal == null) {
         this.medalName = null;
      } else {
         this.medalName = medal.getMedalName();
      }

      this.save("medalName");
   }

   public Medal getMedal() {
      return this.medalName == null ? null : CommonPlugin.getInstance().getPluginInfo().getMedalByName(this.medalName);
   }

   public Tag getTag() {
      if (this.tagName == null) {
         this.tagName = CommonPlugin.getInstance().getPluginInfo().getDefaultTag().getTagName();
         this.save("tag");
      }

      return CommonPlugin.getInstance().getPluginInfo().getTagByName(this.tagName);
   }

   public void setTwitterUrl(String twitterUrl) {
      this.twitterUrl = twitterUrl;
      this.save("twitterUrl");
   }

   public void setYoutubeUrl(String youtubeUrl) {
      this.youtubeUrl = youtubeUrl;
      this.save("youtubeUrl");
   }

   public void setTwitchUrl(String twitchUrl) {
      this.twitchUrl = twitchUrl;
      this.save("twitchUrl");
   }

   public void setDiscordId(String discordId) {
      this.discordId = discordId;
      this.save("discordId");
   }

   public void setPartyId(UUID partyId) {
      this.partyId = partyId;
      this.save("partyId");
   }

   public void setParty(Party party) {
      if (party == null) {
         this.partyId = null;
      } else {
         this.partyId = party.getPartyId();
      }

      this.save("partyId");
   }

   public Party getParty() {
      return this.partyId == null ? null : CommonPlugin.getInstance().getPartyManager().getPartyById(this.partyId);
   }

   public void logIn(@NonNull String playerName, @NonNull String ipAddress) {
      if (playerName == null) {
         throw new NullPointerException("playerName is marked non-null but is null");
      } else if (ipAddress == null) {
         throw new NullPointerException("ipAddress is marked non-null but is null");
      } else {
         this.playerName = playerName;
         this.joinTime = System.currentTimeMillis();
         this.online = true;
         this.ipAddress = ipAddress;
         this.lastIpAddress = this.ipAddress;
         this.loadConfiguration();
         this.loginConfiguration.logOut();
         this.save("playerName", "joinTime", "online", "ipAddress", "lastIpAddress");
      }
   }

   public void connect(String serverId, ServerType serverType) {
      this.lastServerId = this.actualServerId;
      this.lastServerType = this.actualServerType;
      this.actualServerId = serverId;
      this.actualServerType = serverType;
      this.save("lastServerId", "lastServerType", "actualServerId", "actualServerType");
      this.loadConfiguration();
   }

   public void connect() {
      this.connect(CommonPlugin.getInstance().getServerId(), CommonPlugin.getInstance().getServerType());
   }

   public void setOnline(boolean online) {
      this.online = online;
      this.save("online");
   }

   public void logOut() {
      this.lastLogin = System.currentTimeMillis();
      this.onlineTime += this.getSessionTime();
      this.online = false;
      this.save("lastLogin", "onlineTime", "online");
   }

   public long getOnlineTime() {
      return this.onlineTime + this.getSessionTime();
   }

   public long getSessionTime() {
      return this.online ? System.currentTimeMillis() - this.joinTime : 0L;
   }

   public void updateGroup() {
      this.serverGroup = null;
   }

   public Tag getDefaultTag() {
      Tag tag = CommonPlugin.getInstance().getPluginInfo().getTagByGroup(this.getServerGroup());
      return tag == null ? CommonPlugin.getInstance().getPluginInfo().getDefaultTag() : tag;
   }

   public boolean setTag(Tag tag) {
      this.tagName = tag.getTagName();
      this.save("tagName");
      return false;
   }

   public boolean hasTag(Tag tag) {
      if (tag.isExclusive()) {
         return this.hasPermission("tag." + tag.getTagName()) || this.hasPermission("staff.super") && this.getDefaultTag().getTagId() < tag.getTagId();
      } else {
         return tag.isDefaultTag() || this.getDefaultTag().getTagId() < tag.getTagId() || this.hasPermission("tag." + tag.getTagName().toLowerCase());
      }
   }

   public boolean addPermission(String permission) {
      if (!this.permissions.contains(permission.toLowerCase())) {
         this.permissions.add(permission.toLowerCase());
         return true;
      } else {
         return false;
      }
   }

   public boolean removePermission(String permission) {
      if (this.permissions.contains(permission.toLowerCase())) {
         this.permissions.remove(permission.toLowerCase());
         return true;
      } else {
         return false;
      }
   }

   public void removeServerGroup(String groupName) {
      this.groups.remove(groupName.toLowerCase());
      this.serverGroup = this.getHigherGroup();
      this.handleDefaultGroup();
      this.save("groups");
   }

   public void setServerGroup(String groupName, GroupInfo groupInfo) {
      this.groups.clear();
      this.groups.put(groupName.toLowerCase(), groupInfo);
      this.serverGroup = this.getHigherGroup();
      this.save("groups");
   }

   public void addServerGroup(String groupName, GroupInfo groupInfo) {
      this.groups.put(groupName.toLowerCase(), groupInfo);
      this.serverGroup = this.getHigherGroup();
      this.save("groups");
   }

   public void setLanguage(Language language) {
      this.language = language;
      this.save("language");
   }

   @Override
   public boolean isStaff() {
      return CommonPlugin.getInstance()
              .getPluginInfo()
              .getGroupMap()
              .values()
              .stream()
              .filter(group -> group.isStaff() && this.hasGroup(group.getGroupName()))
              .findFirst()
              .isPresent();
   }

   private boolean handleDefaultGroup() {
      boolean save = false;

      for(Group group : CommonPlugin.getInstance().getPluginInfo().getGroupMap().values()) {
         if (group.isDefaultGroup() && !this.hasGroup(group.getGroupName())) {
            this.groups.put(group.getGroupName().toLowerCase(), new GroupInfo());
            save = true;
         }
      }

      if (save) {
         this.save("groups");
      }

      return save;
   }

   public boolean handleCheckGroup() {
      boolean save = false;

      for(Entry<String, GroupInfo> entry : ImmutableSet.copyOf(this.getGroups().entrySet())) {
         if (!entry.getValue().isPermanent() && entry.getValue().getExpireTime() < System.currentTimeMillis()) {
            this.sendMessage(
                    "§eO seu grupo "
                            + CommonPlugin.getInstance().getPluginInfo().getTagByName(entry.getKey()).getRealPrefix()
                            + "§eexpirou.\n§eCompre novamente em§b"
                            + CommonPlugin.getInstance().getPluginInfo().getStore()
            );
            this.groups.remove(entry.getKey());
            save = true;
         }
      }

      save = save && !this.handleDefaultGroup();
      if (save) {
         this.save("groups");
      }

      return save;
   }

   public void saveConfig() {
      this.save("loginConfiguration", "accountConfiguration", "punishConfiguration");
   }

   public void save(String... fields) {
      for(String fieldName : fields) {
         CommonPlugin.getInstance().getAccountData().updateAccount(this, fieldName);
      }
   }

   public void loadConfiguration() {
      this.loginConfiguration.loadConfiguration(this);
      this.accountConfiguration.loadConfiguration(this);
      this.punishConfiguration.loadConfiguration(this);
      this.cachedPermissions = new ArrayList<>();
   }

   public boolean hasGroup(String groupName) {
      return this.groups.containsKey(groupName.toLowerCase());
   }

   @Override
   public Group getServerGroup() {
      if (this.serverGroup == null) {
         this.serverGroup = this.getHigherGroup();
      }

      return CommonPlugin.getInstance().getPluginInfo().getGroupByName(this.serverGroup);
   }

   private String getHigherGroup() {
      return CommonPlugin.getInstance()
              .getPluginInfo()
              .getGroupMap()
              .values()
              .stream()
              .filter(group -> this.groups.containsKey(group.getGroupName().toLowerCase()))
              .sorted((g1, g2) -> (g1.getId() - g2.getId()) * -1)
              .findFirst()
              .orElse(CommonPlugin.getInstance().getPluginInfo().getDefaultGroup())
              .getGroupName();
   }

   public GroupInfo getServerGroup(String groupName) {
      return this.groups.get(groupName.toLowerCase());
   }

   public boolean hasSilentPermission(String permission) {
      if (!this.permissions.contains(permission.toLowerCase()) && !this.cachedPermissions.contains(permission.toLowerCase())) {
         for(Group group : CommonPlugin.getInstance()
                 .getPluginInfo()
                 .getGroupMap()
                 .values()
                 .stream()
                 .filter(groupx -> this.groups.containsKey(groupx.getGroupName()))
                 .collect(Collectors.toList())) {
            if (group.getPermissions().contains(permission.toLowerCase())) {
               this.cachedPermissions.add(permission.toLowerCase());
               return true;
            }
         }

         return false;
      } else {
         return true;
      }
   }

   public void sendTitle(String title, String subTitle) {
      this.sendTitle(title, subTitle, 10, 20, 10);
   }

   public abstract void sendTitle(String var1, String var2, int var3, int var4, int var5);

   public abstract void sendActionBar(String var1);

   @Override
   public boolean hasPermission(String permission) {
      return this.hasSilentPermission(permission);
   }

   @Override
   public String getName() {
      return this.playerName;
   }

   @Override
   public String getSenderName() {
      return this.playerName;
   }

   @Override
   public boolean isPlayer() {
      return true;
   }

   @Override
   public void setTellEnabled(boolean tellEnabled) {
      this.getAccountConfiguration().setTellEnabled(tellEnabled);
   }

   @Override
   public boolean isTellEnabled() {
      return this.getAccountConfiguration().isTellEnabled();
   }

   public static Language getLanguage(UUID uniqueId) {
      return Language.getLanguage(uniqueId);
   }

   public boolean hasTwitch() {
      return this.twitchUrl != null && !this.twitchUrl.isEmpty();
   }

   @Override
   public UUID getUniqueId() {
      return this.uniqueId;
   }

   public String getPlayerName() {
      return this.playerName;
   }

   public Map<String, GroupInfo> getGroups() {
      return this.groups;
   }

   public String getTagName() {
      return this.tagName;
   }

   public String getMedalName() {
      return this.medalName;
   }

   public List<String> getPermissions() {
      return this.permissions;
   }

   public List<String> getCachedPermissions() {
      return this.cachedPermissions;
   }

   public List<String> getMedals() {
      return this.medals;
   }

   public Map<String, Long> getCooldownMap() {
      return this.cooldownMap;
   }

   public List<Profile> getBlockedList() {
      return this.blockedList;
   }

   public List<Profile> getFriendList() {
      return this.friendList;
   }

   public LoginConfiguration getLoginConfiguration() {
      return this.loginConfiguration;
   }

   public AccountConfiguration getAccountConfiguration() {
      return this.accountConfiguration;
   }

   public PunishConfiguration getPunishConfiguration() {
      return this.punishConfiguration;
   }

   public Skin getSkin() {
      return this.skin;
   }

   public boolean isCustomSkin() {
      return this.customSkin;
   }

   public String getFakeName() {
      return this.fakeName;
   }

   public String getTwitterUrl() {
      return this.twitterUrl;
   }

   public String getYoutubeUrl() {
      return this.youtubeUrl;
   }

   public String getTwitchUrl() {
      return this.twitchUrl;
   }

   public String getDiscordId() {
      return this.discordId;
   }

   public UUID getPartyId() {
      return this.partyId;
   }

   public String getIpAddress() {
      return this.ipAddress;
   }

   public String getLastIpAddress() {
      return this.lastIpAddress;
   }

   public long getFirstLogin() {
      return this.firstLogin;
   }

   public long getLastLogin() {
      return this.lastLogin;
   }

   public long getJoinTime() {
      return this.joinTime;
   }

   public boolean isOnline() {
      return this.online;
   }

   public String getActualServerId() {
      return this.actualServerId;
   }

   public ServerType getActualServerType() {
      return this.actualServerType;
   }

   public String getLastServerId() {
      return this.lastServerId;
   }

   public ServerType getLastServerType() {
      return this.lastServerType;
   }

   @Override
   public Language getLanguage() {
      return this.language;
   }

   @Override
   public UUID getReplyId() {
      return this.replyId;
   }

   @Override
   public void setReplyId(UUID replyId) {
      this.replyId = replyId;
   }
}
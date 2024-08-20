package br.com.plutomc.core.common.punish;

import com.mongodb.client.model.Filters;
import java.util.UUID;
import br.com.plutomc.core.common.CommonPlugin;
import br.com.plutomc.core.common.command.CommandSender;
import br.com.plutomc.core.common.language.Language;
import br.com.plutomc.core.common.utils.DateUtils;
import br.com.plutomc.core.common.utils.string.CodeCreator;

public class Punish {
   private final String id;
   private final UUID playerId;
   private String playerName;
   private UUID punisherId;
   private String punisherName;
   private String punishReason;
   private long createdAt;
   private long expireAt;
   private boolean unpunished;
   private UUID unpunisherId;
   private String unpunisherName;
   private PunishType punishType;

   public Punish(UUID playerId, String playerName, UUID punisherId, String punisherName, String punishReason, long expireAt, PunishType punishType) {
      this.id = "#" + randomId(punishType);
      this.playerId = playerId;
      this.playerName = playerName;
      this.punisherId = punisherId;
      this.punisherName = punisherName;
      this.punishReason = punishReason;
      this.createdAt = System.currentTimeMillis();
      this.expireAt = expireAt;
      this.punishType = punishType;
   }

   public Punish(CommandSender punished, CommandSender punisher, String punishReason, long expireAt, PunishType punishType) {
      this(punished.getUniqueId(), punished.getSenderName(), punisher.getUniqueId(), punisher.getName(), punishReason, expireAt, punishType);
   }

   public Punish(CommandSender punished, UUID punisherId, String punisherName, String punishReason, long expireAt, PunishType punishType) {
      this(punished.getUniqueId(), punished.getSenderName(), punisherId, punisherName, punishReason, expireAt, punishType);
   }

   public Punish(UUID playerId, String playerName, CommandSender punisher, String punishReason, PunishType punishType) {
      this(playerId, playerName, punisher.getUniqueId(), punisher.getName(), punishReason, -1L, punishType);
   }

   public boolean unpunish(UUID unpunisherId, String unpunisherName) {
      if (this.isUnpunished()) {
         return false;
      } else {
         this.unpunished = true;
         this.unpunisherId = unpunisherId;
         this.unpunisherName = unpunisherName;
         return true;
      }
   }

   public boolean unpunish(CommandSender sender) {
      return this.unpunish(sender.getUniqueId(), sender.getName());
   }

   public boolean hasExpired() {
      return !this.isPermanent() && this.expireAt < System.currentTimeMillis();
   }

   public boolean isPermanent() {
      return this.expireAt == -1L;
   }

   public long getExpireTime() {
      return this.isPermanent() ? -1L : this.expireAt - this.createdAt;
   }

   public String getMuteMessage(Language language) {
      return language.t(
         "mute-message",
         "%reason%",
         this.getPunishReason(),
         "%expireAt%",
         DateUtils.getTime(language, this.getExpireAt()),
         "%punisher%",
         this.getPunisherName(),
         "%website%",
         CommonPlugin.getInstance().getPluginInfo().getWebsite(),
         "%store%",
         CommonPlugin.getInstance().getPluginInfo().getStore(),
         "%discord%",
         CommonPlugin.getInstance().getPluginInfo().getDiscord()
      );
   }

   public static String randomId(PunishType punishType) {
      String id;
      do {
         id = CodeCreator.DEFAULT_CREATOR.random(6);
      } while(
         CommonPlugin.getInstance()
               .getMemberData()
               .getQuery()
               .getCollection()
               .find(Filters.eq("punishConfiguration.punishMap." + punishType.name() + ".id", id))
            == null
      );

      return id;
   }

   public String getId() {
      return this.id;
   }

   public UUID getPlayerId() {
      return this.playerId;
   }

   public String getPlayerName() {
      return this.playerName;
   }

   public UUID getPunisherId() {
      return this.punisherId;
   }

   public String getPunisherName() {
      return this.punisherName;
   }

   public String getPunishReason() {
      return this.punishReason;
   }

   public long getCreatedAt() {
      return this.createdAt;
   }

   public long getExpireAt() {
      return this.expireAt;
   }

   public boolean isUnpunished() {
      return this.unpunished;
   }

   public UUID getUnpunisherId() {
      return this.unpunisherId;
   }

   public String getUnpunisherName() {
      return this.unpunisherName;
   }

   public PunishType getPunishType() {
      return this.punishType;
   }
}

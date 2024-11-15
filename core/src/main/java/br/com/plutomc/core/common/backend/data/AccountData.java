package br.com.plutomc.core.common.backend.data;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import br.com.plutomc.core.common.report.Report;
import br.com.plutomc.core.common.backend.mongodb.MongoQuery;
import br.com.plutomc.core.common.account.Account;
import br.com.plutomc.core.common.account.status.Status;
import br.com.plutomc.core.common.account.status.StatusType;
import br.com.plutomc.core.common.permission.Group;

public interface AccountData extends Data<MongoQuery> {
   Account loadAccount(UUID var1);

   <T extends Account> T loadAccount(UUID var1, Class<T> var2);

   Account loadAccount(String var1, boolean var2);

   <T extends Account> T loadAccount(String var1, boolean var2, Class<T> var3);

   <T extends Account> Collection<T> loadMembersByAddress(String var1, Class<T> var2);

   <T extends Account> T loadAccount(String var1, String var2, boolean var3, Class<T> var4);

   List<Account> getAccountsByGroup(Group var1);

   boolean createMember(Account var1);

   boolean deleteMember(UUID var1);

   void updateAccount(Account var1, String var2);

   void reloadPlugins();

   void cacheMember(UUID var1);

   void saveRedisMember(Account var1);

   boolean checkCache(UUID var1);

   boolean isRedisCached(String var1);

   boolean isConnectionPremium(String var1);

   void setConnectionStatus(String var1, UUID var2, boolean var3);

   void cacheConnection(String var1, boolean var2);

   UUID getUniqueId(String var1);

   boolean isDiscordCached(String var1);

   UUID getUniqueIdFromDiscord(String var1);

   void setDiscordCache(String var1, UUID var2);

   void deleteDiscordCache(String var1);

   Status loadStatus(UUID var1, StatusType var2);

   boolean createStatus(Status var1);

   void saveStatus(Status var1, String var2);

   Collection<Report> loadReports();

   void createReport(Report var1);

   void deleteReport(UUID var1);

   void updateReport(Report var1, String var2);

   void closeConnection();
}

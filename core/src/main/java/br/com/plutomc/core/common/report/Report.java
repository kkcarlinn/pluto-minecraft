package br.com.plutomc.core.common.report;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import br.com.plutomc.core.common.CommonPlugin;
import br.com.plutomc.core.common.command.CommandSender;
import br.com.plutomc.core.common.account.Account;

public class Report {
   private final UUID reportId;
   private String playerName;
   private Map<UUID, ReportInfo> reportMap;
   private UUID lastReport;
   private long createdAt;
   private boolean online;

   public Report(Account reported, CommandSender reporter, String reason, boolean online) {
      this.reportId = reported.getUniqueId();
      this.playerName = reported.getName();
      this.reportMap = new HashMap<>();
      this.lastReport = reporter.getUniqueId();
      this.createdAt = System.currentTimeMillis();
      this.online = online;
      this.addReport(reporter, reason);
   }

   public void setOnline(boolean online) {
      this.online = online;
      CommonPlugin.getInstance().getAccountData().updateReport(this, "online");
   }

   public void addReport(CommandSender reporter, String reason) {
      this.reportMap.put(reporter.getUniqueId(), new ReportInfo(reporter.getName(), reason));
      this.lastReport = reporter.getUniqueId();
      CommonPlugin.getInstance().getAccountData().updateReport(this, "reportMap");
      CommonPlugin.getInstance().getAccountData().updateReport(this, "lastReport");
   }

   public void deleteReport() {
      CommonPlugin.getInstance().getReportManager().deleteReport(this.reportId);
      CommonPlugin.getInstance().getAccountData().deleteReport(this.reportId);
   }

   public ReportInfo getLastReport() {
      return this.reportMap.get(this.lastReport);
   }

   public long getExpiresAt() {
      return this.getLastReport().getCreatedAt() + 10800000L;
   }

   public boolean hasExpired() {
      return this.getExpiresAt() < System.currentTimeMillis();
   }

   public void notifyPunish() {
      for(Entry<UUID, ReportInfo> entry : this.getReportMap().entrySet()) {
         Account account = CommonPlugin.getInstance().getAccountManager().getAccount(entry.getKey());
         if (account != null) {
            account.sendMessage("§aUm jogador que você denunciou recentemente foi banido.");
            account.sendMessage("§aObrigado por ajudar nossa comunidade!");
         }
      }

      this.deleteReport();
   }

   public UUID getReportId() {
      return this.reportId;
   }

   public String getPlayerName() {
      return this.playerName;
   }

   public Map<UUID, ReportInfo> getReportMap() {
      return this.reportMap;
   }

   public long getCreatedAt() {
      return this.createdAt;
   }

   public boolean isOnline() {
      return this.online;
   }
}

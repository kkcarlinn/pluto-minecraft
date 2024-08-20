package br.com.plutomc.core.common.report;

public class ReportInfo {
   private String playerName;
   private String reason;
   private long createdAt = System.currentTimeMillis();

   public ReportInfo(String playerName, String reason) {
      this.playerName = playerName;
      this.reason = reason;
   }

   public String getPlayerName() {
      return this.playerName;
   }

   public String getReason() {
      return this.reason;
   }

   public long getCreatedAt() {
      return this.createdAt;
   }

   public ReportInfo(String playerName, String reason, long createdAt) {
      this.playerName = playerName;
      this.reason = reason;
      this.createdAt = createdAt;
   }
}

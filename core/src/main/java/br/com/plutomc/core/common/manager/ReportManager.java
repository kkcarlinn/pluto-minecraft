package br.com.plutomc.core.common.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import br.com.plutomc.core.common.report.Report;
import br.com.plutomc.core.common.CommonPlugin;

public class ReportManager {
   private Map<UUID, Report> reportMap = new HashMap<>();

   public Collection<Report> getReports() {
      return this.reportMap.values();
   }

   public void loadReport(Report report) {
      this.reportMap.put(report.getReportId(), report);
   }

   public void createReport(Report report) {
      this.loadReport(report);
      CommonPlugin.getInstance().getMemberData().createReport(report);
   }

   public void loadReports() {
      for(Report report : CommonPlugin.getInstance().getMemberData().loadReports()) {
         this.loadReport(report);
      }
   }

   public Report getReportById(UUID uniqueId) {
      return this.reportMap.get(uniqueId);
   }

   public Report getReportByName(String playerName) {
      return this.reportMap.values().stream().filter(report -> report.getPlayerName().equals(playerName)).findFirst().orElse(null);
   }

   public void deleteReport(UUID reportId) {
      this.reportMap.remove(reportId);
   }

   public void notify(UUID uniqueId) {
      Report report = this.getReportById(uniqueId);
      if (report != null) {
         report.notifyPunish();
      }
   }
}

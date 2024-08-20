package br.com.plutomc.core.common.packet.types;

import br.com.plutomc.core.common.CommonPlugin;
import br.com.plutomc.core.common.packet.Packet;
import br.com.plutomc.core.common.packet.PacketType;
import br.com.plutomc.core.common.report.Report;

public class ReportCreatePacket extends Packet {
   private Report report;

   public ReportCreatePacket(Report report) {
      super(PacketType.REPORT_CREATE);
      this.report = report;
   }

   @Override
   public void receive() {
      CommonPlugin.getInstance().getReportManager().loadReport(this.report);
   }
}

package br.com.plutomc.core.common.packet.types;

import java.util.UUID;

import br.com.plutomc.core.common.CommonPlugin;
import br.com.plutomc.core.common.packet.Packet;
import br.com.plutomc.core.common.packet.PacketType;

public class ReportDeletePacket extends Packet {
   private UUID reportId;

   public ReportDeletePacket(UUID reportId) {
      super(PacketType.REPORT_DELETE);
      this.reportId = reportId;
   }

   @Override
   public void receive() {
      CommonPlugin.getInstance().getReportManager().deleteReport(this.reportId);
   }
}

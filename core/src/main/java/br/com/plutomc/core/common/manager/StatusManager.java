package br.com.plutomc.core.common.manager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import br.com.plutomc.core.common.CommonPlugin;
import br.com.plutomc.core.common.member.status.Status;
import br.com.plutomc.core.common.member.status.StatusType;

public class StatusManager {
   private Map<UUID, Map<StatusType, Status>> statusMap = new HashMap<>();

   public Status loadStatus(UUID uniqueId, StatusType statusType) {
      if (this.statusMap.containsKey(uniqueId) && this.statusMap.get(uniqueId).containsKey(statusType)) {
         return this.statusMap.get(uniqueId).get(statusType);
      } else {
         Status status = CommonPlugin.getInstance().getMemberData().loadStatus(uniqueId, statusType);
         this.statusMap.computeIfAbsent(uniqueId, v -> new HashMap()).put(statusType, status);
         return status;
      }
   }

   public void preloadStatus(UUID uniqueId, StatusType statusType) {
      if (!this.statusMap.containsKey(uniqueId) || !this.statusMap.get(uniqueId).containsKey(statusType)) {
         Status status = CommonPlugin.getInstance().getMemberData().loadStatus(uniqueId, statusType);
         this.statusMap.computeIfAbsent(uniqueId, v -> new HashMap()).put(statusType, status);
      }
   }

   public void unloadStatus(UUID uniqueId) {
      this.statusMap.remove(uniqueId);
   }
}

package br.com.plutomc.core.bukkit.menu.staff.server;

import java.util.Comparator;
import br.com.plutomc.core.common.server.loadbalancer.server.ProxiedServer;

public enum ServerOrdenator implements Comparator<ProxiedServer> {
   ALPHABETIC {
      public int compare(ProxiedServer o1, ProxiedServer o2) {
         return o1.getServerId().compareTo(o2.getServerId());
      }
   },
   ONLINE_TIME {
      public int compare(ProxiedServer o1, ProxiedServer o2) {
         return Long.compare(o1.getStartTime(), o2.getStartTime());
      }
   },
   TYPE {
      public int compare(ProxiedServer o1, ProxiedServer o2) {
         return Integer.compare(o1.getServerType().ordinal(), o2.getServerType().ordinal());
      }
   };

   private ServerOrdenator() {
   }
}

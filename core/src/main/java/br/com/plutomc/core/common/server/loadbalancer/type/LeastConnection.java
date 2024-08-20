package br.com.plutomc.core.common.server.loadbalancer.type;

import br.com.plutomc.core.common.server.loadbalancer.BaseBalancer;
import br.com.plutomc.core.common.server.loadbalancer.element.LoadBalancerObject;
import br.com.plutomc.core.common.server.loadbalancer.element.NumberConnection;

public class LeastConnection<T extends LoadBalancerObject & NumberConnection> extends BaseBalancer<T> {
   @Override
   public T next() {
      T obj = null;
      if (this.nextObj != null && !this.nextObj.isEmpty()) {
         for(T item : this.nextObj) {
            if (item.canBeSelected()) {
               if (obj == null) {
                  obj = item;
               } else if (obj.getActualNumber() >= item.getActualNumber()) {
                  obj = item;
               }
            }
         }
      }

      return obj;
   }

   @Override
   public int getTotalNumber() {
      int number = 0;

      for(T item : this.nextObj) {
         number += item.getActualNumber();
      }

      return number;
   }
}

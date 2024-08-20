package br.com.plutomc.core.common.server.loadbalancer.type;

import br.com.plutomc.core.common.server.loadbalancer.BaseBalancer;
import br.com.plutomc.core.common.server.loadbalancer.element.LoadBalancerObject;
import br.com.plutomc.core.common.server.loadbalancer.element.NumberConnection;

public class RoundRobin<T extends LoadBalancerObject & NumberConnection> extends BaseBalancer<T> {
   private int next = 0;

   @Override
   public T next() {
      T obj = null;
      if (this.nextObj != null && !this.nextObj.isEmpty()) {
         while(this.next < this.nextObj.size()) {
            obj = this.nextObj.get(this.next);
            ++this.next;
            if (obj != null) {
               if (obj.canBeSelected()) {
                  break;
               }

               obj = null;
            }
         }
      }

      if (this.next + 1 >= this.nextObj.size()) {
         this.next = 0;
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

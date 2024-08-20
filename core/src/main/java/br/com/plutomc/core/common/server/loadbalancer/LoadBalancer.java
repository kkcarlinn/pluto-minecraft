package br.com.plutomc.core.common.server.loadbalancer;

import br.com.plutomc.core.common.server.loadbalancer.element.LoadBalancerObject;

public interface LoadBalancer<T extends LoadBalancerObject> {
   T next();
}

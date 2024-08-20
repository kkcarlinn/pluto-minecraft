package br.com.plutomc.core.common.server.loadbalancer.element;

public interface LoadBalancerObject {
   String getServerId();

   long getStartTime();

   boolean canBeSelected();
}

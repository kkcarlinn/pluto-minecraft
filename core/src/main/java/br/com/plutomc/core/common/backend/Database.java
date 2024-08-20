package br.com.plutomc.core.common.backend;

public interface Database {
   void connect() throws Exception;

   boolean isConnected();

   void close();
}
